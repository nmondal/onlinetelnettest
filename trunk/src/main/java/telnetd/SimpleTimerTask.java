package telnetd;

import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author mondal
 */
public class SimpleTimerTask extends TimerTask {
	//times member represent calling times.

	public static long MAX_DIFF = 72000000 ; //As 20 mins.
			
	//public static long MAX_DIFF = 60000 ; //As 1 mins.
	
	private long startTime;
	private QuestionCommunicator com ;

	@Override
	public void run() {

				long l_cur = System.currentTimeMillis();
				if ( l_cur - startTime > MAX_DIFF )
				{
					com.SetTimerExpired();
					//Stop Timer.
					this.cancel();
				}
				else
				{
					
				}
				
	}
	public SimpleTimerTask(QuestionCommunicator com)
	{
		this.com = com;
	}
	
	public static void setTimerOnCommunication(QuestionCommunicator com)
	{
		SimpleTimerTask task = new SimpleTimerTask ( com );
		Timer timer = new Timer("Exam");
		task.startTime = System.currentTimeMillis();
		timer.scheduleAtFixedRate(task, 0, MAX_DIFF/1000);
	}
}
