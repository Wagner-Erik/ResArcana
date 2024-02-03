package resarcana.graphics.gui.container;

import java.util.ArrayList;

import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfacePart;
import resarcana.math.Vector;

public class CardContainer extends InterfaceContainer {

	private ArrayList<InterfaceContainer> container;
	private InterfaceContainer active = null;

	public CardContainer() {
		this.container = new ArrayList<InterfaceContainer>();
	}

	public void add(InterfaceContainer con) {
		this.add(con, true);
	}

	public void add(InterfaceContainer con, boolean setActive) {
		this.container.add(con);
		if (setActive) {
			this.setActive(con);
		}
	}

	private void setActive(InterfaceContainer con) {
		if (this.active != con) {
			this.remove(this.active);
			this.active = con;
			this.add(con, Vector.ZERO);
		}
	}

	public void switchTo(int number) {
		if (this.container.size() > number) {
			this.setActive(this.container.get(number));
		}
	}

	public void switchTo(InterfaceContainer con) {
		if (this.container.contains(con)) {
			this.setActive(con);
		}
	}

	@Override
	public Vector getPositionFor(InterfacePart object) {
		return this.getPosition();
	}

	@Override
	protected boolean resize() {
		if (this.active != null) {
			return this.setHitbox(this.active.getHitbox());
		} else {
			return this.setHitbox(1, 1);
		}
	}
}
