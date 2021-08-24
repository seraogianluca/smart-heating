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
		Collection<Actuator> devices = radiators.values();
		
		for(Actuator device: devices) {
			response = device.post("{\"status\":\"" + status.toLowerCase() + "\"}");
			printInfo(device, "Response: " + response);
			
			if(response.startsWith("2")) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
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
			printInfo(device, "Status: " + device.getStatus());
		}
	}
	
	public void getSensorsStatus() {
		Collection<Sensor> temp_devices = temp_sensors.values();
		Sensor hum_sensor;

		if(temp_devices.size() == 0) {
			System.out.println("No available devices.");
			return;
		}
		
		for(Sensor device: temp_devices) {
			hum_sensor = hum_sensors.get(device.getAddress());
			if(hum_sensor != null) {
				printInfo(device, "Temperature: " + device.getValue() +
								  "\nHumidity: " + hum_sensor.getValue());
			} else {
				printInfo(device, "Temperature: " + device.getValue());
			}
		}
	}
	
	public int getTemperature() {
		int avg = 0;
		int num_sensors;
		Collection<Sensor> temp_devices = temp_sensors.values();
		
		num_sensors = temp_devices.size();
		if(num_sensors == 0) {
			System.out.println("No available devices.");
			return 0;
		}
		
		for(Sensor device: temp_devices) {
			avg += device.getValue();
		}
		
		avg /= num_sensors;		
		return avg;
	}
	
	public void setTemperature(int delta) {
		String response;
		Collection<Sensor> devices = temp_sensors.values();
		Sensor hum_sensor;
		int hum_delta;
		
		if(delta == 0) {
			// if radiators are off suppose to decrease temp
			delta = -3;
		}
		
		hum_delta = -delta;
		
		for(Sensor device: devices) {
			response = device.post("{\"delta\":\"" + delta + "\"}");
			System.out.println("Temperature sensor response: " + response);
			hum_sensor = hum_sensors.get(device.getAddress());
			
			if(hum_sensor != null) {
				// Increase in temp is a decrease in hum and vice versa 
				hum_sensor.post("{\"delta\":\"" + hum_delta + "\"}");
				System.out.println("Humidity sensor response: " + response);
			}		
		}
	}
	
	public int getHumidity() {
		int avg = 0;
		int num_sensors;
		Collection<Sensor> hum_devices = hum_sensors.values();
		
		num_sensors = hum_sensors.size();
		if(num_sensors == 0) {
			System.out.println("No available devices.");
			return 0;
		}
		
		for(Sensor device: hum_devices) {
			avg += device.getValue();
		}
		
		avg /= num_sensors;		
		return avg;
	}
	
	public void deviceList(String type) {
		if(type.contains("radiator")) {
			Collection<Actuator> devices = radiators.values();
			for(Actuator device: devices) {
				printInfo(device, null);
			}
		} else {
			Collection<Sensor> devices = temp_sensors.values();
			for(Sensor device: devices) {
				printInfo(device, null);
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
			printInfo(device, null);
		} else if (temp_sensors.containsKey(address)) {
			Sensor device = temp_sensors.get(address);
			device.setRoom(room);
			printInfo(device, null);
			
			if(hum_sensors.containsKey(address)) {
				device = hum_sensors.get(address);
				device.setRoom(room);
				printInfo(device, null);
			}
		}
		
		return true;
	}
	
	private void printInfo(Resource device, String additional) {
		System.out.println("----- Resource info -----");
		System.out.println(device.toString());
		if(additional != null)
			System.out.println(additional);
	}
}
