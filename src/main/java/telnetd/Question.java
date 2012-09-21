package telnetd;

import java.io.*;
import java.util.HashMap;

/**
 *
 * @author nmondal
 */
public class Question {

	public int number;
	public String description;

	@Override
	public String toString() {
		String ret = String.format("[%d] \r\n %s\r\n", number, description);
		return ret;
	}
	public static HashMap<Integer, Question> questionsMap;

	public static void populateQuestionsMap(String fileName) throws Exception {
		questionsMap = new HashMap<>();
		int question_number = 0;
		String description = "";

		try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
			Question q = null;
			while (true) {
				String line = in.readLine();
				if ( line == null )
				{
					break;
				}
				String formattedLine = line.trim();
				if (formattedLine.startsWith("___") && formattedLine.endsWith("___")) {
					question_number++;
					//New question...
					q = new Question();
					q.number = question_number;
					q.description = description;
					description = "" ;
					questionsMap.put(q.number, q);
				} else {
					description += line + "\r\n";
				}
			}
			question_number++;
			q = new Question();
			q.number = question_number ;
			q.description = description ;
			questionsMap.put(q.number, q);
		}
	}
}
