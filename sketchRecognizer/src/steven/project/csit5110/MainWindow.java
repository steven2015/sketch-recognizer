package steven.project.csit5110;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ResourceBundle;

import steven.ui.SFrame;

public class MainWindow extends SFrame{
	private static final long serialVersionUID = 4361093881408047904L;
	private final Image backImage;
	private final Graphics backImageGraphic;
	private final int width;
	private final int height;
	private final Algorithm algo = new Algorithm();

	public static void main(final String[] args){
		Sample.generateSample();
		Sample.loadSample();
		final ResourceBundle bundle = ResourceBundle.getBundle("main");
		new MainWindow(Integer.parseInt(bundle.getString("width")),Integer.parseInt(bundle.getString("height")));
	}
	public MainWindow(final int width, final int height){
		super(width,height);
		backImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		backImageGraphic = backImage.getGraphics();
		this.width = width;
		this.height = height;
		setLocation(0,0);
		setTimerInterval(100);
		setVisible(true);
		startTimer();
	}
	@Override
	protected void timerTick(){
		repaint();
		algo.draw(backImageGraphic,width,height);
		drawImage(backImage);
		setTitle(String.valueOf(getTickInterval() / 1000000));
	}
	@Override
	public void windowClosing(final WindowEvent event){
		System.exit(0);
	}
	@Override
	protected void mouseDown(final MouseEvent event, final int button, final int x, final int y){
		if(button == MouseEvent.BUTTON1){
			algo.sketch(x,y,false);
		}else if(button == MouseEvent.BUTTON3){
			algo.clearSketch();
		}
	}
	@Override
	protected void mouseUp(final MouseEvent event, final int button, final int x, final int y){
		if(button == MouseEvent.BUTTON1){
			algo.sketch(x,y,true);
		}
	}
	@Override
	protected void mouseMove(final MouseEvent event, final int x, final int y){
		if((event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) == InputEvent.BUTTON1_DOWN_MASK){
			algo.sketch(x,y,false);
		}
	}
	@Override
	public void keyReleased(final KeyEvent event){
		// add image to database
		if(event.getKeyCode() == KeyEvent.VK_1){
			algo.match();
		}
	}
}
