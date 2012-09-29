package telnetd.auth;

import telnetd.PropertyHelper;

import java.util.HashMap;
import java.io.*;
import java.net.Socket;

/**
 *
 */
public class Authentication {

	public static final String AUTH_FILE =
			PropertyHelper.serverProperties.getPropertyDefault("AUTH_FILE","../users.txt" ) ;
	public static HashMap<String, Authentication>  authenticationHashMap;



	public String getUserName() {
		return userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public String getLoginName() {
		return loginName;
	}

	private String userName;
	private String passWord;
	private String loginName;

	protected  Authentication(String line)
	{
		String[] arr = line.split(":");
		loginName = arr[0].trim();
		passWord = arr[1].trim();
		userName = arr[2].trim();

	}

	public static void loadAuthMap()
	{
		authenticationHashMap = new HashMap<>();
		try
		{
			DataInputStream inputStream =
					new DataInputStream( new FileInputStream( AUTH_FILE ));
			String line ="";
			while(true)
			{
				line = inputStream.readLine();
				if ( line == null )
				{
					break;
				}
				Authentication authentication = new Authentication( line );
				authenticationHashMap.put(  authentication.loginName, authentication );
			}
		}catch (Exception e)
		{
			e.printStackTrace();
		}

	}
}
