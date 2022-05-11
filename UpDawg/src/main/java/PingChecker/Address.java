package PingChecker;

import java.util.ArrayList;

import Main.SQLClient;

import java.time.LocalDateTime;

public class Address {
	public boolean updateSQL = false;
	public boolean pingWorking = false;
	public int status = -1;
	public long lastTime = -1, advTime = -1;
	public String address = "", hostName = "", nickname = "", pingingAddress = "";
	
	ArrayList<Long> times = new ArrayList<Long>();
	
	public ArrayList<Port> ports = new ArrayList<Port>();
	
	//Temp Info
	public double lastTemp = -1, lastHumidity = -1;
	
	// Time
	public LocalDateTime lastPingTime, lastTempCheck, lastDownTime;
	
	public Address(String address) {
		nickname = hostName = address;
		this.address = address;
		pingingAddress = address;
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
	}
	
	public void setDown() {
		updateSQL = status != 0;
		status = (status > 0)?status - 1:0;
		lastDownTime = LocalDateTime.now();
		SQLClient.update();
	}
	public void setUp() {
		// Check if change is made
		updateSQL = status != 2;
		status = 2;
		SQLClient.update();
	}
	
	public void setLastPingTime() { lastPingTime =  LocalDateTime.now(); }
	public void setLastTempCheck() { lastTempCheck =  LocalDateTime.now(); }
	public long getSumOfTimes() {
		long sum = 0;
		for(int z=0;z<times.size();z++)
			sum += times.get(z);
		return sum;
	}
	public long getAdvOfTimes() {
		return getSumOfTimes() / times.size();
	}
}