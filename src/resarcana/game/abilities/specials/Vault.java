package resarcana.game.abilities.specials;

import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.utils.EssenceSelection;

public class Vault extends Artifact {

	public Vault(Game parent) {
		super(parent, "artifact/artifact_36.png");
		this.setName("Vault");
		this.setRawCosts(new EssenceSelection(Essences.GOLD, 1, null, 1));
		this.setAutoCollect(CollectMode.ASK);
	}

	@Override
	protected boolean askIncome(boolean collecting) {
		if (!collecting && this.getEssenceCount()[Essences.GOLD.ordinal()] > 0) {
			this.setIncome(new EssenceSelection(2, Essences.GOLD));
		} else {
			this.setIncome(new EssenceSelection());
		}
		return super.askIncome(collecting);
	}
}
