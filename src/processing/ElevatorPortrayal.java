package processing;

import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import processing.InteractiveFloor.RegionArea;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import simulation.Elevator;
import simulation.Simulation;
import simulation.Elevator.Car;
import simulation.Region;

public class ElevatorPortrayal {
	PApplet applet;
	EnvironmentModel model;
	String ID;
	Elevator elevator;
	float elevation;
	float pElevation;
	int height;
	Area area;
	List<List<float[]>> vertexLists;
	boolean isVisible;
	
	int defaultColor;
	int openColor;
	int closedColor;
	int runColor;
	
	public ElevatorPortrayal(PApplet applet, EnvironmentModel model, Elevator elevator){
		this.applet = applet;
		this.elevator = elevator;
		this.model = model;
		this.isVisible = true;
		vertexLists = new ArrayList<List<float[]>>();
		processElevatorPortrayal();
	}
	
	public Elevator getElevator(){
		return this.elevator;
	}
	
	void processElevatorPortrayal(){
		Car car = elevator.getCar();
		int initLevel = car.getInitLevel();
		int floorIndex = Simulation.getFloorIndex(initLevel);
		InteractiveFloor intFloor = model.getFloor(floorIndex);
		String regionID = elevator.getRegionID(initLevel);
		RegionArea regionArea = intFloor.getRegionArea(regionID);
		this.area = regionArea.getArea();
		processColor(regionArea);
		processVertex(car, this.area);
	}
	
	void processColor(RegionArea regionArea){
		this.defaultColor = regionArea.getColor();
		//int a = (this.color >> 24) & 0xFF;
		int r = (this.defaultColor  >> 16) & 0xFF;  // Faster way of getting red(argb)
		int g = (this.defaultColor  >> 8) & 0xFF;   // Faster way of getting green(argb)
		int b = this.defaultColor  & 0xFF;          // Faster way of getting blue(argb)
		this.runColor = applet.color(r, g, b, 64);
		this.closedColor = applet.color(r, g, b, 128);
		this.openColor = applet.color(255, 0, 0, 128);
	}
		
	void processVertex(Car car, Area area){
		float elevation = (float)car.getElevation();
		this.height = car.getHeight();
		List<PVector> tempList = new ArrayList<PVector>();
      	PathIterator path = area.getPathIterator(null);
    	float[] coords = new float[6];
    	boolean isDone = false;
    	while (isDone == false){
    		path.currentSegment(coords);
        	tempList.add(new PVector(coords[0], coords[1], elevation));
        	path.next();
        	isDone = path.isDone();
    	}
    	List<float[]> vertexListA = new ArrayList<float[]>();
    	EnvironmentModel.reduceVertex(vertexListA, tempList);
    	List<float[]> vertexListB = new ArrayList<float[]>();
    	for (float[] vertex : vertexListA){
    		float[] v = new float[]{vertex[0], vertex[1], vertex[2] + height};
    		vertexListB.add(v);
    	}
    	this.vertexLists.add(vertexListA);
    	this.vertexLists.add(vertexListB);
    	for (int i = 0; i < vertexListA.size(); i++){
    		float[] v1,v2,v3,v4;
    		if (i < vertexListA.size() - 1){
    		v1 = vertexListA.get(i);
    		v2 = vertexListA.get(i+1);
    		v3 = vertexListB.get(i+1);
    		v4 = vertexListB.get(i);
    		} else {
    			v1 = vertexListA.get(i);
        		v2 = vertexListA.get(0);
        		v3 = vertexListB.get(0);
        		v4 = vertexListB.get(i);
    		}
    		List<float[]> vertexListN = new ArrayList<float[]>();
    		vertexListN.add(v1);
    		vertexListN.add(v2);
    		vertexListN.add(v3);
    		vertexListN.add(v4);
    		this.vertexLists.add(vertexListN);
    	}
    	this.elevation = elevation;
    	this.pElevation = this.elevation;
	}
	
	void updateVertex(){
		Car car = this.elevator.getCar();
		float elevation = (float)car.getElevation();
		float dElevation = elevation - this.pElevation;
		/*for(List<float[]> vertexts : this.vertexLists){
			for(float[] vertex : vertexts){
				vertex[2] = vertex[2] + dElevation;
			}
		}*/
		for (int i = 0; i < this.vertexLists.size(); i++){
			List<float[]> vertexs = this.vertexLists.get(i);
			if (i == 0){
				for (float[] vertex : vertexs){
					vertex[2] = elevation;
				}
			}
			if (i == 1){
				for (float[] vertex : vertexs){
					vertex[2] = elevation + height;
				}
			}
			if (i > 1){
				for (int j = 0; j < vertexs.size(); j++){
					float[] vertex = vertexs.get(j);
					if (j < 2){
						vertex[2] = elevation;
					} else if (j >= 2){ 
						vertex[2] = elevation + height;
					}
				}
			}
		}
		this.pElevation = elevation;
	}
	
	void updatePosition(){
		updateVertex();
	}
	

	public void draw(){
		updatePosition();
		if(this.isVisible == false){
			return;
		}
		applet.pushMatrix();
		applet.pushStyle();
		//drawing code here;
		int color;
		if (this.elevator.getCar().isRun() == true){ //elevator isRun
			color = this.runColor;
		} else {
			if (this.elevator.getCar().isOpen() == true){ //elevator isOpen
				color = this.openColor;
			} else {
				color = this.closedColor;
			}
		}
		for(List<float[]> vertexList : vertexLists){
			applet.noStroke();
			applet.fill(color);
    		applet.beginShape();
    		for (int i = 0; i < vertexList.size(); i++){
    			float[] p = vertexList.get(i);
    			applet.vertex(p[0], p[1], p[2]);
    		}
    		applet.endShape(PConstants.CLOSE);
		}
		applet.popStyle();
		applet.popMatrix();
	}
}
