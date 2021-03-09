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
		System.out.println(source.getHostAddress());
		CoapClient client = new CoapClient("coap://[" + source.getHostAddress() + "]:5683/.well-known/core");
		CoapResponse response = client.get();
		
		String code = response.getCode().toString();
		if(!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		
		String responseText = response.getResponseText();
		System.out.println("Payload: " + responseText);
		
		String[] resources = responseText.split(",");
		for(String resource:resources) {
			boolean observable = false;
			String[] parameters = resource.split(";");
			// </temp>;title="Temperature sensor";rt="temperature";if="sensor";obs
			String name = parameters[0].substring(parameters[0].indexOf("/") + 1, parameters[0].indexOf(">"));
			
			if(resource.contains("obs")) {
				observable = true;
			}
			
		}
		
	}
	
}
