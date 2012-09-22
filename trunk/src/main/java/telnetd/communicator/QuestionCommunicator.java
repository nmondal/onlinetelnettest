package telnetd.communicator;

import telnetd.PropertyHelper;
import telnetd.Question;

import java.io.*;
import java.net.Socket;

/**
 * @author nmondal
 */
public class QuestionCommunicator extends Communicator {



	private int cur_ans_no = -1;
	private char[] answers = null;
	private boolean timerExpired = false;
	private int currentSection = 0;


	private String formatResult() {
		StringBuffer ret = new StringBuffer( "Section " + currentSection );
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

	private void resultOutput(int section) {
		currentSection = section;
		persistData(  formatResult() );
	}

	public void SetTimerExpired() {
		timerExpired = true;
	}

	private void goPrevious() {
		if (cur_ans_no - 1 >= 0) {
			cur_ans_no--;
		} else {
			cur_ans_no = 0 ;
			out.println("Already at the first Question!");
		}
	}

	private void goNext() {
		if (cur_ans_no < answers.length - 1 ) {
			cur_ans_no++;
		} else {
			cur_ans_no =  answers.length - 1 ;
			out.println("Already at the Last Question!");
		}
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
		} else if (input.equalsIgnoreCase("l")) {
			return formatResult();
		} else if (input.equalsIgnoreCase("p")) {
			goPrevious();
		} else if (input.equalsIgnoreCase("n")) {
			goNext();
		} else {
			try {
				answers[cur_ans_no] = input.charAt(0);
			} catch (Exception e) {
			}
			goNext();
		}
		return Question.questionSections.get(this.currentSection).get(cur_ans_no).toString();

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

		return buf.toString();
	}

	@Override
	public int getLoopCount() {
		return Question.questionSections.size();
	}

	@Override
	public void beforeLoopNumber(int loopNumber) {
		currentSection = loopNumber;
		this.answers = new char[Question.questionSections.get(currentSection).size()];

		out.printf("Starting Section : %d\r\n", loopNumber + 1);
		out.printf("Total Questions : %d\r\n", answers.length);

		SimpleTimerTask.setTimerOnCommunication(this);
	}

	@Override
	public void afterLoopNumber(int loopNumber) {
		resultOutput(loopNumber + 1);
	}

	@Override
	public boolean quitLoop() {
		if (timerExpired) {
			out.print("Times Up For This Section!\r\n");
		}
		return timerExpired;
	}

	public QuestionCommunicator(Socket server, PrintStream persistentDataStream) {
		super(server , persistentDataStream );

	}

}
