package InterviewQuestions.ThreadExamples;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

class MyTimerTask extends TimerTask {
  public void run() {
    System.out.println("Timer task executed.");
  }
}

public class TimerTaskTest {
	 Timer timer;
	 Date d=new Date();
	 long fixedRate = 1000*10;
	 long nextScheduled; 
	  public TimerTaskTest(int seconds) {
	    timer = new Timer();
	    timer.schedule(new RemindTask(), seconds * 1000);
	    timer.scheduleAtFixedRate(new RemindTask() , d, fixedRate);
	   
	  }

	  class RemindTask extends TimerTask {
	    public void run() {
	    	 nextScheduled = new Date().getTime() + fixedRate; 
	      System.out.print("Time's up!:  Next Shcedule-" +nextScheduled);
	    //  timer.cancel(); //Terminate the timer thread
	    }
	  }

	  public static void main(String args[]) {
	    System.out.println("About to schedule task.");
	    new TimerTaskTest(5);
	    System.out.println("Task scheduled.");
	  }
}
      