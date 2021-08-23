package it.unipi.iot.devices;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Resource {
	
	protected CoapClient client;
	protected String name; //e.g. sensor, actuator
	protected String type; // e.g. temp, hum
	protected String address;
	protected String room;
	protected boolean observable = false;
	
	
	public Resource(String name, String type, String address) {
		this.name = name;
		this.type = type;
		this.address = address;
		this.client = new CoapClient("coap://[" + this.address + "]:5683/"+ this.type);
		this.room = "No room assigned.";
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getAddress() {
		return address;
	}
	
	public void setRoom(String room) {
		this.room = room;
	}
	
	public String getRoom() {
		return room;
	}
	
	public void setObservable(boolean observable) {
		this.observable = observable;
	}
	
	public boolean isObservable() {
		return observable;
	}
	
	public String post(String payload) {
		CoapResponse response = client.post(payload, MediaTypeRegistry.APPLICATION_JSON);
		return response.getCode().toString();
	}
	
	public String toString() {
		String device = "Device: " + name +
				   		"\nType: " + type +
				   		"\nAddress: " + address +
				   		"\nRoom: " + room;
		return device; 
	}
}
