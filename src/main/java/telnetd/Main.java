package telnetd;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author nmondal
 */
public class Main {

	public static enum LogOptions
	{
		STDOUT,
		LOG,
		TEMPORAL
	}

	static void redirectServerLog() throws IOException
	{
		LogOptions logValue =
				(LogOptions) PropertyHelper.serverProperties.getEnumDefault("SERVER_LOG",
						LogOptions.STDOUT);
		switch ( logValue )
		{
			case LOG:
				System.setOut( new  PrintStream(new FileOutputStream( "Server.log"), true ) );
				break;
			case TEMPORAL:
				System.setOut( new  PrintStream(new FileOutputStream( PropertyHelper.getTimeStamp()+".log" ), true ) );
				break;
			default:
				break;
		}

	}

	static {
		try {
			redirectServerLog();
			Question.loadQuestions();
		} catch (Exception e) {
			  e.printStackTrace();

		}
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		TelnetServer server = new TelnetServer();
		server.runServer();
	}
}
