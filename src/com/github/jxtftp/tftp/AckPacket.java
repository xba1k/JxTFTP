package com.github.jxtftp.tftp;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AckPacket extends Packet {
    
    int blockNo;
    
    private AckPacket() {
    }
    
    public AckPacket(int blockNo) {
	this.opCode = Packet.OPCODE_ACK;
	this.blockNo = blockNo;
    }

    public static Packet fromBytes(byte data[]) {
	
	if(data.length < 4) {
	    return null;
	}
	
	AckPacket result = new AckPacket();
	ByteBuffer buffer = ByteBuffer.wrap(data);
	
	// Java is the big endian just like network order, but let's be explicit.
	buffer.order(ByteOrder.BIG_ENDIAN);
	
	// Java types are signed, for full-range short we'd need to take care of it	
	result.opCode = buffer.getShort();
	
	// Read the blockNo as unsigned 16-bit value
	result.blockNo = 0xffff & buffer.getShort();
	
	// There should be no bytes left beyond this point
	if(buffer.remaining() > 0) {
	    // TODO: raise an exception due to trailing garbage
	}
	
	return result;
	
    }
    
    @Override
    public byte[] toBytes() {
	
	ByteBuffer buffer = ByteBuffer.allocate(4);
	buffer.order(ByteOrder.BIG_ENDIAN);
	
	buffer.putShort((short)opCode);
	buffer.putShort((short)blockNo);
	
	return buffer.array();
	
    }

    @Override
    public String toString() {
	return "AckPacket{" + "blockNo=" + blockNo + '}';
    }
    
}
