package processing;

import controlP5.*;
import processing.core.PApplet;
import sim.util.Int3D;
import simulation.Human;
import simulation.HumanGenerator;
import simulation.Itenerary;
import simulation.Simulation;
import simulation.Tile;
import javax.swing.JOptionPane;

public class P5 {
	PApplet applet;
	EnvironmentModel model;
	ControlP5 cp5;
	Accordion accordion;
	Group propertiesGroup;
	Group inputDataGroup;
	Group visibilityGroup;
	
	static final String SETPATHDEF = "click to set the path";
	static final String SETPATH1 = "define start point";
	static final String SETPATH2 = "define destination point";
	
	int x;
	int y;
	public static int BASELINE = 10;
	public static int PANELBOX_WIDTH = 300;
	public static int MARGIN = 15;
	public static int MARGIN_TOP = 20;
	public static int PANEL_WIDTH = PANELBOX_WIDTH - MARGIN * 2;
	int backgroundColor;
	int backgroundHeight;

	P5(PApplet applet, EnvironmentModel model){
		this.applet = applet;
		this.model = model;
		x = applet.width - PANEL_WIDTH - MARGIN;
		y = MARGIN_TOP;
		backgroundColor = this.applet.color(255,8);
		backgroundHeight = 150;
		cp5 = new ControlP5(this.applet);
		cp5.setAutoDraw(false);
		setColor();
		CColor color = cp5.getColor();
		//color.setActive(applet.color(255,0,0));
		cp5.setColor(color);
		setSimulationControl();
		setPropertiesPanel();
		setInputDataPanel();
		setVisibilityPanel();
		setAccordion();
	}
	
	void setColor(){
		CColor color = cp5.getColor();
		color.setBackground(applet.color(39));
		color.setActive(applet.color(79));
		color.setForeground(applet.color(79));
	}
	
	void setPropertiesPanel(){
		float pX = MARGIN;
		float pY = BASELINE;
		propertiesGroup = cp5.addGroup("properties");
		propertiesGroup.setBackgroundColor(backgroundColor);
		propertiesGroup.setBackgroundHeight(BASELINE * 13);
		propertiesGroup.setHeight(BASELINE);
		cp5.addTextlabel("namelabel")
				.setText("Area Name")
				.setPosition(pX, pY)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("levellabel")
				.setText("Floor Level")
				.setPosition(pX, pY + 2 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("accessibilitylabel")
				.setText("Floor Accessibility")
				.setPosition(pX, pY + 4 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("typelabel")
				.setText("Type")
				.setPosition(pX, pY + 6 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("idlabel")
				.setText("ID")
				.setPosition(pX, pY + 8 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("locationlabel")
				.setText("Location")
				.setPosition(pX, pY + 10 * BASELINE)
				.moveTo(propertiesGroup);
		for (int i = 0; i <= 10; i +=2){
			float posX = BASELINE * 10;
			cp5.addTextlabel("punc" + i)
					.setText(":")
					.setPosition(posX, (i + 1)  * BASELINE)
					.moveTo(propertiesGroup);
		}
		float vX = BASELINE * 11;
		cp5.addTextlabel("namevalue")
				.setText("<area name>")
				.setPosition(vX, pY)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("levelvalue")
				.setText("<level>")
				.setPosition(vX, pY + 2 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("accessibilityvalue")
				.setText("<accessibility>")
				.setPosition(vX, pY + 4 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("typevalue")
				.setText("<type>")
				.setPosition(vX, pY + 6 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("idvalue")
				.setText("<id>")
				.setPosition(vX, pY + 8 * BASELINE)
				.moveTo(propertiesGroup);
		cp5.addTextlabel("locationvalue")
				.setText("<location>")
				.setPosition(vX, pY + 10 * BASELINE)
				.moveTo(propertiesGroup);
	}
	
	void setInputDataPanel(){
		inputDataGroup = cp5.addGroup("input_data");
		inputDataGroup.setBackgroundColor(backgroundColor);
		inputDataGroup.setBackgroundHeight(BASELINE * 12);
		inputDataGroup.setHeight(BASELINE);
		float pX = MARGIN;
		float pY = BASELINE;
		cp5.addButton("set_itenerary")
				.setPosition(pX, pY)
				.setHeight(BASELINE + 2)
				.setWidth(BASELINE * 8)
				.moveTo(inputDataGroup);
		cp5.addTextlabel("set_path_dialog")
				.setPosition(pX, pY + 2 * BASELINE)
				.setWidth(BASELINE * 8)
				.setText(SETPATHDEF)
				.moveTo(inputDataGroup);
		cp5.addTextfield("input_number_agent")
				.setPosition(pX, pY + 4 * BASELINE)
				.setWidth(BASELINE * 8)
				.setInputFilter(ControlP5.INTEGER)
				.setAutoClear(false)
				.setText(Integer.toString(ProcessingDisplay.RANDOMAGENTNUM))
				.setStringValue(Integer.toString(ProcessingDisplay.RANDOMAGENTNUM))
				.moveTo(inputDataGroup);
		cp5.addButton("set_number_agent")
				.setPosition(pX, pY + 8 * BASELINE)
				.setHeight(BASELINE + 2)
				.setWidth(BASELINE * 8)
				.moveTo(inputDataGroup);
	}

	void setVisibilityPanel() {
		visibilityGroup = cp5.addGroup("visibility_control");
		visibilityGroup.setBackgroundColor(backgroundColor);
		visibilityGroup.setBackgroundHeight(BASELINE * 12);
		visibilityGroup.setHeight(BASELINE);
		float pX = MARGIN;
		float pY = BASELINE;
		CheckBox checkBox = cp5.addCheckBox("visibility_checkbox")
				.setPosition(pX, pY)
				.setSize(BASELINE + 2, BASELINE + 2)
				.moveTo(visibilityGroup)
				.setItemsPerRow(1)
				.addItem("floor", 0)
				.addItem("elevator", 0)
				.addItem("moving_agent", 0)
				.addItem("floor_network", 0);
		float[] value = new float[4];
		value[0] = ProcessingDisplay.floorVisibility;
		value[1] = ProcessingDisplay.elevatorVisibility;
		value[2] = ProcessingDisplay.agentVisibility;
		value[3] = ProcessingDisplay.networkVisibility;
		checkBox.setArrayValue(value);
	}


	void setSimulationControl(){
		float gap = 6;
		float divWidth = Math.round((PANEL_WIDTH - gap * 2) / 3);
		float pX = x;
		float pY = y;
		float p1X = x + divWidth + gap;
		float p2X = p1X + divWidth + gap;
		
		cp5.addButton("start")
				.setPosition(pX, pY)
				.setHeight(BASELINE + 2)
				.setWidth((int)divWidth)
				.setLock(true);
		cp5.addButton("pause")
				.setPosition(p1X, y)
				.setHeight(BASELINE + 2)
				.setWidth((int)divWidth)
				.setLock(true);
		cp5.addButton("stop")
				.setPosition(p2X, y)
				.setHeight(BASELINE + 2)
				.setWidth((int)divWidth)
				.setLock(true);
	}
	
	void setAccordion(){
		float pX = x;
		float pY = y + 3 * BASELINE;
		 accordion = cp5.addAccordion("acc")
				 .setPosition(pX, pY)
				 .setWidth(PANEL_WIDTH)
				 .addItem(propertiesGroup)
				 .addItem(inputDataGroup)
				 .addItem(visibilityGroup);
		 accordion.open(0, 1, 2);
		 accordion.setCollapseMode(Accordion.MULTI);
	}
	
	void draw(){
		cp5.draw();
	}
	
	public void controlEvent(ControlEvent theEvent) {
		///System.out.println(theEvent.getClass().toString());
		String cName = theEvent.getName();
		//System.out.println(theEvent.getGroup().toString());
		//String cName = theEvent.getController().getName();
		if(cName.equals("start")){
			start(theEvent);
		}
		if(cName.equals("pause")){
			pause(theEvent);
		}
		if(cName.equals("stop")){
			stop();
		}
		if(cName.equals("set_itenerary")){
			setItenerary();
		}
		if(cName.equals("set_number_agent")){
			setNumberAgent();
		}
		if(cName.equals("visibility_checkbox")){
			setModelVisibility();
		}
	}


	void start(ControlEvent theEvent){
		ProcessingDisplay.setRun(true);
	  	applet.thread("runSimulation");
	  	theEvent.getController().setLock(true);
	  	cp5.getController("pause").setLock(false);
	  	cp5.getController("stop").setLock(false);
		cp5.getController("start").setLock(true);
		cp5.getController("set_itenerary").setLock(true);
		cp5.getController("set_number_agent").setLock(true);
	}
	
	void pause(ControlEvent theEvent){
		if (ProcessingDisplay.isRun() == true){
			ProcessingDisplay.setRun(false);
			theEvent.getController().setLabel("resume");
			cp5.getController("stop").setLock(true);
		} else {
			ProcessingDisplay.setRun(true);
			theEvent.getController().setLabel("pause");
			applet.thread("runSimulation");
			cp5.getController("stop").setLock(false);
		}
	}
	void stop(){
		ProcessingDisplay.setRun(false);
		cp5.getController("pause").setLock(true);
		cp5.getController("stop").setLock(true);
		cp5.getController("start").setLock(true);
		applet.thread("resetSimulation");
		cp5.getController("set_itenerary").setLock(false);
		cp5.getController("set_number_agent").setLock(false);
	}
	
	void setItenerary(){
		Simulation.agentPath = null;
		Simulation.agentPath = new Itenerary();
		ProcessingDisplay display = (ProcessingDisplay) applet;
		ControlPanel controlPanel = display.getControlPanel();
		controlPanel.resetTileInfoPanel();
		cp5.getController("set_itenerary").setLock(true);
		cp5.getController("set_path_dialog").setValueLabel(SETPATH1);
		controlPanel.setPath(true);
	}
	
	void setIteneraryTile(Tile tile){
		ProcessingDisplay display = (ProcessingDisplay) applet;
		ControlPanel controlPanel = display.getControlPanel();
		Itenerary itenerary = Simulation.agentPath;
		if (controlPanel.isSetPath() == true){
			if (itenerary.isStartSet() == false){
				itenerary.setStart(tile);
				cp5.getController("set_path_dialog").setValueLabel(SETPATH2);
			} else if (itenerary.isFinishSet() == false){
				if(!itenerary.getStart().equalsRegion(tile)){
					itenerary.setFinish(tile);
					cp5.getController("set_path_dialog").setValueLabel(SETPATHDEF);
					cp5.getController("set_itenerary").setLock(false);
					controlPanel.setPath(false);
					boolean isItenerary = itenerary.calculateItenerary();
					//System.out.println(isItenerary);
					if (isItenerary == true){
						Tile startTile = Simulation.agentPath.getStart();
						Human human = Human.createHuman(startTile, Simulation.agentPath);//create human for one itenerary;
						human.setVariableType(Human.TESTEDAGENT);
						human.setItenerary(Simulation.agentPath);
						model.addHuman(human);
						cp5.getController("start").setLock(false);
						//HumanGenerator.generateHuman(ProcessingDisplay.RANDOMAGENTNUM, model);
					}
				}
			}
		}
	}

	void setNumberAgent() {
		Textfield textfield = (Textfield) cp5.getController("input_number_agent");
		textfield.submit(); //make sure the text input on the field are submitted
		int agentNumber = Integer.valueOf(cp5.getController("input_number_agent").getStringValue());
		if (agentNumber > 500){
			String infoMessage = "Maximum people number is 500";
			String titleBar = "Maximum number";
			JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar, JOptionPane.INFORMATION_MESSAGE);
		} else {
			HumanGenerator.generateHuman(agentNumber, model);
			cp5.getController("start").setLock(false);
			cp5.getController("set_number_agent").setLock(true);
		}
	}

	void setModelVisibility() {
		ControllerGroup checkbox = cp5.getGroup("visibility_checkbox");
		float[] values = checkbox.getArrayValue();
		/*for (int i=0; i<values.length;i++) {
			int n = (int)checkbox.getArrayValue()[i];
			System.out.println(n);
		}*/
		ProcessingDisplay.floorVisibility = (int) values[0];
		ProcessingDisplay.elevatorVisibility = (int) values[1];
		ProcessingDisplay.agentVisibility = (int) values[2];
		ProcessingDisplay.networkVisibility = (int) values[3];
	}
	
	
	public ControlP5 getControlP5(){
		return this.cp5;
	}
}
