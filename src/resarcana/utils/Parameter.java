package resarcana.utils;

/**
 * Eine Klasse, in der Konstanten für alle anderen Klassen aus Jumpnevolve
 * gesammelt werden, damit sie an einer zentralen Stelle eingesehen und geändert
 * werden können. Gemeint sind nur Konstanten, die einstellbare Werte angeben,
 * wie Größen und Abstände, <b>nicht</b> solche die z.B. nur verschiedene Modi
 * im Code sichtbar kennzeichnen.
 * <p>
 * Die Konstanten sind folgendermaßen zu benennen:
 * <p>
 * LetzterTeilDesPaketNamens_GekürzterKlassenName_NameDerKonstante
 * <p>
 * Beispiel: GUI_BUTTON_DIMENSION
 * <p>
 * Hier wurde Button statt InterfaceButton als gekürzter Klassenname verwendet
 * <p>
 * Zum besseren Verständnis sollten die Konstanten mit einem kurzen Hinweis über
 * ihre Funktion beschrieben werden.
 * 
 * @author Erik Wagner
 * 
 */
public class Parameter {

	/**
	 * Die gewünschte FPS-Rate, die von der Engine angestrebt werden soll
	 */
	public static final int GAME_FPS_TARGET = 100;

	public static final int GAME_FPS_MINIMUM = 10;

	/**
	 * Die Größe eines Buttons für das Interface
	 */
	public static final float GUI_BUTTON_DIMENSION = 50.0f;

	/**
	 * Die normale Schrittgröße beim Hoch-/Runterzählen der aktuellen Zahl für die
	 * NumberSelection
	 */
	public static final int GUI_NUMBERSELECTION_DEFAULTSTEP = 1;

	/**
	 * Die Größe einer Checkbox für das Interface
	 */
	public static final float GUI_CHECKBOX_SIZE = 20.0f;

	/**
	 * Das Eingabedelay für Textfelder
	 */
	public static final float GUI_TEXTFIELD_DELAY = 0.1f;

	/**
	 * Die Standard-Größe der benutzten Schriftart
	 */
	public static final int GUI_STANDARD_FONT_SIZE = 24;

	public static final int DEFAULT_CARDS_PER_DECK = 8;

	public static final String GAME_NAME = "Res Arcana";
}
