package it.unipi.iot.server;

public class Resource {
	private String name; //e.g. sensor, actuator
	private String type; // e.g. temp, hum
	private String address;
	private String room;
	private boolean observable = false;
	
	public Resource(String name, String type, String address, String room) {
		this.name = name;
		this.type = type;
		this.address = address;
		this.room = room;
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
}
