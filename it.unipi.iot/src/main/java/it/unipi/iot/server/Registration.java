package it.unipi.iot.server;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.unipi.iot.devices.Actuator;
import it.unipi.iot.devices.Resource;
import it.unipi.iot.devices.Sensor;

public class Registration extends CoapResource {
	public Registration(String name) {
		super(name);
	}
	
	public void handleGET(CoapExchange exchange) {	
		exchange.accept();
		String source = exchange.getSourceAddress().getHostAddress();
		//System.out.println("New device found: " + source);
		
		/* Resource discovery */
		CoapClient client = new CoapClient("coap://[" + source + "]:5683/.well-known/core");
		CoapResponse response = client.get(MediaTypeRegistry.APPLICATION_JSON);
			
		String code = response.getCode().toString();
			
		if(!code.startsWith("2")) {
			//System.out.println("Error: " + code);
			return;
		}
			
		String responseText = response.getResponseText();
			
		addResources(source, responseText);
	}
	
	public static void addResources(String sourceAddress, String resources) {
		String[] resourcesText = resources.split(",");
		boolean observable;
		
		for(int i = 1; i < resourcesText.length; i++) {
			observable = false;
			
			// </temp>;title="Temperature sensor";rt="temp";if="sensor";obs
			String[] resourceText = resourcesText[i].split(";");
			String name = resourceText[3].substring(resourceText[3].indexOf("\"") + 1, 
													resourceText[3].length() - 1);
			String type = resourceText[2].substring(resourceText[2].indexOf("\"") + 1,
													resourceText[2].length() - 1);
			
			//System.out.println("Resource found: " + name + " " + type);
			
			Resource resource = null;
			if(name.contains("actuator")) {
				resource = new Actuator(name, type, sourceAddress);
			} else {
				resource = new Sensor(name, type, sourceAddress);
			}
				
			if(resourceText.length > 3) {
				//System.out.println("The resource is observable.");
				observable = true;
				resource.setObservable(observable);
				
				if(name.contains("actuator")) {
					Actuator actuator = (Actuator)resource;
					actuator.observing();
				} else {
					Sensor sensor = (Sensor)resource;
					sensor.observing();
				}
			}
				
			ResourcesHandler df = ResourcesHandler.getInstance();
			df.addDevice(sourceAddress, resource);
		}
	}
	
}
