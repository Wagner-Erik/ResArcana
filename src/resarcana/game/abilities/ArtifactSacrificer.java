package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.math.Vector;

public class ArtifactSacrificer extends Ability implements Selecting {

	public static final int MODE_PLAYER = 0;
	public static final int MODE_SELF = 1;

	private static final int STATE_IDLE = 0;
	private static final int STATE_SELECTION = 1;
	private static final int STATE_OUTPUT = 1;

	private final boolean selfSacrificePossible;
	private final int mode, bonusValue, fixedValue;
	private final EssenceSelection cost;
	private final Essences outputType;

	private int status = STATE_IDLE;
	private Artifact toDestroy;

	/**
	 * 
	 * @param parent                the card this ability belongs to
	 * @param relPos                the relative position of this ability
	 * @param selfSacrificePossible whether the card can sacrifice itself
	 * @param bonusValue            the amount of essence this produces in addition
	 *                              to the sacrificed cards cost
	 * @param cost                  the cost to activate this ability
	 * @param outputType            the type of essence this produces, if
	 *                              <code>null</code> than everything but
	 *                              {@link Essences#GOLD} can be produced
	 * @param fixedValue            if != 0 than the cost of the sacrificed card is
	 *                              ignored and the fixed value is produced
	 */
	public ArtifactSacrificer(Tappable parent, Vector relPos, int mode, boolean selfSacrificePossible, int bonusValue,
			EssenceSelection cost, Essences outputType, int fixedValue) {
		super(parent, relPos);
		this.mode = mode;
		this.selfSacrificePossible = selfSacrificePossible;
		this.bonusValue = bonusValue;
		this.cost = cost;
		this.outputType = outputType;
		this.fixedValue = fixedValue;
	}

	public ArtifactSacrificer(Tappable parent, Vector relPos, boolean selfSacrificePossible, int bonusValue) {
		this(parent, relPos, MODE_PLAYER, selfSacrificePossible, bonusValue, new EssenceSelection(), null, 0);
	}

	private ArrayList<Artifact> getSacrifices() {
		ArrayList<Artifact> sacrifices = this.getPlayer().getArtifactsInPlay();
		int i = 0;
		while (i < sacrifices.size()) {
			if (this.canSacrifice(sacrifices.get(i))) {
				i++;
			} else {
				sacrifices.remove(i);
			}
		}
		return sacrifices;
	}

	protected boolean canSacrifice(Artifact artifact) {
		return this.selfSacrificePossible || artifact != this.getTappable();
	}

	@Override
	protected boolean isActivable() {
		// Atleast one artifact to sacrifice
		return super.isActivable() && this.getSacrifices().size() > 0 && this.getPlayer().isPayable(this.cost);
	}

	@Override
	public boolean activate() {
		this.getGameClient()
				.addSelector(new ImageSelector<Artifact>(this, this.getSacrifices(), "Choose artifact to destroy"));
		this.status = STATE_SELECTION;
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		this.getTappable().tap();
		this.getPlayer().modifyEssence(this.cost, true);
		Artifact destroy = (Artifact) this.getGame().getTappable(overwrite.getParts().get(0));
		this.getPlayer().destroyArtifact(destroy);
		switch (this.mode) {
		case MODE_PLAYER:
			this.getPlayer().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
			break;
		case MODE_SELF:
			this.getTappable().modifyEssence(new EssenceSelection(overwrite.getParts().get(1)), false);
			break;
		default:
			Log.warn("Unknown placing mode " + this.mode + " for " + this);
			break;
		}
		return new HistoryElement(this).setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
				.setOptionalOne(destroy);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_SELECTION) {
			this.getGameClient().unsetSelector(sel);
			this.toDestroy = ((ImageSelector<Artifact>) sel).getResult();
			EssenceSelection output;
			if (this.outputType == null) {
				output = new EssenceSelection(
						this.fixedValue == 0 ? this.toDestroy.getRawCost().getTotal() + this.bonusValue
								: this.fixedValue,
						Essences.GOLD);
				this.getGameClient().addSelector(new EssenceSelector(this, output, "Choose essences from sacrifice"));
				this.status = STATE_OUTPUT;
			} else {
				output = new EssenceSelection(this.outputType,
						this.fixedValue == 0 ? this.toDestroy.getRawCost().getTotal() + this.bonusValue
								: this.fixedValue);
				this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.toDestroy, output));
				this.status = STATE_IDLE;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_OUTPUT) {
			this.getGameClient().unsetSelector(sel);
			EssenceSelection result = ((EssenceSelector) sel).getSelection();
			this.getGame().abilityFinished(this, new UserInputOverwrite(this, this.toDestroy, result));
			this.status = STATE_IDLE;
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGameClient().unsetSelector(sel);
		this.getGame().cancelAbility(this);
		this.status = STATE_IDLE;
	}

}
