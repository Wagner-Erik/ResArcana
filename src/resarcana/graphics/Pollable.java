package resarcana.graphics;

import org.newdawn.slick.Input;

/**
 * Schnittstelle für alle Objekte, die sich mit der Zeit entwickeln, sich
 * bewegen oder aus einem anderen Grund über die vergangene Zeit informiert
 * werden sollen.
 * 
 */
public interface Pollable {

	/**
	 * Wird regelmäßig aufgerufen.
	 * 
	 * @param secounds Seit dem letzten Aufruf vergangene Zeit in Sekunden.
	 */
	public void poll(Input input, float secounds);
}
