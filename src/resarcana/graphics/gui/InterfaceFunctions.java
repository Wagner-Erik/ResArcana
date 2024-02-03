package resarcana.graphics.gui;

/**
 * @author Erik Wagner
 * 
 */
public enum InterfaceFunctions implements InterfaceFunction {

	ERROR(), NONE(),

	INTERFACE_BUTTONLIST_BACK(), INTERFACE_BUTTONLIST_FORTH(),

	INTERFACE_TEXTBUTTONLIST_UP(), INTERFACE_TEXTBUTTONLIST_DOWN(),

	INTERFACE_TEXTFIELD(), INTERFACE_LABEL(), INTERFACE_TEXTFIELD_VECTOR_X(), INTERFACE_TEXTFIELD_VECTOR_Y(),
	INTERFACE_NUMBER_COUNTER(),

	DIALOG_CLOSE(), INTERFACE_NUMBER_SELECTION_BACK(), INTERFACE_NUMBER_SELECTION_FORTH(),

	GAME_EXIT(), JOIN_GAME(), CREATE_SERVER(), ADD_CLIENT(), BACK_TO_MAIN(),

	CLIENT_READY(), CLIENT_NAME(), CLIENT_SHOW_SETTINGS(), CLIENT_CONNECT(), CLIENT_SHOW_DECK(), CLIENT_SHOW_RULES(),
	CLIENT_SHOW_HISTORY(), CLIENT_VOLUME_SOUND(), CLIENT_VOLUME_MUSIC(), CLIENT_SHOW_LOGBOX(), CLIENT_SHOW_STATISTICS(),
	CLIENT_DISCONNECT(), CLIENT_LOAD_SAVE(),

	SERVER_START_GAME(),

	SELECTOR_FINISH_SELECTION(), SELECTOR_HIDE_CONTENT(), SELECTOR_CANCEL(), SELECTOR_RESET(),

	IMAGESELECTOR_SELECT(),

	COLLECTSELECTOR_VALUE(),

	IMAGEVIEWER_SCALE(),

	STATISTICS_EXPORT(), 
	
	HISTORY_ELEMENT();

	private InterfaceFunctions() {
	}

	/**
	 * @return Die Art des Objekts, dem diese Funktion zugeteilt wurde</br>
	 *         Der String ist immer im UpperCase ({@link String#toUpperCase()})
	 *         </br>
	 *         Beispiele: {@code EDITOR} oder {@code INTERFACE}</br>
	 *         {@code ERROR}, wenn dieses Objekt das {@code ERROR}-Objekt ist
	 */
	@Override
	public String getKindOfParent() {
		return this.toString().split("_")[0].toUpperCase();
	}

	/**
	 * @return Der Name der Funktion, für den dieses Enum-Objekt steht.</br>
	 *         Für {@code EDITOR} und {@code FIGURE} entspricht dies einem
	 *         Klassenname für ein zuerstellendes/auszuwählendes Objekt (Beispiel:
	 *         {@code WalkingSoldier}
	 */
	@Override
	public String getFunctionName() {
		String re = "";
		String[] split = this.toString().split("_");
		if (split.length > 1) {
			for (int i = 1; i < split.length; i++) {
				re += split[i].charAt(0) + split[i].toLowerCase().substring(1);
			}
		}
		return re;
	}
}
