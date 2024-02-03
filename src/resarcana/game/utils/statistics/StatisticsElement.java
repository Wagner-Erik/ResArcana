package resarcana.game.utils.statistics;

import java.util.Arrays;

import org.newdawn.slick.util.Log;

public class StatisticsElement {

	private final int[] values;

	public StatisticsElement() {
		this.values = new int[StatisticProperties.values().length];
	}

	@Override
	public String toString() {
		return Arrays.toString(this.values).replace(",", "").replace("[", "").replace("]", "");
	}

	public void setValue(StatisticProperties property, int value) {
		if (property != null) {
			this.values[property.ordinal()] = value;
		} else {
			Log.warn("Putting null property to StatisticsElement: " + value);
		}
	}

	public int getValue(StatisticProperties property) {
		if (property != null) {
			return this.values[property.ordinal()];
		} else {
			Log.warn("Requesting null property from StatisticsElement");
			return 0;
		}
	}

}
