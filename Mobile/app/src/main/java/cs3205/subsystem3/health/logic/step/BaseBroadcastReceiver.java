package cs3205.subsystem3.health.logic.step;

import android.content.BroadcastReceiver;

/**
 * Created by Yee on 09/30/17.
 */

public abstract class BaseBroadcastReceiver extends BroadcastReceiver {
    protected static String PREF_STEPS = "steps";
    protected static String CORRECT_SHUTDOWN = "correctShutdown";
    protected static String PAUSE_COUNT = "pauseCount";
    protected static String ACTION = "action";
}
