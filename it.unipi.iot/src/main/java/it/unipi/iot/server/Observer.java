package it.unipi.iot.server;

import org.json.JSONObject;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class Observer extends CoapClient {
	private Resource resource;
	private CoapObserveRelation observe;
	
	public Observer(Resource resource) {
		super(resource.getCoapURI());
		this.resource = resource;
	}
	
	public void observing() {
		observe = this.observe(new CoapHandler() {

			@Override
			public void onLoad(CoapResponse response) {
				try {
					JSONObject responseJSON = new JSONObject(response.getResponseText());
					System.out.println(responseJSON.toString());
				} catch(Exception e) {
					e.printStackTrace();
				}
				
			}

			@Override
			public void onError() {
				System.err.println("Observing failed.\n");		
			}
			
		});
	}
}
