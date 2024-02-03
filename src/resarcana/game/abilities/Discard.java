package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class Discard extends Ability implements Selecting {

	public static final int STATE_IDLE = 0;
	public static final int STATE_SELECTING = 1;
	public static final int STATE_OUTPUT = 2;

	public final EssenceSelection reward;
	private final boolean tapping;

	private Artifact selected;

	private int status = STATE_IDLE;

	public Discard(Tappable parent, Vector relPos, boolean tapping, EssenceSelection reward) {
		super(parent, relPos);
		this.reward = reward;
		this.tapping = tapping;
	}

	protected ArrayList<Artifact> getTargets() {
		return this.getPlayer().getHand();
	}

	@Override
	protected boolean isActivable() {
		return this.getTargets().size() > 0 && this.getPlayer().isActive()
				&& (!this.tapping || !this.getTappable().isTapped());
	}

	@Override
	public boolean activate() {
		this.getGameClient().addSelector(new ImageSelector<Artifact>(this, this.getTargets(), "Discard artifact"));
		this.status = STATE_SELECTING;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		if (this.tapping) {
			this.getTappable().tap();
		}
		Artifact target = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
		this.getPlayer().discardCard(target);
		this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(target);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_SELECTING) {
			this.getGameClient().unsetSelector(sel);
			this.selected = ((ImageSelector<Artifact>) sel).getResult();
			this.getGameClient()
					.addSelector(new EssenceSelector(this,
							this.reward.isDetermined()
									? this.reward.excludeAll(EssenceSelector.getExcludes(this.reward.getValues()))
									: this.reward,
							"Choose essences"));
			this.status = STATE_OUTPUT;
			return;
		} else if (sel instanceof EssenceSelector && this.status == STATE_OUTPUT) {
			this.getGameClient().unsetSelector(sel);
			EssenceSelection selection = ((EssenceSelector) sel).getSelection();
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.selected, selection));
			this.status = STATE_IDLE;
			return;
		} else {
			Log.info("Unexpected selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}
}
