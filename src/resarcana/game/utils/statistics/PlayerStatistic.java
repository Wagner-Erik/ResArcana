package resarcana.game.utils.statistics;

import java.util.ArrayList;

public class PlayerStatistic {

	private ArrayList<StatisticsElement> elements;

	public PlayerStatistic() {
		this.elements = new ArrayList<StatisticsElement>();
	}

	public void addStatisticsElement(StatisticsElement elem) {
		this.elements.add(elem);
	}

	public int[] getValues(StatisticProperties property) {
		int[] out = new int[this.elements.size()];
		for (int i = 0; i < out.length; i++) {
			out[i] = this.elements.get(i).getValue(property);
		}
		return out;
	}

	public StatisticsElement getElement(int idx) {
		return this.elements.get(idx);
	}

	public int size() {
		return this.elements.size();
	}
}
