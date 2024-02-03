package resarcana.game.abilities.specials;

import java.util.ArrayList;

import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.PowerPlace;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;

public class SorcerersBestiary extends PowerPlace {

	public SorcerersBestiary(Game parent) {
		super(parent, "place/place_08.png", 8);
		this.setName("Sorcerers Bestiary");
		this.setRawCosts(new EssenceSelection(Essences.LIFE, 4, Essences.ELAN, 2, Essences.CALM, 2, Essences.DEATH, 2));
		this.setPoints(0);
	}

	@Override
	public int getPoints() {
		ArrayList<Tappable> inplay = this.getPlayer().getTappablesInPlay();
		int beasts = 0, dragons = 0;
		for (Tappable tappable : inplay) {
			if (tappable.isBeast()) {
				beasts++;
			}
			if (tappable.isDragon()) {
				dragons++;
			}
		}
		return beasts + 2 * dragons;
	}

}
