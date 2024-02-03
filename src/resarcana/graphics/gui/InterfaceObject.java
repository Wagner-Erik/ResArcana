/**
 *
 */
package resarcana.graphics.gui;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.game.GameClient;
import resarcana.game.GameState;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.SoundManager;

/**
 * Eine Superklasse für Objekte im Interface. Gibt die Ereignisse an sein
 * "Parent" weiter und außerdem an {@link Informable}, wenn diese mit
 * {@link addInformable} hinzugefügt wurden
 * 
 * @author Erik Wagner
 * 
 */
public abstract class InterfaceObject extends InterfacePart {

	public static final Color BORDER_COLOR = new Color(255, 85, 50);
	public static final float BORDER_LINEWIDTH = 3.0f;

	private final InterfaceFunction function;

	private Mousestatus status = Mousestatus.STATUS_NOTHING;

	private ArrayList<Informable> toInform = new ArrayList<Informable>();

	private final int key;

	private boolean enabled = true;
	private boolean leftPressed = false, rightPressed = false;

	private boolean border = false;

	private static InterfaceObject LastSelected = null;

	/**
	 * @param function Die Funktion dieses Objekts (ein Enum aus
	 *                 {@link InterfaceFunctions})
	 */
	public InterfaceObject(InterfaceFunction function) {
		this(function, Input.KEY_ENTER);
	}

	public InterfaceObject(InterfaceFunction function, int key) {
		this.function = function;
		this.key = key;
	}

	@Override
	public boolean canBlockMouse() {
		return true;
	}

	@Override
	public Interfaceable getInterfaceable() {
		if (this.getParentContainer() != null) {
			return this.getParentContainer().getInterfaceable();
		} else {
			return null;
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		super.poll(input, secounds);
		if (this.hasMouseOver() && this.enabled) {
			if (!input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) && this.status == Mousestatus.STATUS_LEFT_DOWN
					&& this.leftPressed) {
				onMouseAction(Mousestatus.STATUS_LEFT_RELEASED);
				SoundManager.getInstance().playMenuClickUp();
			} else if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
				onMouseAction(Mousestatus.STATUS_LEFT_PRESSED);
				this.leftPressed = true;
				SoundManager.getInstance().playMenuClickDown();
			} else if (!input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)
					&& this.status == Mousestatus.STATUS_RIGHT_DOWN && this.rightPressed) {
				onMouseAction(Mousestatus.STATUS_RIGHT_RELEASED);
				SoundManager.getInstance().playMenuClickUp();
			} else if (input.isMousePressed(Input.MOUSE_RIGHT_BUTTON)) {
				onMouseAction(Mousestatus.STATUS_RIGHT_PRESSED);
				this.rightPressed = true;
				SoundManager.getInstance().playMenuClickDown();
			} else if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)
					&& input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
				onMouseAction(Mousestatus.STATUS_BOTH_DOWN);
			} else if (input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
				onMouseAction(Mousestatus.STATUS_LEFT_DOWN);
			} else if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON)) {
				onMouseAction(Mousestatus.STATUS_RIGHT_DOWN);
			} else {
				this.status = Mousestatus.STATUS_MOUSE_OVER;
				for (Informable informable : this.toInform) {
					informable.mouseOverAction(this);
				}
			}
			if (this instanceof ImageDisplay) {
				if (input.isKeyDown(Input.KEY_LALT)) {
					Interfaceable inter = this.getInterfaceable();
					if (inter instanceof GameState) {
						GameClient client = ((GameState) inter).getClient();
						if (client != null) {
							client.setDetailedCard(((ImageDisplay) this).getImage());
						}
					}
				}
			}
		} else {
			if (this.key != Input.KEY_ENTER && input.isKeyPressed(this.key)) {
				this.onMouseAction(Mousestatus.STATUS_LEFT_PRESSED);
				this.onMouseAction(Mousestatus.STATUS_LEFT_RELEASED);
			} else {
				this.status = Mousestatus.STATUS_NOTHING;
				this.leftPressed = false;
				this.rightPressed = false;
			}
		}
	}

	public InterfaceObject setBorder(boolean border) {
		this.border  = border;
		return this;
	}
	
	public boolean hasBorder() {
		return this.border;
	}
	
	public void drawBorder(Graphics g) {
		if (this.border) {
			float lw = g.getLineWidth();
			g.setColor(BORDER_COLOR);
			g.setLineWidth(BORDER_LINEWIDTH);
			GraphicUtils.draw(g, this.getHitbox());
			g.setLineWidth(lw);
		}
	}

	private void onMouseAction(Mousestatus status) {
		this.status = status;
		for (Informable informable : this.toInform) {
			informable.mouseButtonAction(this);
		}
		if (LastSelected != this) {
			for (Informable informable : this.toInform) {
				informable.objectIsSelected(this);
			}
		}
		LastSelected = this;
	}

	/**
	 * Fügt ein Informable hinzu, welches von nun an über Aktionen dieses Objekts
	 * informiert wird
	 * 
	 * @param object Das Informable-Objekt
	 */
	public InterfaceObject addInformable(Informable object) {
		if (object != null && this.toInform.contains(object) == false) {
			this.toInform.add(object);
		}
		return this;
	}

	public Mousestatus getStatus() {
		return this.status;
	}

	public InterfaceFunction getFunction() {
		return this.function;
	}

	/**
	 * @param object Das zu prüfende Objekt
	 * @return <code>true</code>, wenn als letztes auf dieses Objekt geklickt wurde
	 */
	public boolean isSelected() {
		return this == LastSelected;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void makeSelected() {
		LastSelected = this;
	}
}
