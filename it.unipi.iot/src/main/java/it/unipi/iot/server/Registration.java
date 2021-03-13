package it.unipi.iot.server;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class Registration extends CoapResource {
	public Registration(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {
		exchange.accept();
		
		InetAddress source = exchange.getSourceAddress();
		String sourceAddr = source.getHostAddress();
		System.out.println("Source address: " + sourceAddr);
		CoapClient client = new CoapClient("coap://[" + sourceAddr + "]:5683/.well-known/core");
		CoapResponse response = client.get(MediaTypeRegistry.APPLICATION_JSON);
		
		String code = response.getCode().toString();
		if(!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		
		String responseText = response.getResponseText();
		System.out.println("Payload: " + responseText);
		
		String[] resources = responseText.split(",");
		for(int i = 1; i < resources.length; i++) {
			boolean observable = false;
			String[] resource = resources[i].split(";");
			// </temp>;title="Temperature sensor";rt="temp";if="sensor";obs
			
			String name = resource[3].substring(resource[3].indexOf("\"") + 1, resource[3].length() - 1);
			String type = resource[2].substring(resource[2].indexOf("\"") + 1, resource[2].length() - 1);
			
			Resource res = new Resource(name, type, sourceAddr);
			
			if(resource.length > 3) {
				System.out.println("The resource is observable.");
				observable = true;
				res.setObservable(observable);
				Observer observer = new Observer(res);
				observer.observing();
			}
		}
		
	}
	
}
