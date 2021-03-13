package it.unipi.iot.server;

public class Resource {
	private String name;
	private String type;
	private String address;
	private boolean observable = false;
	
	public Resource(String name, String type, String address) {
		this.name = name;
		this.type = type;
		this.address = address;
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
	
	public void setObservable(boolean observable) {
		this.observable = observable;
	}
	
	public boolean isObservable() {
		return observable;
	}
}
