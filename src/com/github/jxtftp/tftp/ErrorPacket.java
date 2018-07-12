package com.github.jxtftp.tftp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ErrorPacket extends Packet {

    int errorCode;
    String errMsg;

    public static int ERROR_UNDEFINED = 0,
	    ERROR_NO_SUCH_FILE = 1,
	    ERROR_ACCESS_VIOLATION = 2,
	    ERROR_SPACE_EXCEEDED = 3,
	    ERROR_ILLEGAL_OP = 4,
	    ERROR_UNKNOWN_ID = 5,
	    ERROR_FILE_EXISTS = 6,
	    ERROR_NO_SUCH_USER = 7;

    public static String ERROR_MESSAGES[] = new String[]{
	"Not defined, see error message (if any).",
	"File not found.",
	"Access violation.",
	"Disk full or allocation exceeded.",
	"Illegal TFTP operation.",
	"Unknown transfer ID.",
	"File already exists.",
	"No such user."
    };
    
    private ErrorPacket() {
    }
    
    public ErrorPacket(int errorCode, String errMsg) {
	
	this.opCode = Packet.OPCODE_ERROR;
	this.errorCode = errorCode;
	this.errMsg = errMsg;
	
    }

    public static Packet fromBytes(byte data[]) {
	
	if(data.length < 6) {
	    return null;
	}

	ErrorPacket result = new ErrorPacket();
	ByteBuffer buffer = ByteBuffer.wrap(data);

	// Java is the big endian just like network order, but let's be explicit.
	buffer.order(ByteOrder.BIG_ENDIAN);

	// Java types are signed, for full-range short we'd need to take care of it	
	result.opCode = buffer.getShort();

	// Error code is defined as value 0-7
	result.errorCode = buffer.getShort();

	StringBuilder tmpString = new StringBuilder();

	// parse out the filename, it's a NULL-terminated string
	for (byte b = buffer.get(); b != 0; b = buffer.get()) {
	    tmpString.append((char) b);
	}

	result.errMsg = tmpString.toString();

	return result;

    }
    
    @Override
    public byte[] toBytes() {
	
	ByteBuffer buffer = ByteBuffer.allocate(5 + errMsg.length());
	buffer.order(ByteOrder.BIG_ENDIAN);
	
	buffer.putShort((short)opCode);
	buffer.putShort((short)errorCode);
	
	if(errMsg.length() > 0) {
	    buffer.put(errMsg.getBytes());
	}
	
	buffer.put((byte)0);
	
	return buffer.array();
	
    }

    @Override
    public String toString() {
	return "ErrorPacket{" + "errorCode=" + errorCode + ", errMsg=" + errMsg + '}';
    }

}
