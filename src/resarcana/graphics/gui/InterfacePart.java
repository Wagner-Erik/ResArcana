/**
 *
 */
package resarcana.graphics.gui;

import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.graphics.Drawable;
import resarcana.graphics.Engine;
import resarcana.graphics.Pollable;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

/**
 * Interface wird von allem Klassen implementiert, die zum Interface gehören
 * 
 * @author Erik Wagner
 * 
 */
public abstract class InterfacePart implements Drawable, Pollable {

	/**
	 * @return Das Interfaceable, dem dieser Container zugeordnet ist; {@code null},
	 *         wenn es kein solches Interfaceable gibt, weil dieser
	 *         InterfaceContainer weder dirket einem Interfaceable zugeordnet ist
	 *         und auch keinem InterfaceContainer zugeordent ist, der einem
	 *         Interfaceable zugeordnet wurde
	 */
	public abstract Interfaceable getInterfaceable();

	public abstract boolean canBlockMouse();

	private InterfaceContainer parentContainer;
	private Vector curPosition = Vector.ZERO, curCenter = Vector.ZERO;

	private int frame = -1;

	private Rectangle hitbox = new Rectangle(Vector.ZERO, 1, 1);
	private boolean mouseOver = false;

	public InterfacePart() {
	}

	/**
	 * Setzt das parent-Objekt dieses Objekts
	 * 
	 * @param parent Das parent-Objekt
	 */
	public void setParentContainer(InterfaceContainer parent) {
		if (parent != null) {
			if (parent.contains(this)) {
				this.parentContainer = parent;
			} else {
				Log.error("Parent enthält dieses Objekt nicht");
			}
		} else {
			this.parentContainer = null;
		}
		// this.updatePosition();
	}

	public InterfaceContainer getParentContainer() {
		return this.parentContainer;
	}

	public void updatePosition() {
		if (this.parentContainer != null) {
			this.curPosition = this.parentContainer.getPositionFor(this);
			this.curCenter = this.curPosition.add(this.getHitbox().width / 2, this.getHitbox().height / 2);
		} else {
			this.curPosition = Vector.ZERO;
			this.curCenter = new Vector(this.getHitbox().width / 2, this.getHitbox().height / 2);
		}
	}

	/**
	 * @return Die Position des Zentrum dieses Containers auf der Oberfläche, ohne
	 *         die Translation durch die Kamera
	 */
	public Vector getCenter() {
		return this.curCenter;
	}

	public Vector getPosition() {
		return this.curPosition;
	}

	@Override
	public void poll(Input input, float secounds) {
		this.frame = Engine.getInstance().getFrameNumber();
		this.mouseOver = this.getHitbox().isPointInThis(this.getCenter().sub(input.getMouseX(), input.getMouseY()));
		if (this.mouseOver) {
			if (this.canBlockMouse()) {
				if (this.getInterfaceable() != null) {
					this.getInterfaceable().registerMouseBlockedByGUI();
				}
			}
		}
	}

	public boolean hasMouseOver() {
		return this.mouseOver;
	}

	public boolean isShown() {
		return this.frame == Engine.getInstance().getFrameNumber();
	}

	/**
	 * @return Die Größe, die das Objekt belegen möchte, in Form eines
	 *         {@link Rectangle}, dessen Position aber <b>keine</b> Bedeutung hat,
	 *         das Zentrum ist stets {@link Vector#ZERO}
	 */
	public final Rectangle getHitbox() {
		return this.hitbox;
	}

	public boolean setHitbox(float width, float height) {
		if (width != this.hitbox.width || height != this.hitbox.height) {
			this.hitbox = new Rectangle(Vector.ZERO, width, height);
			if (this.getParentContainer() != null) {
				this.getParentContainer().triggerResize();
			}
			return true;
		}
		return false;
	}

	public boolean setHitbox(Rectangle hitbox) {
		return this.setHitbox(hitbox.width, hitbox.height);
	}
}
