package Main;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import Misc.SDL;
import PingChecker.Address;
import Socket.SocketMisc;

public class LJS extends Thread {
	ArrayList<Handler> handles;
	
	public LJS() {
		this.setName("LJS");
	}
	
	/**
	 * Runs the server that waits for clients only, checking for data will be handled by the main thread to save performance
	 */
	public void run() {
		/* Start thread for checking if clients have sent data */
		(new Thread() {
			public void run() {
				while(true) {
					SDL.sleep(1000);
					checkClients();
				}
			}
		}).start();
		
		try {
			ServerSocket ss = new ServerSocket(Config.ljs_port);
			handles = new ArrayList<Handler>(); //Stores all clients
			System.out.println("Server started");
			
			while (!ss.isClosed()) {
				Socket sock = ss.accept();
				Handler hand = new Handler(sock);
				hand.start();
				handles.add(hand);
			}
			
			ss.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void checkClients() {
		for(int z=0;z<handles.size();z++) {
			checkClient( handles.get(z) );
			if(handles.get(z).clientFailedToRespond >= 10) {
				try {
					handles.get(z).join();
					System.out.println("Client "+handles.get(z).name+"'s connection has been dropped.");
					handles.remove(z--);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void checkClient(Handler handler) {
		try {
			// Grab matching address
			Address address = handler.getAddress();
			if(handler.in.available() > 0) {
				handler.clientFailedToRespond = 0;
				int head = handler.in.read();
				switch(head) {
					case SocketMisc.custom:
						int temperature = handler.in.read();
						int humidity = handler.in.read();
						System.out.printf( "Temp: %d Humidity: %d%n", temperature, humidity );
						
						if(address != null) {
							address.lastTemp = temperature;
							address.lastHumidity = humidity;
							address.updateSQL = true;
						}
						break;
				}
			} else {
				handler.clientFailedToRespond++;
			}
			System.out.println("Hit");
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}

class Handler extends Thread {
	String name = "";
	Socket socket;
	InputStream in;
	OutputStream out;
	int clientFailedToRespond = 0;
	
	public Handler(Socket sock) {
		socket = sock;
		
		try {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} catch(Exception e) {}

		name = socket.getInetAddress().getCanonicalHostName();
		name = name.substring(0,name.indexOf("."));
		
		this.setName("LJS - " + name);
	}
	
	
	/** Returns the address that matches the name of the connection to LJS if there is one */
	public Address getAddress() {
		for(int z=0;z<UpDawgLauncher.addresses.size();z++)
			if(UpDawgLauncher.addresses.get(z).pingingAddress.equalsIgnoreCase(name)) {
				System.out.println("FOUND ~ " + UpDawgLauncher.addresses.get(z).nickname);
				return UpDawgLauncher.addresses.get(z);
			}
		System.out.println("No address found");
		return null;
	}
	
	public void run() {}
}
