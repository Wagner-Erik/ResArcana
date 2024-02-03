package resarcana.game.utils;

import resarcana.game.core.Artifact;
import resarcana.game.utils.userinput.ImageHolder;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Rectangle;
import resarcana.utils.Parameter;

public enum DraftModes implements ImageHolder {

	RANDOM(Parameter.DEFAULT_CARDS_PER_DECK, 1, 0, 0, "random"),
	RANDOM_BIG(Parameter.DEFAULT_CARDS_PER_DECK + 2, 1, 0, 0, "random_big"),
	STANDARD(Parameter.DEFAULT_CARDS_PER_DECK, 1, 0, 0, "standard"),

	DOUBLE_ROUND(Parameter.DEFAULT_CARDS_PER_DECK, 2, 0, 0, "double"),
	REMOVE_ONE(Parameter.DEFAULT_CARDS_PER_DECK, 1, 1, 0, "remove"),
	REMAIN_ONE(Parameter.DEFAULT_CARDS_PER_DECK, 1, 0, 1, "remain"),
	REMOVE_REMAIN(Parameter.DEFAULT_CARDS_PER_DECK, 1, 1, 1, "remove_remain");

	private static final Rectangle HITBOX = Artifact.ARTIFACT_HITBOX;

	public final int deck, round, remove, remain;
	public final String image;

	private DraftModes(int deck, int round, int remove, int remain, String imageIdentifier) {
		this.deck = deck;
		this.round = round;
		this.remove = remove;
		this.remain = remain;
		this.image = "drafts/drafts_" + imageIdentifier + ".png";
		Scheduler.getInstance().scheduleResource(this.image);
	}

	@Override
	public String getImage() {
		return this.image;
	}

	@Override
	public Rectangle getHitbox() {
		return HITBOX;
	}
}