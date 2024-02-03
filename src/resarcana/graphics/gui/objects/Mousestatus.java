package resarcana.graphics.gui.objects;

public enum Mousestatus {

	/**
	 * Die Maus befindet sich nicht über dem Objekt
	 */
	STATUS_NOTHING,
	/**
	 * Die Maus befindet sich über dem Objekt, ohne dass die linke Maustaste
	 * gedrückt wurde
	 */
	STATUS_MOUSE_OVER,
	/**
	 * Es wurde gerade mit der linken Maustaste auf das Objekt geklickt
	 */
	STATUS_LEFT_PRESSED,
	/**
	 * Die Maus befindet sich über dem Objekt, die linke Maustaste ist gedrückt
	 */
	STATUS_LEFT_DOWN,
	/**
	 * Die Maus befindet sich über dem Objekt, die linke Maustaste war letzten Frame
	 * gedrückt und wurde jetzt losgelassen
	 */
	STATUS_LEFT_RELEASED,
	/**
	 * Es wurde gerade mit der rechten Maustaste auf das Objekt geklickt
	 */
	STATUS_RIGHT_PRESSED,
	/**
	 * Die Maus befindet sich über dem Objekt, die rechten Maustaste ist gedrückt
	 */
	STATUS_RIGHT_DOWN,
	/**
	 * Die Maus befindet sich über dem Objekt, die rechte Maustaste war letzten
	 * Frame gedrückt und wurde jetzt losgelassen
	 */
	STATUS_RIGHT_RELEASED,
	/**
	 * Die Maus befindet sich über dem Objekt, die rechte und linke Maustaste sind
	 * gedrückt
	 */
	STATUS_BOTH_DOWN;

}
