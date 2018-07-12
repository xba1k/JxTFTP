package com.github.jxtftp.tftp;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DataPacketTest {
    
    byte[] bogusPacket = new byte[]{0, 0};
    byte[] validPacket = new byte[]{ 0x00, 0x03, (byte)0xff, (byte)0xfe, 0x68, 0x65, 0x6c, 0x6c, 0x6f, 0x2c, 0x20, 0x77, 0x6f, 0x72, 0x6c, 0x64, 0x21, 0x0a  };
    
    @Test
    public void checkPacketValid() {
	assertNotNull(Packet.fromBytes(validPacket));
	assertNull(Packet.fromBytes(bogusPacket));
    }

    @Test
    public void checkParsedValues() {

	Packet packet = Packet.fromBytes(validPacket);
	
	assertTrue(packet instanceof DataPacket);
	
	DataPacket dataPacket = (DataPacket) packet;

	assertEquals(dataPacket.getOpCode(), Packet.OPCODE_DATA);
	assertEquals(dataPacket.blockNo, 65534);
	assertEquals(new String(dataPacket.getFileData(), 0, 5), "hello");

    }
    
    @Test
    public void testToBytes() {
	
	DataPacket dataPacket = new DataPacket(65534, "hello, world!\n".getBytes());
	
	byte[] data = dataPacket.toBytes();
	
	assertTrue(Arrays.equals(validPacket, data));
	
    }
    
    
}
