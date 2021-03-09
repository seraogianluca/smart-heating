package it.unipi.iot.server;

public class Resource {
	private String name;
	private String valueKey;
	private String address;
	private String path;
	private boolean observable = false;
	
	public Resource(String name, String valueKey, boolean observable) {
		this.name = name;
		this.valueKey = valueKey;
		this.observable = observable;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValueKey() {
		return valueKey;
	}
	
	public String getCoapURI(){
		return "coap://[" + address + "]:5683"+ path;
	}
	
	public boolean isObservable() {
		return observable;
	}
}
