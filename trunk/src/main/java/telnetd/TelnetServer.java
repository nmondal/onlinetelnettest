package telnetd;

import java.io.*;
import java.net.*;
import java.util.HashMap;

/**
 *
 * @author nmondal
 */
public class TelnetServer {

    public static final int PORT = 4444 ;
    public static final int MAX_CON = 1000 ;
    
    private  int port = 4444;
    private  int maxConnections = 1000 ;
    public static  HashMap<String , Thread> clientMap;
	
	public synchronized static boolean  clearConnection (String ip)
	{
		if ( clientMap.containsKey( ip ))
		{
			clientMap.remove(ip);
			String fileName = String.format("%s.txt", ip);
			File f = new File(fileName);
			if (f.exists()) {
				f.delete();
			} 
			return true;
		}
		else
		{
			return false ;
		}
	}
	public synchronized static String  getStats ()
	{
		StringBuffer sbuf = new StringBuffer();
		sbuf.append(  "Total Clients Now : " + clientMap.keySet().size() );
		sbuf.append("\r\n");
		sbuf.append( "IP\t\t:   IS Client Active ?" );
		sbuf.append("\r\n");
		
		for ( String s : clientMap.keySet())
		{
			sbuf.append( s + " : " );
			sbuf.append( ((Thread)clientMap.get(s)).isAlive()  );
			sbuf.append("\r\n");
		}
		return sbuf.toString();
	}

	public void printDetails()
	{
		try {
			InetAddress addr = InetAddress.getLocalHost();

			System.out.printf( "Server Started at : %s\n", addr );

		} catch (UnknownHostException e) {
		}
	}
    // Listen for incoming connections and handle them

    public void runServer() {
        int i = 0;

		clientMap = new HashMap<>();
        try {
            ServerSocket listener = new ServerSocket(port);

            Socket server;

	        printDetails();

            while ((i++ < maxConnections) || (maxConnections == 0)) {
                
                server = listener.accept();
                String name = server.getRemoteSocketAddress().toString();
				name = name.split(":")[0];
				name = name.substring(1);
                System.out.printf( "%s\n" , name );
				boolean retry = clientMap.containsKey( name );
				QuestionCommunicator conn_c = new QuestionCommunicator(server, name , retry );
				Thread t = new Thread(conn_c);
				if ( name.equalsIgnoreCase( QuestionCommunicator.ADMIN_CLIENT   ) )
				{
					System.out.println("Starting Admin console...LOCALHOST" );
				}
				else if ( retry)
				{
					System.out.printf("IP %s tried again... ask why?\n" , name );
				}
				else
				{
					clientMap.put(name, t);
					
				}
				t.start();
            }
        } catch (IOException ioe) {
            System.out.println("IOException on socket listen: " + ioe);
            ioe.printStackTrace();
        }
    }
    public TelnetServer(int port, int max_con)
    {
        this.port = port;
        this.maxConnections = max_con;
    }
    public TelnetServer(int port)
    {  
        this(port,MAX_CON);
    }
    public TelnetServer()
    {  
        this(PORT,MAX_CON);
    }
}
