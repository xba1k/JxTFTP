package com.github.jxtftp.tftp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataPacket extends Packet {
    
    int blockNo;
    byte fileData[];
    
    private DataPacket() {
    }
    
    public DataPacket(int blockNo, byte fileData[]) {
	
	this.opCode = Packet.OPCODE_DATA;
	this.blockNo = blockNo;
	this.fileData = fileData;
	
    }

    public int getBlockNo() {
	return blockNo;
    }

    public static Packet fromBytes(byte data[]) {
	
	if(data.length < 4) {
	    return null;
	}
	
	DataPacket result = new DataPacket();
	ByteBuffer buffer = ByteBuffer.wrap(data);
	
	// Java is the big endian just like network order, but let's be explicit.
	buffer.order(ByteOrder.BIG_ENDIAN);
	
	// Java types are signed, for full-range short we'd need to take care of it	
	result.opCode = buffer.getShort();
	
	// Read the blockNo as unsigned 16-bit value
	result.blockNo = 0xffff & buffer.getShort();
	
	result.fileData = new byte[buffer.remaining()];
	
	// read the remaining bytes as data
	
	if(buffer.remaining() > 0) {
	    buffer.get(result.fileData);
	}

	return result;
	
    }

    public byte[] getFileData() {
	return fileData;
    }
    
    @Override
    public byte[] toBytes() {
	
	ByteBuffer buffer = ByteBuffer.allocate(4 + fileData.length);
	buffer.order(ByteOrder.BIG_ENDIAN);
	
	buffer.putShort((short)opCode);
	buffer.putShort((short)blockNo);
	
	if(fileData.length > 0) {
	    buffer.put(fileData);
	}
	
	return buffer.array();
	
    }
    
    @Override
    public String toString() {
	return "DataPacket{" + "blockNo=" + blockNo + ", fileData=" + fileData + '}';
    }
    
}
