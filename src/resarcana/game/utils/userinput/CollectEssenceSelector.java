package resarcana.game.utils.userinput;

import org.newdawn.slick.Input;

import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.objects.Checkbox;
import resarcana.graphics.gui.objects.ImageButton;

public class CollectEssenceSelector extends Selector {

	private AdvancedGridContainer container;
	private Checkbox result;

	public CollectEssenceSelector(Selecting selecting, Tappable tappable, String description) {
		super(selecting, description);
		this.container = new AdvancedGridContainer(1, Essences.values().length + 1);
		int[] count = tappable.getEssenceCount();
		for (Essences ess : Essences.values()) {
			// this.container.add(new InterfaceLabel(ess.toString(), 1), 0, ess.ordinal());
			if (count[ess.ordinal()] > 0) {
				this.container.add(new ImageButton(InterfaceFunctions.NONE, ess.getHitbox(), ess.getImage(),
						count[ess.ordinal()] + ""), 0, ess.ordinal());
				// this.container.add(new InterfaceLabel(count[ess.ordinal()] + "", 3), 0,
				// ess.ordinal());
			}
		}
		// this.container.add(new InterfaceLabel(tappable.getName(), 1), 0,
		// Essences.values().length);
		this.result = new Checkbox(InterfaceFunctions.COLLECTSELECTOR_VALUE, Input.KEY_SPACE, false, 4,
				Checkbox.MODE_YES_NO);
		this.result.setYesNoText("Collect", "Don't collect");
		this.container.add(this.result, 0, Essences.values().length);
	}

	@Override
	protected InterfaceContainer getSelectionInterface() {
		return this.container;
	}

	@Override
	protected boolean isSelectionDone() {
		return true;
	}

	public boolean getResult() {
		return this.result.isChecked();
	}

}
