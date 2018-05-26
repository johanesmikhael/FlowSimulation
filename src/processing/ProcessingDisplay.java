package processing;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.awt.geom.*;

import controlP5.ControlEvent;
import processing.core.*;
import processing.event.MouseEvent;
import processing.opengl.PGraphics3D;
import remixlab.bias.event.DOF2Event;
import remixlab.dandelion.core.Constants.ClickAction;
import remixlab.dandelion.core.Constants.Target;
import remixlab.dandelion.geom.Vec;
import remixlab.proscene.*;
import sim.engine.*;
import sim.field.network.*;
import sim.util.*;
import simulation.Elevator;
import simulation.Floor;
import simulation.Region;
import simulation.Simulation;
import simulation.Tile;

public class ProcessingDisplay extends PApplet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2037839094017919266L;
	
	public static final double HUMANSPEED = 160;
	public static final int SIMULATIONNUM = 999999;
	public static final int RANDOMAGENTNUM = 100;
	public static final int SIMULATIONSPEED = 1000; // normal at 100

	public static void main(String args[]) {
		PApplet.main(new String[] {"processing.ProcessingDisplay"});
	}
		
	public final int GENERALAREA = color(0,0,255,64);
	public final int STAFFPATIENTAREA = color(255,191,0,64);
	public final int STAFFAREA = color(255,0,0,64);
	public final int DOOR = color(127,255,249,64);
	public final int GENERALELEVATOR = color(139,0,139,64);
	public final int BEDELEVATOR = color(255,231,55,64);
	public final int NORMALALPHA = 64;
	public final int ACTIVEALPHA = 128;
	public final int INTACTIVEALPHA = 16;
	
	
	public static final int OBSERVEMODE = 0;
	public static final int SELECTMODE = 1;

	public static int  interfaceMode = SELECTMODE;

	private SimState state;
	private Scene scene;
	EnvironmentModel environmentModel;
	//FloorSurface floorSurface;
	FloorNetwork floorNetwork;
	FloorTile floorTile;
	ControlPanel controlPanel;

	
	static boolean isPaused = false;
	static boolean isRun = false;
	boolean init = false;

	//floorMode
	public static final int NORMALMODE = 0;
	public static final int DENSITYMODE = 1;

	//visibility control
	static int floorVisibility = 1;
	static int elevatorVisibility = 1;
	static int agentVisibility = 1;
	static int floorMode = ProcessingDisplay.DENSITYMODE;

	//maximum density for visualization
	public static final double MAXDENSITY = 1.5;

	static boolean isSaveFrame = false;
	
	DOF2Event prevEvent, event;
	PGraphics canvas;
	
	public void setup() {
		setupFrame();
		thread("setupSimulation");
    }
    public void draw() {
    	if (isSaveFrame == true){
    		saveFrame("/frame/frame-####.png");
    	}
    	if (init == false){
    		setupDisplay();
    	}
    	background(0);
    	if (init == true) {
    		display();
    	}
    }
    
    public void controlEvent(ControlEvent theEvent) {
    	this.controlPanel.controlEvent(theEvent);
	}
    
    public void keyPressed(){
    	switch(key){
    	case 'p':
    		if (isRun == false){
    			isRun = true;
    			
    		} else {
    			isRun = false;
    		}
    		break;
    	case 'm':
    		isSaveFrame = !isSaveFrame;
    	default:
    		break;
    	}
    }
    
   public void mousePressed(MouseEvent e){
	   if (init == false){
		   return;
	   }
		event = new DOF2Event(prevEvent, (float) mouseX, (float) mouseY);

		boolean isGrab = false;
		
				
		if(environmentModel.isFloorActive() == false){
			boolean floorGrab = false;
			checkFloorGrab:
			for (InteractiveFloor floor : environmentModel.floors){
				boolean grab = floor.isGrab();
				if(grab == true){
					floorGrab = true;
					isGrab = floorGrab;
					break checkFloorGrab;
				}
			}
		}
		
		//System.out.println(isGrab);
				if (e.getCount() >= 2){
			if (isGrab == false){
				environmentModel.activateOutside();
			} else {
				//System.out.println("you click object");
			}
		}
		prevEvent = event.get();
	}
   
   public boolean sketchFullScreen() {
	   return false;
	 }

    

        
/////////////////////////////METHODS
   void setupFrame(){	
   	size(2500, 1600, OPENGL);//1366,768 1024, 600  2049, 1152
   	scene = new Scene(this);
   	Vec vec = new Vec();
   	scene.eye().setSceneRadius(400);
   	scene.eye().fitBall(vec, 400);
   	scene.removeMouseClickBinding(Target.EYE, LEFT, 2);
   	scene.setMouseClickBinding(Target.FRAME, LEFT, 1, ClickAction.CUSTOM);
   	scene.setMouseClickBinding(Target.FRAME, LEFT, 2, ClickAction.ALIGN_FRAME);
   	System.out.println(scene.isOffscreen());
	}
    
   
   void setupDisplay(){
    	if (Simulation.isReady() == true){
    		//setFloorSurface();
    		setEnvironmentModel();
    		//setFloorNetwork();
    		//setFloorTile();
    		setupControl();
    		init = true;
    	}
    }
    
    void display(){
    	displayEnvironmentModel();
    	scene.beginScreenDrawing();
		displayControl();
		scene.endScreenDrawing();
    }
    
    public void setupSimulation(){
    	state = new Simulation(System.currentTimeMillis(), this);
    	state.start();
    }
    
    public void runSimulation(){
    	Simulation simulation = (Simulation) state;
    	simulation.scheduleAgents();
    		do
    			state.schedule.step(state);
    		while(isRun == true);
    }
    
    void setupControl(){
    	controlPanel = new ControlPanel(this, environmentModel);
    }
    
    void displayControl(){
    	if (init == false){
    		return;
    	}
    	controlPanel.draw();
    }
    
    void setEnvironmentModel(){
    	environmentModel = new EnvironmentModel(this);
    }
    
    public EnvironmentModel getEnvironmentModel(){
    	return this.environmentModel;
    }
    
    void displayEnvironmentModel(){
    	environmentModel.draw();
    	int num = 0;
    	for (ElevatorPortrayal elvPort : environmentModel.getElevators()){
    		int passNum = elvPort.getElevator().getCar().getPassNum();
    		boolean isFull = elvPort.getElevator().getCar().isFull();
    		//System.out.println("elevator-" + num + " : " + passNum+ " isFull : " + isFull);
    		num++;
    	}
    }
    
    void setFloorNetwork(){
    	floorNetwork = new FloorNetwork(this);
    	floorNetwork.setNetworkEdges();
    }
    
    
    void displayFloorNetwork(){
    	floorNetwork.displayNetworkEdges();
    }
     
    public static void setRun(boolean isRun){
    	ProcessingDisplay.isRun = isRun;
    }
    
    public static  boolean isRun(){
    	return ProcessingDisplay.isRun;
    }
    
    public Simulation getSimulation(){
    	return (Simulation) this.state;
    }
    public ControlPanel getControlPanel(){
    	return this.controlPanel;
    }
    
    public Scene getScene(){
    	return this.scene;
    }
}
