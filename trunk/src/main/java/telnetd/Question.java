package telnetd;

import java.io.*;
import java.util.ArrayList;

/**
 * @author nmondal
 */
public class Question {

	public static int TOTAL_QUESTIONS = 0;
	public static int TOTAL_SECTIONS = 0;

	public int number;
	public String description;

	@Override
	public String toString() {
		String ret = String.format("[%d] \r\n %s\r\n", number, description);
		return ret;
	}
	public String toString(int no) {
		String ret = String.format("[%d] \r\n %s\r\n", no , description);
		return ret;
	}
	public static ArrayList<ArrayList<Question>> questionSections;

	public static void loadQuestions() {
		try {
			questionSections = new ArrayList<>();
			File dir = new File(PropertyHelper.serverProperties.getPropertyDefault("QUESTION_REPO", "../questions"));
			System.out.printf("Loading Questions from Directory : %s\r\n", dir.getCanonicalPath());

			String[] files = dir.list();
			for (String file : files) {
				if (file.toLowerCase().contains("question")) {
					TOTAL_SECTIONS++;
					String fileName = dir.getCanonicalPath() + "/" + file;
					System.out.printf("Trying to get Question from file : %s\r\n", fileName);
					ArrayList<Question> questions = getQuestions(fileName);
					questionSections.add(questions);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static ArrayList<Question> getQuestions(String fileName) {
		ArrayList<Question> questions = new ArrayList<>();
		int question_number = 0;
		String description = "";

		try (BufferedReader in = new BufferedReader(new FileReader(fileName))) {
			Question q = null;
			while (true) {
				String line = in.readLine();
				if (line == null) {
					break;
				}
				String formattedLine = line.trim();
				if (formattedLine.startsWith("___") && formattedLine.endsWith("___")) {
					question_number++;
					//New question...
					q = new Question();
					q.number = question_number;
					q.description = description;
					description = "";
					questions.add(q);
					TOTAL_QUESTIONS++;
				} else {
					description += line + "\r\n";
				}
			}
			question_number++;
			q = new Question();
			q.number = question_number;
			q.description = description;
			questions.add(q);
			TOTAL_QUESTIONS++;
		} catch (FileNotFoundException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		} catch (IOException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
		System.out.printf("Done getting Question from file : %s\r\n", fileName);
		System.out.printf("%d questions found.\r\n", questions.size());

		return questions;
	}
}
