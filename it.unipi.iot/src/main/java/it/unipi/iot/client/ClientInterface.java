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
	private static boolean discovery = false;
	
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
			out.println("Starting coap server at port 5683...");
			server = new Server(5683);
			server.start();	
			
			while(true) {
				input = new BufferedReader(new InputStreamReader(System.in));
				
				help();
				prompt();
				
				int cmd = Integer.parseInt(input.readLine());
				while(cmd < 1 || cmd > 6) {
					out.println("Please insert a valid command.\n");
					prompt();
					cmd = Integer.parseInt(input.readLine());
				}
				
				switch(cmd) {
					case 1: 
						showStatus();
						break;
					case 2: 
						showTemperature();
						break;
					case 3: 
						setTemperature();
						break;
					case 4: 
						setMode();
						break;
					case 5: 
						register();
						break;
					case 6:
						shutDown();
						break;
				}
				
				if(cmd == 6) break;
			}
			
			out.println("Bye...");
			
		} catch(NumberFormatException e) {
			out.println("Invalid command.\n");
			server.stop();
			server.destroy();
		} catch (IOException e) {
			server.stop();
			server.destroy();
			e.printStackTrace();
		}
		
	}
	
	public static void addResources(String sourceAddress, String resources) {
		String[] resourcesText = resources.split(",");
		boolean observable;
		
		for(int i = 1; i < resourcesText.length; i++) {
			observable = false;
			
			// </temp>;title="Temperature sensor";rt="temp";if="sensor";obs
			String[] resourceText = resourcesText[i].split(";");
			String name = resourceText[3].substring(resourceText[3].indexOf("\"") + 1, 
													resourceText[3].length() - 1);
			String type = resourceText[2].substring(resourceText[2].indexOf("\"") + 1,
													resourceText[2].length() - 1);
			
			System.out.println("Resource found: " + name + " " + type);
			System.out.println("Insert room name:");
			prompt();
			
			String room;
			try {
				room = input.readLine();
			} catch (IOException e) {
				return;
			}
			
			Resource resource = null;
			if(name.contains("actuator")) {
				resource = new Actuator(name, type, sourceAddress, room);
			} else {
				resource = new Sensor(name, type, sourceAddress, room);
			}
				
			if(resourceText.length > 3) {
				System.out.println("The resource is observable.");
				observable = true;
				resource.setObservable(observable);
				
				if(name.contains("actuator")) {
					Actuator actuator = (Actuator)resource;
					actuator.observing();
				} else {
					Sensor sensor = (Sensor)resource;
					sensor.observing();
				}
			}
				
			ResourcesHandler df = ResourcesHandler.getInstance();
			df.addDevice(room, resource);
		}
		
		discovery = false;
	}
	
	private static void help() {
		out.print("Choose an option:\n" +
				  "\n" +
				  "1) Show status.\n" +
				  "2) Show temperature.\n" +
				  "3) Set temperature.\n" +
				  "4) Set mode.\n" +
				  "5) Connect a new device.\n" +
				  "6) Shut down.\n");
	}
	
	private static void prompt() {
		out.print("> ");
	}
	
	private static void showStatus() {
		
	}
	
	private static void showTemperature() {
		out.println("Temperature: " + temperature);
	}
	
	private static void setTemperature() throws IOException {
		if(smartMode == SmartMode.AUTO) {
			out.println("Heating system into auto mode, switch to manual mode to set temperature level.");
		} else {
			out.println("Set temperature level (OFF, ECO, COMFORT):");
			prompt();
			String level = input.readLine();
			switch(level) {
				case "OFF": 
					radiatorLevel = Level.OFF;
					break;
				case "ECO":
					radiatorLevel = Level.ECO;
					break;
				case "COMFORT":
					radiatorLevel = Level.COMFORT;
					break;
				default:
					out.println("Please invert a valid level.");
			}
			
			ResourcesHandler rh = ResourcesHandler.getInstance();
			rh.setRadiatorsStatus(radiatorLevel.label);
		}
	}
	
	private static void setMode() throws IOException, NumberFormatException {
			out.println("Actual mode: " + smartMode.toString());
			out.print("Insert new mode (AUTO, MANUAL):");
			prompt();
			String userMode = input.readLine();
			
			switch(userMode) {
				case "AUTO": 
					out.print("Set desired temperature: ");
					prompt();
					int temp = Integer.parseInt(input.readLine());
					temperature = temp;
					break;
				case "MANUAL":
					smartMode = SmartMode.MANUAL;
					setTemperature();
					break;
				default:
					out.println("Please insert a valid mode.");
				
			}
	}
	
	private static void register() {
		discovery = true;
		
		while(discovery) {
			server.registration();
		}
	}
	
	private static void shutDown() {
		out.println("Shutting down...");
		server.stop();
		server.destroy();
	}
}
