package it.unipi.iot.server;

import org.eclipse.californium.core.CoapServer;

public class Server extends CoapServer {

	public static void main(String[] args) {
		System.out.println("Start server...");
		Server server = new Server();
		server.add(new Registration("register"));
		server.start();
	}

}
