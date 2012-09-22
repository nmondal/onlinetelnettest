package telnetd;

/**
 * @author nmondal
 */
public class Main {

	static {
		try {
			Question.loadQuestions();
		} catch (Exception e) {

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
