package com.github.jxtftp.tftp;

import java.util.Arrays;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class WRrqPacketTest {

    byte[] bogusPacket = new byte[]{0, 0};
    byte[] validPacket = new byte[]{ 0x00, 0x02, 0x73, 0x79, 0x6e, 0x63, 0x5f, 0x74, 0x61, 0x62, 0x6c, 0x65, 0x2e, 0x73, 0x71, 0x6c, 0x00, 0x6f, 0x63, 0x74, 0x65, 0x74, 0x00};

    @Test
    public void checkPacketValid() {
	assertNotNull(Packet.fromBytes(validPacket));
	assertNull(Packet.fromBytes(bogusPacket));
    }

    @Test
    public void checkParsedValues() {

	Packet packet = Packet.fromBytes(validPacket);
	
	assertTrue(packet instanceof WRrqPacket);
	
	WRrqPacket wrPacket = (WRrqPacket) packet;

	assertEquals(wrPacket.getOpCode(), Packet.OPCODE_WRQ);
	assertEquals(wrPacket.getFilename(), "sync_table.sql");
	assertEquals(wrPacket.getMode(), "octet");

    }

}
