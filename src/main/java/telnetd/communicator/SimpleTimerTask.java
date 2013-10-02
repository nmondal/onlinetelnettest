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

	//This only takes in milli seconds,
	public static final long MAX_DIFF = SECTION_TIMEOUT_IN_MINUTES * 60 * 1000;


	private long startTime;
	private QuestionCommunicator com;

    public double minutesLeft(){
        long curTime = System.currentTimeMillis() ;
        double d =  (MAX_DIFF - curTime + startTime )/60000.0 ;
        return d;
    }

	@Override
	public void run() {

		long l_cur = System.currentTimeMillis();
		if (l_cur - startTime > MAX_DIFF) {
			com.SetTimerExpired(true);
			//Stop Timer.
			this.cancel();
		} else {
            com.timeLeft = minutesLeft();
		}

	}

	public SimpleTimerTask(QuestionCommunicator com) {
		this.com = com;
	}

	public static void setTimerOnCommunication(QuestionCommunicator com) {
        com.SetTimerExpired(false);
        SimpleTimerTask task = new SimpleTimerTask(com);
		Timer timer = new Timer("Exam");
        task.startTime = System.currentTimeMillis();
		timer.scheduleAtFixedRate(task, 0, MAX_DIFF / 1000);
        com.timeLeft = task.minutesLeft();
	}
}
