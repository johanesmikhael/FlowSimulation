package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import processing.EnvironmentModel;
import ec.util.MersenneTwisterFast;

public class HumanGenerator {
	
	private static int humanNum;
	public static HashMap<String, Human> HumanList = new HashMap<>();
	
	public static MersenneTwisterFast twister = new MersenneTwisterFast();
	//List<Integer> skipFloorList = new ArrayList<Integer>();
	public static List<Floor> floorList = new ArrayList<Floor>();
	//human num
	//generate human in different floor from different place
	
	public static void generateHuman(int num, EnvironmentModel model){
		warmTwister();
		humanNum = num;
		selectFloor();
		int i = 0;
		while (i < humanNum){
			Floor floorA = getRandomFloor();
			Floor floorB = null;
			do {
				floorB = getRandomFloor();
			} while (floorA.getLevel() == floorB.getLevel());
			
			Tile tileA = floorA.getRandomCenterTile(twister);
			Tile tileB = floorB.getRandomCenterTile(twister);
			//System.out.println("tileA : " + tileA);
			//System.out.println("tileB : " + tileB);
			Itenerary itenerary = new Itenerary();
			itenerary.setStart(tileA);
			itenerary.setFinish(tileB);
			boolean isIteneraryExist = itenerary.calculateItenerary();
			//System.out.println(isIteneraryExist);
			if (isIteneraryExist == true){
				Tile startTile = itenerary.getStart();
				Human human = Human.createHuman(startTile, itenerary);//create human for one itenerary;
				human.setVariableType(Human.RANDOMAGENT);
				human.setItenerary(itenerary);
				model.addHuman(human);
				//Simulation.getApplet().getEnvironmentModel().addHuman(human);
				//System.out.println(i);
				i++;
			}
		}
	}
	
	public static Floor getRandomFloor(){
		double random = twister.nextDouble(true,  true);
		int floorRange = floorList.size() - 1;
		double randomFloor = random * (double) floorRange;
		int floorIndex = (int) Math.round(randomFloor);
		Floor floor = floorList.get(floorIndex);
		//System.out.println("floorlevel :" + floor.getLevel());
		return floor;
	}
		
	public static void warmTwister(){
		for (int i = 0 ; i < 1249; i++){
			twister.nextInt();
		}
	}
	
	static void selectFloor(){
		for (int elevation : Simulation.floorElevationList){
			Floor floor = Simulation.floorHash.get(elevation);
			if (floor.getRegionNum() > 1){
				floorList.add(floor);
			}
		}
	}
	
	
}
