package simulation;
import sim.engine.*;
//import sim.util.*;


public class Time implements Steppable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9016107792103470355L;
	public int time = 0;
	public void step(SimState state){
		time++;
		//System.out.println(time);
	}
}
