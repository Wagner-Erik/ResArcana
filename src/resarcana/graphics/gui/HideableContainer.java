package resarcana.graphics.gui;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

public abstract class HideableContainer extends InterfaceContainer implements Hideable {

	private boolean shown = false;

	public HideableContainer() {
		super();
	}

	@Override
	public void show() {
		this.setShown(true);
	}

	@Override
	public void hide() {
		this.setShown(false);
	}

	@Override
	public void switchStatus() {
		this.setShown(!this.shown);
	}

	@Override
	public void setShown(boolean shown) {
		this.shown = shown;
	}

	@Override
	public boolean isShown() {
		return this.shown;
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.shown) {
			super.poll(input, secounds);
		}
	}

	@Override
	public void draw(Graphics g) {
		if (this.shown) {
			super.draw(g);
		}
	}

	@Override
	public boolean canBlockMouse() {
		if (this.isShown()) {
			return super.canBlockMouse();
		} else {
			return false;
		}
	}
}
