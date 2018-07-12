package com.github.jxtftp.tftp;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ErrorPacketTest {

    byte[] bogusPacket = new byte[]{0, 0};
    byte[] validPacket = new byte[]{0x00, 0x05, 0x00, 0x01, 0x73, 0x74, 0x75, 0x66, 0x66, 0x65, 0x72, 0x2e, 0x73, 0x66, 0x64, 
	0x66, 0x64, 0x20, 0x28, 0x4e, 0x6f, 0x20, 0x73, 0x75, 0x63, 0x68, 0x20, 0x66, 0x69, 0x6c, 0x65, 0x20, 0x6f, 0x72, 0x20, 
	0x64, 0x69, 0x72, 0x65, 0x63, 0x74, 0x6f, 0x72, 0x79, 0x29, 0x00};

    @Test
    public void checkPacketValid() {
	assertNotNull(Packet.fromBytes(validPacket));
	assertNull(Packet.fromBytes(bogusPacket));
    }

    @Test
    public void checkParsedValues() {

	Packet packet = Packet.fromBytes(validPacket);
	
	assertTrue(packet instanceof ErrorPacket);
	
	ErrorPacket errorPacket = (ErrorPacket) packet;

	assertEquals(errorPacket.getOpCode(), Packet.OPCODE_ERROR);
	assertEquals(errorPacket.errorCode, 1);
	assertEquals(errorPacket.errMsg.endsWith("(No such file or directory)"), true);

    }
    
    @Test
    public void testToBytes() {
	
	ErrorPacket dataPacket = new ErrorPacket(1, "stuffer.sfdfd (No such file or directory)");
	
	byte[] data = dataPacket.toBytes();
	
	assertTrue(Arrays.equals(validPacket, data));
	
    }

}
