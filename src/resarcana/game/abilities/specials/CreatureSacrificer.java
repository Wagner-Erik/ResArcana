package resarcana.game.abilities.specials;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.ArtifactSacrificer;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.math.Vector;

public class CreatureSacrificer extends ArtifactSacrificer {

	public static final int MODE_BEAST_DRAGON = 0;
	public static final int MODE_BEAST = 1;
	public static final int MODE_DRAGON = 2;
	public static final int MODE_DEMON = 3;

	private final int mode;

	public CreatureSacrificer(Tappable parent, Vector relPos, int placingMode, int targetMode, int bonusValue,
			EssenceSelection cost, Essences outputType, int fixedValue) {
		super(parent, relPos, placingMode, false, bonusValue, cost, outputType, fixedValue);
		this.mode = targetMode;
	}

	public CreatureSacrificer(Tappable parent, Vector relPos, int placingMode, int targetMode, int bonusValue,
			EssenceSelection cost, Essences outputType) {
		this(parent, relPos, placingMode, targetMode, bonusValue, cost, outputType, 0);
	}

	@Override
	protected boolean canSacrifice(Artifact artifact) {
		switch (this.mode) {
		case MODE_BEAST_DRAGON:
			return artifact.isBeast() || artifact.isDragon();
		case MODE_BEAST:
			return artifact.isBeast();
		case MODE_DRAGON:
			return artifact.isDragon();
		case MODE_DEMON:
			return artifact.isDemon();
		default:
			Log.warn("Unknwon mode " + this.mode + " for " + this);
			return false;
		}
	}

}
