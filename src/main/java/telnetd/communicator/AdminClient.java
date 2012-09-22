package telnetd.communicator;


import telnetd.TelnetServer;

import java.io.PrintStream;
import java.net.Socket;

public class AdminClient extends Communicator {

	public AdminClient(Socket client , PrintStream persistentDataStream ) {
		super(client,persistentDataStream);
	}

	@Override
	public String processInput(String input) {

		String sRet = "";
		if (input.equalsIgnoreCase("s")) {
			sRet = TelnetServer.getStats();
		} else if (input.startsWith("c")) {

			String[] words = input.split(" ");
			if (words.length <= 1) {
				sRet = "The command syntax is 'c <ip_address>'\r\n";
			} else {

				String ip = words[1].trim();
				boolean ret = TelnetServer.clearConnection(ip);

				if (ret) {
					sRet = ip + " Was Cleared";
				} else {
					sRet = ip + " Was NOT  Cleared!!! because not found.";
				}

			}
		}
		return sRet;
	}

	@Override
	public String getGlobalWelcomeText() {
		StringBuffer buf = new StringBuffer();
		buf.append("Welcome to the Admin Console...");
		buf.append("\r\n");
		buf.append("press  's' for status of the Clients.");
		buf.append("\r\n");
		buf.append("type  'c <ip>' for Clearing client with ip <ip>.");
		buf.append("\r\n");
		return buf.toString();
	}

	@Override
	public int getLoopCount() {
		return 1;
	}

	@Override
	public void beforeLoopNumber(int loopNumber) {

	}

	@Override
	public void afterLoopNumber(int loopNumber) {

	}

	@Override
	public boolean quitLoop() {
		return false;
	}
}
