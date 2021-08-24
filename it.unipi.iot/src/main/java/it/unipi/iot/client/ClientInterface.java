package it.unipi.iot.client;

import it.unipi.iot.server.*;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class ClientInterface {
	private static Server server;
	private static BufferedReader input;
	private static SmartMode smartMode = SmartMode.MANUAL;
	private static Level radiatorLevel = Level.OFF;
	private static int temperature = 0;
	private static int humidity = 0;
	
	public enum SmartMode {
		AUTO,
		MANUAL;
	}
	
	public enum Level {
		OFF("off"),
		ECO("on"),
		COMFORT("max");
		
		public final String label;
		
		private Level(String label) {
			this.label = label;
		}
	}
	
	public static void main(String[] args) {
		try {
			out.println("+++++++++++++++++++ SMART HEATING CLIENT +++++++++++++++++++");
			out.println("Starting coap server at port 5683...");
			server = new Server(5683);
		
			new Thread() {
				public void run() {
					server.start();
				}
			}.start();
			
			while(true) {
				input = new BufferedReader(new InputStreamReader(System.in));
				
				help();
				prompt();
				
				String cmd = input.readLine();
				
				switch(cmd) {
					case "!status": 
						showStatus();
						break;
					case "!setstatus": 
						setTemperature();
						break;
					case "!temp": 
						showTemperature();
						break;
					case "!mode": 
						setMode();
						break;
					case "!addroom": 
						addRoom();
						break;
					case "!shutdown":
						shutDown();
						break;
					default:
						out.println("Please insert a valid command.");
				}
				
				if(cmd.contains("!shutdown")) break;
			}
			
			out.println("Bye...");
			input.close();
		} catch (Exception e) {
			server.stop();
			server.destroy();
			e.printStackTrace();
		}
		
	}
	
	private static void help() {
		out.print("------------------- Choose an option -------------------\n" 		+
				  "\n" 																+
				  "!status - show system status.\n" 								+
				  "!setstatus - set radiators temperature.\n" 						+
				  "!temp - house temperature.\n" 									+
				  "!mode - set system mode.\n" 										+
				  "!addroom - assign a device to a room.\n" 						+
				  "!shutdown - shut down the system.\n");
	}
	
	private static void prompt() {
		out.print("> ");
	}
	
	private static void showStatus() {
		ResourcesHandler rh = ResourcesHandler.getInstance();
		out.println("------------------- Radiators Status -------------------");
		rh.getRadiatorsStatus();
		out.println("------------------- Sensors Status -------------------");
		rh.getSensorsStatus();
	}
	
	private static void showTemperature() {
		out.println("------------------- House Temperature -------------------");
		ResourcesHandler rh = ResourcesHandler.getInstance();
		temperature = rh.getTemperature();
		humidity = rh.getHumidity();
		out.println("The average house temperature is " + temperature);
		out.println("The average house humidity is " + humidity);
	}
	
	private static void setTemperature() throws IOException {
		out.println("------------------- Set Temperature -------------------");
		if(smartMode == SmartMode.AUTO) {
			out.println("Heating system into auto mode, switch to manual mode to set temperature level.");
		} else {
			ResourcesHandler rh = ResourcesHandler.getInstance();
			int temp_delta = 0;
			out.println("Set temperature level (off, eco, comfort):");
			prompt();
			String level = input.readLine();
			switch(level.toLowerCase()) {
				case "off": 
					radiatorLevel = Level.OFF;
					break;
				case "eco":
					radiatorLevel = Level.ECO;
					temp_delta = 2;
					break;
				case "comfort":
					radiatorLevel = Level.COMFORT;
					temp_delta = 5;
					break;
				default:
					out.println("Please invert a valid level.");
			}
			rh.setRadiatorsStatus(radiatorLevel.label);
			out.println("Setting new temperature and humidity.");
			rh.setTemperature(temp_delta);
		}
	}
	
	private static void autoMode() {
		int house_temp = 0;
		int delta = 0;
		ResourcesHandler rh = ResourcesHandler.getInstance();
		
		out.println("------------------- Auto Mode -------------------");
		out.println("Checking house temperature...");
		house_temp = rh.getTemperature();
		out.println("House temperature: " + house_temp);
		out.println("Desired temperature: " + temperature);
		
		delta = Math.abs(house_temp - temperature);
		if(delta == 0) {
			radiatorLevel = Level.OFF;
		} else if(delta < 5) {
			radiatorLevel = Level.ECO;
		} else {
			radiatorLevel = Level.COMFORT;
		}	
		
		rh.setRadiatorsStatus(radiatorLevel.label);
		out.println("Setting new temperature and humidity.");
		rh.setTemperature(delta);
	}
	
	private static void setMode() throws IOException, NumberFormatException {
		out.println("------------------- Set Mode -------------------");
		out.println("Actual mode: " + smartMode.toString());
		out.print("Insert new mode (auto, manual):");
		prompt();
		String userMode = input.readLine();
			
		switch(userMode.toLowerCase()) {
			case "auto":
				smartMode = SmartMode.AUTO;
				out.print("Set desired temperature: ");
				prompt();
				temperature = Integer.parseInt(input.readLine());
				autoMode();
				break;
			case "manual":
				smartMode = SmartMode.MANUAL;
				setTemperature();
				break;
			default:
				out.println("Please insert a valid mode.");
		}
	}
	
	private static void addRoom() throws IOException {
		out.println("------------------- Assign a room -------------------");
		out.println("Insert a device type (radiator, sensor):");
		prompt();
		String type = input.readLine();
		
		if(!type.equalsIgnoreCase("radiator") &&
		   !type.equalsIgnoreCase("sensor")) {
			out.println("Device type not valid.");
			return;
		}
		
		ResourcesHandler rh = ResourcesHandler.getInstance();
		rh.deviceList(type);
		
		out.println("Insert a valid device address:");
		prompt();
		String address = input.readLine();
		
		out.println("Insert a room name:");
		prompt();
		String room = input.readLine();
		
		if(rh.addDeviceToRoom(address, room)) {
			out.println("Success.");
		} else {
			out.println("Device address not valid.");
		}
	}
	
	private static void shutDown() {
		out.println("Shutting down...");
		server.stop();
		server.destroy();
	}
}
