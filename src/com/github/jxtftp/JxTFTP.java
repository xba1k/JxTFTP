package com.github.jxtftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * Java extra-Trivial FTP server
 */
public class JxTFTP implements Runnable {

    public final static int DEFAULT_TFTP_PORT = 69; // this is privileged, so mostly for reference
    public final static int SOCKET_RECV_TIMEOUT = 100; // not too quick, not too slow
    public final static int STATS_INTERVAL = 60000;

    DatagramSocket socket;
    byte recvBuffer[] = new byte[516];
    boolean terminateCalled = false;

    long statBytesIn = 0;
    long statBytesOut = 0;
    long statSessionsProcessed = 0;

    Map<SocketAddress, Session> activeSessions = new HashMap<SocketAddress, Session>();

    public JxTFTP() throws SocketException {
	this(JxTFTP.DEFAULT_TFTP_PORT);
    }

    public JxTFTP(int port) throws SocketException {

	socket = new DatagramSocket(port);
	// put it into non-blocking mode, where all the fun is
	socket.setSoTimeout(SOCKET_RECV_TIMEOUT);

    }

    Session getOrCreateSession(DatagramPacket datagram) {

	Session session = activeSessions.get(datagram.getSocketAddress());

	if (session == null) {

	    statSessionsProcessed++;

	    System.out.println("Creating new session for " + datagram.getSocketAddress());

	    session = new Session(datagram.getSocketAddress());
	    activeSessions.put(datagram.getSocketAddress(), session);

	}

	return session;

    }

    void removeSession(Session session) {
	session.cleanup();
	this.activeSessions.remove(session.getAddress());
    }

    // Walk the sessions, and remove those that haven't seen any activity recently
    void cleanupSessions() {

	Iterator<Session> sessionIter = activeSessions.values().iterator();

	while (sessionIter.hasNext()) {

	    Session session = sessionIter.next();

	    if (session.isExpired()) {
		System.out.println("Removing expired session " + session);
		session.cleanup();
		sessionIter.remove();
	    }

	}

    }

    /*
    
    Main processing loop. Most important things here are not to break out due to uncaught exception,
    and never block during processing.
    
     */
    public void run() {

	DatagramPacket datagram = new DatagramPacket(recvBuffer, 0, recvBuffer.length);

	while (!terminateCalled) {

	    try {

		if (activeSessions.size() > 0) {
		    // Find and remove sessions that may have expired
		    cleanupSessions();
		}

		// pickup a datagram if available. Exception will be thrown on socket timeout.
		socket.receive(datagram);

		statBytesIn += datagram.getLength();

		Session session = getOrCreateSession(datagram);

		if (session != null) {

		    DatagramPacket response = session.process(datagram);

		    if (response != null) {
			socket.send(response);
			statBytesOut += response.getLength();

			// Cleanup on ERROR, but leave DONE session behind for the final ACK
			if (session.isError()) {
			    removeSession(session);
			}
		    } else {
			// This typically means transfer has completed, no more to send.
			if(session.isDone()) {
			    System.out.println("Session "+session+" completed");
			}
			
		    }

		}

	    } catch(SocketTimeoutException ex) {
		// this is expected when no datagrams are ready for pickup
	    } catch (IOException ex) {
		ex.printStackTrace();
	    }

	}

    }
    
    /*
    Set the flag for the main loop to terminate
    */
    
    public void terminate() {
	this.terminateCalled = true;
    }

    public void printStats() {

	System.out.println("Total bytes received: " + statBytesIn);
	System.out.println("Total bytes sent: " + statBytesOut);
	System.out.println("Sessions processed: " + statSessionsProcessed);
	System.out.println("Active sessions:");

	for (Session session : activeSessions.values()) {
	    System.out.println(session);
	}

    }

    public static void main(String[] args) {

	System.out.println("Current directory: " + System.getProperty("user.dir"));

	try {

	    JxTFTP server = new JxTFTP(6969); // use a non-privileged port

	    // This implementation is intended to be single-threaded, but let's
	    // use the main thread for showing the stats	    
	    Thread serverThread = new Thread(server);

	    serverThread.start();

	    while (true) {

		try {
		    Thread.sleep(STATS_INTERVAL);
		} catch (InterruptedException ex) {

		}

		server.printStats();

	    }

	} catch (SocketException ex) {

	    ex.printStackTrace();

	}

    }

}
