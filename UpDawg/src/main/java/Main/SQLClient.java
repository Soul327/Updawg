package Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Converter.TimeConverter;
import PingChecker.Address;

public class SQLClient {
	static Statement stmt = null;
	static Connection con = null;
	
	public static String status = "No Info";
	
	public static void openConnection() {
		System.out.println("Opening SQL connection");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(
					"jdbc:mysql://"+Config.sql_address+"/"+Config.sql_database,
					Config.sql_username, Config.sql_password);
			
			stmt = con.createStatement();
			setupDatabase();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
	}
	public static void closeConnection() {
		System.out.println("Closing SQL conection");
		try {
			con.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cycles though all addresses in UpDawgLauncher.addresses and passes them to the 
	 * SQLClient.update(address) to be updated in the SQL server
	 */
	public static void update() {
		
		for(int z=0;z<UpDawgLauncher.addresses.size();z++) {
			Address a = UpDawgLauncher.addresses.get(z);
			if(!a.updateSQL) continue;
			update(a);
		}
	}
	
	/**
	 * Updates the address given in the SQL then sets address.updateSQL to false
	 * 
	 * @param address  The address that will be updated in the SQL
	 */
	public static void update(Address address) {
		try {
			String query;
			query  = "UPDATE Ips SET";
			query += " HostName='"+address.hostName+"',";
			query += " Time='"+(int)TimeConverter.nanoToMillisecond( address.lastTime )+"',";
			query += " AdvTime='"+(int)TimeConverter.nanoToMillisecond( address.advTime )+"',";
			query += " Stat="+address.status+",";
			query += " Temperature="+address.lastTemp+",";
			query += " Humidity="+address.lastHumidity;
			query += " WHERE PingingAddress='"+address.pingingAddress+"';";
			
			stmt.executeUpdate(query);
			address.updateSQL = false;
		} catch (SQLException e) {
			System.out.println( "SQL connection loss, atempting to reopen." );
			openConnection();
		}
	}
	
	/**
	 * Gets addresses from the SQL server and updates the live addresses
	 */
	public static void getAddresses() {
//		UpDawgLauncher.addresses = new ArrayList<Address>();
		// Create list of addresses from the SQL server
		if(stmt == null || con == null) openConnection();
		if(stmt == null || con == null) return;
		
		ArrayList<Address> addresses = new ArrayList<Address>();
		try {
			ResultSet rs = stmt.executeQuery("SELECT * FROM Ips");
			while (rs.next()) {
				Address address = null;
				String pingingAddress = rs.getString("PingingAddress");
				String nickname = rs.getString("NickName");
				
				for(int x=0;x<UpDawgLauncher.addresses.size();x++)
					if(UpDawgLauncher.addresses.get(x).pingingAddress.equals(pingingAddress)) {
						address = UpDawgLauncher.addresses.get(x);
						break;
					}
				
				if(address == null) address = new Address( pingingAddress );
				
				
				if(nickname != null) address.nickname = nickname;
				addresses.add( address );
				
			}
			UpDawgLauncher.addresses = addresses;
		} catch (SQLException e) {
//			e.printStackTrace();
		}
		
		// Update all addresses to fix stuck addresses
		update();
	}
	
	/**
	 * This function will check the given database and, if required, create the table of Ips automatically
	 */
	public static void setupDatabase() {
		// Create Ips table
		String query = "CREATE TABLE IF NOT EXISTS Ips (PingingAddress varchar(255) NOT NULL, Address varchar(255), HostName varchar(255), NickName varchar(255), Stat int(11) DEFAULT -1, PRIMARY KEY (PingingAddress))";
		try {
			stmt.executeUpdate(query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
