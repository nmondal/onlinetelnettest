package telnetd.communicator;


import telnetd.PropertyHelper;
import telnetd.auth.Authentication;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;

public abstract class Communicator implements Runnable {

	public static final String PROMPT = ">";
	protected Socket server;


	protected PrintStream persistentDataStream;

	protected PrintStream out;
	protected DataInputStream in;

	protected String userName;
	public void setUserName(String uName)
	{
		userName  = uName ;
	}


	protected synchronized void persistData(String data) {
		String outputData = String.format("#\r\n%s->%s\r\n%s#\r\n",
				userName, PropertyHelper.getTimeStamp(), data);
		persistentDataStream.printf(outputData);
	}

	public abstract String processInput(String input);

	public abstract String getGlobalWelcomeText();

	public abstract int getLoopCount();

	public abstract void beforeLoopNumber(int loopNumber);

	public abstract void afterLoopNumber(int loopNumber);

	public abstract boolean quitLoop();




	public Communicator(Socket socket, PrintStream persistentDataStream) {
		server = socket;
		this.persistentDataStream = persistentDataStream;
	}

	@Override
	public void run() {


		try {

			// Get input from the client
			in = new DataInputStream(server.getInputStream());
			out = new PrintStream(server.getOutputStream());
			out.printf("Welcome...%s\r\n", userName );
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
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
