package resarcana.game.utils.userinput;

/**
 * An interface for an object that can process the selection of some
 * {@link Selector} (which has been created in reference to this object:
 * {@link Selector#Selector(Selecting, String)})
 * 
 * @author Erik
 *
 */
public interface Selecting {

	/**
	 * Process a selection from the given {@link Selector}
	 * 
	 * @param sel the selector which has finished its selection
	 */
	public void processSelection(Selector sel);

	/**
	 * Cancel the pending selection from the given {@link Selector}
	 * 
	 * @param sel the selector which requested its cancelation
	 */
	public void cancelSelection(Selector sel);
}
