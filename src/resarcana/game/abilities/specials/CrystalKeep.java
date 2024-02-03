package resarcana.game.abilities.specials;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.PowerPlace;
import resarcana.game.utils.EssenceSelection;

public class CrystalKeep extends PowerPlace {

	public CrystalKeep(Game parent) {
		super(parent, "place/place_13.png", 13);
		this.setName("Crystal Keep");
		this.setRawCosts(new EssenceSelection(Essences.ELAN, 4, Essences.LIFE, 4, Essences.CALM, 4, Essences.DEATH, 4,
				Essences.GOLD, 4));
		this.setPoints(5);
	}

	@Override
	public int getPoints() {
		return super.getPoints() + this.getPlayer().getArtifactsInPlay().size() / 2;
	}

}
