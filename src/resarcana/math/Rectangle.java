package resarcana.math;

/**
 * Diese Klasse speichert Rechtecke und ermöglicht Berechnungen damit.
 */
public class Rectangle implements Shape {

	private static final long serialVersionUID = -4556824470497571683L;

	/**
	 * Die X-Koordinate der oberen linken Ecke
	 */
	public final float x;

	/**
	 * Die Y-Koordinate der oberen linken Ecke
	 */
	public final float y;

	/**
	 * Die Breite
	 */
	public final float width;

	/**
	 * Die Höhe
	 */
	public final float height;

	/**
	 * The center of rectangle
	 */
	public final Vector center;

	private Vector tlc, trc, blc, brc;

	private void testMetrics(float width, float height) {
		if (width == 0 || height == 0) {
			throw new IllegalArgumentException("All rectangles must have width and height.");
		}
	}

	/**
	 * <p>
	 * Erzeugt ein neues Rechteck.
	 * </p>
	 * 
	 * <p>
	 * Wird eine negative Breite oder Höhe übergeben, werden die Parameter so neu
	 * bestimmt, dass x|y die obere linke Ecke bleibt und Breite und Höhe positiv
	 * sind.
	 * </p>
	 * 
	 * @param x      Die X-Koordinate der oberen linken Ecke
	 * @param y      Die Y-Koordinate der oberen linken Ecke
	 * @param width  Die Breite
	 * @param height Die Höhe
	 * 
	 * @throws IllegalArgumentException Wenn Breite oder Höhe {@code 0} sind.
	 */
	public Rectangle(float x, float y, float width, float height) {
		testMetrics(width, height);
		if (width < 0) {
			x += width;
			width = -width;
		}
		if (height < 0) {
			y += height;
			height = -height;
		}
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.center = new Vector(x + width / 2.0f, y + height / 2.0f);
	}

	/**
	 * <p>
	 * Erzeugt ein neues Rechteck.
	 * </p>
	 * 
	 * <p>
	 * Zwischen positiven und negativen Breiten- und Höhenangaben wird nicht
	 * unterschieden.
	 * </p>
	 * 
	 * @param center Ortsvektor des Mittelpunktes
	 * @param width  Breite
	 * @param height Höhe
	 * 
	 * @throws IllegalArgumentException Wenn Breite oder Höhe {@code 0} sind.
	 */
	public Rectangle(Vector center, float width, float height) {
		testMetrics(width, height);
		width = Math.abs(width);
		height = Math.abs(height);
		this.x = center.x - width / 2.0f;
		this.y = center.y - height / 2.0f;
		this.width = width;
		this.height = height;
		this.center = center;
	}

	/**
	 * @see #Rectangle(Vector, float, float)
	 * 
	 * @param center         Ortsvektor des Mittelpunkts
	 * @param centerToCorner Vektor, der vom Zentrum auf eine Ecke des Rechtecks
	 *                       zeigt
	 */
	public Rectangle(Vector center, Vector centerToCorner) {
		this(center, centerToCorner.x * 2, centerToCorner.y * 2);
	}

	public Rectangle(org.newdawn.slick.geom.Rectangle rect) {
		this(new Vector(rect.getCenterX(), rect.getCenterY()), rect.getWidth(), rect.getHeight());
	}

	/**
	 * @return Die Fläche des Rechtecks
	 */
	public float getArea() {
		return this.width * this.height;
	}

	@Override
	public Vector getCenter() {
		return this.center;
	}

	@Override
	public Circle getBestCircle() {
		return new Circle(this.getCenter(), (this.width + this.height) / 4.0f);
	}

	/**
	 * @return Der Umkreis des Rechtecks. Alle vier Ecken liegen auf dem Kreisrand.
	 */
	public Circle getBoundingCircle() {
		return new Circle(this.getCenter(),
				0.5f * (float) Math.sqrt(this.width * this.width + this.height * this.height));
	}

	/**
	 * @return Der Ortsvektor der unteren rechten Ecke
	 */
	public Vector getLowerRightCorner() {
		return this.getBottomRightCorner();
	}

	/**
	 * @param other Ein zweites Rechteck
	 * @return Das kleinste Rechteck das beide Rechtecke vollständig enthält.
	 */
	public Rectangle getBoundingRectangle(Rectangle other) {
		// Obere linke Ecke
		float left = Math.min(this.x, other.x);
		float top = Math.min(this.y, other.y);

		// Untere Rechte Ecke
		Vector thisCorner = getLowerRightCorner();
		Vector thatCorner = other.getLowerRightCorner();
		float right = Math.max(thisCorner.x, thatCorner.x);
		float bottom = Math.max(thisCorner.y, thatCorner.y);

		// Rechteck erzeugen
		return new Rectangle(left, top, right - left, bottom - top);
	}

	@Override
	public boolean doesCollide(Shape shape) {
		if (shape instanceof Rectangle) {
			// http://www.back-side.net/codingrects.html
			Rectangle other = (Rectangle) shape;
			Rectangle bounding = other.getBoundingRectangle(this);
			return bounding.width <= this.width + other.width && bounding.height <= this.height + other.height;
		} else {
			return shape.getBestCircle().doesCollide(this);
		}
	}

	@Override
	public int hashCode() {
		return (int) (this.getArea() + this.getCenter().hashCode());
	}

	@Override
	public boolean equals(Object object) {
		if (object != null && object instanceof Rectangle) {
			Rectangle rectangle = (Rectangle) object;
			return rectangle.x == this.x && rectangle.y == this.y && rectangle.width == this.width
					&& rectangle.height == this.height;
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return "(" + this.x + ", " + this.y + "): " + this.width + " x " + this.height;
	}

	@Override
	public float getLeftEnd() {
		return this.x;
	}

	@Override
	public float getRightEnd() {
		return this.x + this.width;
	}

	public Vector getTopLeftCorner() {
		if (this.tlc == null) {
			tlc = this.getCenter().add(new Vector(-this.width / 2.0f, -this.height / 2.0f));
		}
		return this.tlc;
	}

	public Vector getTopRightCorner() {
		if (this.trc == null) {
			trc = this.getCenter().add(new Vector(this.width / 2.0f, -this.height / 2.0f));
		}
		return this.trc;
	}

	public Vector getBottomLeftCorner() {
		if (this.blc == null) {
			blc = this.getCenter().add(new Vector(-this.width / 2.0f, this.height / 2.0f));
		}
		return this.blc;

	}

	public Vector getBottomRightCorner() {
		if (this.brc == null) {
			brc = this.getCenter().add(new Vector(this.width / 2.0f, this.height / 2.0f));
		}
		return this.brc;

	}

	@Override
	public boolean isPointInThis(Vector p) {
		if (p.x < this.x || p.y < this.y || p.x > this.x + this.width || p.y > this.y + this.height) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	public float getUpperEnd() {
		return this.getTopLeftCorner().y;
	}

	@Override
	public float getLowerEnd() {
		return this.getBottomLeftCorner().y;
	}

	@Override
	public Rectangle modifyCenter(Vector center) {
		return new Rectangle(center, this.width, this.height);
	}

	public Rectangle modifyCenter(float cx, float cy) {
		return this.modifyCenter(new Vector(cx, cy));
	}

	public Rectangle modifyCorner(float x, float y) {
		return new Rectangle(x, y, this.width, this.height);
	}

	@Override
	public org.newdawn.slick.geom.Shape toSlickShape() {
		return new org.newdawn.slick.geom.Rectangle(this.x, this.y, this.width, this.height);
	}

	@Override
	public float getDistanceToSide(byte direction) {
		switch (direction) {
		case Shape.UP:
		case Shape.DOWN:
			return this.height / 2.0f;
		case Shape.RIGHT:
		case Shape.LEFT:
			return this.width / 2.0f;
		default:
			return 0;
		}
	}

	@Override
	public float getXRange() {
		return width;
	}

	@Override
	public float getYRange() {
		return height;
	}

	public Vector getCorner(Vector direction) {
		if (direction.x > 0) {
			if (direction.y > 0) {
				return this.getBottomRightCorner();
			} else {
				return this.getTopRightCorner();
			}
		} else {
			if (direction.y > 0) {
				return this.getBottomLeftCorner();
			} else {
				return this.getTopLeftCorner();
			}
		}
	}

	@Override
	public Vector getOverlap(PointLine line, Vector pointInOtherShape) {
		Vector dir = line.getDistanceVectorTo(this.getCenter());
		if (!line.arePointsOnTheSameSide(this.getCenter(), pointInOtherShape)) {
			dir = dir.neg();
		}
		return line.getDistanceVectorTo(this.getCorner(dir));
	}

	@Override
	public boolean isIntersecting(PointLine line) {
		if (line.crosses(new PointLine(this.getTopLeftCorner(), this.getTopRightCorner()))) {
			return true;
		} else if (line.crosses(new PointLine(this.getTopRightCorner(), this.getBottomRightCorner()))) {
			return true;
		} else if (line.crosses(new PointLine(this.getBottomRightCorner(), this.getBottomLeftCorner()))) {
			return true;
		} else if (line.crosses(new PointLine(this.getBottomLeftCorner(), this.getTopLeftCorner()))) {
			return true;
		} else {
			return this.isPointInThis(line.p1);
		}
	}

	@Override
	public boolean isCompletlyIn(PointLine line) {
		return this.isPointInThis(line.p1) && this.isPointInThis(line.p2);
	}

	@Override
	public Rectangle getBoundingRect() {
		return this;
	}

	/**
	 * Scales the width and height of this Rectangle by some factor while keeping
	 * the center unmodified
	 * 
	 * @param scaleFactor the scaling factor
	 * @return the scaled {@link Rectangle}
	 */
	public Rectangle scale(float scaleFactor) {
		return new Rectangle(this.getCenter(), this.width * scaleFactor, this.height * scaleFactor);
	}

	public Rectangle addToWidthAndHeight(float dw, float dh) {
		return new Rectangle(this.getCenter(), this.width + dw, this.height + dh);
	}

	public Rectangle moveBy(float dx, float dy) {
		return new Rectangle(this.x + dx, this.y + dy, this.width, this.height);
	}

	public Rectangle moveBy(Vector delta) {
		return new Rectangle(this.x + delta.x, this.y + delta.y, this.width, this.height);
	}

	public Rectangle scaleWithCenter(float scaleFactor) {
		return new Rectangle(this.getCenter().mul(scaleFactor), this.width * scaleFactor, this.height * scaleFactor);
	}

	@Override
	public Vector getPositionAtAng(float phi) {
		phi = (float) (phi % (2 * Math.PI));
		if (phi < 0) {
			phi += 2 * Math.PI;
		}
		float px = this.height / 2 * (float) Math.tan(phi);
		float py = -this.width / 2 / (float) Math.tan(phi);
		if (phi > Math.PI / 2 && phi < Math.PI * 3 / 2) {
			px = -px;
		}
		if (phi > Math.PI) {
			py = -py;
		}
		px = px > this.width / 2 ? this.width / 2 : (px < -this.width / 2 ? -this.width / 2 : px);
		py = py > this.height / 2 ? this.height / 2 : (py < -this.height / 2 ? -this.height / 2 : py);
		return this.center.add(px, py);
	}

	@Override
	public Vector getPositionAtOutline(float ratio) {
		ratio = ratio % 1.0f;
		if (ratio < 0) {
			ratio += 1;
		}
		ratio = (this.width + this.height) * 2 * ratio;
		float dx = Math.min(ratio, this.width);
		ratio -= dx;
		if (ratio < 0.1f) {
			return new Vector(this.x + dx, this.y);
		}
		float dy = Math.min(ratio, this.height);
		ratio -= dy;
		if (ratio < 0.1f) {
			return new Vector(this.x + this.width, this.y + dy);
		}
		dx = Math.min(ratio, this.width);
		ratio -= dx;
		dx = this.width - dx;
		if (ratio < 0.1f) {
			return new Vector(this.x + dx, this.y + this.height);
		}
		dy = this.height - ratio;
		return new Vector(this.x, this.y + dy);
	}
}
