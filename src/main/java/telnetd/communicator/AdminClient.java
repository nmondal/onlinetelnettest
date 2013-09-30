package telnetd.communicator;


import telnetd.PropertyHelper;
import telnetd.TelnetServer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class AdminClient extends Communicator {

	public AdminClient(Socket client , String fileLocation ) throws Exception {
		super(client, fileLocation);
	}


    // Hash by section number, and then by answers
    static HashMap<Integer,String[]> answers;

    static void loadAnswers() throws Exception {
        File file = new File(PropertyHelper.serverProperties.getPropertyDefault("ANSWERS_FILE", "../answers.txt"));
        /*
            The format is very simple:-
            section index :  ans 1  ans 2 ...

        */
        answers = new HashMap<>();
        BufferedReader br = new BufferedReader(new FileReader( file.getAbsoluteFile() ) );
        while(true){
            String line = br.readLine();
            if ( line == null ){
                break;
            }
            line = line.trim();
            String[] arr = line.split(":");
            int section = Integer.parseInt(arr[0].trim());
            arr = arr[1].trim().split(" +");
            answers.put(section,arr);

        }
        br.close();
    }

    static {
        try
        {
            loadAnswers();
        } catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    ArrayList<String> filterByUserName(String userName){
        ArrayList<String> responses = new ArrayList<>();
        try{
            BufferedReader reader = new BufferedReader( new FileReader( super.persistentFile ) );
            while(true){
                String line = reader.readLine();
                if ( line == null ){
                    break;
                }
                line = line.trim();
                if ( line.startsWith(userName +"#")){
                    responses.add( line );
                }
            }

        }catch (Exception e){

        }
        return responses ;
    }

    String findResult( String userName){
        ArrayList<String> responses = filterByUserName(userName);
        StringBuffer ret = new StringBuffer();
        int last = responses.size() - 1;
        for ( int i = last; i >= 0 ; i-- ){
            String response = responses.get(i);
            String[] arr = response.split("\\#") ;
            String time = arr[1];
            arr = arr[2].split(":");
            int section = Integer.parseInt( arr[0] );
            arr = arr[1].trim().split(" +");
            String[] knownAnswers = answers.get(section);
            ret.append( time + " Section " + section + " : " );
            int total = 0 ;
            for ( int j = 0 ; j < knownAnswers.length;j++ ){
                if ( knownAnswers[j].equalsIgnoreCase( arr[j] ) ){
                      total+= 2;
                }
                else{
                    total-= 1 ;
                }
            }
            ret.append(total +"\r\n");
        }

        return ret.toString();
    }

	@Override
	public String processInput(String input) {

		String sRet = "";
		if (input.equalsIgnoreCase("s")) {
			sRet = TelnetServer.getStats();
		} else if (input.startsWith("c")) {

			String[] words = input.split(" +");
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
		} else if ( input.startsWith("r")){
            String[] words = input.split(" ");
            if (words.length <= 1) {
                sRet = "The command syntax is 'r user_name'\r\n";
            } else {
                sRet = findResult(words[1]);
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
        buf.append("type  'r <user_name>' for Showing result for user_name.");
        buf.append("\r\n");
        buf.append("type  'r' for Showing result for All Users");
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

    @Override
    public double timeLeft() {
        return 0 ;
    }
}
