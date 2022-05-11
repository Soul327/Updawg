package Main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import Misc.GetSystemInfo;

public class Config {
	// Local config file variables
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ssa MM/dd/YYYY");
	
	// General
	public static File    configFile  = new File("C:\\Sol\\UpDawg.config");
	public static boolean debug = false;
	
	//Ping Information
	public static boolean nmap                = false;
	public static int     threadCount         = 10;
	public static int     pingTimeOutTime     = 1000;
	
	// Local Java Server
	public static boolean ljs_enable          = false;
	public static int     ljs_port            = 1919;// Server
	public static String  ljs_address         = "127.0.0.1";// Client
	public static int     passDataTime        = 1000;
	public static int     pingCheckerTime     = 5000;
	public static int     tempServerRequestTime = 60000;
	
	// Local Window Client
	public static int     swFontSize          = 15;
	public static boolean localWindowClient   = false;
	
	// SQL
	public static boolean sql_enable          = true;
	public static boolean sql_getAddresses    = true;
	public static boolean sql_updateAddresses = false;
	public static int     sql_updateTime      = 1000*10;// In milliseconds
	public static String  sql_address         = "";
	public static String  sql_username        = "";
	public static String  sql_password        = "";
	public static String  sql_database        = "";
	
	public static void init() {
		if(!GetSystemInfo.isWindows())
			configFile = new File("UpDawg.config");
		
		try {
			System.out.println( configFile.getAbsolutePath() );
			System.out.println("Loading config file...");
			if(!configFile.exists()) {
				System.out.println("No config file found.");
				configFile.getParentFile().mkdirs();
				configFile.createNewFile();
				return;
			}
			Scanner scanner = new Scanner(configFile);
			
			while(scanner.hasNextLine()) {
				String temp = "";// Init variable
				String line = scanner.nextLine();// Get next line from file
				
				// May not be need anymore, not sure where this is used
				if(line.startsWith(temp = "DEBUG ")) { debug = true; System.out.println("WARNING: DEBUGGING IS ENABLED"); }
				
				// Local Window Client
				if(line.startsWith(temp = "SWfontSize "         )) swFontSize          = Integer.parseInt( line.replaceAll(temp, "") );
				if(line.startsWith(temp = "localWindowClient "  )) localWindowClient   = line.contains("TRUE") || line.contains("true");
				
				// SQL 
				if(line.startsWith(temp = "sql_enable"          )) sql_enable          = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_getAddresses"    )) sql_getAddresses    = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_updateAddresses" )) sql_updateAddresses = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "sql_updateTime "     )) sql_updateTime      = Integer.parseInt(line.replaceAll(temp, ""));
				if(line.startsWith(temp = "sql_address "        )) sql_address         = line.replaceAll(temp, "");
				if(line.startsWith(temp = "sql_username "       )) sql_username        = line.replaceAll(temp, "");
				if(line.startsWith(temp = "sql_password "       )) sql_password        = line.replaceAll(temp, "");
				if(line.startsWith(temp = "sql_database "       )) sql_database        = line.replaceAll(temp, "");
				
				// Ping information
				if(line.startsWith(temp = "pingTimeOutTime "    )) pingTimeOutTime     = Integer.parseInt( line.replaceAll(temp, "") );
				if(line.startsWith(temp = "nmap "               )) nmap                = line.contains("TRUE") || line.contains("true");
				if(line.startsWith(temp = "pingingThreadCount " )) threadCount         = Integer.parseInt( line.substring("PingingThreadCount".length()+1) );
				
				// Local Java Server
				if(line.startsWith(temp = "ljs_enable "  )) ljs_enable   = line.contains("TRUE") || line.contains("true");
			}
			scanner.close();
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public static void writeConfigFile() {
		try {
			FileWriter mw = new FileWriter( configFile );
			mw.write("# General\n");
			mw.write("DEBUG "+debug+"\n");
			mw.write("\n");
			mw.write("SWfontSize "+swFontSize+"\n");
			mw.write("localWindowClient "+localWindowClient+"\n");
			mw.write("\n");
			mw.write("# SQL\n");
			mw.write("sql_enable "+sql_enable+"\n");
			mw.write("sql_getAddresses "+sql_getAddresses+"\n");
			mw.write("sql_updateAddresses "+sql_updateAddresses+"\n");
			mw.write("sql_updateTime "+sql_updateTime+"\n");
			mw.write("sql_address "+sql_address+"\n");
			mw.write("sql_database "+sql_database+"\n");
			mw.write("sql_username "+sql_username+"\n");
			mw.write("sql_password "+sql_password+"\n");
			mw.write("\n");
			mw.write("# Ping Information\n");
			mw.write("nmap "+nmap+"\n");
			mw.write("pingTimeOutTime "+pingTimeOutTime+"\n");
			mw.write("pingingThreadCount "+threadCount+"\n");
			mw.write("\n");
			mw.write("# Local Java Server\n");
			mw.write("ljs_enable "+ljs_enable+"\n");
			
			mw.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
}
