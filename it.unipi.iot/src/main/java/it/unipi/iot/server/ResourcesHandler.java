package it.unipi.iot.server;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import it.unipi.iot.devices.Actuator;
import it.unipi.iot.devices.Resource;
import it.unipi.iot.devices.Sensor;

public class ResourcesHandler {
	private static final ResourcesHandler instance = new ResourcesHandler(); //Thread safety
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
	
	public void addDevice(String address, Resource device) {
		if(device.getName().contains("sensor")) {
			Sensor dev = (Sensor)device;
			if(dev.getType().contains("temp")) {
				temp_sensors.put(address, dev);
			} else {
				hum_sensors.put(address, dev);
			}
		} else {
			Actuator dev = (Actuator)device;
			radiators.put(address, dev);
		}	
	}
	
	public void setRadiatorsStatus(String status) {
		String response;
		String payload = "{\"status\":\"" + status.toLowerCase() + "\"}";
		Collection<Actuator> devices = radiators.values();
		
		for(Actuator device: devices) {
			response = device.post(payload);
			
			System.out.println(device.toString() +
					           "\nResponse: " + response);
			
			if(response.startsWith("2")) {
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("New status: " + device.getStatus());
			}
		}
	}
	
	public void getRadiatorsStatus() {
		Collection<Actuator> devices = radiators.values();
		
		if(devices.size() == 0) {
			System.out.println("No available devices.");
			return;
		}
		
		for(Actuator device: devices) {
			System.out.println(device.toString() + 
							   "\nStatus: " + device.getStatus());
			}
	}
	
	public int getTemperature() {
		int avg = 0;
		int num_sensors;
		int temp;
		Collection<Sensor> temp_devices = temp_sensors.values();
		Sensor hum_sensor;
		
		num_sensors = temp_devices.size();
		if(num_sensors == 0) {
			System.out.println("No available devices.");
			return 0;
		}
		
		for(Sensor device: temp_devices) {
			temp = device.getValue();
			avg += temp;
			
			System.out.println(device.toString() +
					   		   "\nTemperature: " + temp);
			
			hum_sensor = hum_sensors.get(device.getAddress());
			if(hum_sensor != null) {
				System.out.println("Humidity: " + hum_sensor.getValue());
			}
		}
		
		avg /= num_sensors;
		
		return avg;
	}
	
	public void deviceList(String type) {
		if(type.contains("radiator")) {
			Collection<Actuator> devices = radiators.values();
			for(Actuator device: devices) {
				System.out.println(device.toString());
			}
		} else {
			Collection<Sensor> devices = temp_sensors.values();
			for(Sensor device: devices) {
				System.out.println(device.toString());
			}
		}
	}
	
	public boolean addDeviceToRoom(String address, String room) {
		if(!radiators.containsKey(address) &&
		   !temp_sensors.containsKey(address) &&
		   !hum_sensors.containsKey(address))
			return false;
		
		if(radiators.containsKey(address)) {
			Actuator device = radiators.get(address);
			device.setRoom(room);
			System.out.println(device.toString());
		} else if (temp_sensors.containsKey(address)) {
			Sensor device = temp_sensors.get(address);
			device.setRoom(room);
			System.out.println(device.toString());
			
			if(hum_sensors.containsKey(address)) {
				device = hum_sensors.get(address);
				device.setRoom(room);
				System.out.println(device.toString());
			}
		}
		
		return true;
	}
	
}
