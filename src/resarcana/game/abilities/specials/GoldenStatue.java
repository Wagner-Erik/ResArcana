package resarcana.game.abilities.specials;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Monument;

public class GoldenStatue extends Monument {

	public GoldenStatue(Game parent) {
		super(parent, "monument/monument_01.png");
		this.setName("Golden Statue");
	}

	@Override
	public int getPoints() {
		return this.getPlayer().getEssenceCounter().getCount()[Essences.GOLD.ordinal()] >= 3 ? 4 : 1;
	}
}
