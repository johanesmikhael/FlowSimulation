package simulation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



import java.util.PriorityQueue;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double3D;
import sim.util.Int3D;

public class Elevator {
	private int elevatorIndex;
	private String elevatorName;
	private String elevatorId;
	private List<Integer> levelList; 
	private List<String> regionIDList;
	private List<Region> regionList;
	private HashMap<String, Region> regionHash;
	private HashMap<Integer, Region> levelToRegionHash;
	private HashMap<Integer, ElevatorRegionGroup> regionToService;
	private int type;
	private int accessibility;
	private int capacity;
	
	//accessibility
	public static final int STAFFONLY = Region.STAFFONLY;
	public static final int STAFFANDPATIENT = Region.STAFFANDPATIENT;
	public static final int GENERAL = Region.GENERAL;
	
	//type
	public static final int GENERALELEVATOR = 0;
	public static final int BEDELEVATOR = 1;
	
	
	
	private Car car;
	
	
	public Elevator(String elvName, String elvId, int elvIndex, int type, int accessibility, int capacity){
		this.elevatorIndex = elvIndex;
		this.elevatorName = elvName;
		this.elevatorId = elvId;
		this.type = type;
		this.accessibility = accessibility;
		this.levelList = new ArrayList<Integer>();
		this.regionIDList = new ArrayList<String>();
		this.regionHash = new HashMap<>();
		this.levelToRegionHash = new HashMap<>();
		this.regionToService = new HashMap<>();
		this.regionList = new ArrayList<Region>();
		this.capacity = capacity;
		this.car = new Car(this, 0, this.capacity );
	}
	
	public String getName(){
		return this.elevatorName;
	}
	
	public String getID(){
		return this.elevatorId;
	}
	
	public List<Floor> getFloors(){
		List<Floor> floors = new ArrayList<Floor>();
		for (int level : this.levelList){
			int elevation = Simulation.floorLeveltoElevationMap.get(level);
			Floor floor = Simulation.floorHash.get(elevation);
			floors.add(floor);
		}
		return floors;
	}
	
	public int getIndex(){
		return this.elevatorIndex;
	}
	
	public void setType(int type){
		this.type = type;
	}
	
	public int getType(){
		return this.type;
	}
	
	public void setAccessibility(int accessibility){
		this.accessibility = accessibility;
	}
	
	public int getAccessibility(){
		return this.accessibility;
	}
	
	public Int3D getQueueLocation(Region lobby){
		int level = lobby.getLevel();
		Region elvRegion = this.levelToRegionHash.get(level);
		Collection<RegionNode> nodes = elvRegion.getNodes();
		for (RegionNode node : nodes){
			if (node.getID().equals(lobby.getID())){
				Int3D center = node.getCenter().getLocation();
				int centerIndex = node.getCenterIndex();
				Int3D side1 = node.getTile(centerIndex + 2).getLocation();
				int x = side1.x - center.x;
				int y = side1.y - center.y;
				int x1 = center.x - y;
				int y1 = center.y + x;
				int x2 = center.x + y;
				int y2 = center.x - x;
				Int3D p1 = new Int3D(x1, y1, center.z);
				Int3D p2 = new Int3D(x2, y2, center.z);
				if (lobby.isTile(x1, y1)){
					return p1;
				} else if (lobby.isTile(x2, y2)){
					return p2;
				}
			}
		}
		return null;
	}
	
	public Car getCar(){
		return this.car;
	}
	
	public int getFloorNum(){
		return this.levelList.size();
	}
	
	
	public void addRegion(Region elvRegion){
		String id = elvRegion.getID();
		int level = elvRegion.getLevel();
		if (!regionHash.containsKey(id)){
			this.regionHash.put(id, elvRegion);
			this.levelToRegionHash.put(new Integer(level), elvRegion);
			this.regionList.add(elvRegion);
			this.regionIDList.add(id);
			this.levelList.add(elvRegion.getLevel());
		}		
	}
	
	public Collection<Region> getRegions(){
		return this.regionList;
	}
	
	public Region getRegion(int level){
		Region region = null;
		int i = 0;
		for(int lvl : this.levelList){
			if(lvl == level){
				region = this.regionList.get(i);
			}
		i++;
		}
		return region;
	}
	
	public String getRegionID(int level){
		Region region = getRegion(level);
		return region.getID();
	}
	
	public void randomize(){
		
	}
	
	public void setPosition(){
		this.getCar().setPosition();
	}
	
	public void setRandomPosition(MersenneTwisterFast twister){
		this.getCar().setRandomPosition(twister);
	}
	
	public int getFeasibility(int startLevel, int destLevel){
		int fs = 0;
		int lvlSubstract = destLevel - startLevel;
		int callDirection = 0;
		//int n = this.getFloorNum() - 1;
		int n = Simulation.floorElevationList.size() - 1;
		if (lvlSubstract > 0){
			callDirection = Car.UP;
		} else if (lvlSubstract < 0){
			callDirection = Car.DOWN;
		}
		double startElevation = Simulation.floorLeveltoElevationMap.get(startLevel);
		double lvlDoubleDistance = startElevation - car.getElevation();
		int lvlDistance = startLevel - car.getCurrentLevel();//car position is below if positive
		boolean isToward = false;
		boolean isSameDirection = false;
		if (this.getCar().getDirection() == Car.IDLE){
			if(callDirection == Car.UP){
				if (lvlDoubleDistance >= 0){
					isToward = true;
					isSameDirection = true;
				} else {
					isToward = true;
					isSameDirection = false;
				}
			} else if (callDirection == Car.DOWN){
				if (lvlDoubleDistance <= 0){
					isToward = true;
					isSameDirection = true;
				} else {
					isToward = true;
					isSameDirection = false;
				}
			}
		} else if (this.getCar().getDirection() == Car.UP){
			if (lvlDoubleDistance >= 0){
				isToward = true;
				if (callDirection == Car.UP){
					isSameDirection = true;
				} else {
					isSameDirection = false;
				}
			} else {
				isToward = false;
			}
		} else if (this.getCar().getDirection() == Car.DOWN){
			if (lvlDoubleDistance <= 0){
				isToward = true;
				if (callDirection == Car.DOWN){
					isSameDirection = true;
				} else {
					isSameDirection = false;
				}
			} else {
				isToward = false;
			}
		}
		if (isToward == true && isSameDirection == true){
			fs = (n + 2) - Math.abs(lvlDistance) + this.getCar().getExcessCapacity();
		} else if (isToward == true  && isSameDirection == false){
			fs = (n + 1) - Math.abs(lvlDistance) + this.getCar().getExcessCapacity();
		} else if (isToward == false){
			fs = 1 + this.getCar().getExcessCapacity();
		}
		if (this.getCar().closeDoorSwitch == true && this.getCar().currentLevel == startLevel){ ////experimental
			fs = 0;
		}
		return fs;
	}
	
	public void addRegionGroup(ElevatorRegionGroup elvRegionGroup){
		 int level = elvRegionGroup.getLevel();
		 this.regionToService.put(new Integer(level), elvRegionGroup);
	}
	
	public ElevatorRegionGroup getElevatorRegionGroup(int level){
		ElevatorRegionGroup elvRegionGroup = this.regionToService.get(level);
		if (elvRegionGroup == null){
			return null;
		} else {
			return elvRegionGroup;
		}
	}
	
	
	public class Car implements Steppable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 792564649625265002L;
		Elevator elevator;
		boolean isOpen;
		boolean isRun;
		boolean isFull;
		boolean isHold;
		boolean isOnTargetFloor;
		double elevation;
		double pElevation = 0;
		int height;
		int initLevel;
		int currentLevel;
		int maxCapacity;
		int passangerNum;
		int direction;
		int movementDirection;
		double maxSpeed;
		boolean openDoorSwitch = false;
		boolean closeDoorSwitch = false;
		boolean runSwitch = false;
		int doorWaitingTime = 200;
		int doorCounter = 0;
		int moveWaitingTime = 200;
		int moveCounter = 0;
		
		public static final int UP = 1;
		public static final int IDLE = 0;
		public static final int DOWN = -1;
		
		public int doorOpenCounter = 0;
		
		int maxDoorWaitingTime = 500;
		
		HashMap<Integer, HashMap<String, Human>> enterHoldList = new HashMap<>();
		HashMap<Integer, HashMap<String, Human>> exitHoldList = new HashMap<>();
		HashMap<Integer, List<Human>> enterHoldOrderedList = new HashMap<>();
		HashMap<Integer, List<Human>> exitHoldOrderedList = new HashMap<>();
		
		PriorityQueue<Integer> tempUpQueue = new PriorityQueue<Integer>();
		PriorityQueue<Integer> tempDownQueue = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
			public int compare(Integer intA, Integer intB){
				if (intA > intB){
					return -1;
				}
				if (intA < intB){
					return 1;
				}
				return 0;
			}
		});
		PriorityQueue<Integer> upQueue = new PriorityQueue<Integer>();
		PriorityQueue<Integer> downQueue = new PriorityQueue<Integer>(10, new Comparator<Integer>(){
			public int compare(Integer intA, Integer intB){
				if (intA > intB){
					return -1;
				}
				if (intA < intB){
					return 1;
				}
				return 0;
			}
		});
		private Iterator<Integer> itr;
		
		private List<int[]> callList = new ArrayList<int[]>();
		
		private List<Human> humanEnterUnregistrationList = new ArrayList<Human>();
		private HashMap<String, Integer> enterUnregistrationLevelMap = new HashMap<>();
		private List<Human> humanExitUnregistrationList = new ArrayList<Human>();
		private HashMap<String, Integer> exitUnregisterationLevelMap = new HashMap<>();
		
		public Car(Elevator elevator, int initLevel, int capacity){
			this.elevator = elevator;
			this.initLevel = initLevel;
			this.currentLevel = initLevel;
			this.maxCapacity = capacity;
			this.height = 10;
			this.direction = IDLE;
			this.movementDirection = IDLE;
			this.isOpen = false;
			this.isRun = false;
			this.isHold = false;
			this.isOnTargetFloor = false;
			this.passangerNum = 0;
			this.maxSpeed = 0.064; //(160cm/s)= .064 unit/centisecond (1 unit = 25 cm)
			this.setFull(false);
		}
		
		void setupQueueHashMap(){
			for (int level : this.elevator.levelList){
				HashMap<String, Human> levelEnterQueue = new HashMap<>();
				List<Human> levelEnterOrderedList = new ArrayList<Human>();
				this.enterHoldList.put(new Integer(level), levelEnterQueue);
				this.enterHoldOrderedList.put(new Integer(level), levelEnterOrderedList);
				HashMap<String, Human> levelExitQueue = new HashMap<>();
				List<Human> levelExitOrderedList = new ArrayList<Human>();
				this.exitHoldList.put(new Integer(level), levelExitQueue);
				this.exitHoldOrderedList.put(new Integer(level), levelExitOrderedList);
			}
		}
		
		public void step(SimState arg0) {
			doorControl();
			moveControl();
			switch(this.movementDirection){
			case UP:
				if (this.isRun() == true){
					move(this.maxSpeed);
				}
				break;
			case DOWN:
				if (this.isRun() == true){
					move(-this.maxSpeed);
				}
				break;
			case IDLE:
				break;
			}
			updateLevel();
			checkLevelQueue();
			callCheck();
			processEnterHoldListUnregistration();
			processExitHoldListUnregistration();
		}
		
		void callCheck(){
			for (int[] levelCall : this.callList){
				this.call(levelCall[0], levelCall[1]);
			}
			this.callList.clear();
		}
		
				
		void move(double distance){
			double newElevation = this.elevation + distance;
			this.pElevation = this.elevation;
			this.setElevation(newElevation);		
		}
		
		void updateLevel(){
			if (this.movementDirection == IDLE){
				return;
			} else if (this.movementDirection == UP){
				if (this.currentLevel != this.elevator.levelList.get(levelList.size()-1)){
					double currentElevation = Simulation.floorLeveltoElevationMap.get(this.currentLevel);
					if (this.elevation > currentElevation){
						this.currentLevel++;
					}
				} else {
					return;//must change direction
				}
			} else if (this.movementDirection == DOWN){
				if (this.currentLevel != this.elevator.levelList.get(0)){
					double currentElevation = Simulation.floorLeveltoElevationMap.get(this.currentLevel);
					if (this.elevation < currentElevation){
						this.currentLevel--;
					}
				} else {
					return;//must change direction
				}
			}
		}
		
		void checkLevelQueue(){
			switch(this.direction){
			case UP:
				checkUpQueue();
				break;
			case DOWN:
				checkDownQueue();
				break;
			case IDLE:
				checkBothQueue();
			}
		}
		
		void checkUpQueue(){
			if (this.upQueue.size() != 0){
				int destLevel = this.upQueue.peek();
				if (destLevel > this.currentLevel){
					this.setMovementDirection(UP);
				} else if (destLevel < this.currentLevel){
					this.setMovementDirection(DOWN);
				}
				if (this.currentLevel == destLevel){
					double targetElevation = Simulation.floorLeveltoElevationMap.get(destLevel);
					if (this.movementDirection == UP){
						if (this.elevation >= targetElevation){
							this.elevation = targetElevation;
							this.upQueue.poll();
							stopAndOpenCar(destLevel);
						}
					} else if (this.movementDirection == DOWN){
						if (this.elevation <= targetElevation){
							this.elevation = targetElevation;
							this.upQueue.poll();
							stopAndOpenCar(destLevel);
						}
					} else if (this.movementDirection == IDLE){
						if (this.elevation == targetElevation){
							this.upQueue.poll();
							stopAndOpenCar(destLevel);
						}
					}
				}
			} else {//up queue list empty
				if (!this.tempDownQueue.isEmpty() || !this.downQueue.isEmpty()){
					//there is queue on opposite direction
					this.downQueue.addAll(this.tempDownQueue);
					this.tempDownQueue.clear();
					setDirection(DOWN);
					checkDownQueue();
				} else if (!this.tempUpQueue.isEmpty()){
					//there is temporary queue in same direction;
					this.upQueue.addAll(this.tempUpQueue);
					this.tempUpQueue.clear();
					setDirection(UP);
					checkUpQueue();
				} else {
					this.setDirection(IDLE);
					this.setMovementDirection(IDLE);
				}
			}
		}
		
		void checkDownQueue(){
			if (this.downQueue.size() != 0){
				int destLevel = this.downQueue.peek();
				if (destLevel > this.currentLevel){
					this.setMovementDirection(UP);
				} else if (destLevel < this.currentLevel){
					this.setMovementDirection(DOWN);
				}
				if (this.currentLevel == destLevel){
					double targetElevation = Simulation.floorLeveltoElevationMap.get(destLevel);
					if (this.movementDirection == DOWN){
						if (this.elevation <= targetElevation){
							this.elevation = targetElevation;
							this.downQueue.poll();
							stopAndOpenCar(destLevel);
						}
					} else if (this.movementDirection == UP){
						if (this.elevation >= targetElevation){
							this.elevation = targetElevation;
							this.downQueue.poll();
							stopAndOpenCar(destLevel);
						}
					} else if (this.movementDirection == IDLE){
						if (this.elevation == targetElevation){
							this.downQueue.poll();
							stopAndOpenCar(destLevel);
						}
					}				
				}
			} else {
				if (!this.tempUpQueue.isEmpty() || !this.upQueue.isEmpty()){
					this.upQueue.addAll(this.tempUpQueue);
					this.tempUpQueue.clear();
					this.setDirection(UP);
					checkUpQueue();
				} else if (!this.tempDownQueue.isEmpty()){
					this.downQueue.addAll(this.tempDownQueue);
					this.tempDownQueue.clear();
					this.setDirection(DOWN);
					checkDownQueue();
				} else {
					this.setDirection(IDLE);
					this.setMovementDirection(IDLE);
				}
			}
		}
		
		void checkBothQueue(){
			if (this.upQueue.size() != 0){
				upQueue.peek();
				this.setDirection(UP);
				checkUpQueue();
			} else if (this.downQueue.size()!= 0){
				downQueue.peek();
				this.setDirection(DOWN);
				checkDownQueue();
			} else if (this.tempUpQueue.size() != 0){
				this.setDirection(UP);
				checkUpQueue();
			} else if (this.tempDownQueue.size() != 0){
				this.setDirection(DOWN);
				checkDownQueue();
			}
			
		}
		
		void stopAndOpenCar(int level){
			this.setRun(false);
			setSwitchOpen(true);
			double elevation = Simulation.floorLeveltoElevationMap.get(level);
			this.setElevation(elevation);
		}
		
		void stopAndIdleCar(){
			this.setRun(false);
			this.setDirection(IDLE);
		}
		
		public void queueCall(int level, int destLevel){
			int[] levelList = new int[] {level, destLevel};
			this.callList.add(levelList);
		}
		
		public void call(int level, int destLevel){
			if (this.direction == IDLE){
				if (destLevel - level > 0){//UP call
					addToUpQueue(level);
					addToUpQueue(destLevel);
					this.setDirection(UP);
				} else if (destLevel - level < 0){//down call
					addToDownQueue(level);
					addToDownQueue(destLevel);
					this.setDirection(DOWN);
				}
				setDirectionToLevel(level);
			} else if (this.direction == UP){
				if(this.movementDirection == UP){
					if (destLevel - level > 0){// UP call
						if (this.getElevation() < Simulation.floorLeveltoElevationMap.get(level)){//on beaten path
							addToUpQueue(level);
							addToUpQueue(destLevel);
						} else if (this.getElevation() == Simulation.floorLeveltoElevationMap.get(level) && this.isOpen() == true){ //on same level and not run
							//addToUpQueue(level);
							addToUpQueue(destLevel);
						} else { // not on beaten path
							addToTempUpQueue(level);
							addToTempUpQueue(destLevel);
						}
					} else if (destLevel - level < 0){//DOWN call
						addToTempDownQueue(level);
						addToTempDownQueue(destLevel);
					}
				} else if (this.movementDirection == DOWN){//
					if (destLevel - level > 0){//UP call
						addToUpQueue(level);
						addToUpQueue(destLevel);
					} else if (destLevel - level < 0){// DOWN call
						addToTempDownQueue(level);
						addToTempDownQueue(destLevel);
					}
				}
				
			} else if (this.direction == DOWN){
				if (this.movementDirection == DOWN){
					if (destLevel - level > 0){//UP call
						addToTempUpQueue(level);
						addToTempUpQueue(destLevel);
					} else if (destLevel - level < 0){// DOWN call
						if (this.getElevation() > Simulation.floorLeveltoElevationMap.get(level)){//on beaten path
							addToDownQueue(level);
							addToDownQueue(destLevel);
						} else if (this.getElevation() == Simulation.floorLeveltoElevationMap.get(level) && this.isRun() == false){ //on same level and not run
							addToDownQueue(destLevel);
						} else { //not on beaten path
							addToTempDownQueue(level);
							addToTempDownQueue(destLevel);
						}
					}
				} else if (this.movementDirection == UP){
					if (destLevel - level > 0){//UP call
						addToTempUpQueue(level);
						addToTempUpQueue(destLevel);
					} else if (destLevel - level < 0){// DOWN call
						addToDownQueue(level);
						addToDownQueue(destLevel);
					}
				}
			}
		}
		
		void addToUpQueue(int level){
			if (!this.upQueue.contains(level)){// not queue yet
				this.upQueue.add(level);
			}
		}
		
		void addToTempUpQueue(int level){
			if (!this.tempUpQueue.contains(level)){
				this.tempUpQueue.add(level);
			}
		}
		
		void addToDownQueue(int level){
			if (!this.downQueue.contains(level)){
				this.downQueue.add(level);
			}
		}
		
		void addToTempDownQueue(int level){
			if (!this.tempDownQueue.contains(level)){
				this.tempDownQueue.add(level);
			}
		}

		
		public void printUpQueue(){
			itr = this.upQueue.iterator();
			System.out.println("start");
			while (itr.hasNext()){
				System.out.println(itr.next());
			}
			System.out.println("finish");
		}
		
		void doorControl(){
			if (this.isOpen() == true){
				this.doorOpenCounter++;
			}
			if (this.isRun() == false){
				countForOpen();
				countForClose();
			}
		}
		
		void countForOpen(){
			if(this.isOpen() == true){
				return;
			}
			if (this.isSwitchOpen() == true){
				this.doorCounter++;
				if (this.doorCounter == this.doorWaitingTime){
					this.doorCounter = 0;
					this.setOpen(true);
					this.setSwitchOpen(false);
				}
			}		
		}
		
		void countForClose(){
			if(this.isOpen() == false){
				return;
			}
			if (this.isSwitchClose() == true || this.doorOpenCounter >= this.maxDoorWaitingTime){
				this.doorCounter++;
				if (this.doorCounter == this.doorWaitingTime){
					this.setSwitchClose(false);
					this.doorCounter = 0;
					this.setOpen(false);					
					processQueue();
				}
			}
		}
		
		void processQueue(){
			if (isQueueEmpty() == true){
				this.direction = IDLE;
				this.movementDirection = IDLE;
			} else {
				this.setSwitchRun(true);
			}
		}
		boolean isQueueEmpty(){
			boolean isQueueUpEmpty = this.upQueue.isEmpty();
			boolean isQueueDownEmpty = this.downQueue.isEmpty();
			boolean isTempQueueUpEmpty = this.tempUpQueue.isEmpty();
			boolean isTempQueueDownEmpty = this.tempDownQueue.isEmpty();
			return (isQueueUpEmpty && isQueueDownEmpty && isTempQueueUpEmpty && isTempQueueDownEmpty);
		}
		
		void moveControl(){
			countForRun();
		}
		
		void countForRun(){
			if(this.isRun() == true){
				return;
			} else {
				if (this.isSwitchRun() == true){
					this.moveCounter++;
					if (this.moveCounter == this.moveWaitingTime){
						this.moveCounter = 0;
						this.setRun(true);
						this.setSwitchRun(false);
					}
				}
			}
		}
		
		boolean isOnTargetFloor(){
			return this.isOnTargetFloor;
		}
		
		void setOnTargetFloor(boolean isOnTargetFloor){
			this.isOnTargetFloor = isOnTargetFloor;
		}
		
		public int getEnterPassQueueNum(int level){
			HashMap<String, Human> levelEnterQueue = this.enterHoldList.get(level);
			return levelEnterQueue.size();
		}
		
		public void registerEnterHoldList(int level, Human human){
			HashMap<String, Human> levelEnterQueue = this.enterHoldList.get(level);
			List<Human> levelEnterOrderedList = this.enterHoldOrderedList.get(level);
			levelEnterQueue.put(human.getID(), human);
			levelEnterOrderedList.add(human);
		}
		
		public boolean isEnterHoldListEmpty(int level){
			HashMap<String, Human> levelEnterQueue = this.enterHoldList.get(level);
			boolean isEmpty = true;
			if (levelEnterQueue == null){
				return true;
			}
			if (levelEnterQueue.size() != 0){
				for (Human human : levelEnterQueue.values()){
					boolean isSkipElevator = human.isSkipElevator();
					if (isSkipElevator == false){
						isEmpty = false;
						break;
					}
				}
			} else {
				isEmpty = true;
			}
			return isEmpty;
		}
		
		public void registerExitHoldList(int level, Human human){
			HashMap<String, Human> levelExitQueue = this.exitHoldList.get(level);
			List<Human> levelExitOrderedList = this.exitHoldOrderedList.get(level);
			levelExitQueue.put(human.getID(), human);
			levelExitOrderedList.add(human);
		}
		
		public boolean isExitHoldListEmpty(int level){
			HashMap<String, Human> levelExitQueue = this.exitHoldList.get(level);
			boolean isEmpty = true;
			if (levelExitQueue == null){
				return true;
			}
			if (levelExitQueue.size() !=0){
				for (Human human : levelExitQueue.values()){
					Integer humanLvl = human.getCurrentLevel();
					if (humanLvl != null){
						if (humanLvl == this.getCurrentLevel()){
							isEmpty = false;
							break;
						}
					}				
				}
			} else {
				isEmpty = true;
			}
			return isEmpty;
		}
		
		public boolean isBothHoldListEmpty(int level){
			return isEnterHoldListEmpty(level) && isExitHoldListEmpty(level);
		}
		
		public void setEnterHoldListUnregistration(int level, Human human){
			this.humanEnterUnregistrationList.add(human);
			this.enterUnregistrationLevelMap.put(human.getID(), level);
		}
		
		public void processEnterHoldListUnregistration(){
			if (!this.humanEnterUnregistrationList.isEmpty()){
				for (Human human : this.humanEnterUnregistrationList){
					int level = this.enterUnregistrationLevelMap.get(human.getID());
					unregisterEnterHoldList(level, human);
				}
				this.humanEnterUnregistrationList.clear();
				this.enterUnregistrationLevelMap.clear();
				boolean isNoHold = this.isBothHoldListEmpty(this.getCurrentLevel());
				if (isNoHold == true && this.isOpen() == true && this.isSwitchClose() == false){
					car.setSwitchClose(true);
				}
			}
		}
		
		public boolean unregisterEnterHoldList(int level, Human human){
			HashMap<String, Human> levelEnterQueue = this.enterHoldList.get(level);
			List<Human> levelEnterOrderedList = this.enterHoldOrderedList.get(level);
			if (levelEnterQueue != null){
				if (levelEnterQueue.containsKey(human.getID())){
					levelEnterQueue.remove(human.getID());
					levelEnterOrderedList.remove(human);
					this.addPassanger(1);
					this.doorOpenCounter = 0;
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
		public void setExitHoldListUnregistration(int level, Human human){
			this.humanExitUnregistrationList.add(human);
			this.exitUnregisterationLevelMap.put(human.getID(), level);
		}
		
		public void processExitHoldListUnregistration(){
			if (!this.humanExitUnregistrationList.isEmpty()){
				for (Human human : this.humanExitUnregistrationList){
					int level = this.exitUnregisterationLevelMap.get(human.getID());
					this.unregisterExitHoldList(level, human);
				}
				this.humanExitUnregistrationList.clear();
				this.exitUnregisterationLevelMap.clear();
				boolean isNoHold = this.isBothHoldListEmpty(this.getCurrentLevel());
				if (isNoHold == true && this.isOpen() == true && this.isSwitchClose() == false){
					car.setSwitchClose(true);
				}
			}
		}
		
		public boolean unregisterExitHoldList(int level, Human human){
			HashMap<String, Human> levelExitQueue = this.exitHoldList.get(level);
			List<Human> levelExitOrderedList = this.exitHoldOrderedList.get(level);
			if (levelExitQueue != null){
				if (levelExitQueue.containsKey(human.getID())){
					levelExitQueue.remove(human.getID());
					levelExitOrderedList.remove(human);
					this.removePassanger(1);
					this.doorOpenCounter = 0;
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		
		public int getInitLevel(){
			return this.initLevel;
		}
		
		public double getElevation(){
			return this.elevation;
		}
		
		public void setElevation(double elevation){
			this.elevation = elevation;
			Double3D position = Simulation.continuous3D.getObjectLocation(this);
			Simulation.continuous3D.setObjectLocation(this, new Double3D(position.x, position.y, this.elevation));
		}
		
		public void setPosition(){
			Region region = this.elevator.getRegion(0);
			Int3D center = region.getCenter();
			this.elevation = Simulation.floorLeveltoElevationMap.get(this.initLevel);
			Simulation.continuous3D.setObjectLocation(this, new Double3D(center.x, center.y, this.elevation));
		}
		
		public void setRandomPosition(MersenneTwisterFast twister){
			double nextDouble = twister.nextDouble(true, true);
			int doubleIndexMax = this.elevator.levelList.size()-1;
			double index = nextDouble * (double) doubleIndexMax;
			int intIndex = (int) Math.round(index);
			int level = this.elevator.levelList.get(intIndex);
			this.initLevel = level;
			this.currentLevel = initLevel;
			setPosition();	
		}
		
		public int getDirection(){
			return this.direction;
		}
		
		public void setDirection(int direction){
			this.direction = direction;
		}
		
		public int getMovementDirection(){
			return this.movementDirection;
		}
		
		public void setMovementDirection(int direction){
			this.movementDirection = direction;
		}
		
		public void setDirectionToLevel(int destLevel){
			if (this.currentLevel < destLevel){
				setMovementDirection(UP);
			} else if (this.currentLevel > destLevel){
				setMovementDirection(DOWN);
			} else if (this.currentLevel == destLevel){
				//System.out.println("cuurentleel == destLevel");
				return;
			}
			this.setOpen(false);
			this.setSwitchRun(true);
			//this.setRun(true);
		}
		
		public int getCurrentLevel(){
			return this.currentLevel;
		}
		
		public int getHeight(){
			return this.height;
		}
		
		public int getMaxCapacity(){
			return this.maxCapacity;
		}
		
		public int getPassNum(){
			return this.passangerNum;
		}
		
		public void addPassanger(int num){
			this.passangerNum += num;
			if (this.passangerNum >= this.maxCapacity){
				this.setFull(true);
			}
		}
		
		public void removePassanger(int num){
			this.passangerNum -= num;
			if (!(this.passangerNum >= this.maxCapacity)){
				this.setFull(false);
			}
		}
		
		public int getExcessCapacity(){
			return this.maxCapacity - this.passangerNum;
		}
		
		public void setFull(boolean isFull){
			this.isFull = isFull;
		}
		
		public boolean isFull(){
			return this.isFull;
		}
		
		public boolean isRun(){
			return this.isRun;
		}
		
		public void setRun(boolean isRun){
			if (isRun == false){
				double elevation = Simulation.floorLeveltoElevationMap.get(this.currentLevel);
				this.setElevation(elevation);
			}
			this.isRun = isRun;
		}
		
		public boolean isSwitchOpen(){
			return this.openDoorSwitch;
		}
		
		public void setSwitchOpen(boolean isSwitchOpen){
			this.openDoorSwitch = isSwitchOpen;
		}
		
		public boolean isSwitchClose(){
			return this.closeDoorSwitch;
		}
		
		public void setSwitchClose(boolean isSwitchClose){
			this.closeDoorSwitch = isSwitchClose;
		}
		
		public boolean isSwitchRun(){
			return this.runSwitch;
		}
		
		public void setSwitchRun(boolean isSwitchRun){
			if (isSwitchRun == true){
				if (this.runSwitch == false){
					this.runSwitch = true;
				}
			} else {
				this.runSwitch = false;
			}
		}
		
		public boolean isOpen(){
			return this.isOpen;
		}
		
		public void setOpen(boolean isOpen){
			if (isOpen == false){
				this.doorOpenCounter = 0;
			}
			this.isOpen = isOpen;
		}
		
		public boolean isHold(){
			return this.isHold;
		}
		
		public void setHold(boolean isHold){
			this.isHold = isHold;
		}
	}
}
