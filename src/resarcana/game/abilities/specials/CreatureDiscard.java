package resarcana.game.abilities.specials;

import java.util.ArrayList;

import resarcana.game.abilities.Discard;
import resarcana.game.core.Artifact;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class CreatureDiscard extends Discard {

	public CreatureDiscard(Tappable parent, Vector relPos, boolean tapping, EssenceSelection reward) {
		super(parent, relPos, tapping, reward);
	}

	@Override
	protected ArrayList<Artifact> getTargets() {
		ArrayList<Artifact> targets = new ArrayList<Artifact>();
		for (Artifact artifact : this.getPlayer().getHand()) {
			if (artifact.isBeast() || artifact.isDragon() || artifact.isDemon()) {
				targets.add(artifact);
			}
		}
		return targets;
	}
}
