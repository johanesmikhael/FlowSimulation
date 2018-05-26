package processing;

import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import processing.core.*;
import remixlab.proscene.Scene;
import simulation.Elevator;
import simulation.Environment;
import simulation.Floor;
import simulation.Human;
import simulation.Simulation;




public class EnvironmentModel {
		
	
	PApplet applet;
	Scene scene;
	boolean isActive;
	boolean isGrab;
	boolean isFloorActive;
	/*private final int GENERALAREA;
	private final int STAFFPATIENTAREA;
	private final int STAFFAREA;
	private final int DOOR;
	private final int GENERALELEVATOR;
	private final int BEDELEVATOR;*/
	List<InteractiveFloor> floors;
	HashMap<String,ElevatorPortrayal> elevators;
	List<HumanPortrayal> humans;
	
	EnvironmentModel(PApplet applet){
		this.applet = applet;
		ProcessingDisplay display = (ProcessingDisplay) applet;
		this.scene = display.getScene();
		floors = new ArrayList<InteractiveFloor>();
		elevators = new HashMap<>();
		this.isActive = false;
		this.isGrab = false;
		this.isFloorActive = false;
		setFloor();
		setElevator();
		humans = new ArrayList<HumanPortrayal>();
	}
	
	public void addHuman(Human human){
		HumanPortrayal humanPortrayal = new HumanPortrayal(applet, this, human);
		this.humans.add(humanPortrayal);
	}
	
	private void setFloor(){
		for (int elevation : Simulation.floorElevationList){
			Floor floor = Simulation.floorHash.get(elevation);
			InteractiveFloor interactiveFloor = new InteractiveFloor(this.applet, this, floor);
			floors.add(interactiveFloor);
		}
	}
	
	public InteractiveFloor getFloor(int index){
		return this.floors.get(index);
	}
	
	private void setElevator(){
		for(Elevator elevator : Simulation.elevatorHash.values()){
			ElevatorPortrayal elevatorPortrayal = new ElevatorPortrayal(this.applet, this, elevator);
			elevators.put(elevator.getID(), elevatorPortrayal);
		}
	}
	
	public Collection<ElevatorPortrayal> getElevators(){
		return elevators.values();
	}
	
	public static Area createAreaTile(int x, int y){
    	int nX = x - 400;
    	int nY = -y + 400;
    	float cornerX = nX - 0.5f;
    	float cornerY = nY - 0.5f;
    	Rectangle2D rectangle = new Rectangle2D.Float(cornerX, cornerY, 1, 1);
    	Area tileArea = new Area(rectangle);
    	return tileArea;
    }
	
	public static void reduceVertex(List<float[]> vertexList, List<PVector> vertexs){
		float pX = 0;
		float pY = 0;
		for(int i = 0; i < vertexs.size(); i++){
			PVector vertex = vertexs.get(i);
			if (i == 0){
				pX = vertex.x;
				pY = vertex.y;
				float[] v = new float[]{vertex.x, vertex.y, 0};
				vertexList.add(v);
			} else if (pX != vertex.x && pY != vertex.y){
				pX = vertex.x;
				pY = vertex.y;
				float[] v = new float[]{vertex.x, vertex.y, 0};
				vertexList.add(v);
			} else if (pX == vertex.x && pY != vertex.y){
				PVector nextVrt = getNextVertex(i, vertexs);
				if (vertex.x != nextVrt.x){
					pX = vertex.x;
    				pY = vertex.y;
    				float[] v = new float[]{vertex.x, vertex.y, 0};
    				vertexList.add(v);
				}
			} else if (pX != vertex.x && pY == vertex.y){
				PVector nextVrt = getNextVertex(i, vertexs);
				if (vertex.y != nextVrt.y){
					pX = vertex.x;
    				pY = vertex.y;
    				float[] v = new float[]{vertex.x, vertex.y, 0};
    				vertexList.add(v);
				}
			}   			
		}
	}
	
	public static PVector getNextVertex(int index, List<PVector> vertexs){
		int next = index + 1;
		if (next == vertexs.size()){
			next = 0;
		}
		PVector p = vertexs.get(next);
		return p;
	}
	
	public void deActivatedAll(){
		for(InteractiveFloor floor : floors){
			floor.setActive(false);
		}
		setFloorActive(false);
	}
	
	public void setFloorActive(boolean isFloorActive){
		this.isFloorActive = isFloorActive;
	}
	
	public boolean isFloorActive(){
		return this.isFloorActive;
	}
	
	public void activateInside(){
		if (this.isActive == false){
			this.isActive = true;
			deActivatedAll();
		} else {
			 deActivatedAll();
			 for(InteractiveFloor floor: floors){
				 boolean floorGrab = floor.isGrab();
				 if (floorGrab == true){
					 floor.setActive(true);
					 setFloorActive(true);
					 return;
				 }
			 }
		}
	}
	
	public void activateOutside(){
		ProcessingDisplay display = (ProcessingDisplay) applet;
		ControlPanel controlPanel = display.getControlPanel();
		if(isFloorActive == true){
			deActivatedAll();
			controlPanel.resetTileInfoPanel();
		} else {
			if(this.isActive() == true){
				this.setActive(false);
				controlPanel.resetTileInfoPanel();
			}
		}
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	public void setActive(boolean isActive){
		this.isActive = isActive;
	}

	
	public void draw() {
		// need improvement in visibility control
		if (ProcessingDisplay.agentVisibility == 1) {
			for (HumanPortrayal human : humans) {
				human.draw();
			}
		}
		if (ProcessingDisplay.elevatorVisibility == 1) {
			for (ElevatorPortrayal elevator : elevators.values()) {
				elevator.draw();
			}
		}
		if (ProcessingDisplay.floorVisibility == 1) {
			for (InteractiveFloor floor : floors) {
				floor.draw();
			}
		}
	}
}
