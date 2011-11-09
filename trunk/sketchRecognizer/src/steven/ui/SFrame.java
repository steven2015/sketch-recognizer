package steven.ui;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

public abstract class SFrame extends JFrame implements WindowListener, WindowFocusListener, MouseListener, MouseMotionListener, MouseWheelListener, KeyListener{
	private static final long serialVersionUID = -1862042276393650759L;
	private final SFrameTimer timer = new SFrameTimer(this);
	private final long DEFAULT_TIMER_INTERVAL = 100;
	private final Image frontImage;
	private boolean leftButtonPressed;
	private boolean middleButtonPressed;
	private boolean rightButtonPressed;

	protected SFrame(final int width, final int height){
		frontImage = new BufferedImage(width,height,BufferedImage.TYPE_INT_ARGB);
		super.setSize(width,height);
		addWindowFocusListener(this);
		addWindowListener(this);
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		addKeyListener(this);
		setResizable(false);
		setTimerInterval(DEFAULT_TIMER_INTERVAL);
		addMouseListener(new MouseListener(){
			@Override
			public void mouseReleased(final MouseEvent event){
				switch(event.getButton()){
					case MouseEvent.BUTTON1:
						if(leftButtonPressed){
							leftButtonPressed = false;
							mouseUp(event,MouseEvent.BUTTON1,event.getX(),event.getY(),false,middleButtonPressed,rightButtonPressed);
						}
						break;
					case MouseEvent.BUTTON2:
						if(middleButtonPressed){
							middleButtonPressed = false;
							mouseUp(event,MouseEvent.BUTTON2,event.getX(),event.getY(),leftButtonPressed,false,rightButtonPressed);
						}
						break;
					case MouseEvent.BUTTON3:
						if(rightButtonPressed){
							rightButtonPressed = false;
							mouseUp(event,MouseEvent.BUTTON3,event.getX(),event.getY(),leftButtonPressed,middleButtonPressed,false);
						}
						break;
				}
				mouseUp(event,event.getButton(),event.getX(),event.getY(),(event.getModifiersEx() | InputEvent.BUTTON1_DOWN_MASK) > 0,(event.getModifiersEx() | InputEvent.BUTTON2_DOWN_MASK) > 0,
						(event.getModifiersEx() | InputEvent.BUTTON3_DOWN_MASK) > 0);
			}
			@Override
			public void mousePressed(final MouseEvent event){
				switch(event.getButton()){
					case MouseEvent.BUTTON1:
						if(leftButtonPressed == false){
							leftButtonPressed = true;
							mouseDown(event,MouseEvent.BUTTON1,event.getX(),event.getY(),true,middleButtonPressed,rightButtonPressed);
						}
						break;
					case MouseEvent.BUTTON2:
						if(middleButtonPressed == false){
							middleButtonPressed = true;
							mouseDown(event,MouseEvent.BUTTON2,event.getX(),event.getY(),leftButtonPressed,true,rightButtonPressed);
						}
						break;
					case MouseEvent.BUTTON3:
						if(rightButtonPressed == false){
							rightButtonPressed = true;
							mouseDown(event,MouseEvent.BUTTON3,event.getX(),event.getY(),leftButtonPressed,middleButtonPressed,true);
						}
						break;
				}
			}
			@Override
			public void mouseExited(final MouseEvent event){
				if(leftButtonPressed){
					leftButtonPressed = false;
					mouseUp(event,MouseEvent.BUTTON1,event.getX(),event.getY(),false,middleButtonPressed,rightButtonPressed);
				}
				if(middleButtonPressed){
					middleButtonPressed = false;
					mouseUp(event,MouseEvent.BUTTON2,event.getX(),event.getY(),leftButtonPressed,false,rightButtonPressed);
				}
				if(rightButtonPressed){
					rightButtonPressed = false;
					mouseUp(event,MouseEvent.BUTTON3,event.getX(),event.getY(),leftButtonPressed,middleButtonPressed,false);
				}
			}
			@Override
			public void mouseEntered(final MouseEvent event){
				leftButtonPressed = false;
				middleButtonPressed = false;
				rightButtonPressed = false;
			}
			@Override
			public void mouseClicked(final MouseEvent event){
				mouseClick(event,event.getButton(),event.getX(),event.getY(),leftButtonPressed,middleButtonPressed,rightButtonPressed);
			}
		});
		addMouseMotionListener(new MouseMotionListener(){
			@Override
			public void mouseMoved(final MouseEvent event){
				mouseMove(event,event.getX(),event.getY(),false,false,false);
			}
			@Override
			public void mouseDragged(final MouseEvent event){
				mouseMove(event,event.getX(),event.getY(),leftButtonPressed,middleButtonPressed,rightButtonPressed);
			}
		});
	}
	@Override
	public void windowGainedFocus(final WindowEvent event){
	}
	@Override
	public void windowLostFocus(final WindowEvent event){
	}
	@Override
	public void windowClosing(final WindowEvent event){
	}
	@Override
	public void windowIconified(final WindowEvent event){
	}
	@Override
	public void windowDeiconified(final WindowEvent event){
	}
	@Override
	public void windowActivated(final WindowEvent event){
	}
	@Override
	public void windowDeactivated(final WindowEvent event){
	}
	@Override
	public void mouseWheelMoved(final MouseWheelEvent event){
	}
	@Override
	public void mouseDragged(final MouseEvent event){
	}
	@Override
	public void mouseMoved(final MouseEvent event){
	}
	@Override
	public void mouseClicked(final MouseEvent event){
	}
	@Override
	public void mousePressed(final MouseEvent event){
	}
	@Override
	public void mouseReleased(final MouseEvent event){
	}
	@Override
	public void mouseEntered(final MouseEvent event){
	}
	@Override
	public void mouseExited(final MouseEvent event){
	}
	@Override
	public void keyTyped(final KeyEvent event){
	}
	@Override
	public void keyPressed(final KeyEvent event){
	}
	@Override
	public void keyReleased(final KeyEvent event){
	}
	protected void mouseDown(final MouseEvent event, final int button, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
	}
	protected void mouseUp(final MouseEvent event, final int button, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
	}
	protected void mouseClick(final MouseEvent event, final int button, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
	}
	protected void mouseMove(final MouseEvent event, final int x, final int y, final boolean leftButtonDown, final boolean middleButtonDown, final boolean rightButtonDown){
	}
	@Override
	public final void paint(final Graphics g){
		g.drawImage(frontImage,0,0,null);
	}
	@Override
	public final void windowOpened(final WindowEvent event){
	}
	@Override
	public final void windowClosed(final WindowEvent event){
	}
	@Override
	public final void update(final Graphics g){
		paint(g);
	}
	@Override
	public final void setSize(final int width, final int height){
	}
	public final long getTickInterval(){
		return timer.thread.tickInterval;
	}
	public final void drawImage(final Image img){
		final Graphics g = frontImage.getGraphics();
		g.drawImage(img,0,0,null);
		g.dispose();
	}
	public final void startTimer(){
		timer.start();
	}
	public final void stopTimer(){
		timer.stop();
	}
	public final void setTimerInterval(final long ms){
		timer.interval = ms;
	}
	protected abstract void timerTick();
}

class SFrameTimer{
	final SFrame frame;
	volatile long interval;
	final SFrameTimerThread thread = new SFrameTimerThread(this);

	SFrameTimer(final SFrame frame){
		this.frame = frame;
	}
	void start(){
		if(thread.active == false){
			new Thread(thread).start();
		}
	}
	void stop(){
		thread.active = false;
	}
	void callback(){
		frame.timerTick();
	}
}

class SFrameTimerThread implements Runnable{
	final SFrameTimer timer;
	volatile long tickInterval;
	volatile boolean active;

	SFrameTimerThread(final SFrameTimer timer){
		this.timer = timer;
	}
	@Override
	public void run(){
		long lastTick = System.nanoTime();
		long curTick;
		active = true;
		while(active){
			curTick = System.nanoTime();
			tickInterval = curTick - lastTick;
			lastTick = curTick;
			timer.callback();
			try{
				Thread.sleep(timer.interval);
			}catch(final InterruptedException e){
				e.printStackTrace();
			}
		}
	}
}
