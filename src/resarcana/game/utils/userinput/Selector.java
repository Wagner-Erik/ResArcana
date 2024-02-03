package resarcana.game.utils.userinput;

import org.newdawn.slick.util.Log;

import resarcana.graphics.gui.Informable;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TextButton;
import resarcana.graphics.gui.objects.TileableBackgroundButton;

public abstract class Selector implements Informable {

	protected abstract InterfaceContainer getSelectionInterface();

	protected abstract boolean isSelectionDone();

	private AdvancedGridContainer contents, buttonContainer;

	private final TextButton hideButton;

	private final Selecting selecting;
	private final String description;

	private boolean setup = false;
	private boolean cancelable = true;
	private boolean hidden = false;

	public Selector(Selecting selecting, String description) {
		this.selecting = selecting;
		this.description = description;
		this.contents = new AdvancedGridContainer(3, 1);
		this.contents.add(new TileableBackgroundButton(this.description, 1), 0, 0);

		this.buttonContainer = new AdvancedGridContainer(1, 3);
		TileableBackgroundButton b = new TileableBackgroundButton(InterfaceFunctions.SELECTOR_FINISH_SELECTION,
				"Confirm selection", 1);
		b.addInformable(this);
		this.buttonContainer.add(b, 0, 1, AdvancedGridContainer.MODUS_DEFAULT, AdvancedGridContainer.MODUS_DEFAULT);

		this.hideButton = new TextButton(InterfaceFunctions.SELECTOR_HIDE_CONTENT, "hide", 1);
		this.hideButton.addInformable(this);
		this.buttonContainer.add(this.hideButton, 0, 2, AdvancedGridContainer.MODUS_X_RIGHT,
				AdvancedGridContainer.MODUS_Y_DOWN);

		this.contents.add(this.buttonContainer, 2, 0);
	}

	public InterfaceContainer getInterfaceContainer() {
		if (!this.setup) {
			this.contents.add(this.getSelectionInterface(), 1, 0);
			if (this.cancelable) {
				TileableBackgroundButton b = new TileableBackgroundButton(InterfaceFunctions.SELECTOR_CANCEL, "Cancel",
						1);
				b.addInformable(this);
				this.buttonContainer.add(b, 0, 0, AdvancedGridContainer.MODUS_X_LEFT,
						AdvancedGridContainer.MODUS_DEFAULT);
			}
		}
		return this.contents;
	}

	public Selector disableCancel() {
		this.cancelable = false;
		return this;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (this.isSelectionDone() && object.getFunction() == InterfaceFunctions.SELECTOR_FINISH_SELECTION
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			Log.debug("Accept selection " + this + "::" + this.description);
			this.selecting.processSelection(this);
		}
		if (object.getFunction() == InterfaceFunctions.SELECTOR_CANCEL
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			Log.debug("Cancel selection " + this + "::" + this.description);
			this.selecting.cancelSelection(this);
		}
		if (object.getFunction() == InterfaceFunctions.SELECTOR_HIDE_CONTENT
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (this.hidden) {
				Log.debug("Show selection " + this + "::" + this.description);
				this.contents.add(this.getSelectionInterface(), 1, 0);
				this.hideButton.setText("hide");
				this.hidden = false;
			} else {
				Log.debug("Hide selection " + this + "::" + this.description);
				this.contents.remove(this.getSelectionInterface());
				this.hideButton.setText("show");
				this.hidden = true;
			}
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Ignore
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Ignore
	}
}
