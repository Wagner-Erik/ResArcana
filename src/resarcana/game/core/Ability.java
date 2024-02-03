package resarcana.game.core;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.communication.Server;
import resarcana.game.GameClient;
import resarcana.game.abilities.Discard;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.Drawable;
import resarcana.graphics.Pollable;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;

public abstract class Ability implements Drawable, Pollable {

	/**
	 * Is called when the ability is activate via the GUI
	 * 
	 * @return <code>true</code> if the ability is handled without any additional
	 *         userinput (via {@link GameClient#addSelector(Selector)}),
	 *         <code>false</code> if any userinput is still pending
	 */
	public abstract boolean activate();

	/**
	 * Is called to activate the ability with a predefined userinput (if any is
	 * needed) <br>
	 * This is invoked when receiving actions via {@link Server}-communication
	 * 
	 * @param overwrite the userinput to use
	 * @return a {@link HistoryElement} which represents the action done by this
	 *         ability (with the given input)
	 */
	public abstract HistoryElement activateOverwrite(UserInputOverwrite overwrite);

	public static final Vector ABILITY_HITBOX = new Vector(80, 16);
	public static final Color COLOR_AVAILABLE = Color.yellow;
	public static final Color COLOR_SELECTED = Color.green;
	public static final Color COLOR_AVAILABLE_OVER = new Color(255, 255, 0, 100); // transparent yellow
	public static final Color COLOR_SELECTED_OVER = new Color(0, 255, 0, 50); // transparent green

	private final Tappable parent;
	private final Vector relPos;

	private final int idx;

	private final Rectangle relHitbox;

	private boolean mouseOver = false;
	private boolean mouseDownLast = false;

	/**
	 * Creates an ability for a card at some relative position on the card
	 * 
	 * @param parent the card to which this ability belongs
	 * @param relPos the relative postion at which to display the ability-hitbox on
	 *               the card
	 */
	public Ability(Tappable parent, Vector relPos) {
		if (!parent.getGame().constructionAllowed) {
			Log.warn("Trying to construct Ability after stopConstruction() was called");
		}
		this.idx = parent.getGame().abilityIndexer.getNextNumber();
		this.parent = parent;
		this.relPos = relPos;
		this.relHitbox = new Rectangle(relPos, ABILITY_HITBOX);
		// Register for global access later
		parent.getGame().allAbilities.add(this);
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + CommunicationKeys.SEPERATOR_NAME + this.idx
				+ CommunicationKeys.SEPERATOR_NAME + this.getTappable().toString();
	}

	@Override
	public void draw(Graphics g) {
		if (this.isActivable() && !this.getGame().isWaitingForAbility()) {
			if (this.mouseOver) {
				GraphicUtils.drawImage(g, this.relHitbox,
						ResourceManager.getInstance().getImage("misc/ability_hitbox.png"), COLOR_SELECTED_OVER);
			} else {
				GraphicUtils.drawImage(g, this.relHitbox,
						ResourceManager.getInstance().getImage("misc/ability_hitbox.png"), COLOR_AVAILABLE_OVER);
			}
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		this.mouseOver = this.relHitbox.isPointInThis(
				this.getGame().getCamera().getTransformedMousePos(input).sub(this.getTappable().getPosition())
						.rotate((float) (this.getTappable().isTapped() && !(this instanceof Discard)
								? -Math.PI * this.getTappable().getAngle() / 180
								: 0)));
		if (this.mouseOver) {
			if (this.isActivable() && !this.getGame().isWaitingForAbility()) {
				if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
					this.mouseDownLast = true;
				}
				if (!input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
					if (this.mouseDownLast) {
						this.getGame().activate(this);
					}
					this.mouseDownLast = false;
				}
			} else {
				this.mouseDownLast = false;
			}
		} else {
			this.mouseDownLast = false;
		}
	}

	/**
	 * Should be overwriten by {@link Ability} implementations if needed <br>
	 * In the basic form it checks if the {@link Tappable} is untapped in play and
	 * the {@link Player} is active
	 * 
	 * @return <code>true</code> if this ability could currently be correctly used
	 *         with some userinput
	 */
	protected boolean isActivable() {
		return !this.getTappable().isTapped() && this.getTappable().isInPlay() && this.getPlayer().isActive();
	}

	/**
	 * @return the relativate position of this ability to the {@link Tappable}
	 *         parent
	 */
	public Vector getRelPos() {
		return this.relPos;
	}

	/**
	 * @return the {@link Tappable} parent of the ability
	 */
	public Tappable getTappable() {
		return this.parent;
	}

	/**
	 * @return the {@link Player} who owns the {@link Tappable} parent
	 */
	public Player getPlayer() {
		return this.parent.getPlayer();
	}

	/**
	 * @return the {@link Game} this ability takes place in
	 */
	public Game getGame() {
		return this.parent.getGame();
	}

	/**
	 * @return the {@link GameClient} which hosts the {@link Game} this ability
	 *         takes place in
	 */
	public GameClient getGameClient() {
		return this.getTappable().getGameClient();
	}
}
