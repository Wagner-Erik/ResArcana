package resarcana.game.abilities.specials;

import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Monument;
import resarcana.game.utils.EssenceSelection;

public class DarkCathedral extends Monument {

	public DarkCathedral(Game parent) {
		super(parent, "monument/monument_11.png");
		this.setName("Dark Cathedral");
		this.setPoints(1);
		this.setRawCosts(new EssenceSelection(Essences.GOLD, 4));
	}

	@Override
	public int getPoints() {
		for (Artifact demon : this.getPlayer().getDemons()) {
			if (!demon.isTapped()) {
				return 3;
			}
		}
		if (this.getPlayer().isActive()) {
			if (this.getPlayer().getMagicItem() instanceof Illusion && !this.getPlayer().getMagicItem().isTapped()) {
				return 3;
			}
		}
		return 2;
	}
}
