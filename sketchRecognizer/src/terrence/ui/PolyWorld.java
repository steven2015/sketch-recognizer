package terrence.ui;

import org.jbox2d.callbacks.DebugDraw;
import org.jbox2d.callbacks.QueryCallback;
import org.jbox2d.collision.AABB;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.collision.shapes.Shape;
import org.jbox2d.common.Color3f;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.pooling.IWorldPool;
import org.jbox2d.testbed.framework.TestbedSettings;
import org.jbox2d.testbed.framework.TestbedTest;

/**
 * @author Daniel Murphy
 */
public class PolyWorld extends TestbedTest {
	int k_maxBodies = 256;
	int m_bodyIndex;
	Body m_bodies[] = new Body[k_maxBodies];
	PolygonShape m_polygons[] = new PolygonShape[4];
	CircleShape m_circle;
	
	/**
	 * @see org.jbox2d.testbed.framework.TestbedTest#initTest(boolean)
	 */
	@Override
	public void initTest(boolean argDeserialized) {
		// Ground body
		{
			BodyDef bd = new BodyDef();
			Body ground = getWorld().createBody(bd);
			PolygonShape shape = new PolygonShape();
			shape.setAsEdge(new Vec2(-40.0f, 0.0f), new Vec2(40.0f, 0.0f));
			ground.createFixture(shape, 0.0f);
		}

		{
			Vec2 vertices[] = new Vec2[3];
			vertices[0] = new Vec2(-0.5f, 0.0f);
			vertices[1] = new Vec2(0.5f, 0.0f);
			vertices[2] = new Vec2(0.0f, 1.5f);
			m_polygons[0] = new PolygonShape();
			m_polygons[0].set(vertices, 3);
		}
		
		{
			Vec2 vertices[] = new Vec2[3];
			vertices[0] = new Vec2(-0.1f, 0.0f);
			vertices[1] = new Vec2(0.1f, 0.0f);
			vertices[2] = new Vec2(0.0f, 1.5f);
			m_polygons[1] = new PolygonShape();
			m_polygons[1].set(vertices, 3);
		}

		{
			float w = 1.0f;
			float b = w / (2.0f + MathUtils.sqrt(2.0f));
			float s = MathUtils.sqrt(2.0f) * b;

			Vec2 vertices[] = new Vec2[8];
			vertices[0] = new Vec2(0.5f * s, 0.0f);
			vertices[1] = new Vec2(0.5f * w, b);
			vertices[2] = new Vec2(0.5f * w, b + s);
			vertices[3] = new Vec2(0.5f * s, w);
			vertices[4] = new Vec2(-0.5f * s, w);
			vertices[5] = new Vec2(-0.5f * w, b + s);
			vertices[6] = new Vec2(-0.5f * w, b);
			vertices[7] = new Vec2(-0.5f * s, 0.0f);

			m_polygons[2] = new PolygonShape();
			m_polygons[2].set(vertices, 8);
		}

		{
			m_polygons[3] = new PolygonShape();
			m_polygons[3].setAsBox(0.5f, 0.5f);
		}

		{
			m_circle = new CircleShape();
			m_circle.m_radius = 0.5f;
		}

		m_bodyIndex = 0;
	}
	
	void Create(int index)
	{
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;

		float x = MathUtils.randomFloat(-10.0f, 10.0f);
		bd.position.set(x, 10.0f);
		bd.angle = MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI);

		if (index == 4)
		{
			bd.angularDamping = 0.02f;
		}

		m_bodies[m_bodyIndex] = getWorld().createBody(bd);

		if (index < 4)
		{
			FixtureDef fd = new FixtureDef();
			fd.shape = m_polygons[index];
			fd.density = 1.0f;
			fd.friction = 0.3f;
			m_bodies[m_bodyIndex].createFixture(fd);
		}
		else
		{
			FixtureDef fd = new FixtureDef();
			fd.shape = m_circle;
			fd.density = 1.0f;
			fd.friction = 0.3f;

			m_bodies[m_bodyIndex].createFixture(fd);
		}

		m_bodyIndex = (m_bodyIndex + 1) % k_maxBodies;
	}
	
	public void Create( float positionX, float positionY, Shape shape)
	{
		BodyDef bd = new BodyDef();
		bd.type = BodyType.DYNAMIC;

		bd.position.set(positionX, positionY);
		bd.angle = MathUtils.randomFloat(-MathUtils.PI, MathUtils.PI);

		if(shape.getClass().isInstance(CircleShape.class))
			bd.angularDamping = 0.02f;

		m_bodies[m_bodyIndex] = getWorld().createBody(bd);

		
		FixtureDef fd = new FixtureDef();
		fd.shape = shape;
		fd.density = 1.0f;
		fd.friction = 0.3f;

		m_bodies[m_bodyIndex].createFixture(fd);
		

		m_bodyIndex = (m_bodyIndex + 1) % k_maxBodies;
	}
	
	public void increaseBodyIdx(){
		m_bodyIndex = (m_bodyIndex + 1) % k_maxBodies;
	}
	public Body getBodies(){
		return m_bodies[m_bodyIndex];
	}
	public void setBodies(Body body){
		m_bodies[m_bodyIndex] = body;
		increaseBodyIdx();
	}

	void DestroyBody()
	{
		for (int i = 0; i < k_maxBodies; ++i)
		{
			if (m_bodies[i] != null)
			{
				getWorld().destroyBody(m_bodies[i]);
				m_bodies[i] = null;
				return;
			}
		}
	}
	
	/**
	 * @see org.jbox2d.testbed.framework.TestbedTest#keyPressed(char, int)
	 */
	@Override
	public void keyPressed(char key, int argKeyCode) {
		switch (key)
		{
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
			Create(key - '1');
			break;

		case 'a':
			for (int i = 0; i < k_maxBodies; i += 2)
			{
				if (m_bodies[i] != null)
				{
					boolean active = m_bodies[i].isActive();
					m_bodies[i].setActive(!active);
				}
			}
			break;

		case 'd':
			DestroyBody();
			break;
		}
	}
	
	/**
	 * @see org.jbox2d.testbed.framework.TestbedTest#step(org.jbox2d.testbed.framework.TestbedSettings)
	 */
	@Override
	public void step(TestbedSettings settings) {
		super.step(settings);
		
		PolyShapesCallback callback = new PolyShapesCallback(getWorld().getPool());
		callback.m_circle.m_radius = 2.0f;
		callback.m_circle.m_p.set(0.0f, 2.1f);
		callback.m_transform.setIdentity();
		callback.debugDraw = getDebugDraw();

		AABB aabb = new AABB();
		callback.m_circle.computeAABB(aabb, callback.m_transform);

		getWorld().queryAABB(callback, aabb);

		Color3f color = new Color3f(0.4f, 0.7f, 0.8f);
		getDebugDraw().drawCircle(callback.m_circle.m_p, callback.m_circle.m_radius, color);

//		addTextLine("Press 1-5 to drop stuff");
//		addTextLine("Press 'a' to (de)activate some bodies");
//		addTextLine("Press 'd' to destroy a body");
//		addTextLine("Up to 30 bodies in the target circle are highlighted");
	}
	
	/**
	 * @see org.jbox2d.testbed.framework.TestbedTest#getTestName()
	 */
	@Override
	public String getTestName() {
		return "Physics World";
	}
	
}

/**
 * This callback is called by b2World::QueryAABB. We find all the fixtures
 * that overlap an AABB. Of those, we use b2TestOverlap to determine which fixtures
 * overlap a circle. Up to 30 overlapped fixtures will be highlighted with a yellow border.
 * @author Daniel Murphy
 */

class PolyShapesCallback implements QueryCallback{
	int e_maxCount = 30;
	CircleShape m_circle = new CircleShape();
	Transform m_transform = new Transform();
	DebugDraw debugDraw;
	int m_count;
	IWorldPool p;
	
	public PolyShapesCallback(IWorldPool argWorld){
		m_count = 0;
		p = argWorld;
	}
	
	void DrawFixture(Fixture fixture)
	{
		Color3f color = new Color3f(0.95f, 0.95f, 0.6f);
		final Transform xf = fixture.getBody().getTransform();

		switch (fixture.getType())
		{
		case CIRCLE:
			{
				CircleShape circle = (CircleShape)fixture.getShape();

				Vec2 center = Transform.mul(xf, circle.m_p);
				float radius = circle.m_radius;

				debugDraw.drawCircle(center, radius, color);
			}
			break;

		case POLYGON:
			{
				PolygonShape poly = (PolygonShape)fixture.getShape();
				int vertexCount = poly.m_vertexCount;
				assert(vertexCount <= Settings.maxPolygonVertices);
				Vec2 vertices[] = new Vec2[Settings.maxPolygonVertices];

				for (int i = 0; i < vertexCount; ++i)
				{
					vertices[i] = Transform.mul(xf, poly.m_vertices[i]);
				}

				debugDraw.drawPolygon(vertices, vertexCount, color);
			}
			break;
		}
	}
	
	public boolean reportFixture(Fixture fixture)
	{
		if (m_count == e_maxCount)
		{
			return false;
		}

		Body body = fixture.getBody();
		Shape shape = fixture.getShape();

		boolean overlap = p.getCollision().testOverlap(shape, m_circle, body.getTransform(), m_transform);

		if (overlap)
		{
			DrawFixture(fixture);
			++m_count;
		}

		return true;
	}
}
