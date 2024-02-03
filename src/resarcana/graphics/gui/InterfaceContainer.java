/**
 *
 */
package resarcana.graphics.gui;

import java.util.HashMap;
import java.util.LinkedList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;

import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Shape;
import resarcana.math.Vector;

/**
 * Ein Container für InterfaceObjects und andere InterfaceContainer
 * 
 * @author Erik Wagner
 * 
 */
public abstract class InterfaceContainer extends InterfacePart {

	private static final int MAXIME_NOTHING = 0;
	private static final int MAXIME_X = 1;
	private static final int MAXIME_Y = 2;
	private static final int MAXIME_BOTH = 3;

	protected HashMap<InterfacePart, Vector> objects = new HashMap<InterfacePart, Vector>();
	protected LinkedList<InterfacePart> keys = new LinkedList<InterfacePart>();
	private boolean backgroundState = false;
	private Color backgroundColor = Color.black;

	private int toMaximize = MAXIME_NOTHING;
	private String backgroundImage = null;
	private boolean backgroundTexture = false;
	private Rectangle backgroundImageBox;

	protected abstract boolean resize();

	public InterfaceContainer() {
	}

	public void triggerResize() {
		if (!this.resize()) { // triggerResize() not triggered on parent
			if (!this.checkMaximizing()) {// triggerResize() not triggered on parent
				if (this.getParentContainer() != null) {
					this.getParentContainer().updatePosition(); // adjust positions of this container and all contents
				}
			}
		}
	}

	@Override
	public void updatePosition() {
		super.updatePosition();
		if (!this.checkMaximizing()) {
			for (InterfacePart interfacePart : this.keys) {
				interfacePart.updatePosition();
			}
		}
	}

	private float checkSize(float size) {
		if (size <= 0) {
			return Float.NaN;
		} else {
			return size;
		}
	}

	private boolean checkMaximizing() {
		float nx = Float.NaN;
		float ny = Float.NaN;
		Interfaceable inter = this.getInterfaceable();
		if (inter != null) {
			Vector pos = this.getPosition();
			switch (this.toMaximize) {
			case MAXIME_BOTH:
				nx = inter.getWidth() - pos.x;
				ny = inter.getHeight() - pos.y;
				break;
			case MAXIME_X:
				nx = inter.getWidth() - pos.x;
				break;
			case MAXIME_Y:
				ny = inter.getHeight() - pos.y;
				break;
			case MAXIME_NOTHING:
			default:
				return false;
			}
			nx = checkSize(nx);
			ny = checkSize(ny);
		}
		if (Float.isNaN(nx)) {
			nx = this.getHitbox().width;
		}
		if (Float.isNaN(ny)) {
			ny = this.getHitbox().height;
		}
		return this.setHitbox(nx, ny);
	}

	/**
	 * Maximiert die Größe des Containers anhand der ihm zugewiesenen Position und
	 * der Größe des Interfaceables
	 * 
	 * Es wird das Rechteck zwischen Position und unterer rechter Ecke des
	 * Interfaceables als Size gesetzt
	 */
	public void maximizeSize() {
		this.toMaximize = MAXIME_BOTH;
		this.checkMaximizing();
	}

	/**
	 * Maximiert die Größe des Containers auf der X-Achse anhand der ihm
	 * zugewiesenen Position und der Größe des Interfaceables
	 * 
	 * Es wird die maximal verfügbare Breite gewählt und die Höhe über
	 * getWantedSize() ermittelt
	 */
	public void maximizeXRange() {
		switch (this.toMaximize) {
		case MAXIME_BOTH:
			break;
		case MAXIME_X:
			break;
		case MAXIME_Y:
			this.toMaximize = MAXIME_BOTH;
			break;
		case MAXIME_NOTHING:
			this.toMaximize = MAXIME_X;
			break;
		default:
			break;
		}
		this.checkMaximizing();
	}

	/**
	 * Maximiert die Größe des Containers auf der Y-Achse anhand der ihm
	 * zugewiesenen Position und der Größe des Interfaceables
	 * 
	 * Es wird die maximal verfügbare Höhe gewählt und die Breite über
	 * getWantedSize() ermittelt
	 */
	public void maximizeYRange() {
		switch (this.toMaximize) {
		case MAXIME_BOTH:
			break;
		case MAXIME_X:
			this.toMaximize = MAXIME_BOTH;
			break;
		case MAXIME_Y:
			break;
		case MAXIME_NOTHING:
			this.toMaximize = MAXIME_Y;
			break;
		default:
			break;
		}
		this.checkMaximizing();
	}

	public void maximizeNone() {
		this.toMaximize = MAXIME_NOTHING;
		this.checkMaximizing();
	}

	public void setBackgroundImage(String background) {
		this.backgroundImage = background;
		Image image = ResourceManager.getInstance().getImage(background);
		this.backgroundTexture = false;
		this.setBackgroundState(image != null);
		if (this.backgroundState) {
			this.backgroundImageBox = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		}
	}

	public void setBackgroundTexture(String texture) {
		this.backgroundImage = texture;
		Image image = ResourceManager.getInstance().getImage(texture);
		this.backgroundTexture = true;
		this.setBackgroundState(image != null);
		if (this.backgroundState) {
			this.backgroundImageBox = new Rectangle(0, 0, image.getWidth(), image.getHeight());
		}
	}

	public void enableBackground() {
		this.setBackgroundState(true);
	}

	public void disableBackground() {
		this.setBackgroundState(false);
	}

	public void setBackgroundState(boolean state) {
		this.backgroundState = state;
	}

	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}

	public boolean contains(InterfacePart object) {
		return this.objects.containsKey(object);
	}

	@Override
	public Interfaceable getInterfaceable() {
		if (this.getParentContainer() != null) {
			return this.getParentContainer().getInterfaceable();
		} else {
			return null;
		}
	}

	/**
	 * Fügt ein InterfacePart diesem Container hinzu
	 * 
	 * @param adding                   Das hinzuzufügende Objekt
	 * @param relativePositionOnScreen Die linke, obere Ecke des Objekts auf der
	 *                                 Ausgabefläche
	 */
	protected void add(InterfacePart adding, Vector relativePositionOnScreen) {
		if (adding != null) {
			if (!this.keys.contains(adding)) {
				this.objects.put(adding, relativePositionOnScreen);
				this.keys.addFirst(adding);
				adding.setParentContainer(this);
				this.triggerResize();
			}
		}
	}

	public void remove(InterfacePart removing) {
		if (this.contains(removing)) {
			this.objects.remove(removing);
			this.keys.remove(removing);
			this.triggerResize();
		}
	}

	public void clear() {
		this.objects.clear();
		this.keys.clear();
		this.triggerResize();
	}

	private void drawBackground(Graphics g) {
		Shape shape = this.getHitbox();
		shape = shape.modifyCenter(this.getPosition().add(new Vector(shape.getXRange(), shape.getYRange()).div(2.0f)));
		if (this.backgroundImage != null) {
			if (this.backgroundTexture) {
				GraphicUtils.textureImage(g, shape, ResourceManager.getInstance().getImage(this.backgroundImage),
						this.backgroundImageBox, false, false);
			} else {
				GraphicUtils.drawImage(g, shape, ResourceManager.getInstance().getImage(this.backgroundImage));
			}
		} else {
			GraphicUtils.fill(g, shape, this.backgroundColor);
		}
	}

	@Override
	public void draw(Graphics g) {
		// Hintergrund einfärben, wenn gewünscht
		if (this.backgroundState) {
			this.drawBackground(g);

		}

		// Im Container enthaltene Objekte zeichnen
		for (InterfacePart part : this.keys) {
			part.draw(g);
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		super.poll(input, secounds);
		for (int i = this.keys.size() - 1; i >= 0; i--) {
			this.keys.get(i).poll(input, secounds);
		}
	}

	@Override
	public boolean canBlockMouse() {
		return this.backgroundState;
	}

	/**
	 * @param object Das Objekt, dessen Position angefragt wird
	 * @return Position der oberen, linken Ecke des Objekts auf der Oberfläche (ohne
	 *         Translation durch die Kamera); {@code null}, wenn das Objekt nicht in
	 *         diesem Container enthalten ist
	 */
	public Vector getPositionFor(InterfacePart object) {
		Vector pos = this.objects.get(object);
		if (pos != null) {
			return pos.add(this.getPosition());
		} else {
			return this.getPosition();
		}
	}
}
