package telnetd.auth;


import telnetd.communicator.AdminClient;
import telnetd.communicator.Communicator;
import telnetd.communicator.QuestionCommunicator;

import java.io.DataInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.Socket;

public class Session implements Runnable {

	public static final char IAC = (char) 0xff;
	public static final char WILL = (char) 0xfb;
	public static final char WONT = (char) 0xfc;
	public static final char ECHO = (char) 0x01;


	public static final String ADMIN_NAME = "Administrator";


	protected PrintStream out;
	protected DataInputStream in;

	public static enum ClientType {
		User,
		Admin,
		UnAuthorized,
		Unknown
	}

	public static String getIdentityFromSocket(Socket socket) {
		String name = socket.getRemoteSocketAddress().toString();
		name = name.substring(1);
		return name;
	}

	public static Communicator createCommunicator(ClientType clientType, Socket socket, PrintStream stream) {
		switch (clientType) {
			case User:
				return new QuestionCommunicator(socket, stream);
			case Admin:
				return new AdminClient(socket, stream);
			default:
				break;
		}
		return null;
	}

	protected ClientType clientType;

	public ClientType getClientType() {
		return clientType;
	}

	protected Socket socket;
	protected PrintStream persistentDataStream;

	protected Communicator communicator;

	protected Authentication authentication;
	protected String ipAddress;

	public String getIpAddress() {
		return ipAddress;
	}

	public String getName() {
		if (authentication != null) {
			return String.format("%s@%s", authentication.getUserName(), ipAddress);
		} else {
			return String.format("unknown@%s", ipAddress);
		}
	}

	protected void stopEcho() throws Exception {
		OutputStreamWriter pass_out = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");
		pass_out.write(IAC);
		pass_out.write(WILL);
		pass_out.write(ECHO);
		pass_out.flush();
		in.skipBytes(3);
	}

	protected void startEcho() throws Exception {
		OutputStreamWriter pass_out = new OutputStreamWriter(socket.getOutputStream(), "ISO-8859-1");
		pass_out.write(IAC);
		pass_out.write(WONT);
		pass_out.write(ECHO);
		pass_out.flush();
		in.skipBytes(3);
	}

	public String getPassWord() {

		String pass = "";
		try {
			stopEcho();
			out.print("Password:");

			pass = in.readLine();
			if (pass != null) {
				pass = pass.trim();
			}
			startEcho();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pass;
	}

	protected synchronized ClientType login() {
		ClientType cType = ClientType.UnAuthorized;
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new PrintStream(socket.getOutputStream());
			String login = "";
			while (true) {
				out.print("Login:");
				login = in.readLine();
				if (login != null) {
					login = login.trim();
					break;
				}
			}

			String pass = getPassWord();

			authentication = Authentication.authenticationHashMap.get(login.trim());
			if (authentication != null) {
				if (authentication.getPassWord().equals(pass)) {
					if (authentication.getUserName().equalsIgnoreCase(ADMIN_NAME)) {
						cType = ClientType.Admin;
					} else {
						cType = ClientType.User;
					}
					System.out.printf("%s Logged in.\r\n", getName());
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cType;
	}

	protected Thread runningThread;

	public Thread getThread() {
		return runningThread;
	}

	@Override
	public void run() {
		try {
			runningThread = Thread.currentThread();
			clientType = login();
			communicator = createCommunicator(clientType, socket, persistentDataStream);
			if (communicator != null) {
				communicator.setUserName(authentication.getUserName());
				communicator.run();

			} else {
				socket.getOutputStream().write("Login invalid\r\n".getBytes());
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.out.printf("%s DONE \r\n", getName());

		}
	}

	public Session(Socket s, PrintStream printStream) {
		this.clientType = ClientType.Unknown;
		this.socket = s;
		this.persistentDataStream = printStream;
		this.ipAddress = getIdentityFromSocket(socket);
	}
}
