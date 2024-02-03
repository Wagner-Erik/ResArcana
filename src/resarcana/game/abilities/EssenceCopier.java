package resarcana.game.abilities;

import org.newdawn.slick.util.Log;

import javafx.util.Pair;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class EssenceCopier extends Ability {

	public static final int MODE_ESSENCE = 0;
	public static final int MODE_DEMON = 1;

	private final Essences input, output;
	private final int mode;

	public EssenceCopier(Tappable parent, Vector relPos, int mode, Essences input, Essences output) {
		super(parent, relPos);
		this.mode = mode;
		this.input = input;
		this.output = output;
	}

	public EssenceCopier(Tappable parent, Vector relPos, Essences input, Essences output) {
		this(parent, relPos, MODE_ESSENCE, input, output);
	}

	@Override
	public boolean activate() {
		return true;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		
		Pair<Player, Integer> result = new Pair<Player, Integer>(this.getPlayer(), 0);
		Vector start = Vector.ZERO;
		switch (this.mode) {
		case MODE_ESSENCE:
			result = this.getGame().getMaxEssenceCount(this.input, this.getPlayer());
			start = result.getKey().getEssencePosition(this.input);
			break;
		case MODE_DEMON:
			result = this.getGame().getMaxDemonCount(this.getPlayer());
			start = result.getKey().getMagePosition();
			break;
		default:
			Log.warn("Unknown mode for " + this);
			break;
		}
		
		if (result.getKey() != this.getPlayer()) {
			// Copy essences
			this.getPlayer().modifyEssence(new EssenceSelection(this.output, result.getValue()), false);
			// Play animation
			this.getPlayer().playTransferAnimation(this.output, result.getValue(), start,
					this.getPlayer().getEssencePosition(this.output));
		}
		
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
	}

}
