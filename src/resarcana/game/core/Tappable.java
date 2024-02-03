package resarcana.game.core;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.GameClient;
import resarcana.game.abilities.Attack;
import resarcana.game.abilities.CostReduction;
import resarcana.game.abilities.Protection;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.animation.Animation;
import resarcana.game.utils.animation.DestroyAnimation;
import resarcana.game.utils.animation.generator.PathFactory;
import resarcana.game.utils.animation.generator.WarpFactory;
import resarcana.game.utils.userinput.CollectEssenceSelector;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageHolder;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.Drawable;
import resarcana.graphics.Pollable;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.SoundManager;
import resarcana.graphics.utils.Timer;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

/**
 * A {@link Tappable} is a card-like object used for {@link Game} that can be
 * {@link #tap()}ed to mark as used.
 * <p>
 * Amongst other propertiet, it holds a list of {@link Ability} which represent
 * the powers useable with this card.
 * <p>
 * <code>implements</code> {@link Drawable}, {@link Pollable}, {@link Selecting}
 * and {@link ImageHolder}
 * 
 * @author Erik
 *
 */
public abstract class Tappable implements Drawable, Pollable, Selecting, ImageHolder {

	/**
	 * @return The raw hitbox of this object, its center should be
	 *         {@link Vector#ZERO}
	 */
	public abstract Rectangle getRawHitbox();

	/**
	 * @return The position at which to draw the {@link EssenceCounter} at, relative
	 *         to the {@link Tappable#getPosition()}
	 */
	public abstract Vector getEssencePosition();

	public static final float TAP_ROTATE = 30;

	private static final Rectangle AUTO_COLLECT_CHECKBOX = new Rectangle(Vector.ZERO, 20, 20);

	public enum CollectMode {
		ALWAYS, NEVER, ASK
	};

	public enum AnimationMode {
		NONE, PLACING, DESTROY, TAP, UNTAP
	};

	private static final Color COST_PAYABLE = new Color(0, 1.f, 0, 0.5f);
	private static final Color COST_NOT_PAYABLE = new Color(1.f, 0, 0, 0.5f);

	private static final float ANIMATION_TIME = 1.0f;

	private static int STATE_IDLE = 0;
	private static int STATE_COLLECT = 1;
	private static int STATE_INCOME = 2;
	private static int STATE_COLLECT_INCOME = 3;

	private final Game parent;
	private final String image;
	private final int idx;

	private String name = null;

	private Player player = null;
	private Vector position;
	private Rectangle hitbox, collectHitbox;

	private boolean tapped = false;
	private int state = STATE_IDLE;
	private boolean collectResult = false;
	private ArrayList<Ability> abilities = new ArrayList<Ability>();
	private EssenceCounter counter;

	private boolean mouseDownCollect = false;

	// General card specifications
	private boolean beast = false, dragon = false, demon = false;
	private int points = 0;
	private EssenceSelection rawCosts = new EssenceSelection(), income = null;
	private int costNum = 1;
	private CollectMode collectMode = CollectMode.ALWAYS;

	// Passive abilities, set internally after adding abilities
	private Protection protection = null;
	private CostReduction costReduction = null;

	private AnimationMode animation = AnimationMode.NONE;
	private Timer animationTime = new Timer(ANIMATION_TIME);
	private float scale = 1, tapAngle = 0;

	private Animation destroyAnimation;

	/**
	 * Create a tappable card
	 * 
	 * @param parent       the game the card is created for
	 * @param image        the image of the card
	 * @param counterScale the {@link EssenceCounter} scale to use
	 */
	public Tappable(Game parent, String image, float counterScale) {
		if (!parent.constructionAllowed) {
			Log.warn("Trying to construct Tappable after stopConstruction() was called");
		}
		this.idx = parent.tappableIndexer.getNextNumber();
		this.parent = parent;
		this.image = image;
		this.counter = new EssenceCounter(this, counterScale, EssenceCounter.BOXES_COLUMN, false, false);
		this.setPosition(Vector.ZERO);

		this.destroyAnimation = new DestroyAnimation(this.getRawHitbox().modifyCenter(Vector.ZERO), this.image,
				ANIMATION_TIME,
				PathFactory.getArcPathBetweenPoints(this.getRawHitbox().getPositionAtAng((float) Math.PI * 1.7f),
						this.getRawHitbox().getPositionAtAng((float) Math.PI * 0.5f), 0, 0.2f),
				this.hitbox.height / 20, WarpFactory.modifySpeedSmooth(WarpFactory.getDestroyWarpUp(), 0, 2.5f),
				WarpFactory.getStaticWarper(), false);

		// Register for global access later
		parent.allTappables.add(this);
	}

	/**
	 * Set the name of the card
	 * 
	 * @param name the name
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		if (this.name != null) {
			return this.name + CommunicationKeys.SEPERATOR_NAME + this.idx;
		} else {
			return super.toString();
		}
	}

	/**
	 * @return the name of this card
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Set the abilities of this card if <b>not</b> already set
	 * 
	 * @param abilities all abilities of the card
	 */
	public void setAbilities(ArrayList<Ability> abilities) {
		if (this.abilities.size() == 0) {
			this.abilities = abilities;
			for (Ability ability : abilities) {
				if (ability instanceof Protection) {
					this.protection = (Protection) ability;
				}
				if (ability instanceof CostReduction) {
					this.costReduction = (CostReduction) ability;
				}
			}
		} else {
			Log.warn("Abilities already set for " + this);
		}
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		g.translate(this.position.x, this.position.y);
		g.rotate(0, 0, this.tapAngle);

		// Draw image and if in hand the payability marker
		if (this.getPlayer() != null && this.getPlayer().isInHand(this)) { // in hand
			if (this.getPlayer().isMouseOverHand() || this.getPlayer().isActive()) { // mouse over or active

				GraphicUtils.drawImage(g, this.getRawHitbox(), ResourceManager.getInstance().getImage(this.image));

				if (this.getPlayer().isPayable(this)) {
					GraphicUtils.drawImage(g, this.getRawHitbox(),
							ResourceManager.getInstance().getImage("misc/glow_cost_" + this.costNum + ".png"),
							COST_PAYABLE);
				} else {
					GraphicUtils.drawImage(g, this.getRawHitbox(),
							ResourceManager.getInstance().getImage("misc/glow_cost_" + this.costNum + ".png"),
							COST_NOT_PAYABLE);
				}
			} else { // inactive and not mouse over
				GraphicUtils.drawImage(g, this.getRawHitbox(), ResourceManager.getInstance().getImage(this.image),
						Player.HAND_TRANSPARENCY);
			}
		} else { // not in hand
			if (this.destroyAnimation.isRunning()) {
				this.destroyAnimation.draw(g);
			} else {
				GraphicUtils.drawImage(g, this.getRawHitbox().scale(this.scale),
						ResourceManager.getInstance().getImage(this.image));
			}
		}

		// Draw additional indicators only when in play and while no animation is
		// running
		if (this.isInPlay()) {
			// Draw essence counter
			this.counter.drawAt(g, this.getEssencePosition());
			if (!this.isAnimationRunning()) {
				// Indicator for the selected collectMode
				if (this.getPlayer().isClientPlayer()) {
					switch (this.collectMode) {
					case ALWAYS:
						GraphicUtils.fill(g, this.getCollectCheckbox());
						break;
					case NEVER:
						GraphicUtils.draw(g, this.getCollectCheckbox());
						break;
					case ASK:
						GraphicUtils.draw(g, this.getCollectCheckbox());
						g.setFont(FontManager.getInstance().getFont((int) (this.getCollectCheckbox().height * 0.8f
								/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(), "!")
								* Parameter.GUI_STANDARD_FONT_SIZE)));
						GraphicUtils.drawStringCentered(g, this.getCollectCheckbox().getCenter().add(0, -3), "!");
						break;
					default:
						Log.error("Unknown collectMode: " + this.collectMode);
						break;
					}
				}
				// This is needed to keep the hitboxes of the discard abilities correct
				if (this instanceof Mage) {
					g.rotate(0, 0, -this.tapAngle);
				}

				// Draw abilities last and only if the player is active
				if (this.getPlayer().isActive()) {
					for (Ability a : this.abilities) {
						a.draw(g);
					}
				}
			}
		}

		g.popTransform();
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.getPlayer() != null) {
			if (this.isInPlay()) {
				if (this.getPlayer().isActive()) {
					if (this.getCollectCheckbox().isPointInThis(
							this.getGame().getCamera().getTransformedMousePos(input).sub(this.getPosition())
									.rotate((float) (this.isTapped() ? -Math.PI * this.tapAngle / 180 : 0)))) {
						if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
							this.mouseDownCollect = true;
						}
						if (!input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON)) {
							if (this.mouseDownCollect) {
								this.cycleCollectModus();
							}
							this.mouseDownCollect = false;
						}
					} else {
						this.mouseDownCollect = false;
					}
					for (Ability a : this.abilities) {
						a.poll(input, secounds);
					}
				}
				// Poll essence-counter
				this.counter.poll(input, secounds);
			}
		}
		this.pollDetailedView(input, secounds);
		this.pollAnimation(input, secounds);
	}

	/**
	 * Poll whether this card should be shown via
	 * {@link GameClient#setDetailedCard(Image)}
	 * 
	 * @param input    the user input
	 * @param secounds the time since the last poll-loop
	 */
	public void pollDetailedView(Input input, float secounds) {
		if (!this.getGameClient().getGameState().isMouseBlockedByGUI()) {
			if (input.isKeyDown(Input.KEY_LALT)) {
				Vector cameraPos = this.getGame().getCamera().getPosition();
				float zoom = this.getGame().getCamera().getZoom();
				if (this.getPlayer() != null) {
					if (this.getPlayer().isInHand(this)) {
						if (!this.getPlayer().isClientPlayer()) { // do not show cards from other player's hands
							return;
						}
						cameraPos = Vector.ZERO;
						zoom = 1;
					}
				}
				if (this.getHitbox().isPointInThis(cameraPos.add(input.getMouseX() / zoom, input.getMouseY() / zoom))) {
					this.getGameClient().setDetailedCard(this.image);
				}
			}
		}
	}

	/**
	 * Poll the animation for this card, if any {@link #isAnimationRunning()}
	 * 
	 * @param input    the user input
	 * @param secounds the time since the last poll-loop
	 */
	public void pollAnimation(Input input, float secounds) {
		this.destroyAnimation.poll(input, secounds);
		this.animationTime.poll(input, secounds);
		if (this.animationTime.didFinish()) {
			if (this.animation == AnimationMode.DESTROY) {
				this.getPlayer().removeDestroyed(this);
			}
			this.setAnimation(AnimationMode.NONE);
		}
		switch (this.animation) {
		case PLACING:
			this.scale = 0.3f * this.animationTime.getRemainingTime() / this.animationTime.getStartingTime() + 1;
			break;
		case DESTROY:
			// this.scale = 0.3f * (1 - this.animationTime.getRemainingTime() /
			// this.animationTime.getStartingTime()) + 1;
			break;
		case TAP:
			this.tapAngle = (float) (TAP_ROTATE
					* (1 - Math.pow(this.animationTime.getRemainingTime() / this.animationTime.getStartingTime(), 2)));
			break;
		case UNTAP:
			this.tapAngle = (float) (TAP_ROTATE * (1
					- Math.pow(1 - this.animationTime.getRemainingTime() / this.animationTime.getStartingTime(), 2)));
			break;
		case NONE:
		default:
			this.scale = 1;
			this.tapAngle = this.isTapped() ? TAP_ROTATE : 0;
			break;
		}
	}

	/**
	 * Cycle through the different essence-collection modes
	 * <p>
	 * {@link #ALWAYS} -> {@link #NEVER} -> {@link #ASK} -> {@link #ALWAYS}
	 */
	private void cycleCollectModus() {
		switch (this.collectMode) {
		case ALWAYS:
			this.getGameClient().informAllClients_Control(
					new UserInputOverwrite(this, CommunicationKeys.VALUE_CHANGE_COLLECT, CollectMode.NEVER));
			break;
		case NEVER:
			this.getGameClient().informAllClients_Control(
					new UserInputOverwrite(this, CommunicationKeys.VALUE_CHANGE_COLLECT, CollectMode.ASK));
			break;
		case ASK:
			this.getGameClient().informAllClients_Control(
					new UserInputOverwrite(this, CommunicationKeys.VALUE_CHANGE_COLLECT, CollectMode.ALWAYS));
			break;
		default:
			Log.error("Unknown collectMode: " + this.collectMode);
			break;
		}
	}

	/**
	 * @return the hitbox of the card, centered around its position in the game
	 *         ({@link #getPosition()})
	 */
	@Override
	public Rectangle getHitbox() {
		return this.hitbox;
	}

	/**
	 * @return the hitbox for the automatic essence-collect selection
	 */
	private Rectangle getCollectCheckbox() {
		return this.collectHitbox;
	}

	/**
	 * Assign the ownership of this card to a player
	 * 
	 * @param player the new owner of this card
	 */
	public void assignPlayer(Player player) {
		this.player = player;
	}

	/**
	 * Make this card a beast-type
	 */
	public void makeBeast() {
		this.beast = true;
	}

	/**
	 * @return <code>true</code> if this card is a beast (via {@link #makeBeast()})
	 */
	public boolean isBeast() {
		return this.beast;
	}

	/**
	 * Make this card a dragon-type
	 */
	public void makeDragon() {
		this.dragon = true;
	}

	/**
	 * @return <code>true</code> if this card is a dragon (via
	 *         {@link #makeDragon()})
	 */
	public boolean isDragon() {
		return this.dragon;
	}

	/**
	 * Make this card a demon-type
	 */
	public void makeDemon() {
		this.demon = true;
	}

	/**
	 * @return <code>true</code> if this card is a demon (via {@link #makeDemon()})
	 */
	public boolean isDemon() {
		return this.demon;
	}

	/**
	 * Set the flat victory points of this card
	 * 
	 * @param points the amount of flat points, has to be > 0
	 */
	public void setPoints(int points) {
		if (points >= 0) {
			this.points = points;
		}
	}

	/**
	 * @return the victory points this card is worth if in play
	 */
	public int getPoints() {
		return this.points;
	}

	/**
	 * Set the raw cost of this card. Raw means unmodified by any
	 * {@link CostReduction} or similar
	 * 
	 * @param costs the price of this card to play
	 */
	public void setRawCosts(EssenceSelection costs) {
		this.rawCosts = costs;
		this.costNum = Math.max(1, this.rawCosts.getNumberDifferentEssences());
	}

	/**
	 * @return the unmodified price of this card
	 */
	public EssenceSelection getRawCost() {
		return this.rawCosts;
	}

	/**
	 * @return the effective price of this card after being modified by its owners
	 *         {@link Player#getCostReducers()}
	 */
	public EssenceSelection getCost() {
		return this.getCost(this.getPlayer());
	}

	/**
	 * @param player the player which to get the effective price of this card for
	 * @return the effective price of this card after being modified by the players
	 *         {@link Player#getCostReducers()}
	 */
	public EssenceSelection getCost(Player player) {
		EssenceSelection cost = this.getRawCost();
		if (player != null) {
			for (Tappable tappable : player.getCostReducers()) {
				cost = tappable.costReduction.reduceCost(this, cost);
			}
		}
		return cost;
	}

	/**
	 * @return <code>true</code> if this card has an {@link CostReduction} ability
	 */
	public boolean canCostReduce() {
		return this.costReduction != null;
	}

	/**
	 * @return <code>true</code> if this card is currently in play (for its owner)
	 */
	public boolean isInPlay() {
		if (this.player == null) {
			return false;
		} else {
			return this.player.isInPlay(this);
		}
	}

	/**
	 * @param origin the origin card of the attack
	 * @return <code>true</code> if this card can provide protection againt the
	 *         attack
	 */
	public boolean canProtectFrom(Tappable origin) {
		if (this.canProtect()) {
			return this.protection.canProtectFrom(origin);
		} else {
			return false;
		}
	}

	/**
	 * @return <code>true</code> if this card has a {@link Protection} ability
	 *         <p>
	 *         Use {@link #canProtectFrom(Tappable)} to get information against a
	 *         specific attack
	 */
	public boolean canProtect() {
		return this.protection != null;
	}

	/**
	 * Use this card as a protection, should only be used after checking
	 * {@link #canProtectFrom(Tappable)}
	 */
	public void protect(Attack attack) {
		if (this.canProtect()) {
			this.protection.protect(attack);
		} else {
			Log.warn("Triggering protection on " + this + " without protection ability");
		}
	}

	/**
	 * @return <code>true</code> if this card is currently tapped
	 */
	public boolean isTapped() {
		return this.tapped;
	}

	/**
	 * Tap this card, effectively disallowing the activation of any* of its
	 * abilities
	 * <p>
	 * *some abilities might ignore this
	 */
	public void tap() {
		if (this.tapped) {
			Log.error("Tapping " + this + " while it is already tapped");
		} else {
			this.tapped = true;
			this.setAnimation(AnimationMode.TAP);
			SoundManager.getInstance().playTap();
		}
	}

	/**
	 * Untap this card
	 */
	public void untap() {
		if (this.tapped && this.getPlayer() != null && this.isInPlay()) {
			this.setAnimation(AnimationMode.UNTAP);
		}
		this.tapped = false;
		this.tapAngle = 0;
	}

	/**
	 * Set the position of this card in the game world
	 * <p>
	 * This will readjust all needed hitboxes of this card
	 * 
	 * @param position the new position of this card
	 */
	public void setPosition(Vector position) {
		this.position = position;
		this.hitbox = this.getRawHitbox().modifyCenter(this.position);
		this.collectHitbox = AUTO_COLLECT_CHECKBOX
				.modifyCenter(this.getRawHitbox().getLowerRightCorner().add(AUTO_COLLECT_CHECKBOX.getTopLeftCorner()));
	}

	/**
	 * @return the current position of this card in the game world
	 */
	public Vector getPosition() {
		return this.position;
	}

	public float getAngle() {
		return this.tapAngle;
	}

	public boolean setAnimation(AnimationMode mode) {
		if (!this.isAnimationRunning()) {
			this.animation = mode;
			if (this.animation != AnimationMode.NONE) {
				this.animationTime.restart();
				if (this.animation == AnimationMode.DESTROY) {
					this.playDestroyAnimation();
				}
			}
			return true;
		}
		return false;
	}

	public boolean isAnimationRunning() {
		return this.animationTime.isRunning();
	}

	public void playDestroyAnimation() {
		this.destroyAnimation.start(Color.white);
		SoundManager.getInstance().playDestroy();
	}

	/**
	 * Modify the {@link EssenceCounter} of this card by some amount
	 * 
	 * @param sel      the essences to add to or subtract from the counter
	 * @param subtract <code>true</code> if the essences should be <b>subtracted</b>
	 *                 from the counter <br>
	 *                 <code>false</code> if the essences should be <b>added</b> to
	 *                 the counter
	 */
	public void modifyEssence(EssenceSelection sel, boolean subtract) {
		if (subtract) {
			this.counter.sub(sel.getValues());
		} else {
			this.counter.add(sel.getValues());
		}
	}

	/**
	 * @return the current owner of this card
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Set the income at round-start of this card
	 * <p>
	 * If the {@link EssenceSelection#getTotal()} == 0, the income will be set as
	 * <code>null</code> signaling no income
	 * 
	 * @param income the income at round-start
	 */
	public void setIncome(EssenceSelection income) {
		if (income.getTotal() == 0) {
			this.income = null;
		} else {
			this.income = income;
		}
	}

	/**
	 * @return the income of this card at round-start, it may be indetermined and
	 *         the player has to make a choice <br>
	 *         <code>null</code> if this card has no income
	 */
	public EssenceSelection getIncome() {
		return this.income;
	}

	/**
	 * @return the {@link Game} in which this card exists
	 */
	public Game getGame() {
		return this.parent;
	}

	@Override
	public String getImage() {
		return this.image;
	}

	/**
	 * @return a clone of the current essence count on this card
	 */
	public int[] getEssenceCount() {
		return this.counter.getCount();
	}

	/**
	 * @return the {@link GameClient} in which the {@link Game} of this card exists
	 */
	public GameClient getGameClient() {
		return this.parent.getGameClient();
	}

	/**
	 * Set the collection mode of this card
	 * 
	 * @param mode Either {@link #ALWAYS}, {@link #ASK} or {@link #NEVER}
	 */
	public void setAutoCollect(CollectMode mode) {
		Log.info("Changing collect mode to " + mode);
		this.collectMode = mode;
	}

	/**
	 * Trigger the collection and income selections at round-start
	 * 
	 * @return <code>true</code> if no selection is to be made by the player, i.e.
	 *         the collection and income can be processed automatically
	 */
	public boolean askCollectEssencesAndIncome() {
		if (this.hasEssences()) {
			switch (this.collectMode) {
			case ALWAYS:
				return this.askIncome(true);
			case NEVER:
				return this.askIncome(false);
			case ASK:
				this.getGameClient().addSelector(
						new CollectEssenceSelector(this, this, "Collect essences from " + this.getName() + "?")
								.disableCancel());
				this.state = STATE_COLLECT;
				return false;
			default:
				Log.error("Unknown collectMode: " + this.collectMode);
				return true;
			}
		}
		return this.askIncome(false);
	}

	/**
	 * Trigger the income selection for this card at round-start
	 * 
	 * @param collecting whether the essences on this card were collected this round
	 * @return <code>true</code> if no selection for the income is to be made by the
	 *         player and the income thus can be handled automatically
	 */
	protected boolean askIncome(boolean collecting) {
		if (this.getIncome() != null) {
			if (this.getIncome().isDetermined()) {
				return true;
			} else {
				this.getGameClient()
						.addSelector(new EssenceSelector(this, this.getIncome(), "Choose income from " + this.getName())
								.disableCancel());
				if (this.state == STATE_COLLECT) {
					this.state = STATE_COLLECT_INCOME;
				} else {
					this.state = STATE_INCOME;
				}
				return false;
			}
		}
		return true;
	}

	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof CollectEssenceSelector && this.state == STATE_COLLECT) {
			this.getGameClient().unsetSelector(sel);
			this.collectResult = ((CollectEssenceSelector) sel).getResult();
			if (this.askIncome(this.collectResult)) {
				this.getPlayer().incomeFinished(this, new UserInputOverwrite(this, "Collect", this.collectResult));
				this.state = STATE_IDLE;
			}
		} else if (sel instanceof EssenceSelector
				&& (this.state == STATE_INCOME || this.state == STATE_COLLECT_INCOME)) {
			this.getGameClient().unsetSelector(sel);
			EssenceSelection selection = ((EssenceSelector) sel).getSelection();
			if (this.state == STATE_INCOME) {
				this.getPlayer().incomeFinished(this, new UserInputOverwrite(this, "Income", selection));
			} else {
				this.getPlayer().incomeFinished(this,
						new UserInputOverwrite(this, "CollectIncome", this.collectResult, selection));
			}
			this.state = STATE_IDLE;
		}
	}

	/**
	 * Collect the essences of this card by removing them from this {@link #counter}
	 * and adding them to the {@link EssenceCounter} of its owner
	 * ({@link #getPlayer()})
	 */
	public void collectEssences() {
		this.getPlayer().modifyEssence(new EssenceSelection(this.getEssenceCount()), false);
		this.counter.resetCounter();
	}

	public void clearEssences() {
		this.counter.resetCounter();
	}

	/**
	 * Collect a fixed, i.e. determined, income of this card (see also
	 * {@link #getIncome()})
	 */
	protected void collectFixedIncome() {
		if (this.getIncome() != null) {
			this.getPlayer().modifyEssence(this.getIncome(), false);
		}
	}

	/**
	 * Collect the income and essences on this card automatically
	 */
	public void automaticCollectIncome() {
		if (this.hasEssences()) {
			switch (this.collectMode) {
			case ALWAYS:
				this.collectEssences();
				break;
			case NEVER:
				break;
			case ASK:
			default:
				Log.error("Unknown automatic income collectMode: " + this.collectMode);
				break;
			}
		}
		this.collectFixedIncome();
	}

	/**
	 * Perform a user-defined action
	 * <p>
	 * Currently this involves only the collection and income processing of this
	 * card at round-start
	 * 
	 * @param action the user-defined action
	 */
	public void userAction(UserInputOverwrite action) {
		if (action.getParts().get(0).equalsIgnoreCase("Collect")) {
			if (Boolean.parseBoolean(action.getParts().get(1))) {
				this.collectEssences();
			}
			this.collectFixedIncome();
		} else if (action.getParts().get(0).equalsIgnoreCase("CollectIncome")) {
			if (Boolean.parseBoolean(action.getParts().get(1))) {
				this.collectEssences();
			}
			this.getPlayer().modifyEssence(new EssenceSelection(action.getParts().get(2)), false);
		} else if (action.getParts().get(0).equalsIgnoreCase("Income")) {
			if (this.hasEssences() && this.collectMode == CollectMode.ALWAYS) {
				this.collectEssences();
			}
			this.getPlayer().modifyEssence(new EssenceSelection(action.getParts().get(1)), false);
		} else {
			Log.warn("Unknown userAction: " + action);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		Log.warn("Cancel should not be possible for " + this + " " + sel);
		this.getGameClient().unsetSelector(sel);
	}

	public boolean hasEssences() {
		return !this.counter.isEmpty();
	}

	public Protection getProtection() {
		return this.protection;
	}
}
