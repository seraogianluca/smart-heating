package it.unipi.iot.server;

import org.json.JSONObject;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Observer extends CoapClient {
	private Resource resource;
	
	public Observer(Resource resource) {
		super(resource.getCoapURI());
		this.resource = resource;
	}
	
	public void observing() {
		this.observeAndWait(new CoapHandler() {

			public void onLoad(CoapResponse response) {
				try {
					JSONObject responseJSON = new JSONObject(response.getResponseText());
					System.out.print("room: " + responseJSON.getString("room") + " ");
					System.out.print(resource.getType() + ": " + responseJSON.getString(resource.getType()) + "\n");
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
