package simulation;

import sim.engine.SimState;
import sim.engine.Steppable;

public class RateAdjuster implements Steppable {
    private static final long serialVersionUID = 1;

    long initialTime;
    long totalTics;
    boolean started = false;
    double rate;
        
    public RateAdjuster(double targetRate)
        {
        rate = targetRate;
        }
        
    public void step(SimState state)
        {
        if (!started)
            {
            initialTime = System.currentTimeMillis();
            started = true;
            }
        else
            {
            long currentTime = System.currentTimeMillis();
            long time = currentTime - initialTime;
            totalTics++;
                        
            long expectedTime = (long)(totalTics / rate * 1000);
            if (time < expectedTime)  // too fast, need to slow down
                try
                    { 
                    Thread.currentThread().sleep(expectedTime - time); 
                    }
                catch (InterruptedException e) { } 
            else    // we lost time, but don't try to make up for it. We do this by resetting the clock
                {
                initialTime = currentTime;
                totalTics = 0;
                }
            }
        }
    }
