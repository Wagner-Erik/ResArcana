package resarcana.graphics;

import org.newdawn.slick.Graphics;

/**
 * Schnittstelle, für alle Objekte, die gezeichnet werden können.
 * 
 */
public interface Drawable {

	/**
	 * Wird aufgerufen, wenn das Objekt gezeichnet werden soll.
	 * 
	 * @param g Grafikkontext
	 */
	public void draw(Graphics g);
}
