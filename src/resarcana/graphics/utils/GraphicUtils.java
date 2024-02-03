package resarcana.graphics.utils;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;

import resarcana.math.Circle;
import resarcana.math.PointLine;
import resarcana.math.Rectangle;
import resarcana.math.Shape;
import resarcana.math.Vector;

/**
 * Zeichnet Objekte in einen OpenGL Grafikkontext.
 * 
 */
public class GraphicUtils {

	public static final float DISTORTION_TOLERANCE = 1.01f; // 1 + x, where x is the relative tolerance

	/**
	 * Zeichnet einen Kreis.
	 * 
	 * @param g      Grafikkontext
	 * @param circle Kreis
	 */
	public static void draw(Graphics g, Circle circle) {
		Rectangle rect = circle.getBoundingRect();
		g.drawOval(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Zeichnet ein Rechteck.
	 * 
	 * @param g    Grafikkontext
	 * @param rect Rechteck
	 */
	public static void draw(Graphics g, Rectangle rect) {
		g.drawRect(rect.x, rect.y, rect.width, rect.height);
	}

	/**
	 * Zeichnet eine Figur.
	 * 
	 * @param g     Grafikkontext
	 * @param shape Figur
	 */
	public static void draw(Graphics g, Shape shape) {
		if (shape instanceof Circle) {
			draw(g, (Circle) shape);
		} else if (shape instanceof Rectangle) {
			draw(g, (Rectangle) shape);
		}
	}

	/**
	 * Zeichnet eine Figur in einer bestimmten Farbe;
	 * 
	 * @param g     Der Grafikkontext
	 * @param shape Die Figur
	 * @param color Die Farbe
	 */
	public static void draw(Graphics g, Shape shape, Color color) {
		Color c = g.getColor();
		g.setColor(color);
		draw(g, shape);
		g.setColor(c);
	}

	/**
	 * Zeichnet eine Punktlinie in einer bestimmten Farbe
	 * 
	 * @param g     Der Grafikkontext
	 * @param line  Die Punktline
	 * @param color Die Farbe
	 */
	public static void draw(Graphics g, PointLine line, Color color) {
		Color c = g.getColor();
		g.setColor(color);
		g.drawLine(line.p1.x, line.p1.y, line.p2.x, line.p2.y);
		g.setColor(c);
	}

	public static void drawScaled(Graphics g, Shape shape, float zoom) {
		if (shape instanceof Circle) {
			drawScaled(g, (Circle) shape, zoom);
		} else if (shape instanceof Rectangle) {
			drawScaled(g, (Rectangle) shape, zoom);
		}
	}

	public static void drawScaled(Graphics g, Rectangle rect, float zoom) {
		Rectangle rect2 = new Rectangle(rect.getCenter(),
				new Vector(rect.width / 2.0f * zoom, rect.height / 2.0f * zoom));
		draw(g, rect2);
	}

	public static void drawScaled(Graphics g, Circle circle, float zoom) {
		draw(g, new Circle(circle.getCenter(), circle.radius * zoom));
	}

	/**
	 * Zeichnet einen Vektor ausgehend vom Ursprung.
	 * 
	 * @param g      Grafikkontext
	 * @param vector Vektor
	 */
	public static void draw(Graphics g, Vector vector) {
		g.drawLine(0, 0, vector.x, vector.y);
	}

	/**
	 * Zeichnet einen Vektor ausgehend von einem Orsvektor.
	 * 
	 * @param g        Grafikkontext
	 * @param position Ortsvektor
	 * @param vector   Vektor
	 */
	public static void draw(Graphics g, Vector position, Vector vector) {
		g.drawLine(position.x, position.y, position.x + vector.x, position.y + vector.y);
	}

	/**
	 * Zeichnet eine gefüllte Fläche anhand eines Shapes
	 * 
	 * @param g     Grafikkontext
	 * @param shape Das zuzeichnende Shape
	 */
	public static void fill(Graphics g, Shape shape) {
		g.fill(shape.toSlickShape());
	}

	/**
	 * Zeichnet eine gefüllte Fläche anhand eines Shapes in einer bestimmten Farbe
	 * 
	 * @param g     Grafikkontext
	 * @param shape Shape
	 * @param c     Die Farbe mit der das Shape gefüllt wird
	 */
	public static void fill(Graphics g, Shape shape, Color c) {
		Color cur = g.getColor();
		g.setColor(c);
		fill(g, shape);
		g.setColor(cur);
	}

	/**
	 * Zeichnet eine Textur
	 */
	public static void texture(Graphics g, Shape shape, Image image, boolean fit) {
		g.texture(shape.toSlickShape(), image, fit);
	}

	/**
	 * Tiles a shape with an Image The shape is handled like as its containing
	 * rectangle
	 * 
	 * @param g        Graphics to draw on
	 * @param shape    Shape to tile over
	 * @param image    Image to tile
	 * @param streched wheter the image shall be streched (TRUE) or cut (FALSE) at
	 *                 the lower and right edge of the shape
	 */
	public static void textureImage(Graphics g, Shape shape, Image image, Rectangle tileBox, boolean strechedX,
			boolean strechedY) {
		float w = image.getWidth(), h = image.getHeight(), x0 = shape.getLeftEnd(), y0 = shape.getUpperEnd();
		float tw = tileBox.width, th = tileBox.height;
		int nx = (int) (shape.getXRange() / tw), ny = (int) (shape.getYRange() / th);
		float dw = ((shape.getXRange() / tw) - nx) * w, dh = ((shape.getYRange() / th) - ny) * h;
		if (strechedX) {
			if (nx == 0) {
				nx = 1;
			}
			tw = shape.getXRange() / nx;
			dw = 0;
		}
		if (strechedY) {
			if (ny == 0) {
				ny = 1;
			}
			th = shape.getYRange() / ny;
			dh = 0;
		}
		int i, j;
		for (i = 0; i < nx; i++) {
			for (j = 0; j < ny; j++) {
				g.drawImage(image, x0 + i * tw - 1, y0 + j * th - 1, x0 + (i + 1) * tw + 1, y0 + (j + 1) * th + 1, 0, 0,
						w, h);
			}
			if (!strechedY && dh > 1) {
				g.drawImage(image, x0 + i * tw - 1, y0 + j * th - 1, x0 + (i + 1) * tw + 1, shape.getLowerEnd() + 1, 0,
						0, w, dh);
			}
		}
		if (!strechedX && dw > 1) {
			for (j = 0; j < ny; j++) {
				g.drawImage(image, x0 + i * tw - 1, y0 + j * th - 1, shape.getRightEnd() + 1, y0 + (j + 1) * th + 1, 0,
						0, dw, h);
			}
			if (!strechedY && dh > 1) {
				g.drawImage(image, x0 + i * tw, y0 + j * th, shape.getRightEnd(), shape.getLowerEnd(), 0, 0, dw, dh);
			}
		}
	}

	/**
	 * Zeichnet einen String
	 * 
	 * @param position Obere linke Ecke des Textes
	 */
	public static void drawString(Graphics g, Vector position, String text) {
		FontManager.getInstance().drawString(g, position, text);
	}

	/**
	 * Zeichnet einen String mit Farbe color
	 * 
	 * @param position Obere linke Ecke des Textes
	 */
	public static void drawString(Graphics g, Vector position, String text, Color color) {
		FontManager.getInstance().drawString(g, position, text, color);
	}

	/**
	 * Zeichnet einen String zentriert auf der Position
	 * 
	 * @param position Center of String
	 */
	public static void drawStringCentered(Graphics g, Vector position, String text) {
		FontManager.getInstance().drawStringCentered(g, position, text);
	}

	/**
	 * Zeichnet einen String zentriert auf der Position mit Farbe color
	 * 
	 * @param position Center of String
	 */
	public static void drawStringCentered(Graphics g, Vector position, String text, Color color) {
		FontManager.getInstance().drawStringCentered(g, position, text, color);
	}

	/**
	 * Zeichnet ein Bild in ein Shape
	 * 
	 * ACHTUNG: Das Bild wird bei einem unpassenden Shape verzerrt (z.B.
	 * quadratisches Bild in einem länglichen Rechteck) bzw. über die Grenzen hinaus
	 * gezeichnet (z.B. rechteckiges Bild in einem Kreis)
	 * 
	 * @param shape Das Shape
	 * @param image Das Bild
	 */
	public static void drawImage(Graphics g, Shape shape, Image image) {
		g.drawImage(image, shape.getLeftEnd(), shape.getUpperEnd(), shape.getRightEnd(), shape.getLowerEnd(), 0, 0,
				image.getWidth(), image.getHeight());
	}

	public static void drawImage(Graphics g, Shape shape, Image image, Color color) {
		g.drawImage(image, shape.getLeftEnd(), shape.getUpperEnd(), shape.getRightEnd(), shape.getLowerEnd(), 0, 0,
				image.getWidth(), image.getHeight(), color);
	}

	public static void drawImage(Graphics g, Shape shape, Image image, Color color, float paddingX, float paddingY) {
		g.drawImage(image, shape.getLeftEnd() - paddingX, shape.getUpperEnd() - paddingY,
				shape.getRightEnd() + paddingX, shape.getLowerEnd() + paddingY, 0, 0, image.getWidth(),
				image.getHeight(), color);
	}

	private static Rectangle getUndistortedDrawingRectangle(Shape shape, Image image) {
		float sw = shape.getXRange(), sh = shape.getYRange();
		float sr = sw / sh, ir = ((float) image.getWidth()) / image.getHeight();
		if (sr > ir * DISTORTION_TOLERANCE) { // shape is wider than image
			return new Rectangle(shape.getCenter(), sh * ir, sh);
		} else if (sr * DISTORTION_TOLERANCE < ir) { // shape is taller than image
			return new Rectangle(shape.getCenter(), sw, sw / ir);
		} else { // shape fits image within DISTORTION_TOLERANCE
			return shape.getBoundingRect();
		}
	}

	public static void drawImageUndistorted(Graphics g, Shape shape, Image image) {
		GraphicUtils.drawImage(g, getUndistortedDrawingRectangle(shape, image), image);
	}

	public static void drawImageUndistorted(Graphics g, Shape shape, Image image, Color color) {
		GraphicUtils.drawImage(g, getUndistortedDrawingRectangle(shape, image), image, color);
	}

	public static void drawImageWarped(Vector tl, Vector tr, Vector br, Vector bl, Image image) {
		image.drawWarped(tl.x, tl.y, bl.x, bl.y, br.x, br.y, tr.x, tr.y);
	}

	/**
	 * Markiert eine Position durch ein Kreuz
	 * 
	 * @param g        Der Grafikkontext
	 * @param position Die Position, die markiert werden soll
	 * @param distance Die Größe des Kreuzes, mit dem die Position markiert wird
	 *                 (Länge des Kreuzes vom Zentrum in alle Richtungen)
	 */
	public static void markPosition(Graphics g, Vector position, float distance, Color c) {
		Color save = g.getColor();
		g.setColor(c);
		g.drawLine(position.x - distance, position.y, position.x + distance, position.y);
		g.drawLine(position.x, position.y - distance, position.x, position.y + distance);
		g.setColor(save);
	}

	public static void drawLine(Graphics g, Vector vector, Vector vector2) {
		g.drawLine(vector.x, vector.y, vector2.x, vector2.y);
	}

	public static void drawSplittedImage(Graphics g, Shape shape, Shape rawShape, Image image) {
		float hwRaw = rawShape.getXRange() / 2, hwImage = image.getWidth() / 2;
		float hhRaw = rawShape.getYRange() / 2, hhImage = image.getHeight() / 2;
		g.drawImage(image, shape.getLeftEnd(), shape.getUpperEnd(), shape.getLeftEnd() + hwRaw,
				shape.getUpperEnd() + hhRaw, 0, 0, hwImage, hhImage);
		g.drawImage(image, shape.getRightEnd() - hwRaw, shape.getUpperEnd(), shape.getRightEnd(),
				shape.getUpperEnd() + hhRaw, hwImage, 0, 2 * hwImage, hhImage);
		g.drawImage(image, shape.getLeftEnd(), shape.getLowerEnd() - hhRaw, shape.getLeftEnd() + hwRaw,
				shape.getLowerEnd(), 0, hhImage, hwImage, 2 * hhImage);
		g.drawImage(image, shape.getRightEnd() - hwRaw, shape.getLowerEnd() - hhRaw, shape.getRightEnd(),
				shape.getLowerEnd(), hwImage, hhImage, 2 * hwImage, 2 * hhImage);
	}

	public static void translate(Graphics g, Vector pos) {
		g.translate(pos.x, pos.y);
	}
}
