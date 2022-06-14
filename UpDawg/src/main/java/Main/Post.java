package Main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import Misc.Address;

public class Post {
	public static void updateAddress(Address address) {
		
	}
	
	public static ArrayList<String> post(String link, String args[]) {
		ArrayList<String> reList = new ArrayList<String>();
		try {
			// open a connection to the site
			URL url = new URL(link);
			URLConnection con = url.openConnection();
			// activate the output
			con.setDoOutput(true);
			PrintStream ps = new PrintStream(con.getOutputStream());
			// send your parameters to your site
			ps.print("IPID=89");
			
			ps.close(); // close the print stream
			
			// Read the webpage and close input
			BufferedReader in = new BufferedReader( new InputStreamReader(con.getInputStream()) );
			String line;
			while((line = in.readLine()) != null) {
				System.out.println( line );// Prints out website
				reList.add( line );
			}
			in.close();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reList;
	}
}
