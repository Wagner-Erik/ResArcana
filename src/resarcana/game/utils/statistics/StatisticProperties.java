package resarcana.game.utils.statistics;

import resarcana.game.utils.userinput.ImageHolder;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public enum StatisticProperties implements ImageHolder, InterfaceFunction {

	POINTS("Points"), POWER_PLACES("Places"), MONUMENTS("Monuments"),

	VALUE_IN_PLAY("Value"), CARDS_IN_PLAY("Inplay"), CARDS_IN_HAND("Hand"),

	NON_GOLD("Nongold"), GOLD("Gold"), HIGHEST_ESSENCE("Highest"), TOTAL_ESSENCES("Total");

	public static final Rectangle STATISTIC_HITBOX = new Rectangle(Vector.ZERO, 100, 100);
	
	private final String identifier;
	private final String image;

	private StatisticProperties(String identifier) {
		this.identifier = identifier;
		this.image = "statistics/statistics_" + identifier.toLowerCase() + ".png";
		Scheduler.getInstance().scheduleResource(this.image);
	}

	@Override
	public String toString() {
		return this.identifier;
	}

	@Override
	public String getImage() {
		return this.image;
	}

	@Override
	public Rectangle getHitbox() {
		return STATISTIC_HITBOX;
	}

	@Override
	public String getKindOfParent() {
		return "Statistics";
	}

	@Override
	public String getFunctionName() {
		return this.identifier;
	}
}
