package processing;

import java.util.List;

import processing.core.PApplet;
import sim.util.Double3D;
import sim.util.MutableDouble3D;
import simulation.Human;

public class HumanPortrayal {
	PApplet applet;
	EnvironmentModel model;
	String ID;
	Human human;
	float[] position;
	
	int color;
	
	
	public HumanPortrayal(PApplet applet, EnvironmentModel model, Human human){
		this.applet = applet;
		this.model = model;
		this.human = human;
		this.ID = this.human.getID();
		processColor();
		//this.color= applet.color(244,244,0);
		position = new float[3];
		updatePosition();
	}
	
	void processColor(){
		switch (this.human.getVariableType()){
		case Human.RANDOMAGENT:
			this.color = applet.color(200,200,0);
			break;
		case Human.TESTEDAGENT:
			this.color = applet.color(10,230,230);
			break;
		default:
			break;
		}
	}
	
	public void updatePosition(){
		Double3D position = this.human.getLocation();
		float x = (float)position.x - 400;
		float y = -(float)position.y + 400;
		float z = (float)position.z + 5;
		this.position[0] = x;
		this.position[1] = y;
		this.position[2] = z;
	}
	
	public void draw(){
		updatePosition();
		applet.pushMatrix();
		applet.pushStyle();
		applet.noStroke();
		applet.fill(this.color);
		applet.translate(this.position[0], this.position[1], this.position[2]);
		applet.sphere(1.5f);
		applet.popStyle();
		applet.popMatrix();
	}
}
