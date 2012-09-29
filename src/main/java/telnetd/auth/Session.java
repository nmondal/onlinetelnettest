package telnetd.auth;


import telnetd.communicator.AdminClient;
import telnetd.communicator.Communicator;
import telnetd.communicator.QuestionCommunicator;

import java.io.PrintStream;
import java.net.Socket;

public class Session implements Runnable {

	public static final String ADMIN_NAME = "Administrator";

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
	public ClientType getClientType()
	{
		return clientType;
	}
	protected Socket socket;
	protected PrintStream persistentDataStream;

	protected Communicator communicator;

	protected Authentication authentication;
	protected String ipAddress;
	public String getIpAddress()
	{
		return ipAddress;
	}
	public String getName() {
		if ( authentication != null)
		{
			return String.format( "%s@%s", authentication.getUserName() , ipAddress);
		}
		else
		{
			return String.format( "unknown@%s" , ipAddress);
		}
	}
	protected synchronized ClientType login() {
		ClientType cType = ClientType.UnAuthorized;
		 try
		 {
			 socket.getOutputStream().write("Login:".getBytes() );
			 byte[] responseBytes = new byte[32];
			 socket.getInputStream().read(responseBytes);
			 String login = new String( responseBytes );
			 login = login.trim();
			 socket.getOutputStream().write("Password:".getBytes() );
			 socket.getInputStream().read(responseBytes);
			 String pass = new String( responseBytes );
			 pass = pass.trim();

			 authentication = Authentication.authenticationHashMap.get( login.trim() );
			 if ( authentication != null )
			 {
				 if ( authentication.getPassWord().equals( pass ) )
				 {
					 if ( authentication.getUserName().equalsIgnoreCase( ADMIN_NAME ))
					 {
						 cType = ClientType.Admin;
					 }
					 else
					 {
						 cType = ClientType.User ;
					 }
					 System.out.printf("%s Logged in.\r\n" , getName());
				 }
			 }

		 }catch (Exception e)
		 {
			 e.printStackTrace();
		 }

		return cType;
	}
	protected Thread runningThread;
	public Thread getThread()
	{
		return runningThread;
	}
	@Override
	public void run() {
		try {
			runningThread = Thread.currentThread();
			clientType = login();
			communicator = createCommunicator(clientType, socket, persistentDataStream);
			if (communicator != null) {
				communicator.setUserName(  authentication.getUserName() );
				communicator.run();

			} else {
				socket.getOutputStream().write("Login invalid\r\n".getBytes());
			}
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			System.out.printf("%s DONE \r\n", getName() );

		}
	}
	public Session ( Socket s, PrintStream printStream)
	{
		this.clientType = ClientType.Unknown ;
		this.socket = s;
		this.persistentDataStream = printStream ;
		this.ipAddress = getIdentityFromSocket( socket);
	}
}
