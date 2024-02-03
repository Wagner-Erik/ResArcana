package resarcana.utils;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.Color;

import resarcana.communication.CommunicationKeys;
import resarcana.game.core.Game;
import resarcana.game.core.Tappable;

public class UtilFunctions {

	public static int[] toIntArray(List<Integer> list) {
		int[] out = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			out[i] = list.get(i).intValue();
		}
		return out;
	}

	public static ArrayList<Tappable> StringArrayToTappables(String[] strings, Game parent) {
		ArrayList<Tappable> out = new ArrayList<Tappable>();
		for (int i = 0; i < strings.length; i++) {
			out.add(parent.getTappable(strings[i]));
		}
		return out;
	}

	/**
	 * Create a String containing all T.toString() values in the list concatenate
	 * with {@link CommunicationKeys#SEPERATOR_VALUES}
	 * 
	 * @param <T>  template class, should have a useful {@link #toString()}
	 *             implementation
	 * @param list a list of objects
	 * @return the resulting string
	 */
	public static <T> String ListToString(ArrayList<T> list) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < list.size(); i++) {
			sb.append(list.get(i).toString());
			if (i < list.size() - 1) {
				sb.append(CommunicationKeys.SEPERATOR_VALUES);
			}
		}
		return sb.toString();
	}

	/**
	 * Parse a list of strings to a list of integers
	 * 
	 * @param list the strings
	 * @return the integers
	 */
	public static int[] parseIntAll(String[] list) {
		int[] out = new int[list.length];
		for (int i = 0; i < list.length; i++) {
			out[i] = Integer.parseInt(list[i]);
		}
		return out;
	}

	/**
	 * Get a range of numbers
	 * 
	 * @param min the start of the range, <b>inclusive</b>
	 * @param max the end of the range, <b>exclusive</b>
	 * @return the range
	 */
	public static int[] getRange(int min, int max) {
		int[] out = new int[max - min];
		for (int i = min; i < max; i++) {
			out[i - min] = i;
		}
		return out;
	}

	/**
	 * Add two colors togehter, one in the background, one in the foreground
	 * 
	 * @param bg the background color
	 * @param fg the foreground color
	 * @return the resulting color
	 */
	public static Color addColors(Color bg, Color fg) {
		float r, g, b, a;
		a = 1 - (1 - fg.a) * (1 - bg.a);
//		r = fg.r * fg.a / a + bg.r * bg.a * (1 - fg.a) / a;
//		g = fg.g * fg.a / a + bg.g * bg.a * (1 - fg.a) / a;
//		b = fg.b * fg.a / a + bg.b * bg.a * (1 - fg.a) / a;
		float t = fg.a / a;
		r = (float) Math.sqrt(bg.r * bg.r * (1 - t) + fg.r * fg.r * t);
		g = (float) Math.sqrt(bg.g * bg.g * (1 - t) + fg.g * fg.g * t);
		b = (float) Math.sqrt(bg.b * bg.b * (1 - t) + fg.b * fg.b * t);
		return new Color(r, g, b, a);
	}

}
