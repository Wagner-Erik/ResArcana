package resarcana.graphics.utils;

import org.newdawn.slick.Input;

import resarcana.graphics.Pollable;

/**
 * <p>
 * Dieser Timer hilft dabei Zeitabschnitte zu messen.
 * </p>
 * 
 * <p>
 * Die Genaugkeit hängt von der Rechenleistung des PCs ab, ist aber auf keinen
 * Fall für exakte Berechnungen geeignet.
 * </p>
 * 
 * <p>
 * Ein Timer hat die Zustände laufend und nicht laufend. Um zwischen diesen
 * Zuständen zu wechseln, kann er gestartet oder gestoppt werden. Nach dem
 * erzeugen läuft er noch <strong>nicht</strong>. Sobald die eingestellte Zeit
 * abgelaufen ist, wird der Timer gestoppt.
 * </p>
 * 
 */
public class Timer implements Pollable {

	private float remainingTime = 0;

	private float startingTime = 0;

	private boolean running = false;

	@Override
	public void poll(Input input, float secounds) {
		if (this.running) {
			this.remainingTime -= secounds;
			if (didFinish()) {
				stop();
				onFinish();
			}
		}
	}

	/**
	 * Erzeugt einen Timer, der noch nicht läuft und noch keine Zeit eingestellt
	 * hat.
	 */
	public Timer() {

	}

	/**
	 * Erzeugt einen Timer, der noch nicht läuft.
	 * 
	 * @param time Einzustellende Zeit in Sekunden.
	 */
	public Timer(float time) {
		this.remainingTime = time;
		this.startingTime = time;
	}

	/**
	 * Startet den Timer.
	 * 
	 * @param time Einzustellende Zeit in Sekunden.
	 */
	public void start(float time) {
		this.remainingTime = time;
		this.startingTime = time;
		this.running = true;
	}

	/**
	 * Startet den Timer mit der vorher eingestellten Zeit.
	 */
	public void start() {
		this.running = true;
	}

	/**
	 * Startet den Timer mit der ursprünglichen Startzeit erneut.
	 */
	public void restart() {
		this.remainingTime = this.startingTime;
		this.running = true;
	}

	/**
	 * Hält den Timer an. Er kann jederzeit wieder gestartet werden.
	 */
	public void stop() {
		this.running = false;
	}

	/**
	 * @return {@code true}, wenn der Timer läuft.
	 */
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * @return {@code true}, wenn die eingestellte Zeit abgelaufen ist.
	 */
	public boolean didFinish() {
		return this.remainingTime < 0;
	}

	/**
	 * @return Die verbleibende Zeit in Sekunden, bis der Timer abläuft, wenn der
	 *         eingeschaltet ist.
	 */
	public float getRemainingTime() {
		return this.remainingTime;
	}

	/**
	 * Stellt die Zeit neu ein.
	 * 
	 * @param time Einzustellende Zeit in Sekunden
	 */
	public void setTime(float time) {
		this.remainingTime = time;
	}

	/**
	 * Wird aufgerufen, wenn die Zeit abgelaufen ist.
	 */
	protected void onFinish() {
	}

	/**
	 * @return Die Zeit, mit der der Timer gestartet wurde
	 */
	public float getStartingTime() {
		return this.startingTime;
	}
}
