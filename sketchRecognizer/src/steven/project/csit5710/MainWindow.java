package steven.project.csit5710;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import steven.ui.SFrame;

public class MainWindow extends SFrame{
	private static final long serialVersionUID = 4361093881408047904L;
	private final Image backImage;
	private int lastX;
	private int lastY;

	public static void main(final String[] args){
		new MainWindow(800,600);
	}
	public MainWindow(final int width, final int height){
		super(width,height);
		backImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		setLocation(0,0);
		setTimerInterval(10);
		setVisible(true);
		startTimer();
	}
	@Override
	protected void timerTick(){
		repaint();
		drawImage(backImage);
		setTitle(String.valueOf(getTickInterval() / 1000000));
	}
	@Override
	public void windowClosing(final WindowEvent event){
		System.exit(0);
	}
	@Override
	protected void mouseDown(final MouseEvent event, final int button, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
		if(button == MouseEvent.BUTTON1){
			lastX = x;
			lastY = y;
		}
	}
	@Override
	protected void mouseMove(final MouseEvent event, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
		if(leftButtonDown){
			final Graphics g = backImage.getGraphics();
			g.setColor(new Color((int)(Math.random() * Integer.MAX_VALUE)));
			g.drawLine(lastX,lastY,x,y);
			lastX = x;
			lastY = y;
			g.dispose();
		}
	}
	@Override
	protected void mouseClick(final MouseEvent event, final int button, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
		if(button == MouseEvent.BUTTON3){
			final Graphics g = backImage.getGraphics();
			g.setColor(Color.WHITE);
			g.fillRect(0,0,backImage.getWidth(null),backImage.getHeight(null));
			g.dispose();
		}
	}
}
