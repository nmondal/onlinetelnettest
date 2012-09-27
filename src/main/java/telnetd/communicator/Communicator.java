package telnetd.communicator;


import telnetd.PropertyHelper;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;

public abstract class Communicator implements Runnable {

	public static final String ADMIN_CLIENT = "127.0.0.1";
	public static final String PROMPT = ">";
	protected Socket server;
	protected String name;

	protected PrintStream persistentDataStream;

	protected PrintStream out;
	protected DataInputStream in;
	protected Thread runningThread;

	public Thread getThread()
	{
		return runningThread;
	}

	public String getName() {
		return name;
	}

	public boolean isAdminConsole() {
		return isSocketAdminConsole(server);
	}

	protected synchronized void persistData(String data) {
		String outputData = String.format("#\r\n%s->%s\r\n%s#\r\n", name, PropertyHelper.getTimeStamp(), data);
		persistentDataStream.printf(outputData);
	}

	public abstract String processInput(String input);

	public abstract String getGlobalWelcomeText();

	public abstract int getLoopCount();

	public abstract void beforeLoopNumber(int loopNumber);

	public abstract void afterLoopNumber(int loopNumber);

	public abstract boolean quitLoop();

	public static String getNameFromSocket(Socket socket) {
		String name = socket.getRemoteSocketAddress().toString();
		name = name.substring(1);
		return name;
	}


	public Communicator(Socket socket, PrintStream persistentDataStream) {
		server = socket;
		name = getNameFromSocket(socket);
		this.persistentDataStream = persistentDataStream;
	}

	public static boolean isSocketAdminConsole(Socket socket) {
		String name = getNameFromSocket(socket);
		return name.equalsIgnoreCase(ADMIN_CLIENT);
	}

	@Override
	public void run() {


		try {
			runningThread = Thread.currentThread();
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
				while (!quitLoop()) {

					line = in.readLine();
					if (line == null) {
						break;
					}
					line = line.trim();

					if (line.endsWith("bye") || line.endsWith("BYE")) {
						loop = false;
						break;
					}
					String output = processInput(line);
					out.println(output);
					out.print(PROMPT);

				}
				afterLoopNumber(i);
			}
			System.out.printf("%s DONE\r\n", name);
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
