package Main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import Converter.TimeConverter;
import Misc.SDL;
import PingChecker.Address;
import PingChecker.Port;

@Deprecated
public class SQLClient {
	static Statement stmt = null;
	static Connection con = null;
	
	public static String status = "No Info";
	
	public static void openConnection() {
		UpDawgLauncher.log("Opening SQL connection\n");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			con = DriverManager.getConnection(
					"jdbc:mysql://"+Config.sql_address+"/"+Config.sql_database,
					Config.sql_username, Config.sql_password);
			
			stmt = con.createStatement();
			setupDatabase();
		} catch (Exception e) {
//			UpDawgLauncher.log(e);
//			System.exit(0);
			SDL.sleep(1000);
			UpDawgLauncher.log("Failed to open SQL connection\n");
		}
	}
	
	
	public static void closeConnection() {
		UpDawgLauncher.log("Closing SQL conection\n");
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
			if(Config.sql_lazy) update(); else update(a);
		}
	}
	
	public static void updateForce() {
		for(int z=0;z<UpDawgLauncher.addresses.size();z++) {
			Address a = UpDawgLauncher.addresses.get(z);
			if(Config.sql_lazy) update(); else update(a);
		}
	}
	
	/**
	 * Updates the address given in the SQL then sets address.updateSQL to false
	 * 
	 * @param address  The address that will be updated in the SQL
	 */
	public static void update(Address address) {
		if(Config.sql_groupID == null && Config.sql_groupID.length() <= 0) {
			UpDawgLauncher.log("ERROR! Can not update when no groupID is given!\n");
			return;
		}
		if(!Config.sql_updateAddresses) return;
		
		String timeStamp = Instant.now().getEpochSecond() + "";
		
		String query = "";
		
		try {
			query  = "UPDATE Ips SET";
			query += " HostName='"+address.hostName+"',";
			query += " Stat="+address.status+",";
			query += " LastUpdate=" + timeStamp;
			query += " WHERE PingingAddress='"+address.pingingAddress+"';";
			UpDawgLauncher.log( query );
			stmt.executeUpdate(query);
			
			query = "DELETE FROM NMap WHERE uID=" + address.uid;
			stmt.executeUpdate(query);
			
			for(Port port:address.ports) {
				query = String.format("INSERT INTO NMap (uID, port, state, service) VALUES (%s, '%s', '%s', '%s')", address.uid, port.number, port.state, port.service);
				UpDawgLauncher.log( query + "\n" );
				stmt.executeUpdate(query);
			}
			
			address.updateSQL = false;
		} catch (SQLException e) {
//			e.printStackTrace();
			UpDawgLauncher.log("Query: " + query + "\n");
			UpDawgLauncher.log( "SQL connection loss, atempting to reopen.\n" );
			openConnection();
		}
	}
	
	/**
	 * Gets addresses from the SQL server and updates the live addresses
	 */
	public static void getAddresses() {
		// Create list of addresses from the SQL server
		if(stmt == null || con == null) openConnection();
		if(stmt == null || con == null) return;
		
		ArrayList<Address> addresses = new ArrayList<Address>();
		try {
			String where = String.format("('HID', '%s')", Config.sql_groupID);
			
			String query = "SELECT * FROM Ips WHERE GroupID IN " + where;
//			String query = (Config.sql_groupID != null && Config.sql_groupID.length() > 0)? "SELECT * FROM Ips WHERE GroupID='" + Config.sql_groupID + "'" : "SELECT * FROM Ips";
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				Address address = null;
				String pingingAddress = rs.getString("PingingAddress");
				String nickname = rs.getString("NickName");
				String uid = rs.getString("uID");
				Boolean hid = rs.getString("GroupID").equals("HID");
				
				for(int x=0;x<UpDawgLauncher.addresses.size();x++)
					if(UpDawgLauncher.addresses.get(x).pingingAddress.equals(pingingAddress)) {
						address = UpDawgLauncher.addresses.get(x);
						break;
					}
				
				if(address == null) {
					address = new Address( pingingAddress, uid );
					address.hidden = hid;
					if(!hid) UpDawgLauncher.log("Added new address: " + pingingAddress + "\n");
				}
				
				
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
