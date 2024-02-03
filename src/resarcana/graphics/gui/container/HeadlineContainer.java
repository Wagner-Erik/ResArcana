package resarcana.graphics.gui.container;

import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfacePart;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

/**
 * Ein InterfaceContainer, der die ihm zur Verfügung stehende Fläche in eine
 * Kopfzeile und einen darunterliegenden Hauptteil aufteilt
 * 
 * @author Erik Wagner
 * 
 */
public class HeadlineContainer extends InterfaceContainer {

	private InterfaceContainer headlineCon, mainCon;
	private float lastYhead = 0;

	/**
	 * Erzeugt einen neuen HeadlineContainer
	 * 
	 * @param headlineCon Der InterfaceContainer, der die Kopfzeilen-Fläche einnimmt
	 * @param mainCon     Der InterfaceContainer, der die Hauptteil-Fläche belegt
	 */
	public HeadlineContainer(InterfaceContainer headlineCon, InterfaceContainer mainCon) {
		this.setHeadlineContainer(headlineCon);
		this.setMainContainer(mainCon);
		this.triggerResize();
	}

	public void setHeadlineContainer(InterfaceContainer con) {
		this.remove(this.headlineCon);
		this.headlineCon = con;
		this.add(this.headlineCon, Vector.ZERO);
	}

	public void setMainContainer(InterfaceContainer con) {
		this.remove(this.mainCon);
		this.mainCon = con;
		this.lastYhead = this.headlineCon.getHitbox().height;
		this.add(this.mainCon, new Vector(0, this.lastYhead));
	}

	public InterfaceContainer getMainContainer() {
		return this.mainCon;
	}

	public InterfaceContainer getHeadlineContainer() {
		return this.headlineCon;
	}

	@Override
	public Vector getPositionFor(InterfacePart object) {
		if (object == this.mainCon) {
			return this.getPosition().add(0, this.headlineCon.getHitbox().height);
		} else {
			return this.getPosition();
		}
	}

	@Override
	protected boolean resize() {
		Rectangle head, main;
		if (this.headlineCon != null) {
			head = this.headlineCon.getHitbox();
		} else {
			head = new Rectangle(0, 0, 1, 1);
		}
		if (this.mainCon != null) {
			main = this.mainCon.getHitbox();
		} else {
			main = new Rectangle(0, 0, 1, 1);
		}
		if (this.lastYhead != head.height) {
			this.setMainContainer(this.mainCon);
		}
		return this.setHitbox(Math.max(head.width, main.width), head.height + main.height);
	}
}
