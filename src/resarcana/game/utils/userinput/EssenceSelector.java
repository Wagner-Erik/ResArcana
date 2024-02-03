package resarcana.game.utils.userinput;

import java.util.Arrays;
import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Essences;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.gui.ContentListener;
import resarcana.graphics.gui.Contentable;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.graphics.gui.objects.NumberCountingButton;
import resarcana.graphics.gui.objects.TileableBackgroundButton;

public class EssenceSelector extends Selector implements ContentListener {

	private NumberCountingButton[] selection;
	private final int totalWanted;
	private boolean anyValue = false;

	private AdvancedGridContainer container;
	private TileableBackgroundButton resetButton;

	public EssenceSelector(Selecting selecting, int totalMax, EnumSet<Essences> excludes, String description) {
		super(selecting, description + " (" + totalMax + ") ");
		this.totalWanted = totalMax;
		// Build interface
		this.selection = new NumberCountingButton[Essences.values().length];

		this.container = new AdvancedGridContainer(1, 2);
		GridContainer essenceContainer = new GridContainer(1, Essences.values().length - excludes.size() + 1);

		this.resetButton = new TileableBackgroundButton(InterfaceFunctions.SELECTOR_RESET, "Reset", 1);
		this.resetButton.addInformable(this);
		this.container.add(this.resetButton, 0, 0, AdvancedGridContainer.MODUS_X_LEFT,
				AdvancedGridContainer.MODUS_DEFAULT);
		int pos = 0;
		for (Essences ess : Essences.values()) {
			this.selection[ess.ordinal()] = new NumberCountingButton(ess.getHitbox(), ess.getImage(), 0, totalMax, 0,
					1);
			if (!excludes.contains(ess)) {
				this.selection[ess.ordinal()].addContentListener(this);
				essenceContainer.add(this.selection[ess.ordinal()], 0, pos);
				pos++;
			}
		}
		this.container.add(essenceContainer, 0, 1, AdvancedGridContainer.MODUS_X_RIGHT,
				AdvancedGridContainer.MODUS_DEFAULT);
	}

	public EssenceSelector(Selecting selecting, int[] maxValues, int totalMax, String text) {
		this(selecting, totalMax, getExcludes(maxValues), text);
		// Set maximum values
		if (maxValues.length == this.selection.length) {
			for (int i = 0; i < this.selection.length; i++) {
				this.selection[i].setMaximum(maxValues[i]);
			}
		}
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, String text, boolean anyValue) {
		this(selecting, request.getTotal(), request.getExcludes(), text);
		this.anyValue = anyValue;
		// Set minimum values
		for (Essences ess : Essences.values()) {
			this.selection[ess.ordinal()].setMinimum(request.getValue(ess));
		}
		// No reset button needed if selection is unmodifiable
		if (request.isDetermined()) {
			this.removeReset();
		}
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, String text) {
		this(selecting, request, text, false);
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, EssenceSelection maxValues, String text) {
		this(selecting, maxValues.isDetermined() ? request.excludeAll(getExcludes(maxValues.getValues())) : request,
				text);
		// Set maximum values
		for (Essences ess : Essences.values()) {
			this.selection[ess.ordinal()].setMaximum(maxValues.getValue(ess) + maxValues.getIndeterminedValue());
		}
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, int[] maxValuesPayable, String text,
			boolean anyValue) {
		this(selecting, request.excludeAll(getExcludes(maxValuesPayable)), text, anyValue);
		if (maxValuesPayable.length == Essences.values().length) {
			// Set maximum values
			for (int i = 0; i < Essences.values().length; i++) {
				this.selection[i].setMaximum(maxValuesPayable[i]);
			}
		} else {
			Log.warn("Cannot set maximum Values because array-length expected is " + Essences.values().length
					+ ", but got " + maxValuesPayable.length + " (" + Arrays.toString(maxValuesPayable) + ")");
		}
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, int[] maxValuesPayable, String text) {
		this(selecting, request, maxValuesPayable, text, false);
	}

	public EssenceSelector(Selecting selecting, EssenceSelection request, int[] maxValuesPayable,
			int[] maxValuesAllowed, String text) {
		this(selecting, maxValuesAllowed[Essences.values().length] > 0 ? request
				: request.excludeAll(getExcludes(maxValuesAllowed)), maxValuesPayable, text);
		// Set maximum values again if lower
		for (int i = 0; i < Essences.values().length; i++) {
			if (this.selection[i].getMaximum() > maxValuesAllowed[i] + maxValuesAllowed[maxValuesAllowed.length - 1]) {
				this.selection[i].setMaximum(maxValuesAllowed[i] + maxValuesAllowed[maxValuesAllowed.length - 1]);
			}
		}
	}

	/**
	 * Creates a list of {@link Essences} to exclude from a {@link EssenceSelector}
	 * because the maximum value to select would be zero
	 * 
	 * @param max the list of maximum values for the {@link Essences}, a value of
	 *            zero means that the essence can be excluded
	 * @return the {@link Essences} to exclude
	 */
	public static EnumSet<Essences> getExcludes(int[] max) {
		EnumSet<Essences> excludes = EnumSet.noneOf(Essences.class);
		if (max.length >= Essences.values().length) {
			for (Essences ess : Essences.values()) {
				if (max[ess.ordinal()] == 0) {
					excludes.add(ess);
				}
			}
		} else {
			Log.warn("Too short maximum-value list " + Arrays.toString(max));
		}
		return excludes;
	}

	public int[] getValues() {
		int[] values = new int[this.selection.length];
		for (int i = 0; i < this.selection.length; i++) {
			values[i] = this.selection[i].getValue();
		}
		return values;
	}

	private void removeReset() {
		this.container.remove(this.resetButton);
	}

	public EssenceSelection getSelection() {
		return new EssenceSelection(this.getValues());
	}

	@Override
	protected InterfaceContainer getSelectionInterface() {
		return this.container;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getFunction() == InterfaceFunctions.SELECTOR_RESET) {
			this.resetSelection();
		} else {
			super.mouseButtonAction(object);
		}
	}

	/**
	 * Resets the selection to the minimum value possible for each essence
	 */
	public void resetSelection() {
		for (NumberCountingButton sel : this.selection) {
			sel.setValue(sel.getMinimum());
		}
	}

	@Override
	public void contentChanged(Contentable object) {
		if (object instanceof NumberCountingButton) {
			NumberCountingButton sel = ((NumberCountingButton) object);
			if (!this.anyValue && this.getTotal() > this.totalWanted) {
				sel.setValue(sel.getValue() - (this.getTotal() - this.totalWanted));
			}
		}
	}

	private int getTotal() {
		int total = 0;
		for (int i : this.getValues()) {
			total += i;
		}
		return total;
	}

	@Override
	protected boolean isSelectionDone() {
		return this.anyValue || this.getTotal() == this.totalWanted;
	}

}
