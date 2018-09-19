package processing;

import java.awt.Rectangle;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//import javafx.scene.Parent;
import processing.core.*;
import remixlab.bias.core.BogusEvent;
import remixlab.dandelion.core.AbstractScene;
import remixlab.dandelion.core.Frame;
import remixlab.dandelion.core.InteractiveFrame;
import remixlab.dandelion.core.Constants.DandelionAction;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.Scene;
import sim.util.Int3D;
import simulation.Elevator;
import simulation.Floor;
import simulation.Region;
import simulation.Simulation;
import simulation.Tile;



public class InteractiveFloor{
	
	private final int GENERALAREA;
	private final int STAFFPATIENTAREA;
	private final int STAFFAREA;
	private final int DOOR;
	private final int GENERALELEVATOR;
	private final int BEDELEVATOR;
	private final int NORMALALPHA;
	private final int ACTIVEALPHA;
	private final int INACTIVEALPHA;
	
	
	private PApplet applet;
	private Scene scene;
	private EnvironmentModel model;
	private boolean isActive;
	private boolean isVisible;
	//private int gridElevation;
	private int elevation;
	private int level;
	HashMap<String, RegionArea>regions;
	FloorTile floorTile;
	
	private boolean isGrab = false;
	
	InteractiveFloor(PApplet applet, EnvironmentModel model, Floor floor){		
		//super(scene);
		this.applet = applet;
		ProcessingDisplay display = (ProcessingDisplay) this.applet;
		this.model = model;
		this.scene = display.getScene();
		this.regions = new HashMap<>();
		this.isActive = false;
		this.isVisible = true;
		GENERALAREA = applet.color(0,0,255,96);
		STAFFPATIENTAREA = applet.color(255,191,0,96);
		STAFFAREA = applet.color(255,0,0,96);
		DOOR = applet.color(127,255,249,96);
		GENERALELEVATOR = applet.color(139,0,139,96);
		BEDELEVATOR = applet.color(255,231,55,96);
		NORMALALPHA = 64;
		ACTIVEALPHA = 128;
		INACTIVEALPHA = 16;
		processFloor(floor);
		floorTile = new FloorTile(applet, scene, model, this);
		processFloorTile(floor);
	}
	
	public void setActive(boolean isActive){
		this.isActive = isActive;
	}
	
	public boolean isActive(){
		return this.isActive;
	}
	
	void processFloorTile(Floor floor){
		floorTile.setTiles(floor);
	}
	
	void processFloor(Floor floor){
		this.elevation = floor.getElevation();
		this.level = floor.getLevel();
		//this.gridElevation = floor.getGridElevation();
		this.elevation = floor.getElevation();
		for (Region region : floor.getRegions()){
			if (region.getType() == Region.FLOORREGION){
				switch(region.getAccessibility()){
				case Region.STAFFONLY:
					processRegion(region, STAFFAREA);
					break;
				case Region.STAFFANDPATIENT:
					processRegion(region, STAFFPATIENTAREA);
					break;
				case Region.GENERAL:
					processRegion(region, GENERALAREA);
					break;
				default:
					break;
				}
			} else if (region.getType() == Region.DOORREGION){
				processRegion(region, DOOR);
			} else if (region.getType() == Region.ELEVATORREGION){
				String id = region.getID();
				Elevator elevator = Simulation.regionIdtoElevatorHash.get(id);
				switch(elevator.getType()){
				case Elevator.GENERALELEVATOR:
					processRegion(region, GENERALELEVATOR);
					break;
				case Elevator.BEDELEVATOR:
					processRegion(region, BEDELEVATOR);
					break;
				default:
					break;
				}
			}
		}
	}
	
	public void processRegion(Region region, int color){
		String id = region.getID();
		RegionArea regionArea = new RegionArea(this, scene, id, region);
		for(Tile tileObject : region.getTiles()){
			Int3D location = tileObject.getLocation();
			Area tile = EnvironmentModel.createAreaTile(location.x, location.y);
			regionArea.add(tile);
		}
		regionArea.setColor(color);
		regionArea.processArea();
		if (region.getType() == Region.ELEVATORREGION){
			regionArea.setVisible(false);
		}
		regions.put(id, regionArea);
	}

	public void clearHumans(){
		for (RegionArea regionArea : regions.values()){
			regionArea.clearHumans();
		}
	}
	
	public int getElevation(){
		return this.elevation;
	}
	
	public int getLevel(){
		return this.level;
	}
	
	public RegionArea getRegionArea(String id){
		return this.regions.get(id);
	}
	
	/*public int getGridElevation(){
		return this.gridElevation;
	}*/
	
	
	public boolean isGrab(){
		return this.isGrab;
	}
	
	public boolean isVisible(){
		return this.isVisible;
	}
	
	public void draw(){
		for (RegionArea regionArea : regions.values()){
			regionArea.draw();
		}
		floorTile.draw();
	}
	
	
	class RegionArea extends InteractiveFrame{
    	Area area;
    	InteractiveFloor floor;
    	int color;
    	int normalColor;
    	int activeColor;
    	int inActiveColor;
    	int densityColor;
    	int densityNormalColor;
		int densityActiveColor;
		int densityInActiveColor;
    	int grabColor = applet.color(255,255,0,128);
    	boolean isVisible;
    	String id;
    	Region region;
    	
    	int z;
    	List<float[]> vertexList;
    	
    	int x;
    	int y;
    	int height;
    	int width;
    	
    	RegionArea(InteractiveFloor iFloor, Scene scene, String id, Region region){
    		super(scene);
    		this.id = id;
    		area = new Area();
    		vertexList = new ArrayList<float[]>();
    		z = elevation;
    		this.floor = iFloor;
    		this.isVisible = this.floor.isVisible();
    		this.region = region;
    	}
    	
    	void add(Area newArea){
    		area.add(newArea);
    	}
    	
    	void processArea(){
    		List<PVector> tempList = new ArrayList<PVector>();
          	PathIterator path = this.area.getPathIterator(null);
        	float[] coords = new float[6];
        	boolean isDone = false;
        	while (isDone == false){
        		path.currentSegment(coords);
            	tempList.add(new PVector(coords[0], coords[1], z));
            	path.next();
            	isDone = path.isDone();
        	}
        	EnvironmentModel.reduceVertex(this.vertexList, tempList);
        	
        	Rectangle rect = area.getBounds();
    		this.height = rect.height;
    		this.width = rect.width;
    		this.x = rect.x;
    		this.y = rect.y;
    	}
    	
    	
    	Area getArea(){
    		return this.area;
    	}
    	
    	void setColor(int color){
    		this.color = color;
    		//int a = (this.color >> 24) & 0xFF;
    		int r = (this.color  >> 16) & 0xFF;  // Faster way of getting red(argb)
    		int g = (this.color  >> 8) & 0xFF;   // Faster way of getting green(argb)
    		int b = this.color  & 0xFF;          // Faster way of getting blue(argb)
    		this.normalColor = applet.color(r, g, b, NORMALALPHA);
    		this.activeColor = applet.color(r, g, b, ACTIVEALPHA);
    		this.inActiveColor = applet.color(r, g, b, INACTIVEALPHA);
    	}


    	int getColor(){
    		return this.color;
    	}

    	void clearHumans(){
    		this.region.clearHumans();
		}

    	void setDensityColor(){
			double density = this.region.getDensity();
			//System.out.println(this.id + " density : " + density);
			double minDensity = 0;
			double maxDensity = ProcessingDisplay.MAXDENSITY;
			applet.colorMode(PConstants.HSB);
			float hue = (float) (128.0f - density/maxDensity * 128);
			float sat = (float) (density/maxDensity * 255);
			float brg = 255;
			float alpha = (float) ( 10 + density/maxDensity * 245);
			if (density > ProcessingDisplay.MAXDENSITY){
				hue = 255;
				sat = 255;
				alpha = 255;
			}
			int color = applet.color(hue, sat, brg, alpha);
			this.densityColor = color;
			applet.colorMode(PConstants.RGB);
			int r = (this.densityColor  >> 16) & 0xFF;  // Faster way of getting red(argb)
			int g = (this.densityColor  >> 8) & 0xFF;   // Faster way of getting green(argb)
			int b = this.densityColor  & 0xFF;          // Faster way of getting blue(argb)
			this.densityNormalColor = applet.color(r, g, b, NORMALALPHA);
			this.densityActiveColor = applet.color(r, g, b, alpha);
			this.densityInActiveColor = applet.color(r, g, b, 0);
		}
    	
    	void setHeight(int z){
    		this.z = z;
    	}
    	
    	float getHeight(){
    		return this.z;
    	}
    	
    	public void setVisible(boolean isVisible){
    		this.isVisible = isVisible;
    	}
    	
    	
    	public boolean checkIfGrabsInput(BogusEvent event) {
    		if (ProcessingDisplay.interfaceMode != ProcessingDisplay.SELECTMODE){
    			isGrab = false;
    			return false;
    		}
    		if (model.isActive() == false){
    			isGrab = pointInsideQuad(scene);
    			model.isGrab = isGrab;
    			return isGrab;
    		} else {
    			model.isGrab = false;
    			if (model.isFloorActive() == true){
        			isGrab = false;
        			return false;
        		} else {
        			isGrab = pointInsideQuad(scene);
        			return isGrab;
        		}
    		}		
    	}
		 
		private boolean pointInsideQuad(AbstractScene scene) {
			Vec v1 = scene.projectedCoordinatesOf(new Vec(x, y,z));
			Vec v2 = scene.projectedCoordinatesOf(new Vec(x, y + height,z));
			Vec v3 = scene.projectedCoordinatesOf(new Vec(x+width, y + height,z));
			Vec v4 = scene.projectedCoordinatesOf(new Vec(x+width, y,z));

			return
			computePointPosition(applet.mouseX, applet.mouseY, v1.x(), v1.y(), v2.x(), v2.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v2.x(), v2.y(), v3.x(), v3.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v3.x(), v3.y(), v4.x(), v4.y()) < 0 &&
			computePointPosition(applet.mouseX, applet.mouseY, v4.x(), v4.y(), v1.x(), v1.y()) < 0;
		}

		private float computePointPosition(float x, float y, float x0, float y0, float x1, float y1) {
		// see "solution 3" at http://local.wasp.uwa.edu.au/~pbourke/geometry/insidepoly/
			return (y - y0) * (x1 - x0) - (x - x0) * (y1 - y0);
		}
		
		public void execAction3D(DandelionAction a) {
		    switch(a) {
		    	case CUSTOM:
		    		//activateFloor();
		    		break;
		    	case ALIGN_FRAME:
		    		model.activateInside();
		    		break;
		    	default:
		    		super.execAction3D(a);
		    		break;
		    }
		}
    	
    	public void draw(){
    		if (applet.frameCount % 10 == 0){
    			this.setDensityColor();
			}
    		if(this.isVisible == false){
    			return;
    		}
    		applet.pushMatrix();
    		applet.pushStyle();
    		//drawing code here
    		this.setPosition(0, 0, z);
    		this.setRotation(0,0,0,0);
    		this.setScaling(1);
    		this.applyTransformation();
    		
    		int color = 0;
    		if (ProcessingDisplay.floorMode == ProcessingDisplay.NORMALMODE){
				if (model.isActive() == false){
					color = this.color;
				} else {
					if (isActive == true){
						color = activeColor;
					} else {
						if(model.isFloorActive() == true){
							color = inActiveColor;
						} else {
							color = normalColor;
						}
					}
				}
			} else if(ProcessingDisplay.floorMode == ProcessingDisplay.DENSITYMODE){
				if (model.isActive() == false){
					color = this.densityColor;
				} else {
					if (isActive == true){
						color = densityActiveColor;
					} else {
						if(model.isFloorActive() == true){
							color = densityInActiveColor;
						} else {
							color = densityNormalColor;
						}
					}
				}
			}
    		if (model.isGrab == true){
    			color = grabColor;
    		} else if (isGrab == true){
    			color = grabColor;
    		}
    		if (vertexList.size() > 0){
    			applet.pushStyle();
        		applet.noStroke();
        		applet.fill(color);
        		applet.beginShape();
        		for (int i = 0; i < vertexList.size(); i++){
        			float[] p = vertexList.get(i);
        			applet.vertex(p[0], p[1], p[2]);
        		}
        		applet.endShape(PConstants.CLOSE);
        		applet.popStyle();
    		}
    		applet.popStyle();
    		applet.popMatrix();
    	}
    }
}
