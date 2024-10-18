package soufix.utility;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import soufix.main.Config;


public class TimerWaiterPlus {

    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(Config.getInstance().numberOfThread());

    public static void addNext(Runnable run, long time, TimeUnit unit) {
        TimerWaiterPlus.scheduler.schedule(run, time, unit);
    }

    public static void addNext(Runnable run, long time) {
        TimerWaiterPlus.addNext(run, time, TimeUnit.MILLISECONDS);
    }

}