/**
 *
 */
package resarcana.graphics.gui.container;

import java.util.HashMap;

import org.newdawn.slick.Graphics;

import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.InterfacePart;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.graphics.gui.objects.TileableBackgroundButton;
import resarcana.math.Vector;

/**
 * @author Erik Wagner
 * 
 */
public class TextButtonList extends InterfaceContainer implements Informable {

	private final int distanceBetweenButtons;
	private HashMap<Integer, InterfacePart> list = new HashMap<Integer, InterfacePart>();
	private HashMap<InterfacePart, Integer> invertList = new HashMap<InterfacePart, Integer>();
	private int next = 0;
	private int curPos = 0;
	private final int numberOfButtonsDisplayed;
	private float maxWidth = 1;

	private float[] runningHeights;

	private static final int DOWN_POS = -2;
	private static final int UP_POS = -1;

	/**
	 *
	 */
	public TextButtonList(int numberOfButtonsDisplayed, int distanceBetweenButtons) {
		this.numberOfButtonsDisplayed = numberOfButtonsDisplayed;
		this.distanceBetweenButtons = distanceBetweenButtons;
		TextButton up = new TextButton(InterfaceFunctions.INTERFACE_TEXTBUTTONLIST_UP, "Nach oben"),
				down = new TextButton(InterfaceFunctions.INTERFACE_TEXTBUTTONLIST_DOWN, "Nach unten");
		this.list.put(UP_POS, up);
		this.invertList.put(up, UP_POS);
		this.add(up, Vector.ZERO);
		this.list.put(DOWN_POS, down);
		this.invertList.put(down, DOWN_POS);
		this.add(down, Vector.ZERO);
		up.addInformable(this);
		down.addInformable(this);
		this.runningHeights = new float[numberOfButtonsDisplayed + 2];
		this.triggerResize();
	}

	public void addTextButton(TextButton button) {
		this.list.put(this.next, button);
		this.invertList.put(button, this.next);
		this.next = next + 1;
		this.add(button, Vector.ZERO);
	}

	public void addTextButton(TileableBackgroundButton button) {
		this.list.put(this.next, button);
		this.invertList.put(button, this.next);
		this.next = next + 1;
		this.add(button, Vector.ZERO);
	}

//	@Override
//	public void poll(Input input, float secounds) {
//		Object[] abbild = this.list.values().toArray();
//		for (Object button : abbild) {
//			((InterfacePart) button).poll(input, secounds);
//		}
//	}

	@Override
	public void draw(Graphics g) {
		this.updateButtonHeights();
		Object[] abbild = this.list.keySet().toArray();
		java.util.Arrays.sort(abbild);
		for (int i = this.curPos + 2; i < this.curPos + this.numberOfButtonsDisplayed + 2 && i < abbild.length; i++) {
			this.list.get(abbild[i]).draw(g);
		}
		if (this.isDownMoveable()) {
			this.list.get(abbild[0]).draw(g);
		}
		if (this.isUpMoveable()) {
			this.list.get(abbild[1]).draw(g);
		}
	}

	private void updateButtonHeights() {
		this.runningHeights[0] = 0;
		this.runningHeights[1] = this.list.get(UP_POS).getHitbox().height + this.distanceBetweenButtons;
		for (int i = this.curPos; i < this.curPos + this.numberOfButtonsDisplayed && i < this.next; i++) {
			this.runningHeights[i - this.curPos + 2] = this.runningHeights[i - this.curPos + 1]
					+ this.list.get(i).getHitbox().height + this.distanceBetweenButtons;
		}
	}

	@Override
	public Vector getPositionFor(InterfacePart object) {
		if (this.invertList.containsKey(object)) {
			int listPos = this.invertList.get(object);
			if (listPos != DOWN_POS && listPos != UP_POS) {
				if (listPos < this.curPos) {
					return new Vector(0.0f, -100000);
				}
				if (listPos >= this.curPos + this.numberOfButtonsDisplayed) {
					return new Vector(0.0f, 100000);
				}
			}
			return getPositionForListPos(listPos).add(this.getPosition());
		} else {
			return super.getPositionFor(object);
		}
	}

	private Vector getPositionForListPos(int listPos) {
		if (listPos == DOWN_POS) {
			listPos = this.numberOfButtonsDisplayed;
		} else if (listPos == UP_POS) {
			listPos = -1;
		} else {
			listPos = listPos - this.curPos;
		}
		return new Vector(0.0f, this.runningHeights[listPos + 1]);

	}

	private void moveUp() {
		if (this.curPos > 0) {
			this.curPos--;
		}
	}

	private void moveDown() {
		if (this.curPos < this.list.keySet().toArray().length - 2 - numberOfButtonsDisplayed) {
			this.curPos++;
		}
	}

	private boolean isDownMoveable() {
		return this.list.keySet().toArray().length - 2 > numberOfButtonsDisplayed + curPos;
	}

	private boolean isUpMoveable() {
		return this.curPos > 0;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (object.getFunction() == InterfaceFunctions.INTERFACE_TEXTBUTTONLIST_DOWN) {
				this.moveDown();
			} else if (object.getFunction() == InterfaceFunctions.INTERFACE_TEXTBUTTONLIST_UP) {
				this.moveUp();
			}
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nichts tun
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nichts tun
	}

	@Override
	protected boolean resize() {
		for (InterfacePart button : this.list.values()) {
			this.maxWidth = Math.max(this.maxWidth, button.getHitbox().getXRange());
		}
		return this.setHitbox(this.maxWidth, this.runningHeights[Math.min(this.numberOfButtonsDisplayed, this.next) + 1]
				+ this.list.get(DOWN_POS).getHitbox().height);
	}

}
