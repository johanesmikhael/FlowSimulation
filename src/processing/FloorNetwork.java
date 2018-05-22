package processing;

import java.util.ArrayList;
import java.util.List;

import processing.core.*;
import sim.field.network.Edge;
import sim.field.network.Network;
import sim.util.Int3D;
import simulation.Floor;
import simulation.Region;
import simulation.Simulation;


public class FloorNetwork {
	private PApplet applet;
	private final int EDGECOLOR;
	private final int NODECOLOR;
	private List<NetworkEdge> edges;
	
	public FloorNetwork(PApplet p){
		this.applet = p;
		edges = new ArrayList<NetworkEdge>();
		EDGECOLOR = applet.color(255,255,0);
		NODECOLOR = applet.color(255,0,0);
	}
	
	
	 void setNetworkEdges(){
	    	//int i = 0;
	    	for (int elevation : Simulation.floorElevationList){
	    		if(true){
	    			Floor floor = Simulation.floorHash.get(elevation);
	    			Network network = floor.getNetworkAll();
	    			Edge[][] edgesList= network.getAdjacencyList(true);
	    			for (int j = 0; j < edgesList.length; j++){
	    				Edge[] edgeList = edgesList[j];
	    				for (Edge edge : edgeList){
	    					Object from = edge.getFrom();
	    					Object to = edge.getTo();
	    					Region regionFrom = (Region) from;
	    					Region regionTo = (Region) to;
	    					Int3D centerFrom = regionFrom.getCenter();
	    					Int3D centerTo = regionTo.getCenter();
	    					edges.add(new NetworkEdge(centerFrom, centerTo));
	    				}
	    			}
	    		}
				//i++;
	    	}
	    }
	    
	    void displayNetworkEdges(){
	    	for(NetworkEdge edge : edges){
	    		edge.draw();
	    	}
	    }
	    
	    public float[] getVertex(Int3D vertex){
			float[] p = new float[] {(vertex.x - 400), -(vertex.y - 400), vertex.z};
			return p;
		} 
	
	
	class NetworkEdge{
    	float[] p1;
    	float[] p2;
    	
    	NetworkEdge(Int3D point1, Int3D point2){
    		this.p1 = getVertex(point1);
    		this.p2 = getVertex(point2);
    	}
    	
    	public void draw(){
    		applet.pushStyle();
    		applet.noFill();
    		applet.stroke(EDGECOLOR);
    		applet.strokeWeight(0.5f);
    		applet.line(p1[0], p1[1], p1[2], p2[0], p2[1], p2[2]);
    		applet.noStroke();
    		applet.fill(NODECOLOR);
    		applet.pushMatrix();
    		applet.translate(p1[0], p1[1], p1[2]);
    		applet.box(1);
    		applet.popMatrix();
    		applet.pushMatrix();
    		applet.translate(p2[0], p2[1], p2[2]);
    		applet.box(1);
    		applet.popMatrix();
    		applet.popStyle();   		
    	}
    }    
}
