package resarcana.graphics.gui;

import resarcana.graphics.Camera;
import resarcana.graphics.Drawable;
import resarcana.graphics.Pollable;

/**
 * Ein Interfaceable ist ein Objekt, was ein Interface beinhalten kann
 * 
 * @author Erik Wagner
 * 
 */
public interface Interfaceable extends Pollable, Drawable, Informable {

	/**
	 * @return Die Breite des dargestellten Bereichs in Pixeln
	 */
	public int getWidth();

	/**
	 * @return Die Höhe des dargestellten Bereichs in Pixeln
	 */
	public int getHeight();

	/**
	 * @return Die Kamera, die die aktuellen "translate-Einstellungen" liefert
	 */
	public Camera getCamera();

	/**
	 * @return Der aktuelle Zoom-Faktor in x-Richtung
	 */
	public float getZoomX();

	/**
	 * @return Der aktuelle Zoom-Faktor in y-Richtung
	 */
	public float getZoomY();

	public boolean isShown(InterfaceContainer interfaceContainer);

	public boolean isMouseBlockedByGUI();

	public void registerMouseBlockedByGUI();
}
