package resarcana.game.abilities.specials;

import java.util.ArrayList;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.PowerPlace;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;

public class GateOfHell extends PowerPlace {

	public GateOfHell(Game parent) {
		super(parent, "place/place_11.png", 11);
		this.setName("Gate of Hell");
		this.setRawCosts(new EssenceSelection(Essences.ELAN, 6, Essences.DEATH, 3));
		this.setPoints(0);
		this.setPointsPerEssence(Essences.DEATH, 1);
	}

	@Override
	public int getPoints() {
		ArrayList<Tappable> inplay = this.getPlayer().getTappablesInPlay();
		int demons = 0;
		for (Tappable tappable : inplay) {
			if (tappable.isDemon()) {
				demons++;
			}
		}
		return super.getPoints() + demons;
	}

}
