package resarcana.graphics;

import org.lwjgl.opengl.Display;
import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.GameState;
import org.newdawn.slick.state.StateBasedGame;

import resarcana.math.Rectangle;
import resarcana.utils.Parameter;

public class SlickEngine extends AppGameContainer implements AbstractEngine {

	private static SlickEngine instance;

	/**
	 * @return Die geteilte Instanz der Grafikengine.
	 * 
	 * @throws GraphicsError Wenn ein OpenGL Fehler beim ersten Erzeugen der Instanz
	 *                       auftrat
	 */
	public static SlickEngine getInstance() {
		if (instance == null) {
			try {
				instance = new SlickEngine(new StateBasedGame(Parameter.GAME_NAME) {
					@Override
					public void initStatesList(GameContainer container) throws SlickException {
					}
				});
			} catch (SlickException e) {
				throw new GraphicsError(e);
			}
			Engine.makeCurrent(instance);
		}
		return instance;
	}

	private StateBasedGame states;

	private Rectangle box = new Rectangle(0, 0, 1600, 900);

	private SlickEngine(StateBasedGame states) throws SlickException {
		super(states);

		this.states = states;
	}

	@Override
	public void addState(AbstractState state) {
		if (!containsState(state)) {
			this.states.addState(state);
		}
	}

	@Override
	public boolean containsState(AbstractState state) {
		return this.states.getState(state.getID()) != null;
	}

	@Override
	public void switchState(AbstractState state) {
		if (this.states.getState(state.getID()) == null) {
			this.states.addState(state);
		}
		if (this.states.getCurrentStateID() != state.getID()) {
			this.states.enterState(state.getID());
		}
	}

	@Override
	public AbstractState getCurrentState() {
		GameState state = this.states.getCurrentState();
		if (!(state instanceof AbstractState)) {
			throw new RuntimeException("The current state does not inherit AbstractState.");
		}
		return (AbstractState) state;
	}

	@Override
	public void start() {
		this.box = new Rectangle(0, 0, this.getWidth(), this.getHeight());
		// Anwendung starten
		try {
			super.start();
		} catch (SlickException e) {
			throw new GraphicsError(e);
		}
	}

	/*
	 * Copied from super#gameLoop() to NOT catch and overwrite exceptions from updateAndRender
	 */
	@Override
	protected void gameLoop() throws SlickException {
		int delta = getDelta();
		if (!Display.isVisible() && this.updateOnlyOnVisible) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
			}
		} else {
			updateAndRender(delta);
		}

		updateFPS();

		Display.update();

		if (Display.isCloseRequested()) {
			if (game.closeRequested()) {
				running = false;
			}
		}
	}

	public Rectangle getScreenBox() {
		return this.box;
	}

	public int getFrameNumber() {
		return this.getCurrentState().getFrameNumber();
	}
}
