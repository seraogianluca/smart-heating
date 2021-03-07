package it.unipi.iot.server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Registration extends CoapResource {
	public Registration(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {
		exchange.respond("Hello world!");
	}
	
}
