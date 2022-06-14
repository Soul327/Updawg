package Main;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import Events.SimpleWindowEvent;
import Misc.KeyManager;
import Misc.MouseManager;
import Misc.SimpleWindow;
import PingChecker.Address;
import PingChecker.Port;
import Rendering.Graphics;

public class WindowClient implements SimpleWindowEvent {
	public static SimpleWindow window;
	
	public WindowClient() {
		System.out.println("Starting local client");
		window = new SimpleWindow();
		window.addSimpleWindowEvent(this);
		window.width = 1005;
		window.height = 645;
		window.name = "UpDawg "+UpDawgLauncher.version;
		window.start();
	}
	
	int longestAddress = 0;
	public void tar(Graphics g) {
		// Quick change font size
		if(KeyManager.keyRelease(KeyEvent.VK_UP)) Config.swFontSize++;
		if(KeyManager.keyRelease(KeyEvent.VK_DOWN)) Config.swFontSize--;
		
		longestAddress = 0;
		g.setFont("Courier New", Font.PLAIN, Config.swFontSize);
		//
		for(int z=0;z<UpDawgLauncher.addresses.size();z++)
			if(longestAddress < g.getStringLength( UpDawgLauncher.addresses.get(z).nickname ) )
				longestAddress = g.getStringLength( UpDawgLauncher.addresses.get(z).nickname );
		longestAddress += 10;
		
		int col = 0, lz = 0;
		for(int z=0;z<UpDawgLauncher.addresses.size();z++) {
			Address a = UpDawgLauncher.addresses.get(z);
			if(a.hidden) continue;
			if(g.fontSize*(1+z-lz) > window.height) { lz = z; col++; }
			
			switch(a.status) {
				case -1:g.setColor(new Color(100,100,100));break;
				case  0:g.setColor(new Color(255,  0,  0));break;
				case  1:g.setColor(new Color(255,155,  0));break;
				case  2:g.setColor(new Color(100,240,  0));break;
			}
			drawAddress(g, a, longestAddress*col+col, g.fontSize*(z-lz)+col);
		}
		
		if(drawAddressTime-- <= 0) {
			drawAddressTime = 30;
			if(drawAddress<3)
				drawAddress++;
			else
				drawAddress = 0;
		}
		
		renderInfoMenu(g);
	}
	
	int drawAddress = 0, drawAddressTime = 0;
	public void drawAddress(Graphics g, Address a, int x, int y) {
		String str = a.nickname;
		if(a.pingWorking) 
			switch(drawAddress) {
				case 0:str += "/";break;
				case 1:str += "|";break;
				case 2:str += "\\";break;
				case 3:str += "|";break;
			}
		g.outlineRect(x, y, longestAddress, g.fontSize);
		g.drawString(str, x+5, y+g.fontSize-g.fontSize*.05);
		
		int mx = MouseManager.mouseX;
		int my = MouseManager.mouseY;
		// Show extra data
		if(MouseManager.leftPressed)
			if(mx > x && mx < x+longestAddress && my > y && my < y+g.fontSize) {
//				g.setColor(new Color(255,0,0));
				g.setColor( g.g.getColor().darker() );
				g.drawRect(x, y, longestAddress, g.fontSize);
				infoMenuList.add("Address : "+a.address);
				infoMenuList.add("Hostname: "+a.hostName);
				infoMenuList.add("Nickname: "+a.nickname);
				infoMenuList.add("Pinging Address: "+a.pingingAddress);
				
				// Write ports out
				for(int z=0;z<a.ports.size();z++) {
					if(z==0) {
						infoMenuList.add("");
						infoMenuList.add( String.format("%-10s%-6s%s", "PORT", "STATE", "SERVICE") );
					}
					Port port = a.ports.get(z);
					infoMenuList.add( String.format("%-10s%-6s%s", port.number+"/"+port.conType, port.state, port.service) );
				}
				
				// Write
				if(a.lastPingTime != null || a.lastTempCheck != null) {
					infoMenuList.add("");
					
					if(a.lastPingTime != null) infoMenuList.add("Last Ping Time: " + Config.dtf.format( a.lastPingTime ));
					if(a.lastTempCheck != null) {
						infoMenuList.add("");
						infoMenuList.add("Temp: "+a.lastTemp+" Humidity:"+a.lastHumidity);
						infoMenuList.add("Last Temp Time: " + Config.dtf.format( a.lastTempCheck ));
					}
				}
			}
	}
	
	// Info menu
	ArrayList<String> infoMenuList = new ArrayList<String>();
	
	public void renderInfoMenu(Graphics g) {
		g.setFont("Courier New", Font.PLAIN, g.fontSize*.75);
		
		int width = 100, height = infoMenuList.size() * g.fontSize;
		// Set width as max width of the list being render in pixels
		for(int z=0;z<infoMenuList.size();z++)
			if(width<g.getStringLength(infoMenuList.get(z)))
				width = g.getStringLength(infoMenuList.get(z));
		width += 10;// Add 10 to width for formating
		
		g.setColor(new Color(100,100,100));
		g.outlineRect(window.width-width, 0, width, height);
		
		for(int z=0;z<infoMenuList.size();z++)
			g.drawOutlinedString(infoMenuList.get(z), window.width-width+5, g.fontSize*(1+z)-g.fontSize*.2);
		
		infoMenuList = new ArrayList<String>();
	}
}
