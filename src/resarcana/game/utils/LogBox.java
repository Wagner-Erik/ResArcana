package resarcana.game.utils;

import org.newdawn.slick.Color;
import org.newdawn.slick.Input;

import resarcana.graphics.gui.HideableContainer;
import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.container.SpecialBackgroundContainer;
import resarcana.graphics.gui.objects.Label;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TextArea;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.math.Vector;

public class LogBox extends HideableContainer implements Informable {

	private AdvancedGridContainer mainCon;
	private SpecialBackgroundContainer background;

	private TextArea textArea;

	public LogBox(String title) {
		super();

		TextButton closeButton = new TextButton(InterfaceFunctions.DIALOG_CLOSE, Input.KEY_ESCAPE, "Close", 1.5f);
		closeButton.addInformable(this);

		this.mainCon = new AdvancedGridContainer(3, 1, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 10, 20);
		this.mainCon.add(closeButton, 2, 0);

		this.mainCon.add(new Label(title, 1.5f, Color.red), 0, 0);

		this.textArea = new TextArea(
				"ERROR: Test text Test text Test text Test text Test text Test text Test text Test text Test text", 8,
				1.f, Color.black);
		this.mainCon.add(this.textArea, 1, 0);

		this.background = new SpecialBackgroundContainer(this.mainCon, true, true, false, true, 1.f);
		this.add(this.background, Vector.ZERO);
	}

	public void addLog(String line) {
		this.textArea.addLine(line);
		this.textArea.scrollFullDown();
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isShown()) {
			super.poll(input, secounds);
			if (input.isKeyPressed(Input.KEY_DOWN)) {
				this.textArea.scrollDown();
			} else if (input.isKeyPressed(Input.KEY_UP)) {
				this.textArea.scrollUp();
			}
		}
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.DIALOG_CLOSE
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			this.hide();
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	protected boolean resize() {
		return this.setHitbox(this.background.getHitbox());
	}
}
