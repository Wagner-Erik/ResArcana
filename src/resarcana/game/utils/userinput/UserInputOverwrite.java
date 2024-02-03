package resarcana.game.utils.userinput;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.core.Ability;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;

/**
 * Interface information objects to be used by Abilities to delivier some action
 * based on server input instead of user input
 * 
 * The information is stored in form of a Server-transferrable string "code" and
 * classified in a "sourceType" flag, a "source" string and a list of "parts"
 * strings the latter shall be used to construct the needed user input
 * 
 * @author Erik
 *
 */
public class UserInputOverwrite {

	public static final String SOURCE_TAPPABLE = "Tappable";
	public static final String SOURCE_ABILITY = "Ability";
	public static final String SOURCE_PLAYER = "Player";

	public static final int SOURCE_TYPE_ERROR = -1;
	public static final int SOURCE_TYPE_TAPPABLE = 1;
	public static final int SOURCE_TYPE_ABILITY = 2;
	public static final int SOURCE_TYPE_PLAYER = 3;

	private final String code;

	private final int sourceType;
	private final String source;
	private final ArrayList<String> parts;

	public UserInputOverwrite(String code) {
		this.code = code;
		String[] split = this.code.split(CommunicationKeys.SEPERATOR_PARTS);
		String[] split2 = split[0].split(CommunicationKeys.SEPERATOR_VALUES);
		if (split2.length == 2) {
			if (split2[0].equals(SOURCE_TAPPABLE)) {
				this.sourceType = SOURCE_TYPE_TAPPABLE;
			} else if (split2[0].equals(SOURCE_ABILITY)) {
				this.sourceType = SOURCE_TYPE_ABILITY;
			} else if (split2[0].equals(SOURCE_PLAYER)) {
				this.sourceType = SOURCE_TYPE_PLAYER;
			} else {
				Log.error("Invalid UserInputOverwrite source" + split2[0]);
				this.sourceType = SOURCE_TYPE_ERROR;
			}
			this.source = split2[1];
		} else {
			Log.error("Invalid UserInputOverwrite code: " + code);
			this.sourceType = SOURCE_TYPE_ERROR;
			this.source = "";
		}
		this.parts = new ArrayList<String>();
		for (int i = 1; i < split.length; i++) {
			this.parts.add(split[i]);
		}
	}

	public UserInputOverwrite(Tappable source) {
		this(SOURCE_TAPPABLE + CommunicationKeys.SEPERATOR_VALUES + source.toString());
	}

	public UserInputOverwrite(Tappable source, Object elem1) {
		this(SOURCE_TAPPABLE + CommunicationKeys.SEPERATOR_VALUES + source.toString()
				+ CommunicationKeys.SEPERATOR_PARTS + elem1.toString());
	}

	public UserInputOverwrite(Tappable source, Object elem1, Object elem2) {
		this(SOURCE_TAPPABLE + CommunicationKeys.SEPERATOR_VALUES + source.toString()
				+ CommunicationKeys.SEPERATOR_PARTS + elem1.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem2.toString());
	}

	public UserInputOverwrite(Tappable source, Object elem1, Object elem2, Object elem3) {
		this(SOURCE_TAPPABLE + CommunicationKeys.SEPERATOR_VALUES + source.toString()
				+ CommunicationKeys.SEPERATOR_PARTS + elem1.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem2.toString() + CommunicationKeys.SEPERATOR_PARTS + elem3.toString());
	}

	public UserInputOverwrite(Ability source) {
		this(SOURCE_ABILITY + CommunicationKeys.SEPERATOR_VALUES + source.toString());
	}

	public UserInputOverwrite(Ability source, Object elem1) {
		this(SOURCE_ABILITY + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString());
	}

	public UserInputOverwrite(Ability source, Object elem1, Object elem2) {
		this(SOURCE_ABILITY + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString() + CommunicationKeys.SEPERATOR_PARTS + elem2.toString());
	}

	public UserInputOverwrite(Ability source, Object elem1, Object elem2, Object elem3) {
		this(SOURCE_ABILITY + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString() + CommunicationKeys.SEPERATOR_PARTS + elem2.toString()
				+ CommunicationKeys.SEPERATOR_PARTS + elem3.toString());
	}

	public UserInputOverwrite(Player source) {
		this(SOURCE_PLAYER + CommunicationKeys.SEPERATOR_VALUES + source.toString());
	}

	public UserInputOverwrite(Player source, Object elem1) {
		this(SOURCE_PLAYER + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString());
	}

	public UserInputOverwrite(Player source, Object elem1, Object elem2) {
		this(SOURCE_PLAYER + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString() + CommunicationKeys.SEPERATOR_PARTS + elem2.toString());
	}

	public UserInputOverwrite(Player source, Object elem1, Object elem2, Object elem3) {
		this(SOURCE_PLAYER + CommunicationKeys.SEPERATOR_VALUES + source.toString() + CommunicationKeys.SEPERATOR_PARTS
				+ elem1.toString() + CommunicationKeys.SEPERATOR_PARTS + elem2.toString()
				+ CommunicationKeys.SEPERATOR_PARTS + elem3.toString());
	}

	@Override
	public String toString() {
		return this.code;
	}

	public String getCode() {
		return this.code;
	}

	public int getSourceType() {
		return this.sourceType;
	}

	public String getSource() {
		return this.source;
	}

	public ArrayList<String> getParts() {
		return this.parts;
	}
}
