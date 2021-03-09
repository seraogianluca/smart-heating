package it.unipi.iot.server;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Registration extends CoapResource {
	public Registration(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {
		exchange.accept();
		
		InetAddress source = exchange.getSourceAddress();
		CoapClient client = new CoapClient("coap://[" + source.getHostAddress() + "]:5683/.well-known/core");
		CoapResponse response = client.get();
		
		String code = response.getCode().toString();
		if(!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		
		String responseText = response.getResponseText();
		System.out.println("Payload: " + responseText);
	}
	
}
