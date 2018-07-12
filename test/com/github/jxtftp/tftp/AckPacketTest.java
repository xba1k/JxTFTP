package com.github.jxtftp.tftp;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AckPacketTest {
    
    byte[] bogusPacket = new byte[]{0, 0};
    byte[] validPacket = new byte[]{ (byte)0x00, (byte)0x04, (byte)0xff, (byte)0xfe };

    @Test
    public void checkPacketValid() {
	assertNotNull(Packet.fromBytes(validPacket));
	assertNull(Packet.fromBytes(bogusPacket));
    }

    @Test
    public void checkParsedValues() {

	Packet packet = Packet.fromBytes(validPacket);
	assertTrue(packet instanceof AckPacket);
	
	AckPacket ackPacket = (AckPacket)packet;

	assertEquals(ackPacket.getOpCode(), Packet.OPCODE_ACK);
	assertEquals(ackPacket.blockNo, 65534);

    }
    
    @Test
    public void testToBytes() {
	
	AckPacket ackPacket = new AckPacket(65534);
	
	byte[] data = ackPacket.toBytes();
	
	assertTrue(Arrays.equals(validPacket, data));
	
    }
    
}
