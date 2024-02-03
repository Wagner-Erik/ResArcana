package resarcana.game.abilities.specials;

import resarcana.game.core.Artifact;
import resarcana.game.core.Game;
import resarcana.game.utils.EssenceSelection;

public class VialOfLight extends Artifact {

	public VialOfLight(Game parent) {
		super(parent, "artifact/artifact_50.png");
		this.setName("Vial of Light");
		this.setRawCosts(new EssenceSelection());
	}

}
