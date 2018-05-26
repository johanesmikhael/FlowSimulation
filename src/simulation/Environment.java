package simulation;
import java.util.ArrayList;
import java.util.List;
//import java.util.UUID;
import java.io.IOException;


import sim.field.network.Network;
//import sim.engine.*;
import sim.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.w3c.dom.*;

public class Environment {
	
	public static Document dom; //create static hashmap of regions
	
	public static void setup(){
		createElevatorNetwork();
		readXML(Simulation.environmentXMLData);
		processNetwork();	
		processElevator();
	}
	
	public static void createElevatorNetwork(){
		Network generalElevatorNetwork = new Network(false);
		Network staffPatientElevatorNetwork = new Network(false);
		Network staffElevatorNetwork = new Network(false);
		Simulation.elevatorNetworks.put(new Integer(Elevator.GENERAL), generalElevatorNetwork);
		Simulation.elevatorNetworks.put(new Integer(Elevator.STAFFANDPATIENT), staffPatientElevatorNetwork);
		Simulation.elevatorNetworks.put(new Integer(Elevator.STAFFONLY), staffElevatorNetwork);
	}
	
	public static void processNetwork(){
		for (int elevation : Simulation.floorElevationList){ //process network per floor;
			Floor floor = Simulation.floorHash.get(elevation);
			floor.processRegion();
		}
	}
	
	public static void processElevator(){
		for (Elevator elevator : Simulation.elevatorHash.values()){
			//elevator.setPosition();
			elevator.setRandomPosition(HumanGenerator.twister);
			elevator.getCar().setupQueueHashMap();
		}
	}
	
	public static boolean readXML(String xml){	
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		try {
			// use the factory to take an instance of the document builder
			DocumentBuilder db = dbf.newDocumentBuilder();
			// parse using the builder to get the DOM mapping of the
			// XML file
			dom = db.parse(xml);
			Element docEle = dom.getDocumentElement();
			processFloors(docEle);
			processElevators(docEle);		
			return true;		
			} catch (ParserConfigurationException pce) {
	            System.out.println(pce.getMessage());
	            } catch (SAXException se) {
	            System.out.println(se.getMessage());
	            } catch (IOException ioe) {
	            System.err.println(ioe.getMessage());
	            }
		return false;
	}
	
	public static void processFloors(Element data){
		NodeList floorsNodeList = data.getElementsByTagName("floors");
		Element floors = null;
		if(floorsNodeList != null && floorsNodeList.getLength() > 0){
			floors = (Element)floorsNodeList.item(0);
		}
		NodeList floorNodeList = floors.getElementsByTagName("floor");
		if (floorNodeList != null && floorNodeList.getLength() > 0){		
			for(int i = 0; i < floorNodeList.getLength(); i++){
				Element floor = (Element)floorNodeList.item(i);
				processFloor(floor, i);
			}
		}
	}
	
	public static void processFloor(Element floorEle, int index){
		int level = getIntValue(floorEle, "level");
		int elevation = getIntValue(floorEle, "elevation")/Simulation.GRIDDIMENSION;
		Floor floor = new Floor(level, elevation);
		Simulation.addFloor(floor);
		//Simulation.floorList.add(new Floor(level, elevation));
		//Floor singleFloor = Simulation.floorList.get(index);
		NodeList regionsNodeList = floorEle.getElementsByTagName("regions"); //process floor region
		Element regions = (Element)regionsNodeList.item(0);
		NodeList regionNodeList = regions.getElementsByTagName("region");
		for(int i = 0; i < regionNodeList.getLength(); i++){
			Element floorRegion = (Element)regionNodeList.item(i);
			processFloorRegion(floor, floorRegion, level, elevation, i);
		}
		NodeList doorsNodeList = floorEle.getElementsByTagName("doors"); //process door region
		if(doorsNodeList != null && doorsNodeList.getLength() > 0){
			Element doors = (Element)doorsNodeList.item(0);
			NodeList doorNodeList = doors.getElementsByTagName("door");
			if(doorNodeList != null && doorNodeList.getLength() > 0){
				for(int i = 0; i < doorNodeList.getLength(); i++){
					Element doorRegion = (Element)doorNodeList.item(i);
					processDoorRegion(floor, doorRegion, level, elevation, i);
				}
			}
		}
	}
	
	public static void processFloorRegion(Floor singleFloor, Element floorRegion, int level, int elevation, int index){
		String regionName = getTextValue(floorRegion, "name");
		String regionId = getTextValue(floorRegion, "id");
		int regionAccessibility = getIntValue(floorRegion, "accessibility");
		//create region object and put on hashmap
		Region region = new Region(index, regionName, regionId, singleFloor);
		singleFloor.addRegion(region);
		region.setType(Region.FLOORREGION);
		region.setAccessibility(regionAccessibility);
		NodeList gridsNodeList = floorRegion.getElementsByTagName("grids");
		Element grids = (Element)gridsNodeList.item(0);
		NodeList gridNodeList = grids.getElementsByTagName("grid");
		processGrid(gridNodeList, region);		
	}
	
	
	public static void processDoorRegion(Floor singleFloor, Element doorRegion, int level, int elevation, int index){
		String doorName = getTextValue(doorRegion, "name");
		String doorID = getTextValue(doorRegion, "id");
		//create door object and put on hashmap
		Region door = new Region(index, doorName, doorID, singleFloor);
		singleFloor.addRegion(door);
		door.setType(Region.DOORREGION);
		door.setAccessibility(Region.GENERAL);
		NodeList gridsNodeList = doorRegion.getElementsByTagName("grids");
		Element grids = (Element)gridsNodeList.item(0);
		NodeList gridNodeList = grids.getElementsByTagName("grid");
		processGrid(gridNodeList, door);
	}
	
	public static void processElevators(Element data){
		NodeList elevatorsNodeList = data.getElementsByTagName("elevators");
		Element elevators = null;
		if(elevatorsNodeList != null && elevatorsNodeList.getLength() > 0){
			elevators = (Element)elevatorsNodeList.item(0);
		}
		NodeList elevatorNodeList = elevators.getElementsByTagName("elevator");
		if (elevatorNodeList != null && elevatorNodeList.getLength() > 0){
			for(int i = 0; i < elevatorNodeList.getLength(); i++){
				Element elevator = (Element)elevatorNodeList.item(i);
				processElevator(elevator, i);
			}
		}
	}
	
	public static void processElevator(Element elevatorEle, int elvIndex){
		//get elevator data
		String elvName = getTextValue(elevatorEle, "name");
		int elvType = getIntValue(elevatorEle, "type");
		int elvAccessibility = getIntValue(elevatorEle, "accessibility");
		String elvID = null; //waiting for the first region ID
		//List<Integer> elevatorLevels = new ArrayList<Integer>();
		//List<int[]> grids = new ArrayList<int[]>();
		NodeList levelsNodelist = elevatorEle.getElementsByTagName("levels");
		Element levels = (Element)levelsNodelist.item(0);
		NodeList levelNodelist = levels.getElementsByTagName("level");
		if (levelNodelist != null && levelNodelist.getLength() > 0){
			Element firstLevel = (Element)levelNodelist.item(0);
			String id = getTextValue(firstLevel, "id");
			elvID = id;			
		}
		if (elvType == Elevator.BEDELEVATOR){
			if (true){//Simulation.elevatorHash.size() < 2
				Elevator elevator = new Elevator(elvName, elvID, elvIndex, elvType, elvAccessibility, 5);
				if (Simulation.isElevatorRandom == true){
					elevator.randomize();
				}
				Simulation.elevatorHash.put(elvID, elevator);
				processElevatorLevels(elevator, levelNodelist);
			}		
		}
		if (elvType == Elevator.GENERALELEVATOR){
			if (true){
				Elevator elevator = new Elevator(elvName, elvID, elvIndex, elvType, elvAccessibility, 10);
				if (Simulation.isElevatorRandom == true){
					elevator.randomize();
				}
				Simulation.elevatorHash.put(elvID, elevator);
				processElevatorLevels(elevator, levelNodelist);
			}
		}	
	}
	
	public static void processElevatorLevels(Elevator elevator, NodeList levelNodeList){
		List<int[]> elevatorGridList = new ArrayList<int[]>();
		if (levelNodeList != null && levelNodeList.getLength() > 0){
			for (int i = 0; i < levelNodeList.getLength(); i++){
				Element level = (Element)levelNodeList.item(i);
				String id = getTextValue(level, "id");
				int levelIndex = getIntValue(level, "index");
				if (i == 0){
					elevatorGridList = processElevatorGrid(level);
				}
				String levelRegionName = elevator.getName() + " - level " + levelIndex;
				int elevation = Simulation.floorLeveltoElevationMap.get(levelIndex);
				Floor floor = Simulation.floorHash.get(elevation);
				Region elevatorLevelRegion = new Region(i, levelRegionName, id, floor);
				elevatorLevelRegion.setAccessibility(elevator.getAccessibility());
				elevatorLevelRegion.setType(Region.ELEVATORREGION);
				processGrid(elevatorGridList, elevatorLevelRegion);
				elevator.addRegion(elevatorLevelRegion);
				//get appropriate floor;
				
				floor.addRegion(elevatorLevelRegion);
				//add ID hashmap
				Simulation.regionIdtoElevatorHash.put(id, elevator);
			}
		}
	}
	
	public static List<int[]> processElevatorGrid(Element data){
		List<int[]> list = new ArrayList<int[]>();
		NodeList gridsNodeList = data.getElementsByTagName("grids");
		if (gridsNodeList != null && gridsNodeList.getLength() > 0){
			Element grids = (Element)gridsNodeList.item(0);
			NodeList gridNodeList = grids.getElementsByTagName("grid");
			if (gridNodeList != null && gridNodeList.getLength() > 0){
				for (int i = 0; i < gridNodeList.getLength(); i++){
					Element grid = (Element)gridNodeList.item(i);
					int x = getIntValue(grid, "x");
					int y = getIntValue(grid, "y");
					int[] coordinate = new int[] {x, y};
					list.add(coordinate);
				}
			}
		}
		return list;
	}
	static void processGrid(NodeList gridNodeList, Region region){
		if(gridNodeList != null && gridNodeList.getLength() > 0 ){
			int minX = 999999999;
			int maxX = 0;
			int minY = 999999999;
			int maxY = 0;
			for(int i = 0; i < gridNodeList.getLength(); i++){
				Element grid = (Element)gridNodeList.item(i);
				int gridX = getIntValue(grid, "x");
				int gridY = getIntValue(grid, "y");
				//int gridZ = (int)region.getRegionGridElevation();
				int gridZ = region.getElevation();
				if (gridX < minX){
					minX = gridX;
				}
				if (gridX > maxX){
					maxX = gridX;
				}
				if (gridY < minY){
					minY = gridY;
				}
				if (gridY > maxY){
					maxY = gridY;
				}
				region.addTile(new Int3D(gridX, gridY, gridZ), i);
				region.setBoundingBox(minX, maxX, minY, maxY);
			}
		}
	}
	
	static void processGrid(List<int[]> grids, Region region){
		if(grids.size() > 0){
			int minX = 999999999;
			int maxX = 0;
			int minY = 999999999;
			int maxY = 0;
			for(int i = 0; i < grids.size(); i++){
				int[] coordinate = grids.get(i);
				int gridX = coordinate[0];
				int gridY = coordinate[1];
				//int gridZ = (int)region.getRegionGridElevation();
				int gridZ = region.getElevation();
				if (gridX < minX){
					minX = gridX;
				}
				if (gridX > maxX){
					maxX = gridX;
				}
				if (gridY < minY){
					minY = gridY;
				}
				if (gridY > maxY){
					maxY = gridY;
				}
				region.addTile(new Int3D(gridX, gridY, gridZ), i);
				region.setBoundingBox(minX, maxX, minY, maxY);
			}
		}
	}
	
	
	public static String getTextValue(Element ele, String tagName){
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0){
			Element el = (Element)nl.item(0);
			textVal = el.getTextContent();
		} else {
			textVal = "";
		}
		return textVal;
	}
	
	public static Integer getIntValue(Element ele, String tagName){
		String textValue = getTextValue(ele, tagName);
		if (textValue !=null){
			return Integer.parseInt(textValue);
		} else {
			return null;
		}
	}
}
