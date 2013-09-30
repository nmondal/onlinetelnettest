package telnetd.communicator;


import telnetd.PropertyHelper;
import telnetd.auth.Authentication;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

public abstract class Communicator implements Runnable {

	public static final String PROMPT =  "\r>";
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

    public abstract double timeLeft();

	public String readResponse() throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		while ( true )
		{
			int v = in.read();
			char c = (char)v;
			if ( c == '\b' )
			{
				if ( buffer.length() > 0 )
				{
					buffer.deleteCharAt( buffer.length() - 1 );
					out.write(new byte[] { 0x08, 0x20, 0x08 });
				}
				continue;
			}
			else if ( c == '\r' || c == '\n')
			{
				out.println("\r");
				out.flush();

				in.skipBytes(1);
				break;
			}
			else if (  Character.isISOControl( c ) )
			{
				in.skipBytes(2);
				continue;
			}
			out.print(c);
			out.flush();

			buffer.append(c) ;
		}
		return buffer.toString() ;
	}


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
			out.printf("\r\nWelcome...%s\r\n", userName );
			String welcomeMessage = getGlobalWelcomeText();
			out.println(welcomeMessage);
			int loopCount = getLoopCount();
			String line = "";
			boolean loop = true;
			for (int i = 0; i < loopCount && loop; i++) {
				beforeLoopNumber(i);
				out.print(PROMPT);
				while (!quitLoop()) {

					line = readResponse();
					if (line == null) {
						break;
					}
					line = line.trim();

					if (line.endsWith("bye") || line.endsWith("BYE")) {
						loop = false;
						break;
					}
					String output = processInput(line);
					String toClient = String.format("\r%s [%.1f min left]\r", output ,timeLeft()) ;
                    out.println(toClient);
					out.print(PROMPT);

				}
				afterLoopNumber(i);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
