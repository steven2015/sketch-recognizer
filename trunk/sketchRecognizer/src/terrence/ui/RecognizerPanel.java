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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Mat22;
import org.jbox2d.common.OBBViewportTransform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.testbed.framework.TestbedModel;
import org.jbox2d.testbed.framework.TestbedPanel;
import org.jbox2d.testbed.framework.TestbedTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import terrence.recognizer.interaction.dollar.Dollar;
import terrence.recognizer.interaction.dollar.DollarListener;
import terrence.recognizer.interaction.dollar.Result;
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
				Toolkit.getDefaultToolkit().sync();
				dollar.render(g);
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
			addShape(dollar.getResult());
		}
	}

	private void addShape(Result result) {
		Vec2 position = draw.getScreenToWorld((float)(result.bounds[0]+result.bounds[2])/2.0f, (float)(result.bounds[1]+result.bounds[3])/2.0f);
		Vec2 leftTop=draw.getScreenToWorld((float)result.bounds[0],(float)result.bounds[1]);
		Vec2 rightBottom=draw.getScreenToWorld((float)result.bounds[2],(float)result.bounds[3]);
		PolyWorld curTest = (PolyWorld) model.getCurrTest();
		if(curTest != null){
			BodyDef bd = new BodyDef();
			bd.type = BodyType.DYNAMIC;
			bd.position.set(position);
			Body body = curTest.getWorld().createBody(bd);
			if(result.Name.toLowerCase().startsWith("triangle")){
				float bottom=Math.abs(leftTop.x-rightBottom.x)/2.0f;
				float height=Math.abs(leftTop.y-rightBottom.y)/2.0f;
				body.createFixture(createTriangle(-bottom,bottom,height), 0.2f);
			}else if(result.Name.toLowerCase().startsWith("rectangle")){
				body.createFixture(createRectangle(Math.abs(leftTop.x-rightBottom.x)/2.0f,Math.abs(leftTop.y-rightBottom.y)/2.0f), 0.2f);
			}else if(result.Name.toLowerCase().startsWith("circle")){
				float radius=(Math.abs(rightBottom.x-leftTop.x)/2.0f+Math.abs(rightBottom.y-leftTop.y)/2.0f)/2.0f;
				FixtureDef f=new FixtureDef();
				f.shape=createCircle(Math.abs(radius));
				f.density=0.2f;
				f.restitution=0.5f;
				body.createFixture(f);
			}else if(result.Name.toLowerCase().startsWith("x")||result.Name.toLowerCase().startsWith("pigTail")||result.Name.toLowerCase().startsWith("delete")){
				float x1=leftTop.x-position.x;
				float x2=leftTop.y-position.y;
				float y1=rightBottom.x-position.x;
				float y2=rightBottom.y-position.y;
				float midx=(x1+x2)/2.0f;
				float midy=(y1+y2)/2.0f;
				x1-=midx;
				y1-=midy;
				x2-=midx;
				y2-=midy;
				midx=0;
				midy=0;
				float size=0.4f;
				Vec2[] v=new Vec2[8];
				v[0]=new Vec2(x1,y1);
				v[1]=new Vec2(midx-size,midy);
				v[2]=new Vec2(x1,y2);
				v[3]=new Vec2(midx,midy-size);
				v[4]=new Vec2(x2,y2);
				v[5]=new Vec2(midx+size,midy);
				v[6]=new Vec2(x2,y1);
				v[7]=new Vec2(midx,midy+size);
				PolygonShape tri1=new PolygonShape();
				tri1.set(new Vec2[]{v[7],v[0],v[1]}, 3);
				PolygonShape tri2=new PolygonShape();
				tri2.set(new Vec2[]{v[1],v[2],v[3]}, 3);
				PolygonShape tri3=new PolygonShape();
				tri3.set(new Vec2[]{v[3],v[4],v[5]}, 3);
				PolygonShape tri4=new PolygonShape();
				tri4.set(new Vec2[]{v[5],v[6],v[7]}, 3);
				PolygonShape rect=new PolygonShape();
				rect.set(new Vec2[]{v[1],v[3],v[5],v[7]}, 4);
				body.createFixture(tri1,0.2f);
				body.createFixture(tri2,0.2f);
				body.createFixture(tri3,0.2f);
				body.createFixture(tri4,0.2f);
				body.createFixture(rect,0.2f);
			}
			curTest.setBodies(body);
		}
	}
	
	private PolygonShape createRectangle(float w, float h){
		PolygonShape rectangle = new PolygonShape();
		rectangle.setAsBox(w,h);
		return rectangle;
	}
	
	private PolygonShape createTriangle(float x1, float x2, float x3){
		PolygonShape triangle = new PolygonShape();
		Vec2 vertices[] = new Vec2[3];
		vertices[0] = new Vec2(x1, 0.0f);
		vertices[1] = new Vec2(x2, 0.0f);
		vertices[2] = new Vec2(0.0f, x3);
		triangle.set(vertices, 3);
		return triangle;
	}
	
	private CircleShape createCircle(float radius){
		CircleShape circle = new CircleShape();
		circle.m_radius = radius;
		return circle;
	}
}
