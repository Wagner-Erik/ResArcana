package resarcana.game.abilities.specials;

import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.utils.EssenceSelection;

public class WindupMan extends Artifact {

	public WindupMan(Game parent) {
		super(parent, "artifact/artifact_39.png");
		this.setName("Windup Man");
		this.setRawCosts(new EssenceSelection(Essences.ELAN, 1, Essences.LIFE, 1, Essences.CALM, 1, Essences.GOLD, 1));
		this.setAutoCollect(CollectMode.ASK);
	}

	@Override
	protected void collectFixedIncome() {
		int[] add = this.getEssenceCount().clone();
		for (int i = 0; i < add.length; i++) {
			if (add[i] > 0) {
				add[i] = 2;
			}
		}
		this.modifyEssence(new EssenceSelection(add), false);
	}
}
