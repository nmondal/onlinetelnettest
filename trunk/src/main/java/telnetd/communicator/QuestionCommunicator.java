package telnetd.communicator;

import telnetd.PropertyHelper;
import telnetd.Question;

import java.io.PrintStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.ArrayList;


/**
 * @author nmondal
 */
public class QuestionCommunicator extends Communicator {

	public static final boolean SKIP_SECTION =
			PropertyHelper.serverProperties.getBooleanDefault("SKIP_SECTION", true);

	public static final boolean RANDOMIZE_QUESTIONS =
			PropertyHelper.serverProperties.getBooleanDefault("RANDOMIZE_QUESTIONS", false);

	static SecureRandom random = new SecureRandom();

	public static short[] shuffleQuestions(short size, boolean shuffle) {
		short[] arr = new short[size];
		if (shuffle) {
			ArrayList<Short> list = new ArrayList<>();

			for (short i = 0; i < size; i++) {
				list.add(i);
			}

			for (short i = 0; i < size; i++) {
				short inx = (short) random.nextInt(size - i);
				arr[i] = list.get(inx);
				list.remove(inx);
			}
		} else {
			for (short i = 0; i < size; i++) {
				arr[i] = i;
			}
		}
		return arr;
	}

	private int cur_ans_no = -1;
	private char[] answers = null;
	private short[] question_numbers = null;
	private boolean timerExpired = false;
	private int currentSection = 0;
	private boolean skipSection = false;

	private String formatResult(boolean plain) {
		StringBuffer ret = new StringBuffer();

        ret.append(currentSection);
		ret.append(":");
		if (plain) {
			for (int i = 0; i < answers.length; i++) {
				if (answers[i] != 0) {
					ret.append(" " + answers[i] + " ");
				} else {
					ret.append(" * ");
				}

			}
		} else {
			for ( int i = 0 ; i < question_numbers.length; i++ )
			{
				if ( answers[ question_numbers[i] ] != 0  )
				{
					ret.append(" " + answers[ question_numbers[i] ] + " ");
				} else {
					ret.append(" * ");
				}
			}
		}

		return ret.toString();
	}

	private void resultOutput(int section) {
		currentSection = section;
		persistData(formatResult(true));
	}

	public void SetTimerExpired() {
		timerExpired = true;
	}

	private void goPrevious() {
		if (cur_ans_no - 1 >= 0) {
			cur_ans_no--;
		} else {
			cur_ans_no = 0;
			out.println("Already at the first Question!");
		}
	}

	private void goNext() {
		if (cur_ans_no < answers.length - 1) {
			cur_ans_no++;
		} else {
			cur_ans_no = answers.length - 1;
			out.println("Already at the Last Question!");
		}
	}

    double timeLeft;
    @Override
    public double timeLeft(){
        return timeLeft;
    }

	@Override
	public String processInput(String input) {
		input = input.trim();
		boolean jump = false;

		if (input.isEmpty()) {
			return "";
		}

		try {
			cur_ans_no = Integer.parseInt(input);
			if (cur_ans_no >= 1 && cur_ans_no <= answers.length) {

				cur_ans_no--; //0 based index.
				jump = true;
			} else {
				return "Must provide a question no between 1 to " + answers.length;
			}
		} catch (Exception e) {
		}
		if (jump) {
		} else if (input.equalsIgnoreCase("ss")) {
			if (SKIP_SECTION) {
				skipSection = true;
				return "Skipping Section.\r\n";
			}
		} else if (input.equalsIgnoreCase("l")) {
			return formatResult(false);
		} else if (input.equalsIgnoreCase("p")) {
			goPrevious();
		} else if (input.equalsIgnoreCase("n")) {
			goNext();
		} else {
			try {
				answers[question_numbers[cur_ans_no]] = input.charAt(input.length() - 1);
				out.printf("Ans. to Question %d is set to : %c\r\n",
						cur_ans_no + 1, answers[question_numbers[cur_ans_no]]);
				goNext();
			} catch (Exception e) {
				out.println("Invalid Input.");
			}
		}
		if (cur_ans_no < 0 || cur_ans_no >= answers.length) {
			return "Please select a question no, or 'n' or 'p' \r\n";
		}
		return Question.questionSections.get(this.currentSection).get(question_numbers[cur_ans_no]).toString(cur_ans_no + 1);

	}

	@Override
	public String getGlobalWelcomeText() {


		StringBuffer buf = new StringBuffer();
		buf.append(String.format("Total %d Questions in %d Sections\r\n",
				Question.TOTAL_QUESTIONS, Question.TOTAL_SECTIONS));
		buf.append("\r\n");
		buf.append("press  'n' for next question.");
		buf.append("\r\n");
		buf.append("press  'p' for previous  question.");
		buf.append("\r\n");
		buf.append("type   '<question_no>' for going to any question.");
		buf.append("\r\n");
		buf.append("press  'l' to see your  answers.");
		buf.append("\r\n");
		buf.append("To Start the test press  'n' , for the first question.");
		buf.append("\r\n");
		buf.append(String.format("Timeout for Each Section is %d minute[s]",
				SimpleTimerTask.SECTION_TIMEOUT_IN_MINUTES));
		buf.append("\r\n");
		return buf.toString();
	}

	@Override
	public int getLoopCount() {
		return Question.questionSections.size();
	}

	@Override
	public void beforeLoopNumber(int loopNumber) {
		cur_ans_no = -1;
		skipSection = false;
		currentSection = loopNumber;
		this.answers = new char[Question.questionSections.get(currentSection).size()];
		this.question_numbers = shuffleQuestions((short) answers.length, RANDOMIZE_QUESTIONS);


		out.printf("\rStarting Section : %d\r\n", loopNumber + 1);
		out.printf("\rTotal Questions : %d\r\n", answers.length);

		SimpleTimerTask.setTimerOnCommunication(this);
	}

	@Override
	public void afterLoopNumber(int loopNumber) {
		resultOutput(loopNumber + 1);
	}

	@Override
	public boolean quitLoop() {
		if (timerExpired) {
			out.println("\rTimes Up For This Section!");
		}
		if (skipSection) {
			out.println("\rReally want to Skip Section? Fine.");
		}
		return (timerExpired || skipSection);
	}

	public QuestionCommunicator(Socket server, String fileLocation) throws Exception {
		super(server, fileLocation );
	}

}
