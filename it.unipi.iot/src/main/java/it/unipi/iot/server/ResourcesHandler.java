package it.unipi.iot.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ResourcesHandler {
	private static ResourcesHandler instance = new ResourcesHandler();
	private static Map<String, Actuator> radiators;
	private static Map<String, Sensor> temp_sensors;
	private static Map<String, Sensor> hum_sensors;
	
	private ResourcesHandler() {
		radiators = new HashMap<>();
		temp_sensors = new HashMap<>();
		hum_sensors = new HashMap<>();
	}
	
	public static ResourcesHandler getInstance() {
		return instance;
	}
	
	public void addDevice(String room, Resource device) {
		if(device.getName().contains("sensor")) {
			Sensor dev = (Sensor)device;
			if(dev.getType().contains("temp")) {
				temp_sensors.put(room, dev);
			} else {
				hum_sensors.put(room, dev);
			}
		} else {
			Actuator dev = (Actuator)device;
			radiators.put(room, dev);
		}	
	}
	
	public void setRadiatorsStatus(String status) {
		String response;
		String payload = "{\"status\":\"" + status.toLowerCase() + "\"}";
		Collection<Actuator> devices = radiators.values();
		for(Actuator device: devices) {
			response = device.post(payload);
			
			System.out.println(device.toString() + "\n" +
					           "Response: " + response);
			
			if(response.startsWith("2")) {
				
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("New status: " + device.getStatus());
			}
		}
	}
	
	public void getTemperature() {
		Collection<Sensor> temp_devices = temp_sensors.values();
		Sensor hum_sensor;
		
		for(Sensor device: temp_devices) {
			hum_sensor = hum_sensors.get(device.getRoom());
			System.out.println(device.toString() + "\n" +
							   "Temperature: " + device.getValue() + "\n" +
							   "Humidity: " + hum_sensor.getValue());
		}
	}
}
