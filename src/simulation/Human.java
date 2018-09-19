package simulation;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import processing.ControlPanel;
import processing.P5;
import processing.ProcessingDisplay;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.field.network.Network;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Double3D;
import sim.util.Int3D;
import sim.util.MutableDouble3D;
import simulation.Elevator.Car;
import simulation.Path.RegionPath;

public class Human implements Steppable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5660942785794067884L;
	
	public double metricMaxSpeed;
	public double metricAcceleration;
	public double discretization;
	public double maxSpeed;
	public double speed;
	public double acceleration;
	public double[] direction;
	//public MutableDouble3D position;
	public Stoppable stopper;
	public double radius = 1.5;
	
	public final static int WALKING = 0;
	public final static int ELEVATOR = 1;
	int movementMode;
	
	public final static int NONE = 0;
	public final static int SELECTELEVATOR = 1;
	public final static int WAITINGELEVATOR = 2;
	public final static int ENTERELEVATOR = 3;
	public final static int INSIDEELEVATOR = 4;
	public final static int EXITELEVATOR = 6;
	int elevatorMode;
	
	public final static int GENERALVISITOR = 0;
	public final static int STAFF = 1;
	public final static int PATIENT = 2;
	//private int type;
	
	private Itenerary itenerary;
	private Path currentPath;
	private RegionPath currentRegionPath;
	private String pRegionID;
	private String ppRegionID; //placeholder for copy of pRegionID
	private String pppRegionID;
	private Tile pTile;
	
	private Tile currentTargetTile;
	private Elevator currentElevator;
	
	boolean isWaiting = false;
	boolean finish;
	boolean isChangingLevel = false;
	int iteneraryCount; // 0 start, 1, lobby, 2, destLobby, 3 destination
	
	boolean isUsingElevator = false;
	String firstLobbyID = null;
	String secondLobbyID = null;
	
	UUID uuid;
	String id;
	
	Floor currentFloor;
	Floor previousFloor;
	
	SimState simulation;
	
	List<double[]> locationList = new ArrayList<double[]>();
	
	public static final int RANDOMAGENT = 0;
	public static final int TESTEDAGENT = 1;
	private int variableType = RANDOMAGENT;
	
	int simulationCounter = 0;
	int timerCounter = 0;
	
	PrintWriter writer;
	
	public Human(Tile tile, Itenerary itenerary){
		this.uuid = UUID.randomUUID();
		this.id = this.uuid.toString();
		Int3D location = tile.getLocation();
		this.currentFloor = tile.getRegion().getFloor();
		this.previousFloor = null;
		this.metricMaxSpeed = ProcessingDisplay.HUMANSPEED;//139
		this.metricAcceleration = 200;//100
		this.maxSpeed = (this.metricMaxSpeed/(double)Simulation.GRIDDIMENSION)/100;
		this.acceleration = (this.metricAcceleration/(double)Simulation.GRIDDIMENSION)/Math.pow((double)100, 2);
		this.speed = 0;
		this.direction = new double[3];
		Simulation.continuous3D.setObjectLocation(this, new Double3D(location.x, location.y, location.z));
		//this.itenerary = itenerary;
		setItenerary(itenerary);
		this.currentPath = null;
		this.currentRegionPath = null;
		this.currentTargetTile = null;
		this.finish = false;
		this.movementMode = Human.WALKING;
		this.elevatorMode = Human.NONE;
		this.iteneraryCount = 0;
		this.pRegionID = null;
		this.pTile = null;
		setCurrentPath();
	}
	
	public void setItenerary(Itenerary itenerary){
		this.itenerary = itenerary;
		this.isUsingElevator = !this.itenerary.isSingleFloor;
		if (this.isUsingElevator == true){
			this.firstLobbyID = this.itenerary.getItenerary(1).getID();
			this.secondLobbyID = this.itenerary.getItenerary(2).getID();
		} else {
			this.firstLobbyID = null;
			this.secondLobbyID = null;
		}
	}
	
	public void step(SimState state){
		this.simulation = state;
		this.timerCounter++;
		if (this.finish == true){
			resetHuman();
		}
		switch (this.movementMode){
		case Human.WALKING:
			onWalk();
			break;
		case Human.ELEVATOR:
			inElevator();
			break;
		}
		//
		//
		//recordLocation();
	}
	
	void recordLocation(){
		if (this.timerCounter % 100 == 0 ){
			Double3D location = this.getLocation();
			double[] loc = new double[] {location.x, location.y, location.z};
			this.locationList.add(loc);
		}
	}
	
	int floorDirection(){
		if (isUsingElevator == true){
			int level = this.itenerary.getItenerary(1).getLevel();
			int destLevel = this.itenerary.getItenerary(2).getLevel();
			if (level <  destLevel){
				return Car.UP;
			} else if (level > destLevel){
				return Car.DOWN;
			} else {
				return Car.IDLE;
			}
		} else {
			return Car.IDLE;
		}
	}
	
	public boolean isSkipElevator(){
		Car car = this.currentElevator.getCar();
		if (car.getDirection() != Car.IDLE){
			if (this.floorDirection() != this.currentElevator.getCar().getDirection()){
				return true;///////////////////////////////NEED ATTENTION
			} else {
				return false;
			}
		} else if (car.getDirection() == Car.IDLE){
			return false;
		}
		return false;
	}
	
	void onWalk(){
		if (finish != true){
			if (this.currentTargetTile != null){
				if (this.isUsingElevator == true && this.elevatorMode == Human.NONE){
					if (this.pTile.getID().equals(this.firstLobbyID)){
						setCurrentPath();
					}
				} else if (this.isUsingElevator == true && this.elevatorMode == Human.WAITINGELEVATOR){
					runToElevator();
				}
				stepWalk();
			}
			Int3D targetPos = this.currentTargetTile.getLocation();
			if (targetPos.equals(this.getLocationInt())){
				setCurrentTargetTile();
			}
		}
	}
	
	void inElevator(){
		if (this.currentElevator.getCar().isOpen() == false){
			useElevator();
		} else if (this.itenerary.getFinish().getLevel() == this.currentElevator.getCar().getCurrentLevel()){
			finishElevator();
		}
	}
	
	void stepWalk(){
		checkWait();
		if(this.isWaiting == false){
			this.accelerate();
			double speedX = this.speed * this.direction[1];
			double speedY = this.speed * this.direction[2];
			Double3D translation = new Double3D(speedX, speedY, 0);
			MutableDouble3D position = new MutableDouble3D(getLocation());
			position.addIn(translation);
			setLocation(position);	
		} else {
			this.deccelerate();
		}
	}
	
	void useElevator(){
		Double3D position = this.getLocation();
		double carElevation = this.currentElevator.getCar().getElevation();
		Double3D newPos = new Double3D(position.x, position.y, carElevation);
		this.setLocation(newPos);
	}
	
	void checkWait(){
		if (this.isUsingElevator == true && this.elevatorMode == Human.INSIDEELEVATOR){
			//checkWaitingExitQueue();
		}	
	}
	
	void checkWaitingExitQueue(){
		Car car = this.currentElevator.getCar();
		int level = this.getCurrentTile().getLevel();
		if (car.currentLevel == level ){
			if (car.isExitHoldListEmpty(level)){
				this.setWaiting(true);
			}
		} else {
			this.setWaiting(false);
		}
	}
	
	void defineDirection(){
		Int3D targetPosition = this.currentTargetTile.getLocation();
		Double3D position = getLocation();
		Double2D initPoint = new Double2D(position.x, position.y);
		Double2D targetPoint = new Double2D(targetPosition.x, targetPosition.y);
		double distance = initPoint.distance(targetPoint);
		double x = targetPoint.x - initPoint.x;
		double y = targetPoint.y - initPoint.y;
		double cos = x/distance;
		double sin = y/distance;
		double direction = 0;
		if (x < 0 && y == 0){
			direction = 0;
		} else if (x == 0 && y > 0){
			direction = Math.PI * 0.5;
		} else if (x < 0 && y == 0){
			direction = Math.PI;
		} else if (x == 0 && y < 0){
			direction = Math.PI * 1.5;
		} else {
			if (sin > 0){
				direction = Math.acos(cos);
			} else if (sin < 0){
				direction = 2 * Math.PI - Math.acos(cos);
			}
		}
		this.direction[0] = direction;
		this.direction[1] = cos;
		this.direction[2] = sin;
	}
	
	
	void setCurrentPath(){
		Path path = setPath();
		if (path != null){
			this.currentPath = path;
			this.ppRegionID = pRegionID;
			this.pRegionID = null;
			this.pTile = null;
			setCurrentRegionPath();
		} else {
			checkPointPath();
		}
	}
	
	void checkPointPath(){
		if (this.elevatorMode == Human.NONE){
			this.deccelerate();
			this.finish = true;
			this.iteneraryCount = 0;
		} else if (this.elevatorMode == Human.WAITINGELEVATOR){
		
		} else if (this.elevatorMode == Human.ENTERELEVATOR){
			
		} else if (this.elevatorMode == Human.INSIDEELEVATOR){
		
		}
	}
	
	void setCurrentRegionPath(){
		RegionPath regionPath = getNextRegionPath(this.pRegionID);
		if (regionPath != null){
			this.currentRegionPath = regionPath;
			this.pTile = currentRegionPath.getTile(0);
			setCurrentTargetTile();
		} else {
			setCurrentPath();
		}
	}
	
	void  setCurrentTargetTile(){
		Tile targetTile = getNextTargetTile(this.pTile.getLocation());
		if (targetTile != null){
			this.currentTargetTile = targetTile;
		} else {
			setCurrentRegionPath();
		}
		defineDirection();
	}
	
	Path setPath(){
		if (iteneraryCount == 2){
			confirmExitElevator();
		}
		Tile tileStart = this.itenerary.getItenerary(this.iteneraryCount);
		Tile tileFinish = this.itenerary.getItenerary(this.iteneraryCount + 1);
		if (tileFinish == null){
			return null;
		}
		if (tileStart.getRegion().getElevation() == tileFinish.getRegion().getElevation()){//one level path
			Network network = tileStart.getRegion().getFloor().getNetworkAll();
			Path path = new Path(tileStart, tileFinish, network);
			this.iteneraryCount++;
			return path;
		} else { //multilevelpath
			if (this.elevatorMode == Human.NONE){
				this.elevatorMode = Human.SELECTELEVATOR;
			}
			switch (this.elevatorMode){
			case Human.SELECTELEVATOR:
				return selectElevator();
			case Human.WAITINGELEVATOR:
				waitingElevator();
				return null;
			case Human.ENTERELEVATOR:
				return enterElevator();
			case Human.INSIDEELEVATOR:
				insideElevator();
				return null;
			case Human.EXITELEVATOR:
				return exitElevator();
			}
			return null;
		}
	}
	
	
	
	Path selectElevator(){
		Tile currentTile = this.getPreviousTargetTile();
		int currentLevel = currentTile.getLevel();
		Tile destTile = this.itenerary.getItenerary(this.iteneraryCount + 1);
		int destLevel = destTile.getLevel();
		Region currentRegion = currentTile.getRegion();
		Floor currentFloor = currentRegion.getFloor();
		String currentRegionID = currentTile.getID();
		ElevatorRegionGroup elvGroup = currentFloor.getAnyElevatorRegionGroup(currentRegionID);
		Elevator selectedElv = elvGroup.getFeasibleElevator(currentLevel, destLevel);
		Int3D queueLoc = selectedElv.getQueueLocation(currentRegion);
		Tile queueTile = currentRegion.getTile(queueLoc);
		Path path = new Path (currentTile, queueTile, currentFloor.getNetworkAll());
		selectedElv.getCar().queueCall(currentLevel, destLevel);
		this.currentElevator = selectedElv;
		this.elevatorMode = Human.WAITINGELEVATOR;
		this.currentElevator.getCar().registerEnterHoldList(currentLevel, this);
		this.currentElevator.getCar().registerExitHoldList(destLevel, this);
		return path;
	}
	
	void waitingElevator(){
		this.deccelerate();
		Car elvCar = this.currentElevator.getCar();
		int level = this.getCurrentTile().getLevel();
		double elevation = Simulation.floorLeveltoElevationMap.get(level);
		if (elvCar.getElevation() == elevation && elvCar.isOpen == true){ //door open set itenerary to elevator and walk to it
			if (elvCar.direction != Car.IDLE){
				if (this.floorDirection() == elvCar.direction){
					setEnterElevator();
				}
			} else if (elvCar.direction == Car.IDLE){
				setEnterElevator();
			}
		}
	}
	
	void setEnterElevator(){
		Car elvCar = this.currentElevator.getCar();
		Tile currentTile = this.getPreviousTargetTile();
		int currentLevel = currentTile.getLevel();
		Tile destTile = this.itenerary.getItenerary(this.iteneraryCount + 1);
		int destLevel = destTile.getLevel();
		if (elvCar.isFull() == false){
			int excessCapacity = elvCar.getExcessCapacity();
			int queueNum = elvCar.getEnterPassQueueNum(currentLevel);
			if (queueNum > excessCapacity){
				elvCar.setExitHoldListUnregistration(destLevel, this);
				elvCar.setEnterHoldListUnregistration(currentLevel,  this);
				//elvCar.unregisterEnterHoldList(currentLevel, this);
				//elvCar.unregisterExitHoldList(destLevel, this);
				this.elevatorMode = Human.SELECTELEVATOR;
			}else {
				this.elevatorMode = Human.ENTERELEVATOR;
			}
		} else {
			elvCar.setExitHoldListUnregistration(destLevel, this);
			elvCar.setEnterHoldListUnregistration(currentLevel, this);
			//elvCar.unregisterEnterHoldList(currentLevel, this);
			//elvCar.unregisterExitHoldList(destLevel, this);
			this.elevatorMode = Human.SELECTELEVATOR;
		}
	}
	
	void runToElevator(){
		Car elvCar = this.currentElevator.getCar();
		//Tile currentTile = this.getCurrentTile();
		Tile currentTile = this.getPreviousTargetTile();
		int level = currentTile.getLevel();
		double elevation = Simulation.floorLeveltoElevationMap.get(level);
		if (elvCar.getElevation() == elevation && elvCar.isOpen() == true){
			if (elvCar.direction != Car.IDLE){
				if (this.floorDirection() == elvCar.direction){
					setEnterElevator();
					setCurrentPath();
				}
			} else if (elvCar.direction == Car.IDLE){
				setEnterElevator();
				setCurrentPath();
			}
		}
	}
	
	
	Path enterElevator(){
		Tile currentTile = this.getCurrentTile();
		Floor currentFloor = currentTile.getRegion().getFloor();
		int currentLevel = currentTile.getLevel();
		Region selectedElvRegion = this.currentElevator.getRegion(currentLevel);
		Tile elevatorTile = selectedElvRegion.getCenterTile();
		Path path = new Path(currentTile, elevatorTile, currentFloor.getNetworkAll());
		this.elevatorMode = Human.INSIDEELEVATOR;
		return path;
	}
	
	void insideElevator(){
		Car car = this.currentElevator.getCar();
		car.setEnterHoldListUnregistration(this.getCurrentTile().getLevel(), this);
		if (car.isOpen() == false /*&& isNoHold == true*/){
			this.movementMode = Human.ELEVATOR;
		}
	}
	
	void finishElevator(){
		this.elevatorMode = Human.EXITELEVATOR;
		this.movementMode = Human.WALKING;
		setCurrentPath();
	}
	
	Path exitElevator(){
		Tile currentTile = this.getCurrentTile();
		Floor currentFloor = currentTile.getRegion().getFloor();
		int currentLevel = currentTile.getLevel();
		Region selectedElvRegion = this.currentElevator.getRegion(currentLevel);
		Tile elevatorTile = selectedElvRegion.getCenterTile();
		Path path = new Path(currentTile, this.itenerary.getItenerary(2), currentFloor.getNetworkAll());
		this.iteneraryCount++;
		this.elevatorMode = Human.NONE;
		//change current floor status
		this.previousFloor = this.currentFloor;
		this.currentFloor = currentTile.getRegion().getFloor();
		return path;
	}
	
	void confirmExitElevator(){
		Tile currentTile = this.getCurrentTile();
		int level = currentTile.getLevel();
		Car car = this.currentElevator.getCar();
		car.setExitHoldListUnregistration(level, this);
	}

	
	RegionPath getNextRegionPath(String pRegionID){

		RegionPath regionPath = this.currentPath.getNextRegionPath(pRegionID);
		if (regionPath != null){
			if (pRegionID !=null) {
					Region region = this.currentFloor.getRegion(pRegionID);
					region.decrementHuman();

			} else { //pRegion is null indicating a new path
				//System.out.println("this is the catch");
				if (this.ppRegionID != null){
				//	System.out.println("this is current floor");
					Region region = this.currentFloor.getRegion(this.ppRegionID);
					if (region == null){
				//		System.out.println("this is different floor");
						region = this.previousFloor.getRegion(this.ppRegionID);
					}
					this.ppRegionID = null;
					region.decrementHuman();
				} else {
				//	System.out.println("this is reset situation");
					if (pppRegionID != null){
						Region region = this.previousFloor.getRegion(this.pppRegionID);
						if (region != null) {
							region.decrementHuman();
						}
					}
				}
			}
			this.pRegionID = regionPath.getID();
			//System.out.println(this.getCurrentLevel());
			//System.out.println(this.pRegionID);
			Floor currentFloor = Simulation.getFloor(this.getCurrentLevel());
			//System.out.println(currentFloor.getRegion(this.pRegionID).getID());
			currentFloor.getRegion(this.pRegionID).incrementHuman();
			return regionPath;
		} else {
			return null;
		}
	}
	
	Tile getNextTargetTile(Int3D pTile){
		Tile tile = this.currentRegionPath.getNextTile(pTile);
		if (tile != null){
			this.pTile = tile;
			return tile;
		}
		return null;
	}
	
	
	
	void walkToTarget(){
		
	}
	
	
	void accelerate(){
		if (this.speed < this.maxSpeed){
			this.speed += this.acceleration;
		} else {
			this.speed = this.maxSpeed;
		}
	}
	
	void deccelerate(){
		this.speed = 0;
	}
	
	Region getCurrentRegion(){
		Int3D location = this.getLocationInt();
		Tile tile = getTileOnLocation(location);
		Region region = tile.getRegion();
		return region;
	}
	
	Tile getTileOnLocation(Int3D location){
		Bag objects = Simulation.environmentGrid3D.getObjectsAtLocation(location);
		if (objects!= null){
			for (Object object : objects){
				if (object.getClass().equals(Tile.class)){
					Tile tile = (Tile) object;
					return tile;
				}
			}
		}
		return null;
	}
	
	Tile getCurrentTile(){
		Int3D location = getLocationInt();
		Tile tile = getTileOnLocation(location);
		return tile;
	}
	
	Tile getPreviousTargetTile(){
		return this.pTile;
	}
	
	Int3D getLocationInt(){
		Double3D location = getLocation();
		int x = (int) Math.round(location.getX());
		int y = (int) Math.round(location.getY());
		int z = (int) Math.round(location.getZ());
		return new Int3D(x, y, z);
	}
	
	public void setWaiting(boolean isWaiting){
		this.isWaiting = isWaiting;
	}
	
	public boolean isWaiting(){
		return this.isWaiting;
	}
	
	public Double3D getLocation(){
		Double3D location = Simulation.continuous3D.getObjectLocation(this);
		return location;
	}
	
	public void setLocation(MutableDouble3D location){
		Simulation.continuous3D.setObjectLocation(this, new Double3D(location));
	}
	
	public void setLocation(Double3D location){
		Simulation.continuous3D.setObjectLocation(this, new Double3D(location));
	}
	
	public static Human createHuman(Tile tile, Itenerary itenerary){
		Human human = new Human(tile, itenerary);
		Simulation.humanList.add(human);
		return human;
	}
	
	public String getID(){
		return this.id;
	}
	
	public int getVariableType(){
		return this.variableType;
	}
	
	public void setVariableType(int variableType){
		this.variableType = variableType;
	}
	
	public Integer getCurrentLevel(){
		Tile tile = this.getCurrentTile();
		if (tile != null){
			int level = tile.getLevel();
			return level;
		} else {
			return null;
		}
	}
	
	void resetHuman(){
		//Simulation.locationRecords.add(this.locationList);
		//System.out.println(Simulation.locationRecords.size());
		this.locationList = new ArrayList<double[]>();//create new list;
		switch (this.variableType){
		case Human.TESTEDAGENT:
			resetAgent();
			break;
		case Human.RANDOMAGENT:
			resetRandomAgent();
			break;
		default:
			break;	
		}
	}
	
	void resetAgent(){
		checkPoint();
		this.pppRegionID = this.pRegionID;
		this.previousFloor = this.currentFloor;
		Tile firstTile = this.itenerary.getStart();
		Int3D location = firstTile.getLocation();
		Simulation.continuous3D.setObjectLocation(this, new Double3D(location.x, location.y, location.z));
		this.currentFloor = firstTile.getRegion().getFloor();
		this.currentPath = null;
		this.currentRegionPath = null;
		this.currentTargetTile = null;
		this.finish = false;
		this.iteneraryCount = 0;
		this.pRegionID = null;
		this.pTile = null;
		
		this.movementMode = Human.WALKING;
		this.elevatorMode = Human.NONE;
		setCurrentPath();
	}
	
	void checkPoint(){
		if(this.simulationCounter == 0){
			try {
				writer = new PrintWriter("output.txt", "UTF-8");
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (this.variableType == Human.TESTEDAGENT){
			//System.out.println("simulation no : " + simulationCounter);
			double time = (double) timerCounter / 100;
			System.out.println(time);
			writer.println(time);
			this.simulationCounter++;
		}
		this.timerCounter = 0;
		if (this.simulationCounter > ProcessingDisplay.SIMULATIONNUM){
			writer.close();
			Simulation simulation = (Simulation) this.simulation;
			ControlPanel ctrlPanel = Simulation.getApplet().getControlPanel();
			P5 p5 = ctrlPanel.p5Control;
			ProcessingDisplay.setRun(false);
			p5.getControlP5().getController("pause").setLabel("resume");
			writeLocationCSV();
		}
	}

	
	void writeLocationCSV(){
		final String DELIMITER = "#";
		final String NEWLINE = "\n";
		
		try {
			writer = new PrintWriter("location.csv", "UTF-8");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for (List<double[]> locations : Simulation.locationRecords){
			for (double[] location : locations){
				String locationStr = String.valueOf(location[0])+ "," + String.valueOf(location[1]) + "," + String.valueOf(location[2]);
				writer.print(locationStr);
				writer.print(DELIMITER);
			}
			writer.print(NEWLINE);
		}
		writer.close();
	}
	
	void resetRandomAgent(){
		Tile currentTile = this.getCurrentTile();
		Floor currentFloor = currentTile.getRegion().getFloor();

		Floor nextFloor = null;
		boolean isItenerary = false;
		Itenerary newItenerary = null;
				
		do{
			do{
				nextFloor = HumanGenerator.getRandomFloor();
			} while (currentFloor.getLevel() == nextFloor.getLevel());
			Tile nextTile = nextFloor.getRandomCenterTile(HumanGenerator.twister);
			newItenerary = new Itenerary();
			newItenerary.setStart(currentTile);
			newItenerary.setFinish(nextTile);
			isItenerary = newItenerary.calculateItenerary();	
			
		} while (isItenerary == false);
		this.setItenerary(newItenerary);

		/*boolean isItenerary = false;
		Itenerary newItenerary = null;
		do{
			Floor floorA = HumanGenerator.getRandomFloor();
			Floor floorB = null;
			do {
				floorB = HumanGenerator.getRandomFloor();
			} while (floorA.getLevel() == floorB.getLevel());
			
			Tile tileA = floorA.getRandomCenterTile(HumanGenerator.twister);
			Tile tileB = floorB.getRandomCenterTile(HumanGenerator.twister);
			newItenerary = new Itenerary();
			newItenerary.setStart(tileA);
			newItenerary.setFinish(tileB);
			isItenerary = newItenerary.calculateItenerary();
		} while(isItenerary == false);
		this.setItenerary(newItenerary);*/
		
		resetAgent();
	}
}
