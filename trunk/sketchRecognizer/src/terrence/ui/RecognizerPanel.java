package terrence.ui;

import java.awt.AWTError;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.testbed.framework.TestbedFrame;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedTest;
import org.jbox2d.testbed.framework.j2d.DebugDrawJ2D;
import org.jbox2d.testbed.framework.j2d.TestPanelJ2D;
import org.jbox2d.testbed.tests.PolyShapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import terrence.recognizer.interaction.dollar.Dollar;
import terrence.recognizer.interaction.dollar.DollarListener;
import terrence.recognizer.steven.FinalObject;

public class RecognizerPanel extends JPanel implements TestbedPanel,
		DollarListener {
	private static final Logger log = LoggerFactory
			.getLogger(TestbedPanel.class);

	public static final int INIT_WIDTH = 800;
	public static final int INIT_HEIGHT = 600;

	private static final float ZOOM_OUT_SCALE = .95f;
	private static final float ZOOM_IN_SCALE = 1.05f;

	private Graphics2D dbg = null;
	private Image dbImage = null;

	private int panelWidth;
	private int panelHeight;

	private final TestbedModel model;
	private final DebugDrawer draw;

	private final Vec2 dragginMouse = new Vec2();
	private boolean drag = false;

	private Dollar dollar = new Dollar(Dollar.GESTURES_DEFAULT); // Sketch
																	// Recognizer
	private boolean ok = false;
	private final List<FinalObject> objects = new ArrayList<FinalObject>();

	public RecognizerPanel(TestbedModel argModel) {
		setBackground(Color.BLACK);
		draw = new DebugDrawer(this);
		model = argModel;
		updateSize(INIT_WIDTH, INIT_HEIGHT);
		setPreferredSize(new Dimension(INIT_WIDTH, INIT_HEIGHT));

		// Sketch Listener
		dollar.setListener(this);
		dollar.setActive(true);

		addMouseWheelListener(new MouseWheelListener() {

			private final Vec2 oldCenter = new Vec2();
			private final Vec2 newCenter = new Vec2();
			private final Mat22 upScale = Mat22
					.createScaleTransform(ZOOM_IN_SCALE);
			private final Mat22 downScale = Mat22
					.createScaleTransform(ZOOM_OUT_SCALE);

			public void mouseWheelMoved(MouseWheelEvent e) {
				DebugDraw d = draw;
				int notches = e.getWheelRotation();
				TestbedTest currTest = model.getCurrTest();
				if (currTest == null) {
					return;
				}
				OBBViewportTransform trans = (OBBViewportTransform) d
						.getViewportTranform();
				oldCenter.set(model.getCurrTest().getWorldMouse());
				// Change the zoom and clamp it to reasonable values - can't
				// clamp now.
				if (notches < 0) {
					trans.mulByTransform(upScale);
					currTest.setCachedCameraScale(currTest
							.getCachedCameraScale() * ZOOM_IN_SCALE);
				} else if (notches > 0) {
					trans.mulByTransform(downScale);
					currTest.setCachedCameraScale(currTest
							.getCachedCameraScale() * ZOOM_OUT_SCALE);
				}

				d.getScreenToWorldToOut(model.getMouse(), newCenter);

				Vec2 transformedMove = oldCenter.subLocal(newCenter);
				d.getViewportTranform().setCenter(
						d.getViewportTranform().getCenter()
								.addLocal(transformedMove));

				currTest.setCachedCameraPos(d.getViewportTranform().getCenter());
			}
		});

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				dragginMouse.set(e.getX(), e.getY());
				dollar.pointerPressed(e.getX(), e.getY()); // Get the
															// coordinates for
															// sketch
															// recognition
				drag = e.getButton() == MouseEvent.BUTTON3;
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				dollar.pointerReleased(e.getX(), e.getY());
			}
		});

		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				dollar.pointerDragged(e.getX(), e.getY());
				if (!drag) {
					return;
				}
				TestbedTest currTest = model.getCurrTest();
				if (currTest == null) {
					return;
				}
				Vec2 diff = new Vec2(e.getX(), e.getY());
				diff.subLocal(dragginMouse);
				currTest.getDebugDraw().getViewportTranform()
						.getScreenVectorToWorld(diff, diff);
				currTest.getDebugDraw().getViewportTranform().getCenter()
						.subLocal(diff);

				dragginMouse.set(e.getX(), e.getY());
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSize(getWidth(), getHeight());
				dbImage = null;
			}
		});
	}

	@Override
	public DebugDraw getDebugDraw() {
		return draw;
	}

	public Graphics2D getDBGraphics() {
		return dbg;
	}

	private void updateSize(int argWidth, int argHeight) {
		panelWidth = argWidth;
		panelHeight = argHeight;
		draw.getViewportTranform().setExtents(argWidth / 2, argHeight / 2);
	}

	public void render() {
		if (dbImage == null) {
			log.debug("dbImage is null, creating a new one");
			if (panelWidth <= 0 || panelHeight <= 0) {
				return;
			}
			dbImage = createImage(panelWidth, panelHeight);
			if (dbImage == null) {
				log.error("dbImage is still null, ignoring render call");
				return;
			}
			dbg = (Graphics2D) dbImage.getGraphics();
		}
		dbg.setColor(Color.black);
		dbg.fillRect(0, 0, panelWidth, panelHeight);
	}

	public void paintScreen() {
		try {
			Graphics g = this.getGraphics();
			if ((g != null) && dbImage != null) {
				g.drawImage(dbImage, 0, 0, null);
				// synchronized(objects){
				// for(FinalObject obj : objects){
				// obj.render(g);
				// }
				// }
				Toolkit.getDefaultToolkit().sync();
				g.dispose();
			}
		} catch (AWTError e) {
			log.error("Graphics context error", e);
		}
	}

	@Override
	public void dollarDetected(Dollar dollar) {
		double score = dollar.getScore();
		String recognizedName = dollar.getName();
		ok = score > 0.80;
		if (ok) {
			log.debug("Recognized Name:"+recognizedName+" Score:"+score);
			if (recognizedName.toLowerCase().startsWith("rectangle")) {
				PolygonShape rectangle = new PolygonShape();
				float halfW = (float) ((float)(dollar.getResult().bounds[2] - dollar.getResult().bounds[0])/2.0);
				float halfH = (float) ((float)(dollar.getResult().bounds[3] - dollar.getResult().bounds[1])/2.0);
				
				Vec2 worldCor = draw.getScreenToWorld((float)dollar.getResult().bounds[0], (float)dollar.getResult().bounds[1]);
				Vec2 w = draw.getScreenToWorld(halfW, halfH);
				rectangle.setAsBox(w.x/8,w.y/8);
				float x = worldCor.x;
				float y = worldCor.y;
				addShape(x, y, rectangle);
			}
			if (recognizedName.toLowerCase().startsWith("circle")) {
				CircleShape circle = new CircleShape();
				float halfW = (float) ((float)(dollar.getResult().bounds[2] - dollar.getResult().bounds[0])/2.0);
				float halfH = (float) ((float)(dollar.getResult().bounds[3] - dollar.getResult().bounds[1])/2.0);
				
				Vec2 worldCor = draw.getScreenToWorld((float)dollar.getResult().bounds[0], (float)dollar.getResult().bounds[1]);
				Vec2 w = draw.getScreenToWorld(halfW, halfH);
				circle.m_radius = w.x/8;
				float x = worldCor.x;
				float y = worldCor.y;
				addShape(x, y, circle);
			}
			if (recognizedName.toLowerCase().startsWith("triangle")) {
				PolygonShape triangle = new PolygonShape();
				float halfW = (float) ((float)(dollar.getResult().bounds[2] - dollar.getResult().bounds[0])/2.0);
				float halfH = (float) ((float)(dollar.getResult().bounds[3] - dollar.getResult().bounds[1])/2.0);
				
				Vec2 worldCor = draw.getScreenToWorld((float)dollar.getResult().bounds[0], (float)dollar.getResult().bounds[1]);
				Vec2 w = draw.getScreenToWorld(halfW, halfH);
				Vec2 vertices[] = new Vec2[3];
				vertices[0] = new Vec2(-w.x/8, 0.0f);
				vertices[1] = new Vec2(w.x/8, 0.0f);
				vertices[2] = new Vec2(0.0f, w.y/8);
				triangle.set(vertices, 3);
				float x = worldCor.x;
				float y = worldCor.y;
				addShape(x, y, triangle);
			}
		}
	}

	private void addShape(float positionX, float positionY, Shape shape) {
		//if (model.getCurrTest().getClass().isInstance(PolyShapes.class)) {
			PolyShapes curTest = (PolyShapes) model.getCurrTest();
			if(curTest != null){
//				curTest.Create(positionX, positionY, shape);
				BodyDef bd = new BodyDef();
				bd.type = BodyType.DYNAMIC;
				bd.position.set(positionX, positionY);
				Body body = curTest.getWorld().createBody(bd);
				
				PolygonShape tshape = new PolygonShape();
				tshape.setAsBox(4.0f, 4.0f, new Vec2(0.0f, 0.0f), 0.0f);
				body.createFixture(tshape, 0.2f);
				
				curTest.setBodies(body);

			}
		//}
	}
}