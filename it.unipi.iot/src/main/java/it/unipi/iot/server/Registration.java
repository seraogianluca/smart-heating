package it.unipi.iot.server;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.unipi.iot.client.ClientInterface;

public class Registration extends CoapResource {
	public Registration(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {	
		exchange.accept();
		
		InetAddress source = exchange.getSourceAddress();
		String sourceAddr = source.getHostAddress();
		System.out.println("New device found: " + sourceAddr);
		CoapClient client = new CoapClient("coap://[" + sourceAddr + "]:5683/.well-known/core");
		CoapResponse response = client.get(MediaTypeRegistry.APPLICATION_JSON);
		
		String code = response.getCode().toString();
		
		if(!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		
		String responseText = response.getResponseText();
		ClientInterface.addResources(sourceAddr, responseText);
	}
	
}
