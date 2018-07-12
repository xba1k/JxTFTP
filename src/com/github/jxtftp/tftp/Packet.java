package com.github.jxtftp.tftp;

public class Packet {

    public static final int OPCODE_RRQ = 1,
	    OPCODE_WRQ = 2,
	    OPCODE_DATA = 3,
	    OPCODE_ACK = 4,
	    OPCODE_ERROR = 5;

    int opCode;
    
    public byte[] toBytes() {
	return null;
    }

    public static Packet fromBytes(byte[] data) {

	// Bail early on definitely bogus packet
	if(data.length < 2) {
	    return null;
	}

	// opCode is shared between all packet types
	// Peek into it to classify the packet
	int opCode = data[0] << 8 | data[1];
	
	// delegate the rest of it to the specific type
	switch (opCode) {

	    case OPCODE_RRQ:
	    case OPCODE_WRQ:
		return WRrqPacket.fromBytes(data);
	    case OPCODE_DATA:
		return DataPacket.fromBytes(data);
	    case OPCODE_ACK:
		return AckPacket.fromBytes(data);
	    case OPCODE_ERROR:
		return ErrorPacket.fromBytes(data);
	    // TODO: raise an exception here on uknown packet type
	    default:
		return null;

	}

    }

    @Override
    public String toString() {
	return "Packet{" + "opCode=" + opCode + '}';
    }

    public int getOpCode() {
	return opCode;
    }
    
}
