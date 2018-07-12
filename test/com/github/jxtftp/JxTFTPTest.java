package com.github.jxtftp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.apache.commons.net.tftp.TFTPClient;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;

public class JxTFTPTest {

    static JxTFTP server;
    static TFTPClient client;
    static int PORT = 6969;
    

    @BeforeClass
    public static void setUp() throws SocketException {

	server = new JxTFTP(PORT);

	Thread serverThread = new Thread(server);
	serverThread.start();
	
	client = new TFTPClient();

    }

    @Test
    public void testFileUpload() throws UnknownHostException, IOException {
	
	ByteArrayInputStream inputStream = new ByteArrayInputStream("hello, world!".getBytes());
	
	client.open();
	client.sendFile("hello.txt", TFTPClient.BINARY_MODE, inputStream, "localhost", PORT);
	client.close();

    }

    @Test
    public void testFileDownload() throws UnknownHostException, IOException {
	
	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	
	client.open();
	client.receiveFile("hello.txt", TFTPClient.BINARY_MODE, outputStream, "localhost", PORT);
	client.close();
	
	assertEquals(new String(outputStream.toByteArray()), "hello, world!");

    }

    @AfterClass
    public static void cleanUp() {
	
	if(server != null) {
	    server.terminate();
	}
	// TODO: delete that uploaded file
    }

}
