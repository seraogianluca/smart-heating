package it.unipi.iot.server;

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
	
	
	public Resource(String name, String type, String address, String room) {
		this.name = name;
		this.type = type;
		this.address = address;
		this.room = room;
		this.client = new CoapClient("coap://[" + this.address + "]:5683/"+ this.type);
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getCoapURI(){
		return "coap://[" + address + "]:5683/"+ type;
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
		return "Device: " + name + "(" + type + ") " +
						 "Address: " + address +
						 "Room: " + room;
	}
}
