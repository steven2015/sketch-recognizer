package terrence.ui;

import java.applet.Applet;
import java.awt.Button;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;

import org.jbox2d.testbed.framework.TestList;
import org.jbox2d.testbed.framework.TestbedController;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedTest;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.framework.j2d.TestbedSidePanel;

import terrence.recognizer.interaction.dollar.Dollar;
import terrence.recognizer.interaction.dollar.DollarListener;
import terrence.recognizer.steven.FinalObject;
import terrence.recognizer.steven.Rectangle;

public class Recognizer extends Applet implements MouseListener, MouseMotionListener, DollarListener {
	int x;
	int y;
	int state;
	
	private TestbedSidePanel side;
	private TestbedModel model;
	private TestbedPanel panel;
	private TestbedController controller;
	
	Dollar dollar = new Dollar(Dollar.GESTURES_DEFAULT);
	String name = "";
	double score = 0;
	boolean ok = false;;

	private Button cleanScreenBtn;
//	private Button startAnimationBtn;
//	private Button stopAnimationBtn;
	
	Image offScreen;
	
	// steven 20111120 save recognized shape start
	private final List<FinalObject> objects = new ArrayList<FinalObject>();
	// steven 20111120 save recognized shape end
	
	public void init() 
	{
		// steven 20111120 development on pc start
		setSize(800,600);
		// steven 20111120 development on pc end
		offScreen = createImage(getSize().width, getSize().height);
	    model = new TestbedModel();
	    panel = new TestPanelJ2D(model);
	    TestList.populateModel(model);
	    model.setDebugDraw(panel.getDebugDraw());
	    controller = new TestbedController(model, panel);
	    side = new TestbedSidePanel(model, controller);
	    
	    add((Component) panel, "Center");
//	    add(new JScrollPane(side), "East");
	    
		// Terrence
		initButtons();
			
		addMouseListener(this);
		addMouseMotionListener(this);
		
		dollar.setListener(this);
		dollar.setActive(true);
		
		controller.playTest(0);
	    controller.start();
	}
	
	private void initButtons(){
		if(cleanScreenBtn == null)
			cleanScreenBtn = new Button("Clean Screen");
//		if(startAnimationBtn == null)
//			startAnimationBtn = new Button("Start Animation");
//		if(stopAnimationBtn == null)
//			stopAnimationBtn = new Button("Stop Animation");
		
		cleanScreenBtn.setBounds(20,20,100,30);
//		startAnimationBtn.setBounds(cleanScreenBtn.getBounds().x*2,20,100,30);
//		stopAnimationBtn.setBounds(cleanScreenBtn.getBounds().x*3,20,100,30);
		add(cleanScreenBtn);
//		add(startAnimationBtn);
//		add(stopAnimationBtn);
		cleanScreenBtn.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Clean Screen
				
			}
			
		});
	}
	
	public void mouseEntered(MouseEvent e) //mouse entered canvas
	{	}
	
	public void mouseExited(MouseEvent e) //mouse left canvas
	{	}
	
	public void mouseClicked(MouseEvent e) //mouse pressed-depressed (no motion in between), if there's motion -> mouseDragged
	{   
		// steven 20111120 clear screen with right mouse start
		if(e.getButton() == MouseEvent.BUTTON3){
			synchronized(objects){
				objects.clear();
			}
			repaint();
		}
		// steven 20111120 clear screen with right mouse end
	}
		
	public void update(MouseEvent e)
	{	
		x = e.getX();
		y = e.getY();
	
		repaint();
		e.consume();
	}	

	public void mousePressed(MouseEvent e) 
	{
		// steven 20111120 draw with left mouse start
		if(isLeftButtonDown(e)){
			state = 1;
			dollar.pointerPressed(e.getX(), e.getY());		
			update(e);
		}
		// steven 20111120 draw with left mouse end
	}
	public void mouseReleased(MouseEvent e) 
	{ 
		// steven 20111120 draw with left mouse start
		if(e.getButton() == MouseEvent.BUTTON1){
			state = 0;
			dollar.pointerReleased(e.getX(), e.getY());		
			update(e);
		}
		// steven 20111120 draw with left mouse end
	}
	public void mouseMoved(MouseEvent e) 
	{  
		state = 0;
		update(e);
	}
	
	public void mouseDragged(MouseEvent e) 
	{	
		// steven 20111120 draw with left mouse start
		if(isLeftButtonDown(e)){
			state = 2;
			dollar.pointerDragged(e.getX(), e.getY());				
			update(e);
		}
		// steven 20111120 draw with left mouse end
	}
	
	public void update(Graphics g)
	{
		Graphics temp = offScreen.getGraphics();
		temp.setColor(getBackground());
		temp.fillRect(0, 0, getWidth(), getHeight());
		temp.setColor(getForeground());
		
		paint(temp);
		
		temp.dispose();
		
		g.drawImage(offScreen, 0, 0, null);
	}
	
	public void paint(Graphics g)
	{
		// steven 20111120 remove '+' lines start
		//g.drawLine(0, y, getWidth(), y);
		//g.drawLine(x, 0, x, getHeight());
		// steven 20111120 remove '+' lines end

		g.drawString("[" + x + " " + y + "] [" + state + "]", 10, 20);

		if (ok)
			g.drawString("gesture: " + name + " (" + score + ")", 10, 60);	
		
		dollar.render(g);
		// steven 20111120 save recognized shape start
		Graphics2D g2d = (Graphics2D)g;
		synchronized(objects){
			for(FinalObject obj : objects){
				obj.render(g2d);
			}
		}
		// steven 20111120 save recognized shape end
	}
	
	public void dollarDetected(Dollar dollar)
	{
		score = dollar.getScore();
		name = dollar.getName();
		
		ok = score > 0.80;
		// steven 20111120 save recognized shape start
		if(ok){
			if(name.toLowerCase().startsWith("rectangle")){
				synchronized(objects){
					objects.add(new Rectangle(dollar.getResult()));
				}
			}
		}
		// steven 20111120 save recognized shape end
	}
	// steven 20111120 draw with left mouse start
	private static boolean isLeftButtonDown(final MouseEvent event){
		return (event.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) > 0;
	}
	// steven 20111120 draw with left mouse end
}
