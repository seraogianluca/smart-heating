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
		} catch (IOException e) {
			server.stop();
			server.destroy();
			e.printStackTrace();
		}
		
	}
	
	private static void help() {
		out.print("------------------- Choose an option -------------------\n" 		+
				  "\n" 																+
				  "!status - show radiators status.\n" 								+
				  "!setstatus - set radiators temperature.\n" 						+
				  "!temp - show rooms temperature.\n" 								+
				  "!mode - set system mode.\n" 										+
				  "!addroom - assign a room to a device.\n" 									+
				  "!shutdown - shut down the system.\n");
	}
	
	private static void prompt() {
		out.print("> ");
	}
	
	private static void showStatus() {
		out.println("------------------- Radiators Status -------------------");
		ResourcesHandler rh = ResourcesHandler.getInstance();
		rh.getRadiatorsStatus();
		out.println("--------------------------------------------------------");
	}
	
	private static void showTemperature() {
		out.println("------------------- House Temperature -------------------");
		ResourcesHandler rh = ResourcesHandler.getInstance();
		temperature = rh.getTemperature();
		out.println("Average house temperature: " + temperature);
		out.println("---------------------------------------------------------");
	}
	
	private static void setTemperature() throws IOException {
		out.println("------------------- Set Temperature -------------------");
		if(smartMode == SmartMode.AUTO) {
			out.println("Heating system into auto mode, switch to manual mode to set temperature level.");
		} else {
			out.println("Set temperature level (off, eco, comfort):");
			prompt();
			String level = input.readLine();
			switch(level.toLowerCase()) {
				case "off": 
					radiatorLevel = Level.OFF;
					break;
				case "eco":
					radiatorLevel = Level.ECO;
					break;
				case "comfort":
					radiatorLevel = Level.COMFORT;
					break;
				default:
					out.println("Please invert a valid level.");
			}
			
			ResourcesHandler rh = ResourcesHandler.getInstance();
			rh.setRadiatorsStatus(radiatorLevel.label);
			out.println("Success.");
			out.println("-------------------------------------------------------");
		}
	}
	
	private static void setMode() throws IOException, NumberFormatException {
		out.println("------------------- Set Mode -------------------");
		out.println("Actual mode: " + smartMode.toString());
		out.print("Insert new mode (auto, manual):");
		prompt();
		String userMode = input.readLine();
			
		switch(userMode.toLowerCase()) {
			case "auto": 
				out.print("Set desired temperature: ");
				prompt();
				temperature = Integer.parseInt(input.readLine());
				break;
			case "manual":
				smartMode = SmartMode.MANUAL;
				setTemperature();
				break;
			default:
				out.println("Please insert a valid mode.");
		}
		out.println("------------------------------------------------");
	}
	
	private static void addRoom() throws IOException {
		out.println("------------------- Assign a room -------------------");
		out.println("Insert a device type (radiator, temp):");
		prompt();
		String type = input.readLine();
		
		if(!type.equalsIgnoreCase("radiator") &&
		   !type.equalsIgnoreCase("temp")) {
			out.println("Device type not valid.");
			out.println("-----------------------------------------------------");
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
		
		out.println("-----------------------------------------------------");
	}
	
	private static void shutDown() {
		out.println("Shutting down...");
		server.stop();
		server.destroy();
	}
}
