package it.unipi.iot.server;

import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapServer;

public class Server extends CoapServer {
	
	public Server(int port) {
		super(port);
		this.add(new Registration("register"));
		CaliforniumLogger.disableLogging();
	}
}
