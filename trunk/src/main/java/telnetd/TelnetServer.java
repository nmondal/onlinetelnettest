package telnetd;

import telnetd.communicator.AdminClient;
import telnetd.communicator.Communicator;
import telnetd.communicator.QuestionCommunicator;

import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintStream;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

/**
 * @author nmondal
 */
public class TelnetServer {

	public static final int PORT = PropertyHelper.serverProperties.getIntegerDefault("PORT", 4444);
	public static final int MAX_CON = PropertyHelper.serverProperties.getIntegerDefault("MAX_CON", 1000);
	public static String RESULTS_REPO =
			PropertyHelper.serverProperties.getPropertyDefault("RESULTS_REPO", "../results");


	private int port = PORT;
	private int maxConnections = MAX_CON;
	public static HashMap<String, Thread> clientMap;
	private PrintStream persistentStorage;

	public PrintStream getPersistentStorage()
	{
		return persistentStorage;
	}

	public synchronized static boolean clearConnection(String ip) {
		if (clientMap.containsKey(ip)) {
			clientMap.remove(ip);
			return true;
		} else {
			return false;
		}
	}

	public static Communicator createCommunicator(Socket socket, PrintStream stream) {
		if (Communicator.isSocketAdminConsole(socket)) {
			return new AdminClient(socket,stream);
		} else {
			return new QuestionCommunicator(socket,stream );
		}
	}

	public synchronized static String getStats() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Total Clients Now : " + clientMap.keySet().size());
		sbuf.append("\r\n");
		sbuf.append("IP\t\t:   IS Client Active ?");
		sbuf.append("\r\n");

		for (String s : clientMap.keySet()) {
			sbuf.append(s + " : ");
			sbuf.append(((Thread) clientMap.get(s)).isAlive());
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

		clientMap = new HashMap<>();
		try {
			ServerSocket listener = new ServerSocket(port);

			Socket server;

			printDetails();

			while ((i++ < maxConnections) || (maxConnections == 0)) {

				server = listener.accept();
				Communicator communicator = createCommunicator(server, this.persistentStorage );
				String name = communicator.getName();
				System.out.printf("%s\n", communicator.getName());
				Thread t = new Thread(communicator);

				if (communicator.isAdminConsole()) {
					System.out.println("Starting Admin console...LOCALHOST");
				} else {
					boolean retry = clientMap.containsKey(name);
					if (retry) {
						System.out.printf("IP %s tried again... ask why?\r\n", name);
					} else {
						clientMap.put(name, t);
					}
				}
				t.start();
			}
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}

	public TelnetServer(int port, int max_con) {
		this.port = port;
		this.maxConnections = max_con;
		try{
			File file = new File(  RESULTS_REPO +"/log.txt" );
			FileOutputStream fout = new FileOutputStream( file.getCanonicalPath() , true );
			persistentStorage = new PrintStream(fout , true );
			System.out.printf( "Started writing logs and results to file : %s\r\n", file.getCanonicalPath() );

		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	public TelnetServer(int port) {
		this(port, MAX_CON);
	}

	public TelnetServer() {
		this(PORT, MAX_CON);
	}
}
