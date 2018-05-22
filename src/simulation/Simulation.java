package simulation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//import java.util.UUID;

import java.util.UUID;

import processing.ProcessingDisplay;
import sim.engine.*;
import sim.field.continuous.Continuous3D;
//import sim.util.*;
import sim.field.grid.*;
//import sim.field.network.*;
import sim.field.network.Network;

public class Simulation extends SimState {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3429342452121186294L;
	public static int xLength = 800;
	public static int yLength = 800;
	public static int height = 800;
	public static int GRIDDIMENSION = 25;
	public static double TILESIZE = 1;
	public static double DIAGONALTILESIZE = 1.4142135623730950488016887242097;
	public static SparseGrid3D environmentGrid3D = new SparseGrid3D(xLength, yLength, height);
	public static Continuous3D continuous3D = new Continuous3D(1, xLength, yLength, height);
	public static String environmentXMLData = "data/data-25-valid.xml";
	//public static HashMap<String, Region> regionHash = new HashMap<>();
	//public static HashMap<String, Region> doorHash = new HashMap<>();
	
	public static List<Integer> floorElevationList = new ArrayList<Integer>();
	public static HashMap<String, Elevator> elevatorHash = new HashMap<>();
	public static HashMap<Integer, Network> elevatorNetworks = new HashMap<>();
	
	public static HashMap<String, Elevator> regionIdtoElevatorHash = new HashMap<>();
	public static HashMap<Integer, Floor> floorHash = new HashMap<>(); //elevation as key;
	public static HashMap<Integer, Integer> floorElevationtoLevelMap = new HashMap<>(); // elevation in cm --> level
	public static HashMap<Integer, Integer> floorLeveltoElevationMap = new HashMap<>(); // level to elevation in cm
	public static List<Human> humanList = new ArrayList<Human>();
	public static List<List<double[]>> locationRecords = new ArrayList<List<double[]>>();
	
	public static final int SIMULATIONSPEED = ProcessingDisplay.SIMULATIONSPEED;
	
	public static Itenerary agentPath;
	private static boolean isReady = false;
	
	public static boolean isElevatorRandom = true;
	
	private static ProcessingDisplay applet;
	
	public Simulation(long seed, ProcessingDisplay applet){
		super(seed);
		this.applet = applet;
	}
	
	
	
	public void start(){
		//clear elevator Hash
		elevatorHash.clear();
		//clear the environmentGrid
		environmentGrid3D.clear();
		
				
		//setup the region;
		Environment.setup();
		setReady(true);
		//timedummy
		Time time = new Time();
		RateAdjuster rate = new RateAdjuster(SIMULATIONSPEED);
		schedule.scheduleRepeating(time);
		schedule.scheduleRepeating(rate);
		scheduleElevators();
		scheduleAgents();
	}
	
	public void scheduleElevators(){
		for (Elevator elevator : Simulation.elevatorHash.values()){
			schedule.scheduleRepeating(elevator.getCar());
		}
	}
	
	public void scheduleAgents(){
		for (Human human : Simulation.humanList){
			schedule.scheduleRepeating(human);
		}
	}
	
	
	public static void main(String[] args){
		doLoop(Simulation.class, args);
		System.exit(0);
	}
	
	/// another methods
	
	public static void addFloor(Floor floor){
		int elevation = floor.getElevation();
		int level = floor.getLevel();
		Simulation.floorHash.put(new Integer(elevation), floor);
		Simulation.floorLeveltoElevationMap.put(new Integer(level), new Integer(elevation));
		Simulation.floorElevationtoLevelMap.put(new Integer(elevation),new Integer(level));
		Simulation.floorElevationList.add(elevation);
	}

	public static boolean isReady() {
		return isReady;
	}

	public static void setReady(boolean isReady) {
		Simulation.isReady = isReady;
	}
	
	public static int getFloorIndex(int level){
		int elevation = Simulation.floorLeveltoElevationMap.get(level);
		int index = Simulation.floorElevationList.indexOf(elevation);
		return index;
	}
	
	public static ProcessingDisplay getApplet(){
		return Simulation.applet;
	}
}
