package it.unipi.iot.server;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.json.JSONObject;

public class Sensor extends Resource {
	private int last = 0;
	private int num_values = 5;
	private int inserted = 0;
	private int[] values;
	
	public Sensor(String name, String type, String address, String room) {
		super(name, type, address, room);
		values = new int[num_values];
	}
	
	public int getValue() {
		int average = 0;
		for(int value: values) {
			average += value;
		}
		
		if(inserted < num_values) {
			average /= inserted;
		} else {
			average /= num_values;
		}
		return average;
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
						values[last] = Integer.parseInt(responseJSON.getString(type));
						last = (last + 1) % num_values;
						
						if(inserted <= num_values) {
							inserted++;
						}
						
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
