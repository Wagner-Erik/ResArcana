package resarcana.math;

/**
 * Diese Klasse speichert Kreise und ermöglicht Berechnungen damit.
 */
public class Circle implements Shape {

	private static final long serialVersionUID = -4820100555916995691L;

	/**
	 * Der Ortsvektor des Mittelpunkts
	 */
	public final Vector position;

	/**
	 * Der Radius
	 */
	public final float radius;

	@Override
	public boolean doesCollide(Shape other) {
		if (other instanceof Rectangle) {
			Rectangle rect = (Rectangle) other;
			float testX = this.position.x;
			float testY = this.position.y;
			if (testX < rect.x) {
				testX = rect.x;
			} else if (testX > rect.x + rect.width) {
				testX = rect.x + rect.width;
			}
			if (testY < rect.y) {
				testY = rect.y;
			} else if (testY > rect.y + rect.height) {
				testY = rect.y + rect.height;
			}
			float deltaX = this.position.x - testX;
			float deltaY = this.position.y - testY;
			return deltaX * deltaX + deltaY * deltaY <= this.radius * this.radius;
		} else {
			return isCircleCircleCollusion(this, other.getBestCircle());
		}
	}

	@Override
	public Circle getBestCircle() {
		return this;
	}

	@Override
	public Vector getCenter() {
		return this.position;
	}

	private static boolean isCircleCircleCollusion(Circle c1, Circle c2) {
		return c1.position.getDistance(c2.position) < c1.radius + c2.radius;
	}

	/**
	 * @return Das kleinste Rechteck, dass den Kreis vollständig enthält.
	 */
	@Override
	public Rectangle getBoundingRect() {
		return new Rectangle(this.position, this.radius * 2.0f, this.radius * 2.0f);
	}

	private void testRadius(float radius) {
		if (radius < 0) {
			throw new IllegalArgumentException("Circles can't have a negative radius.");
		}
	}

	/**
	 * Erzeugt einen neuen Kreis.
	 * 
	 * @param x      X-Koordinate des Mittelpunkts
	 * @param y      Y-Koordinate des Mittelpunkts
	 * @param radius Radius
	 */
	public Circle(float x, float y, float radius) {
		testRadius(radius);
		this.position = new Vector(x, y);
		this.radius = radius;
	}

	/**
	 * Erzeugt einen neuen Kreis.
	 * 
	 * @param position Ortsvektor des Mittelpunkts
	 * @param radius   Radius
	 */
	public Circle(Vector position, float radius) {
		testRadius(radius);
		this.position = position;
		this.radius = radius;
	}

	public Circle(Vector position, Vector radius) {
		float rad = radius.abs();
		testRadius(rad);
		this.position = position;
		this.radius = rad;
	}

	public Circle(org.newdawn.slick.geom.Circle circle) {
		this(circle.getCenterX(), circle.getCenterY(), circle.getRadius());
	}

	@Override
	public int hashCode() {
		return (int) (this.radius * this.radius + this.position.hashCode());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof Circle) {
			Circle circle = (Circle) object;
			return this.position.equals(circle.position) && this.radius == circle.radius;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "M: " + this.position + " r: " + this.radius;
	}

	@Override
	public float getLeftEnd() {
		return this.position.x - this.radius;
	}

	@Override
	public float getRightEnd() {
		return this.position.x + this.radius;
	}

	@Override
	public boolean isPointInThis(Vector p) {
		return this.getCenter().getDistance(p) < this.radius;
	}

	@Override
	public float getUpperEnd() {
		return this.getCenter().y - this.radius;
	}

	@Override
	public float getLowerEnd() {
		return this.getCenter().y + this.radius;
	}

	@Override
	public Shape modifyCenter(Vector center) {
		return new Circle(center, this.radius);
	}

	@Override
	public org.newdawn.slick.geom.Shape toSlickShape() {
		return new org.newdawn.slick.geom.Circle(this.position.x, this.position.y, this.radius);
	}

	@Override
	public float getDistanceToSide(byte direction) {
		return this.radius;
	}

	@Override
	public float getXRange() {
		return radius * 2;
	}

	@Override
	public float getYRange() {
		return radius * 2;
	}

	@Override
	public Vector getOverlap(PointLine line, Vector pointInOtherShape) {
		Vector overlap = line.getDistanceVectorTo(this.position);
		if (line.arePointsOnTheSameSide(this.position, pointInOtherShape)) {
			return overlap.getDirection().mul(overlap.abs() + this.radius);
		} else {
			return overlap.neg().mul(this.radius / overlap.abs() - 1);
		}
	}

	@Override
	public boolean isIntersecting(PointLine line) {
		Vector dist = line.getDistanceVectorTo(this.position);
		if (dist.abs() > this.radius) {
			return false;
		} else {
			if (!new StraightLine(this.position, this.position.add(dist)).arePointsOnTheSameSide(line.p1, line.p2)) {
				return true;
			} else {
				return line.p1.getDistance(this.position) <= this.radius
						|| line.p2.getDistance(this.position) <= this.radius;
			}
		}
	}

	@Override
	public boolean isCompletlyIn(PointLine line) {
		return this.isPointInThis(line.p1) && this.isPointInThis(line.p2);
	}

	@Override
	public Vector getPositionAtAng(float phi) {
		Vector out = new Vector(0, this.radius);
		return out.rotate(phi).add(this.position);
	}

	@Override
	public Vector getPositionAtOutline(float ratio) {
		return this.getPositionAtAng((float) (2 * Math.PI * ratio));
	}
}
