package telnetd;

import telnetd.auth.Session;
import telnetd.auth.Authentication;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

/**
 * @author nmondal
 */
public class TelnetServer {

	public static final int PORT = PropertyHelper.serverProperties.getIntegerDefault("PORT", 4444);
	public static final int MAX_CON = PropertyHelper.serverProperties.getIntegerDefault("MAX_CON", 1000);
	public static String RESULTS_REPO =
			PropertyHelper.serverProperties.getPropertyDefault("RESULT_REPO", "../results");
	public static final int GC_BASELINE = (int) MAX_CON / 3 ;

	private int port = PORT;
	private int maxConnections = MAX_CON;
	public static ArrayList<Session > clientList;
	private String storageLocation;



	public synchronized static boolean clearConnection(String name) {
		return false;
	}



	public synchronized static String getStats() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Total Clients Now : " + clientList.size());
		sbuf.append("\r\n");
		sbuf.append("IP\t\t:   IS Client Active ?");
		sbuf.append("\r\n");

		for (Session session : clientList) {
			sbuf.append(session.getName() + " : ");
			sbuf.append( session.getThread().isAlive());
			sbuf.append("\r\n");
		}
		return sbuf.toString();
	}

	public void printDetails() {
		try {
			InetAddress addr = InetAddress.getLocalHost();

			System.out.printf("Server Started at : %s\n", addr);

		} catch (UnknownHostException e) {
		}
	}
	// Listen for incoming connections and handle them


	public void runServer() {
		int i = 0;

		clientList = new ArrayList<>();
		try {
			ServerSocket listener = new ServerSocket(port);

			Socket server;

			printDetails();

			while ((i++ < maxConnections) || (maxConnections == 0)) {

				server = listener.accept();
				Session session = new Session(server, this.storageLocation );

				new Thread(session).start();

				String name = session.getName();
				System.out.printf("%s STARTED\r\n", name );
				clientList.add( session );
				doGC();

			}
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

	private synchronized void  doGC()
	{
		if ( clientList.size() >  GC_BASELINE )
		{
			ArrayList<Session> tmp = new ArrayList<>();
			for ( Session session : clientList )
			{
				if ( session.getClientType() != Session.ClientType.UnAuthorized )
				{
					tmp.add(session);
				}
			}
			clientList.clear();
			clientList = tmp;
		}
	}

	public TelnetServer(int port, int max_con) {
		this.port = port;
		this.maxConnections = max_con;
		try{
			File file = new File( String.format("%s/%s_Log.txt",
					RESULTS_REPO , PropertyHelper.getTimeStampAsValidFileName() ) );
			storageLocation = file.getAbsolutePath();
            Authentication.loadAuthMap();
			System.out.printf( "Started writing logs and results to file : %s\r\n", file.getAbsolutePath() );

		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public TelnetServer(int port) {
		this(port, MAX_CON);
	}

	public TelnetServer() {
		this(PORT);
	}
}
