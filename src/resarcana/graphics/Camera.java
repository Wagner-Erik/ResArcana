package resarcana.graphics;

import java.io.Serializable;

import org.newdawn.slick.Graphics;

import resarcana.math.Vector;

/**
 * Wird implementiert um Kameraeinstellungen zu erstellen.
 * 
 */
public interface Camera extends Serializable {

	/**
	 * @return Gibt den Ortsvektor des Mittelpunkts des gewünschten Blickfeldes
	 *         zurück.
	 */
	public Vector getPosition();

	/**
	 * @return Gibt den Zoom-Faktor zurück
	 */
	public float getZoom();
	
	/**
	 * Apply the transformation of this camera to a {@link Graphics} context
	 * @param g the graphics context
	 */
	public void applyCamera(Graphics g);
}
