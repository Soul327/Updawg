package PingChecker;

import java.net.InetAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Main.Config;
import Misc.GetSystemInfo;

public class AddressesThread extends Thread {
	Address address;
	
	public AddressesThread(Address address) {
		this.address = address;
		this.setName("Pinging "+address.nickname);
	}
	
	public void run() {
		ping();
	}
	
	public void ping() {
		address.pingWorking = true;
		
		// Check if pingingAddress is a url
		Pattern p = Pattern.compile("http(s|):");
		Matcher m = p.matcher(address.pingingAddress); 
		if(m.find()) {
			System.out.println( address.pingingAddress );
			return;
		}
		
		try {
			InetAddress ia = InetAddress.getByName(address.address);
			if(ia.isReachable(Config.pingTimeOutTime)) {
				address.setUp();
				address.address = (ia+"" ).substring( (ia+"" ).lastIndexOf("/")+1 );
				address.hostName = ia.getCanonicalHostName(); // Set hostname
				
				if(address.nickname.equals(address.address)) address.nickname = address.hostName;
				
				if(Config.nmap) nmap();
			} else
				address.setDown();
		} catch (Exception e) {
			address.setDown();
		}
		address.pingWorking = false;
	}
	
	/**
	 * This runs an nmap scan and sets the results in the address
	 */
	public void nmap() {
		try {
			String[] output = GetSystemInfo.runCommand("cmd /c nmap "+address.address);
			
			boolean read = false;
			for(String s:output) {
				if(s == null || s.length() == 0) read = false;
				if(read) address.addPort(s);
				if(s.startsWith("PORT")) read = true;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
