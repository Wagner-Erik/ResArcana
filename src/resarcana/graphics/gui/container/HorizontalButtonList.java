package resarcana.graphics.gui.container;

import java.util.HashMap;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.InterfacePart;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.SquareButton;
import resarcana.math.Vector;

/**
 * @author Erik Wagner
 * 
 */
public class HorizontalButtonList extends InterfaceContainer implements Informable {

	private final int distanceBetweenButtons;
	private HashMap<Integer, SquareButton> list = new HashMap<Integer, SquareButton>();
	private HashMap<SquareButton, Integer> invertList = new HashMap<SquareButton, Integer>();
	private int next = 0;
	private int curPos = 0;

	private final int numberOfButtonsDisplayed;
	private static final int BACK_POS = -2;
	private static final int FORTH_POS = -1;

	/**
	 * 
	 * @param parent
	 * @param numberOfButtonDisplayed Anzahl der Buttons, die angezeigt werden
	 *                                sollen
	 */
	public HorizontalButtonList(int numberOfButtonDisplayed, int distanceBetweenButtons) {
		super();
		this.numberOfButtonsDisplayed = numberOfButtonDisplayed;
		this.distanceBetweenButtons = distanceBetweenButtons;
		SquareButton back = new SquareButton(InterfaceFunctions.INTERFACE_BUTTONLIST_BACK,
				"interface-icons/back-arrow.png"),
				forth = new SquareButton(InterfaceFunctions.INTERFACE_BUTTONLIST_FORTH,
						"interface-icons/forth-arrow.png");
		this.list.put(BACK_POS, back);
		this.invertList.put(back, BACK_POS);
		this.add(back, Vector.ZERO);
		this.list.put(FORTH_POS, forth);
		this.invertList.put(forth, FORTH_POS);
		this.add(forth, Vector.ZERO);
		back.addInformable(this);
		forth.addInformable(this);
		this.triggerResize();
	}

	public void addButton(SquareButton object) {
		this.list.put(this.next, object);
		this.invertList.put(object, this.next);
		this.next = next + 1;
		this.add(object, Vector.ZERO);
	}

	@Override
	public void poll(Input input, float secounds) {
		Object[] abbild = this.list.values().toArray();
		for (Object button : abbild) {
			((SquareButton) button).poll(input, secounds);
		}
	}

	@Override
	public void draw(Graphics g) {
		Object[] abbild = this.list.keySet().toArray();
		java.util.Arrays.sort(abbild);
		for (int i = this.curPos + 2; i < this.curPos + numberOfButtonsDisplayed + 2 && i < abbild.length; i++) {
			this.list.get(abbild[i]).draw(g);
		}
		if (this.isBackMoveable()) {
			this.list.get(abbild[0]).draw(g);
		}
		if (this.isForthMoveable()) {
			this.list.get(abbild[1]).draw(g);
		}
	}

	@Override
	public Vector getPositionFor(InterfacePart object) {
		if (object instanceof SquareButton) {
			int listPos = this.invertList.get(object);
			if (listPos != BACK_POS && listPos != FORTH_POS) {
				if (listPos < this.curPos) {
					return new Vector(-SquareButton.BUTTON_DIMENSION * 10, 0);
				}
				if (listPos >= this.curPos + this.numberOfButtonsDisplayed) {
					return new Vector(-SquareButton.BUTTON_DIMENSION * 10, 0);
				}
			}
			listPos = listPos - this.curPos;
			return getPositionForListPos(listPos).add(this.getPosition());
		} else {
			return super.getPositionFor(object);
		}
	}

	private Vector getPositionForListPos(int listPos) {
		if (listPos + this.curPos == BACK_POS) {
			listPos = -1;
		} else if (listPos + this.curPos == FORTH_POS) {
			listPos = this.numberOfButtonsDisplayed;
		}
		return new Vector((SquareButton.BUTTON_DIMENSION + this.distanceBetweenButtons) * (listPos + 1), 0.0f);

	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (object.getFunction() == InterfaceFunctions.INTERFACE_BUTTONLIST_FORTH) {
				this.moveForth();
			} else if (object.getFunction() == InterfaceFunctions.INTERFACE_BUTTONLIST_BACK) {
				this.moveBack();
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

	private void moveBack() {
		if (this.curPos > 0) {
			this.curPos--;
		}
	}

	private void moveForth() {
		if (this.curPos < this.list.keySet().toArray().length - 2 - numberOfButtonsDisplayed) {
			this.curPos++;
		}
	}

	private boolean isForthMoveable() {
		return this.list.keySet().toArray().length - 2 > numberOfButtonsDisplayed + curPos;
	}

	private boolean isBackMoveable() {
		return this.curPos > 0;
	}

	@Override
	protected boolean resize() {
		int z = 1;
		if (this.isForthMoveable()) {
			z++;
		}
		if (this.isBackMoveable()) {
			z++;
		}
		float width = (SquareButton.BUTTON_DIMENSION + this.distanceBetweenButtons)
				* (this.numberOfButtonsDisplayed + z) - this.distanceBetweenButtons;
		return this.setHitbox(width, SquareButton.BUTTON_DIMENSION);
	}
}
