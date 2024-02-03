package resarcana.graphics;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

public abstract class AbstractState extends BasicGameState implements Pollable, Drawable {

	private static int id = 0;

	private final int ID;

	public AbstractState() {
		this.ID = id++;
	}

	public static final float DEFAULT_ZOOM = 1;

	protected float zoomX = DEFAULT_ZOOM, zoomY = DEFAULT_ZOOM;

	private int frame = 0;

	/**
	 * Legt die Zoom Faktoren zur Umrechnung von virtuellen Koordinaten in
	 * Pixelkoordinaten fest.
	 * 
	 * @param zoomX Faktor für die X-Achse
	 * @param zoomY Faktor für die Y-Achse
	 */
	public void setZoom(float zoomX, float zoomY) {
		this.zoomX = zoomX;
		this.zoomY = zoomY;
	}

	/**
	 * Legt einen einheitlichen Zoomfaktor für die beiden Achsen fest.
	 * 
	 * @see #setZoom(float, float)
	 * 
	 * @param zoom Faktor
	 */
	public void setZoom(float zoom) {
		setZoom(zoom, zoom);
	}

	/**
	 * @return Der Zoomfaktor zur Umrechnung von virtuellen Koordinaten in
	 *         Pixelkoordinaten auf der X-Achse.
	 */
	public float getZoomX() {
		return zoomX;
	}

	/**
	 * @return Der Zoomfaktor zur Umrechnung von virtuellen Koordinaten in
	 *         Pixelkoordinaten auf der Y-Achse.
	 */
	public float getZoomY() {
		return zoomY;
	}

	@Override
	public final int getID() {
		return this.ID;
	}

	@Override
	public int hashCode() {
		return getID();
	}

	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof AbstractState && hashCode() == object.hashCode();
	}

	@Override
	public final void render(GameContainer container, StateBasedGame game, Graphics g) {
		g.pushTransform();
		draw(g);
		g.popTransform();
	}

	@Override
	public final void update(GameContainer container, StateBasedGame game, int delta) {
		this.frame++;
		poll(container.getInput(), delta / 1000.0f);
	}

	public int getFrameNumber() {
		return this.frame;
	}
}
