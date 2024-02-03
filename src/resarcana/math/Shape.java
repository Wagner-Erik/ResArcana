package resarcana.math;

import java.io.Serializable;

/**
 * Allgemeine Klasse für eine geometrische Figur.
 * 
 * @author Niklas Fiekas
 */
public interface Shape extends Serializable {
	/**
	 * <p>
	 * Prüft, ob sich die geometrische Figuren überlappen. Eine Berührung ist keine
	 * Kollision.
	 * </p>
	 * 
	 * <p>
	 * Anforderungen an die Funktion:
	 * </p>
	 * <ul>
	 * <li><strong>Symmetrie:</strong> {@code a.doesCollide(b) ==
	 * b.doesCollide(a)}</li>
	 * <li>Sollte möglichst viele andere Figuren kennen. Falls nicht, steht noch
	 * {@link #getBestCircle()} für eine Näherung zur Verfügung.</li>
	 * </ul>
	 * 
	 * @param other Die andere Figur
	 */
	public boolean doesCollide(Shape other);

	/**
	 * Prüft, ob die Linie durch die geometrische Figur verläuft
	 * 
	 * @param line Die Linie, die überprüft werden soll
	 */
	public boolean isIntersecting(PointLine line);

	public boolean isCompletlyIn(PointLine line);

	/**
	 * @return Ein Kreis, der die Figur möglichst gut annähert.
	 */
	public Circle getBestCircle();

	/**
	 * @return Ein Recteck, das die Figur umschließt
	 */
	public Rectangle getBoundingRect();

	/**
	 * @return Der Ortsvektor des Mittelpunkts.
	 */
	public Vector getCenter();

	/**
	 * @return Die X-Koordinate des linken Endes des Objekts
	 */
	public float getLeftEnd();

	/**
	 * @return Die X-Koordinate des rechten Endes des Objekts
	 */
	public float getRightEnd();

	/**
	 * @return Die Y-Koordinate des oberen Endes des Objekts
	 */
	public float getUpperEnd();

	/**
	 * @return Die Y-Koordinate des unteren Endes des Objekts
	 */
	public float getLowerEnd();

	/**
	 * @param direction Die Seite
	 * @return Die Entfernung vom Zentrum des Objekts zu der entsprechenden Seite
	 */
	public float getDistanceToSide(byte direction);

	/**
	 * Ermittelt die Überlappung dieser geometrischen Figur zu der Linie einer
	 * anderen geometrischen Figur
	 * 
	 * @param line              Die zu überprüfende Punkt-zu-Punkt-Linie
	 * @param pointInOtherShape Ein Punkt in der anderen Figur, der charakterisiert,
	 *                          auf welcher Seite der Linie sich die geometrische
	 *                          Figur, zu welcher die Linie gehört, befindet
	 * @return Der Überlappungsvektor, senkrect zu <code>line</code>, zeigt zur
	 *         anderen Figur hin
	 */
	public Vector getOverlap(PointLine line, Vector pointInOtherShape);

	/**
	 * 
	 * @param p Der Punkt in Form eines Ortvektors.
	 * @return {@code true}, wenn der Punkt sich in der Figur befindet.
	 */
	public boolean isPointInThis(Vector p);

	/**
	 * @param center Der neue Ortsvektor des Mittelpunktes.
	 * @return Eine neue Figur, die sich nur durch den Mittelpunkt von dieser
	 *         unterscheidet. Die Figur selbst wird nicht verändert.
	 */
	public Shape modifyCenter(Vector center);

	/**
	 * @return Eine Slick Version dieser Form.
	 */
	public org.newdawn.slick.geom.Shape toSlickShape();

	/**
	 * @return Die Distanz, die das Objekt in x-Richtung einschließt (vom linken
	 *         Ende bis zum rechten Ende)
	 */
	public float getXRange();

	/**
	 * @return Die Distanz, die das Objekt in y-Richtung einschließt (vom oberen
	 *         Ende bis zum unteren Ende)
	 */
	public float getYRange();

	/**
	 * Get the {@link Vector} to the position at (clockwise) angle phi of the Shape
	 * 
	 * @param phi clockwise angle to {@link Vector#UP} direction
	 * @return the position at the shape
	 */
	public Vector getPositionAtAng(float phi);

	/**
	 * Get the {@link Vector} to the position at the ratio of the outline of the
	 * Shape
	 * 
	 * @param ratio the ratio of the outline at which to get the position, between 0
	 *              and 1
	 * @return the position at the outline of the Shape
	 */
	public Vector getPositionAtOutline(float ratio);

	/*
	 * Konstanten, die die Seiten bezeichnen
	 * 
	 * So beschrieben, dass der negierte Wert die gegenüberliegende Seite bezeichnet
	 */
	/**
	 * Konstante, die die obere Seite des Objekts bezeichnet
	 */
	public final static byte UP = 1;
	/**
	 * Konstante, die die rechte Seite des Objekts bezeichnet
	 */
	public final static byte RIGHT = 2;
	/**
	 * Konstante, die die links Seite des Objekts bezeichnet
	 */
	public final static byte LEFT = -RIGHT;
	/**
	 * Konstante, die die untere Seite des Objekts bezeichnet
	 */
	public final static byte DOWN = -UP;

	/*
	 * Konstanten, die die Ecken bezeichnen
	 * 
	 * So beschrieben, dass der negierte Wert die gegenüberliegende Ecke bezeichnet
	 */
	/**
	 * Konstante, die die obere, rechte Ecke des Objekts bezeichnet
	 */
	public final static byte UP_RIGHT = 3;
	/**
	 * Konstante, die die untere, rechte Ecke des Objekts bezeichnet
	 */
	public final static byte DOWN_RIGHT = 4;
	/**
	 * Konstante, die die obere, linke Ecke des Objekts bezeichnet
	 */
	public final static byte UP_LEFT = -DOWN_RIGHT;
	/**
	 * Konstante, die die untere, linke Ecke des Objekts bezeichnet
	 */
	public final static byte DOWN_LEFT = -UP_RIGHT;

	/*
	 * Konstante, die bei einem Fehler ausgegeben wird (wenn die Berechnung nicht
	 * greift)
	 */
	public final static byte NULL = 0;
}
