package com.github.jxtftp;

import com.github.jxtftp.tftp.AckPacket;
import com.github.jxtftp.tftp.ErrorPacket;
import com.github.jxtftp.tftp.Packet;
import com.github.jxtftp.tftp.WRrqPacket;
import com.github.jxtftp.tftp.DataPacket;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.SocketAddress;
import java.util.Arrays;

public class Session {

    public final static int TIMEOUT = 10000;

    SocketAddress address;
    String filename;
    InputStream inputStream;
    OutputStream outputStream;

    int blockNo;
    long statBytesProcessed;

    enum STATE {

	UNINITIALIZED,
	DATA_WAIT,
	ACK_WAIT,
	ERROR,
	DONE

    };

    enum TYPE {

	GET,
	PUT

    }

    STATE state;
    TYPE type;

    long lastActivity;

    public Session(SocketAddress socketAddress) {

	this.state = STATE.UNINITIALIZED;
	this.address = socketAddress;

    }

    /*
    
    Process datagram based on the state of this session and operation code.
    @return DatagramPacket response datagram if any.
    
     */
    public DatagramPacket process(DatagramPacket datagram) {

	DatagramPacket responseDatagram = null;
	Packet responsePacket = null;
	Packet requestPacket = Packet.fromBytes(Arrays.copyOfRange(datagram.getData(), 0, datagram.getLength())); // datagram wraps our original buffer, so it's always 516 bytes
	
	// Probably a bogus packet
	if(requestPacket == null) {
	    state = STATE.ERROR;
	    return null;
	}

	// Session state-machine implementation
	switch (requestPacket.getOpCode()) {

	    case Packet.OPCODE_WRQ:

		if (state != STATE.UNINITIALIZED) {
		    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, "Invalid state");
		    state = STATE.ERROR;
		} else {

		    WRrqPacket writeRequestPacket = (WRrqPacket) requestPacket;

		    // Attempt to open the file for writing and wait for data
		    try {

			filename = writeRequestPacket.getFilename();

			outputStream = new FileOutputStream(filename);
			state = STATE.DATA_WAIT;
			type = TYPE.PUT;

			// Now ACK the request
			responsePacket = new AckPacket(blockNo);

		    } catch (FileNotFoundException ex) {
			// This is actually an umbrella for multiple possible errors
			responsePacket = new ErrorPacket(ErrorPacket.ERROR_NO_SUCH_FILE, ex.getMessage());
			state = STATE.ERROR;
			break;
		    }

		}

		break;
	    case Packet.OPCODE_RRQ:

		if (state != STATE.UNINITIALIZED) {
		    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, "Invalid state");
		    state = STATE.ERROR;
		} else {

		    WRrqPacket readRequestPacket = (WRrqPacket) requestPacket;

		    // Attempt to open the file for reading and wait for data
		    try {

			filename = readRequestPacket.getFilename();
			inputStream = new FileInputStream(filename);

			state = STATE.ACK_WAIT;
			type = TYPE.GET;

		    } catch (FileNotFoundException ex) {
			responsePacket = new ErrorPacket(ErrorPacket.ERROR_NO_SUCH_FILE, ex.getMessage());
			state = STATE.ERROR;
			break;
		    }

		}

	    // don't break out just yet, as ACK logic is shared with RRQ
	    case Packet.OPCODE_ACK:

		if (state != STATE.ACK_WAIT && state != STATE.DONE) {
		    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, "Invalid state");
		    state = STATE.ERROR;
		} else {

		    // final ACK will arrive when we're in DONE state, we can just ignore it
		    if (state == STATE.ACK_WAIT) {

			// Read and return next data block
			blockNo++;

			byte buf[] = new byte[512];
			int count = 0;

			try {

			    count = inputStream.read(buf);
			    statBytesProcessed += count;

			    // resize the buffer in case we reached the end and change the state
			    if (count < buf.length) {
				buf = Arrays.copyOfRange(buf, 0, count);
				state = STATE.DONE;
			    }

			    responsePacket = new DataPacket(blockNo, buf);

			} catch (IOException ex) {
			    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, ex.getMessage());
			    state = STATE.ERROR;
			}

		    }
		}

		break;

	    case Packet.OPCODE_DATA:

		if (state != STATE.DATA_WAIT) {
		    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, "Invalid state");
		    state = STATE.ERROR;
		} else {

		    DataPacket dataPacket = (DataPacket) requestPacket;

		    byte[] payload = dataPacket.getFileData();
		    blockNo = dataPacket.getBlockNo(); // we need to ACK the specific block
		    
		    if (payload.length > 0) {

			try {

			    outputStream.write(payload);
			    responsePacket = new AckPacket(blockNo);

			    // Payload less than 512 bytes means we're also done
			    if (payload.length < 512) {
				state = STATE.DONE;
			    }

			} catch (IOException ex) {
			    responsePacket = new ErrorPacket(ErrorPacket.ERROR_UNDEFINED, ex.getMessage());
			    state = STATE.ERROR;
			}
		    } else if (payload.length == 0) {
			state = STATE.DONE;
		    }

		}

		break;
	    case Packet.OPCODE_ERROR:
		state = STATE.ERROR;
		break;

	    default:
		responsePacket = new ErrorPacket(ErrorPacket.ERROR_ILLEGAL_OP, "Opcode " + requestPacket.getOpCode() + " not implemented");
		state = STATE.ERROR;

	}

	// Wrap response into a datagram
	if (responsePacket != null) {
	    byte responseData[] = responsePacket.toBytes();
	    responseDatagram = new DatagramPacket(responseData, 0, responseData.length);
	    responseDatagram.setSocketAddress(datagram.getSocketAddress());
	    updateLastActivity();
	}

	return responseDatagram;

    }

    public void updateLastActivity() {
	this.lastActivity = System.currentTimeMillis();
    }

    public boolean isError() {
	return state == STATE.ERROR;
    }

    public boolean isDone() {
	return state == STATE.DONE;
    }

    public SocketAddress getAddress() {
	return address;
    }

    public boolean isExpired() {
	return System.currentTimeMillis() - lastActivity > Session.TIMEOUT;
    }

    /*
    
    Cleanup any resources left behind, as we're not guaranteed to hit process() again.
    
     */
    public void cleanup() {

	if (inputStream != null) {
	    try {
		inputStream.close();
	    } catch (IOException ex) {
	    }

	}

	if (outputStream != null) {
	    try {
		outputStream.close();
	    } catch (IOException ex) {
	    }
	}

    }

    @Override
    public String toString() {
	return "Session{" + "address=" + address + ", filename=" + filename + ", statBytesProcessed=" + statBytesProcessed + ", blockNo=" + blockNo + ", state=" + state + ", type=" + type + ", lastActivity=" + lastActivity + '}';
    }

}
