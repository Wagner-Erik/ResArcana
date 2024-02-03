package resarcana.game.utils;

import java.util.Arrays;
import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.core.Essences;

public class EssenceSelection {

	private static final String IDENTIFIER = "Essences";

	private int[] values;

	private EnumSet<Essences> excludes;

	public EssenceSelection() {
		this.values = new int[Essences.values().length + 1];
		this.excludes = EnumSet.noneOf(Essences.class);
	}

	public EssenceSelection(Essences e1, int v1) {
		this();
		this.setEssenceCount(e1, v1);
	}

	public EssenceSelection(Essences e1, int v1, Essences e2, int v2) {
		this(e1, v1);
		this.setEssenceCount(e2, v2);
	}

	public EssenceSelection(Essences e1, int v1, Essences e2, int v2, Essences e3, int v3) {
		this(e1, v1, e2, v2);
		this.setEssenceCount(e3, v3);
	}

	public EssenceSelection(Essences e1, int v1, Essences e2, int v2, Essences e3, int v3, Essences e4, int v4) {
		this(e1, v1, e2, v2, e3, v3);
		this.setEssenceCount(e4, v4);
	}

	public EssenceSelection(Essences e1, int v1, Essences e2, int v2, Essences e3, int v3, Essences e4, int v4,
			Essences e5, int v5) {
		this(e1, v1, e2, v2, e3, v3, e4, v4);
		this.setEssenceCount(e5, v5);
	}

	public EssenceSelection(int v1, Essences exclude1) {
		this(null, v1);
		this.exclude(exclude1);
	}

	public EssenceSelection(int v1, Essences exclude1, Essences exclude2) {
		this(v1, exclude1);
		this.exclude(exclude2);
	}

	public EssenceSelection(int v1, Essences exclude1, Essences exclude2, Essences exclude3) {
		this(v1, exclude1, exclude2);
		this.exclude(exclude3);
	}

	public EssenceSelection(int[] inputValues) {
		this();
		if (this.values.length - 1 == inputValues.length) {
			for (int i = 0; i < inputValues.length; i++) {
				this.values[i] = inputValues[i];
			}
		} else if (this.values.length == inputValues.length) {
			this.values = inputValues.clone();
		} else {
			Log.error("Inadequat inputValue-length, expected " + (this.values.length - 1) + " or " + this.values.length
					+ " got " + inputValues.length);
		}
	}

	public EssenceSelection(int[] inputValues, EnumSet<Essences> excludes) {
		this(inputValues);
		this.excludes = excludes.clone();
	}

	public EssenceSelection(EnumSet<Essences> excludes) {
		this();
		this.excludes = excludes.clone();
	}

	/**
	 * Generates an EssenceSelection from a String code
	 * 
	 * this will NOT have any excludes set
	 * 
	 * @param code
	 */
	public EssenceSelection(String code) {
		this();
		String[] split = code.split("" + CommunicationKeys.SEPERATOR_VALUES);
		if (split[0].equals(IDENTIFIER) && split.length == this.values.length + 1) {
			for (int i = 1; i < split.length; i++) {
				this.values[i - 1] = Integer.parseInt(split[i]);
			}
		} else {
			Log.error("Could not create EssenceSelection from code: " + code);
		}
	}

	@Override
	public String toString() {
		String out = IDENTIFIER;
		for (int i = 0; i < this.values.length; i++) {
			out += CommunicationKeys.SEPERATOR_VALUES + "" + this.values[i];
		}
		return out;
	}

	private void exclude(Essences exclude) {
		if (exclude != null) {
			this.excludes.add(exclude);
		}
	}

	private void setEssenceCount(Essences essence, int value) {
		if (essence != null) {
			this.values[essence.ordinal()] = value;
		} else {
			this.values[this.values.length - 1] = value;
		}
	}

	/**
	 * 
	 * @return the list of values corresponding to the Essence-count set, the last
	 *         entry represents a "variable" Essence part
	 */
	public int[] getValues() {
		return this.values;
	}

	public boolean isDetermined() {
		return this.getIndeterminedValue() == 0;
	}

	/**
	 * A list of Essences which shall be excluded from the "variable" part of
	 * this.values
	 * 
	 * @return the set of Essences to exclude
	 */
	public EnumSet<Essences> getExcludes() {
		return this.excludes;
	}

	public int getIndeterminedValue() {
		return this.values[this.values.length - 1];
	}

	public int getTotal() {
		return Arrays.stream(this.values).sum();
	}

	public static int[] allEssencesOnce() {
		int[] out = new int[Essences.values().length];
		for (int i = 0; i < out.length; i++) {
			out[i] = 1;
		}
		return out;
	}

	public int getValue(Essences ess) {
		if (ess != null) {
			return this.values[ess.ordinal()];
		} else {
			Log.warn("Requesting null essence");
			return 0;
		}
	}

	public EssenceSelection subtract(EssenceSelection inputRequest) {
		EssenceSelection out = new EssenceSelection(this.values, this.excludes);
		for (int i = 0; i < out.values.length - 1; i++) { // Ignore undetermined value
			out.values[i] -= inputRequest.values[i];
		}
		return out;
	}

	public EssenceSelection excludeAll(EnumSet<Essences> excludes2) {
		EssenceSelection out = new EssenceSelection(this.toString());
		for (Essences ess : this.excludes) {
			out.exclude(ess);
		}
		for (Essences ess : excludes2) {
			if (this.getValue(ess) == 0) {
				out.exclude(ess);
			} else {
				Log.warn("Trying to exclude " + ess.toString() + " while requesting value " + out.getValue(ess)
						+ " > 0");
			}
		}
		return out;
	}

	public EssenceSelection getDifference(EssenceSelection essenceSelection) {
		int[] diff = this.values.clone();
		for (int i = 0; i < diff.length; i++) {
			diff[i] -= essenceSelection.values[i];
		}
		return new EssenceSelection(diff);
	}

	public int getNumberDifferentEssences() {
		int n = 0;
		for (int i = 0; i < this.values.length; i++) {
			if (this.values[i] > 0) {
				n++;
			}
		}
		return n;
	}

	public EssenceSelection neg() {
		int[] values = this.values.clone();
		for (int i = 0; i < values.length; i++) {
			values[i] *= -1;
		}
		return new EssenceSelection(values, this.excludes);
	}

}
