package telnetd.communicator;


import telnetd.PropertyHelper;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Communicator implements Runnable {

	public static final String ADMIN_CLIENT = "127.0.0.1";
	public static final String PROMPT = ">";
	protected Socket server;
	protected String name;

	protected PrintStream persistentDataStream;

	protected PrintStream out;
	protected DataInputStream in;



	public String getName() {
		return name;
	}

	public boolean isAdminConsole() {
		return isSocketAdminConsole(server);
	}

	protected synchronized void persistData(String data)
	{
		String outputData = String.format("#\r\n%s->%s\r\n%s\r\n#\r\n", name, PropertyHelper.getTimeStamp(),data ) ;
		persistentDataStream.printf( outputData );
	}

	public abstract String processInput(String input);

	public abstract String getGlobalWelcomeText();

	public abstract int getLoopCount();

	public abstract void beforeLoopNumber(int loopNumber);

	public abstract void afterLoopNumber(int loopNumber);

	public abstract boolean quitLoop();

	public static String getNameFromSocket(Socket socket) {
		String name = socket.getRemoteSocketAddress().toString();
		name = name.split(":")[0];
		name = name.substring(1);
		return name;
	}


	public Communicator(Socket socket, PrintStream persistentDataStream) {
		server = socket;
		name = getNameFromSocket(socket);
		this.persistentDataStream = persistentDataStream ;
	}

	public static boolean isSocketAdminConsole(Socket socket) {
		String name = getNameFromSocket(socket);
		return name.equalsIgnoreCase(ADMIN_CLIENT);
	}

	@Override
	public void run() {


		try {
			// Get input from the client
			in = new DataInputStream(server.getInputStream());
			out = new PrintStream(server.getOutputStream());
			String welcomeMessage = getGlobalWelcomeText();
			out.println(welcomeMessage);
			int loopCount = getLoopCount();
			String line = "";
			boolean loop = true;
			for (int i = 0; i < loopCount && loop; i++) {
				beforeLoopNumber(i);
				out.print(PROMPT);
				while ((line = in.readLine()) != null && !quitLoop()) {
					if (line.equalsIgnoreCase("bye")) {
						loop = false;
						break;
					}
					String output = processInput(line);
					out.println(output);
					out.print(PROMPT);

				}
				afterLoopNumber(i);
			}
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
