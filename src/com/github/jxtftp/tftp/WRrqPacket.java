package com.github.jxtftp.tftp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*

Class representing both Read and Write requests, as their structure is identical

 */

public class WRrqPacket extends Packet {

    String filename;
    String mode;

    public static Packet fromBytes(byte data[]) {
	
	if(data.length < 4) {
	    return null;
	}

	WRrqPacket result = new WRrqPacket();
	ByteBuffer buffer = ByteBuffer.wrap(data);

	// Java is the big endian just like network order, but let's be explicit.
	buffer.order(ByteOrder.BIG_ENDIAN);

	// Java types are signed, for full-range short we'd need to take care of it	
	result.opCode = buffer.getShort();

	StringBuilder tmpString = new StringBuilder();

	// parse out the filename, it's a NULL-terminated string
	for (byte b = buffer.get(); b != 0; b = buffer.get()) {
	    tmpString.append((char) b);
	}

	result.filename = tmpString.toString();

	tmpString = new StringBuilder();

	// mode is also a NULL-terminated string
	for (byte b = buffer.get(); b != 0; b = buffer.get()) {
	    tmpString.append((char) b);
	}

	result.mode = tmpString.toString();

	return result;

    }

    public String getFilename() {
	return filename;
    }

    public String getMode() {
	return mode;
    }

    @Override
    public String toString() {
	return "WRrqtPacket{" + "filename=" + filename + ", mode=" + mode + '}';
    }

}
