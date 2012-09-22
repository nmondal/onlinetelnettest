package telnetd.communicator;

import telnetd.PropertyHelper;

import java.util.Timer;
import java.util.TimerTask;

/**
 * @author mondal
 */
public class SimpleTimerTask extends TimerTask {
	//times member represent calling times.

	public static long SECTION_TIMEOUT_IN_MINUTES =
			PropertyHelper.serverProperties.getLongDefault("SECTION_TIMEOUT_IN_MINUTES", 20);


	public static final long MAX_DIFF = SECTION_TIMEOUT_IN_MINUTES * 60 * 60 * 1000;


	private long startTime;
	private QuestionCommunicator com;

	@Override
	public void run() {

		long l_cur = System.currentTimeMillis();
		if (l_cur - startTime > MAX_DIFF) {
			com.SetTimerExpired();
			//Stop Timer.
			this.cancel();
		} else {

		}

	}

	public SimpleTimerTask(QuestionCommunicator com) {
		this.com = com;
	}

	public static void setTimerOnCommunication(QuestionCommunicator com) {
		SimpleTimerTask task = new SimpleTimerTask(com);
		Timer timer = new Timer("Exam");
		task.startTime = System.currentTimeMillis();
		timer.scheduleAtFixedRate(task, 0, MAX_DIFF / 1000);
	}
}
