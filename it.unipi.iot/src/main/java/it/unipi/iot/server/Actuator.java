package it.unipi.iot.server;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

public class Actuator extends Resource {
	private String status;

	public Actuator(String name, String type, String address, String room) {
		super(name, type, address, room);
	}
	
	public String getStatus() {
		return status;
	}
	
	public void observing() {
		if(observable == false) {
			System.out.println("The resource is not observable.");
			return;
		} else {
			client.observeAndWait(new CoapHandler() {

				public void onLoad(CoapResponse response) {
					try {
						JSONObject responseJSON = new JSONObject(response.getResponseText());
						status = responseJSON.getString("status");
					} catch(Exception e) {
						e.printStackTrace();
					}
				}

				public void onError() {
					System.err.println("Observing failed.\n");		
				}
			}, MediaTypeRegistry.APPLICATION_JSON);
		}
	}

}
