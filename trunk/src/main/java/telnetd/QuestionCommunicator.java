package telnetd;

import java.io.*;
import java.net.*;

/**
 *
 * @author nmondal
 */
class QuestionCommunicator implements Runnable {

	public static final String ADMIN_CLIENT = "127.0.0.1";
	private Socket server;
	private int cur_ans_no = 0;
	private char[] answers = new char[Question.questionsMap.keySet().size()];
	private PrintStream out;
	private DataInputStream in;
	private boolean timerExpired = false;
	private boolean isAdminConsole = false;
	private String name;

	public String formatResult() {
		StringBuffer ret = new StringBuffer(name);
		ret.append("\r\n");
		for (int i = 0; i < answers.length; i++) {
			ret.append(String.format("%2d ", i + 1));
		}
		ret.append("\r\n");
		for (int i = 0; i < answers.length; i++) {
			if (answers[i] != 0) {
				ret.append(" " + answers[i] + " ");
			} else {
				ret.append(" * ");
			}

		}
		ret.append("\r\n");
		return ret.toString();
	}

	public void resultOutput() {
		try {
			String fileName = String.format("%s.txt", name);

			File f = new File(fileName);
			if (f.exists()) {
				out.println("Not overwriting the file");

			} else {
				FileOutputStream fos = new FileOutputStream(fileName);
				OutputStreamWriter outFile = new OutputStreamWriter(fos, "UTF-8");
				outFile.write(formatResult());

				outFile.flush();
				outFile.close();
			}

		} catch (Exception e) {
		}
	}

	public void SetTimerExpired() {
		timerExpired = true;
	}

	private void goPrevious() {
		if (cur_ans_no - 1 > 1) {
			cur_ans_no--;
		} else {
			out.println("Already at the first Question!");
		}
	}

	private void goNext() {
		if (cur_ans_no < answers.length) {
			cur_ans_no++;
		} else {
			out.println("Already at the Last Question!");
		}
	}

	private String processInput(String input) {
		input = input.trim();
		boolean jump = false;

		if (input.isEmpty()) {
			return "";
		}

		if (isAdminConsole) {

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

		} else {
			try {
				cur_ans_no = Integer.parseInt(input);
				if (cur_ans_no >= 1 && cur_ans_no <= answers.length) {
					jump = true;
				} else {
					return "Must provide a question no between 1 to " + answers.length;
				}
			} catch (Exception e) {
			}
			if (jump) {
			} else if (input.equalsIgnoreCase("l")) {
				return formatResult();
			} else if (input.equalsIgnoreCase("p")) {
				goPrevious();
			} else if (input.equalsIgnoreCase("n")) {
				goNext();
			} else {
				try {
					answers[cur_ans_no - 1] = input.charAt(0);
				} catch (Exception e) {
				}
				goNext();
			}
			return Question.questionsMap.get(cur_ans_no).toString();
		}
	}

	QuestionCommunicator(Socket server, String name, boolean expired) {
		this.server = server;
		this.name = name;
		this.timerExpired = expired;
	}

	@Override
	public void run() {


		try {
			// Get input from the client
			in = new DataInputStream(server.getInputStream());
			out = new PrintStream(server.getOutputStream());
			String line = "";

			isAdminConsole = name.equalsIgnoreCase(ADMIN_CLIENT);

			if (timerExpired) {
				out.println("Sorry... you can not try again!");
				out.println("Contact Admin now, if you think there is a mistake.");
			}

			if (isAdminConsole) {

				out.println("Welcome to the Admin Console...");
				out.println("press  's' for status of the Clients.");
				out.println("type  'c <ip>' for Clearing client with ip <ip>.");

			} else {
				out.println("Total Questions :" + answers.length);
				out.println("press  'n' for next question.");
				out.println("press  'p' for previous  question.");
				out.println("type   '<question_no>' for going to any question.");
				out.println("press  'l' to see your  answers.");

				out.println("Start the test press  'n' , for the next question.");
			}
			boolean firstTime = true;
			while ((line = in.readLine()) != null && !line.equals("bye") && !timerExpired) {
				String output = processInput(line);
				if (firstTime) {
					if (!isAdminConsole) {
						SimpleTimerTask.setTimerOnCommunication(this);
					}
					firstTime = false;
				}
				out.print(output + "\r\n>");
				if (timerExpired) {
					out.print("Times Up! Sorry, and best of luck!\r\n");
					break;
				}
			}
			// Now write the result
			if (!isAdminConsole) {
				resultOutput();
			}
			server.close();
		} catch (IOException ioe) {
			System.out.println("IOException on socket listen: " + ioe);
			ioe.printStackTrace();
		}
	}
}
