package PingChecker;

import java.util.ArrayList;

import Main.SQLClient;

import java.time.LocalDateTime;

public class Address {
	public boolean updateSQL = false, pingWorking = false, hidden = false;
	public int status = -1;
	public String address = "", hostName = "", nickname = "", pingingAddress = "", uid = "-1";
	
	public ArrayList<Port> ports = new ArrayList<Port>();
	
	//Temp Info
	public double lastTemp = -1, lastHumidity = -1;
	
	// Time
	public LocalDateTime lastPingTime, lastTempCheck, lastDownTime;
	
	public Address(String address, String uid) {
		nickname = hostName = address;
		this.address = address;
		pingingAddress = address;
		this.uid = uid;
	}
	
	// Will reset the lastTemp, lastHumidity, and status when they have not been pinged after a certain amount of time
	public void checkTimes() {}
	
	public void addPort(String str) {
		Port port = new Port(str);
		for(int z=0;z<ports.size();z++)
			if(port.number == ports.get(z).number) {
				ports.set(z, port);
				return;
			}
		ports.add(port);
		updateSQL = true;
	}
	
	public void setDown() {
		updateSQL = status != 0;
		status = (status > 0)?status - 1:0;
		lastDownTime = LocalDateTime.now();
		SQLClient.update(this);
	}
	public void setUp() {
		// Check if change is made
		updateSQL = status != 2;
		status = 2;
		SQLClient.update(this);
	}
	
	public void setLastPingTime() { lastPingTime =  LocalDateTime.now(); }
	public void setLastTempCheck() { lastTempCheck =  LocalDateTime.now(); }
}