package resarcana.game.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.abilities.specials.Obelisk;
import resarcana.game.abilities.specials.VialOfLight;
import resarcana.game.core.Tappable.AnimationMode;
import resarcana.game.utils.BoardPositioner;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.animation.Animation;
import resarcana.game.utils.animation.AnimationBundle;
import resarcana.game.utils.animation.ArrowAnimation;
import resarcana.game.utils.animation.DestroyAnimation;
import resarcana.game.utils.animation.GlowAnimation;
import resarcana.game.utils.animation.MaskAnimation;
import resarcana.game.utils.animation.SmokeAnimation;
import resarcana.game.utils.animation.SoundAnimation;
import resarcana.game.utils.animation.SplinterAnimation;
import resarcana.game.utils.animation.SweepAnimation;
import resarcana.game.utils.animation.Tracer;
import resarcana.game.utils.animation.generator.ColorVariationGenerator;
import resarcana.game.utils.animation.generator.ColorVariationGenerator.ParticleColorScheme;
import resarcana.game.utils.animation.generator.PathFactory;
import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.game.utils.animation.generator.RandomParticleGenerator;
import resarcana.game.utils.animation.generator.TransferGenerator;
import resarcana.game.utils.animation.generator.WarpFactory;
import resarcana.game.utils.animation.generator.WarpGenerator;
import resarcana.game.utils.statistics.StatisticProperties;
import resarcana.game.utils.statistics.StatisticsElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.Drawable;
import resarcana.graphics.DrawablePollable;
import resarcana.graphics.Engine;
import resarcana.graphics.Pollable;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.ScrollingListener;
import resarcana.graphics.utils.ScrollingManager;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Circle;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Distributor;
import resarcana.utils.DistributorFactory;
import resarcana.utils.Parameter;
import resarcana.utils.UtilFunctions;

public class Player implements Drawable, Pollable, Selecting, ScrollingListener {

	/*
	 * The total board layout should be something like this:
	 * 
	 * name - scrolls .....
	 * 
	 * discard - deck - mage - item - points/essences - places ...
	 * 
	 * artifacts ....
	 * 
	 * monuments ....
	 */

	/*
	 * Parameter for board arangements
	 */
	private static final float RELATIVE_PADDING = 1.2f;
	private static final Vector BOARD_OFFSET = new Vector(Artifact.ARTIFACT_HITBOX.width * RELATIVE_PADDING,
			Artifact.ARTIFACT_HITBOX.height * RELATIVE_PADDING);
	private static final int BOARD_ROWS = 2;
	private static final int BOARD_COLS = 9;
	private static final int BOARD_PRIORITY_COLS = 8;
	private static final int BOARD_MONUMENT_OFFSET = BOARD_PRIORITY_COLS;

	/*
	 * First row offsets
	 */
	private static final Vector NAME_OFFSET = new Vector(50, 25);
	private static final Vector SCROLL_OFFSET = new Vector(BOARD_OFFSET.x * 1.5f, Scroll.SCROLL_HITBOX.height * 0.55f);

	/*
	 * Padding for the whole player area, x/y are used as padding on either side
	 * (right/left, up/down)
	 */
	private static final Vector PADDING = new Vector(50, 50);

	/*
	 * Create the raw player area hitbox with enough place and padding for
	 * everything
	 */
	public static final Rectangle PLAYER_HITBOX = new Rectangle(Vector.ZERO,
			BOARD_OFFSET.x * BOARD_COLS + 2 * PADDING.x,
			BOARD_OFFSET.y * (BOARD_ROWS + 1) + 2 * SCROLL_OFFSET.y + 2 * PADDING.y);
	public static final Rectangle PLAYER_HAND_HITBOX = new Rectangle(Vector.ZERO, 400, 500);

	private static final Rectangle PLAYER_TOKEN_HITBOX = new Rectangle(Vector.ZERO, 128, 128);
	private static final Rectangle PLAYER_WINNER_HITBOX = new Rectangle(Vector.ZERO, 360, 180);

	/*
	 * The padded position from which all spaces on the board are aligned
	 */
	private static final Vector START_POSITION = PLAYER_HITBOX.getTopLeftCorner().add(PADDING);

	/*
	 * First row of the board
	 */
	private static final Vector POSITION_NAME = START_POSITION.add(NAME_OFFSET);
	private static final Vector POSITION_SCROLL = START_POSITION.add(SCROLL_OFFSET);

	/*
	 * Second row of the board
	 */
	private static final Vector POSITION_DISCARD = START_POSITION.add(BOARD_OFFSET.x * 0.5f,
			BOARD_OFFSET.y * 0.5f + SCROLL_OFFSET.y * 2);
	private static final Vector POSITION_DECK = START_POSITION.add(BOARD_OFFSET.x * 1.5f,
			BOARD_OFFSET.y * 0.5f + SCROLL_OFFSET.y * 2);
	private static final Vector POSITION_MAGE = START_POSITION.add(BOARD_OFFSET.x * 2.5f,
			BOARD_OFFSET.y * 0.5f + SCROLL_OFFSET.y * 2);
	private static final Vector POSITION_ITEM = START_POSITION.add(BOARD_OFFSET.x * 3.5f,
			BOARD_OFFSET.y * 0.5f + SCROLL_OFFSET.y * 2);

	private static final Vector POSITION_ESSENCES = START_POSITION.add(BOARD_OFFSET.x * 4.5f,
			BOARD_OFFSET.y * 0.5f + SCROLL_OFFSET.y * 2);
	private static final Vector POSITION_POINTS = POSITION_ESSENCES.add(-PLAYER_TOKEN_HITBOX.width / 2,
			-BOARD_OFFSET.y * 0.4f);
	private static final Vector POSITION_TOKEN = POSITION_ESSENCES.add(PLAYER_TOKEN_HITBOX.width / 2,
			-BOARD_OFFSET.y * 0.4f);

	private static final Vector[] POSITION_PLACES = new Vector[] {
			POSITION_ESSENCES.add(PowerPlace.PLACE_HITBOX.width * RELATIVE_PADDING * 1.2f, 0),
			POSITION_ESSENCES.add(PowerPlace.PLACE_HITBOX.width * RELATIVE_PADDING * 2.2f, 0),
			POSITION_ESSENCES.add(PowerPlace.PLACE_HITBOX.width * RELATIVE_PADDING * 3.2f, 0),
			POSITION_ESSENCES.add(PowerPlace.PLACE_HITBOX.width * RELATIVE_PADDING * 3.2f,
					PowerPlace.PLACE_HITBOX.height * RELATIVE_PADDING) };

	/*
	 * Third row of the board
	 */
	public static final Vector POSITION_FIRST_BOARD_SPACE = START_POSITION.add(BOARD_OFFSET.x * 0.5f,
			BOARD_OFFSET.y * 1.5f + SCROLL_OFFSET.y * 2);

	public static final Vector OFFSET_HAND_STEP = new Vector(50, 50);
	public static final Vector OFFSET_CARD_COUNTER = new Vector(-30, 0);

	public static final Color HAND_TRANSPARENCY = new Color(1, 1, 1, 0.5f);
	public static final Color PASSED_COLOR = new Color(0.3f, 0.3f, 0.3f, 0.5f);
	public static final Color DISCONNECT_COLOR = new Color(0.8f, 0, 0, 0.5f);

	private static final int STATE_IDLE = 0;
	private static final int STATE_PLAY_CARD_COST = 1;
	private static final int STATE_PLAY_CARD = 2;

	private static final Font INDICATOR_FONT = FontManager.getInstance().getFont(2 * Parameter.GUI_STANDARD_FONT_SIZE);
	private static final Font POINT_FONT = FontManager.getInstance()
			.getFont((int) (1.5f * Parameter.GUI_STANDARD_FONT_SIZE));

	public final Game parent;
	public final Vector position;

	private final int id;
	private String name;

	private boolean active = false;
	private int status = STATE_IDLE;

	private EssenceCounter counter;

	private Mage mage;
	private ArrayList<Artifact> hand = new ArrayList<Artifact>();
	private ArrayList<Artifact> board = new ArrayList<Artifact>();
	private ArrayList<Artifact> deck = new ArrayList<Artifact>();
	private ArrayList<Artifact> discard = new ArrayList<Artifact>();
	private MagicItem item;
	private ArrayList<PowerPlace> places = new ArrayList<PowerPlace>();
	private ArrayList<Monument> monuments = new ArrayList<Monument>();
	private ArrayList<Scroll> scrolls = new ArrayList<Scroll>();

	private ArrayList<Artifact> known = new ArrayList<Artifact>();
	private int numberUnknown = 0;

	private ArrayList<Tappable> inplay = new ArrayList<Tappable>();
	private ArrayList<Tappable> costReducers = new ArrayList<Tappable>();
	private ArrayList<Tappable> protections = new ArrayList<Tappable>();
	private ArrayList<Artifact> demons = new ArrayList<Artifact>();

	private ArrayList<Tappable> incomePending = new ArrayList<Tappable>();
	private ArrayList<Tappable> incomeAutomatic = new ArrayList<Tappable>();
	private boolean incomeFinished = true;
	private boolean mouseOverHand = false, handMoved = false;
	private float handDelay = 0;
	private Artifact cardToPlay;
	private boolean winner = false;

	private final BoardPositioner boardPositions, scrollPositions;

	private Rectangle hitbox = PLAYER_HITBOX;
	private Rectangle hitbox_deck, hitbox_discard, hitbox_hand;

	private ArrayList<DrawablePollable> animationList = new ArrayList<DrawablePollable>();

	private TransferGenerator transferGen;
	private Tracer victoryAnimation;
	private Animation activityAnimation, dragonShadow, demonShadow, arrowAttack, growingTree, demonSlayer,
			shieldVsArrow, shieldVsDragon, shieldVsDemon, shield, guardDog, lion, dancingSword;

	@Override
	public String toString() {
		return "Player" + CommunicationKeys.SEPERATOR_NAME + this.id;
	}

	@SuppressWarnings("unused")
	private final static int INIT______________ = 0;

	public Player(Game parent, String name, Vector position) {
		Log.info("Creating player " + name + " at " + position);
		this.parent = parent;
		this.id = parent.playerIndexer.getNextNumber();
		parent.allPlayers.add(this);
		this.name = name;
		this.position = position;

		this.initHitboxes();

		this.boardPositions = new BoardPositioner(BOARD_ROWS, BOARD_COLS, POSITION_FIRST_BOARD_SPACE.add(this.position),
				this.hitbox.getLowerRightCorner(), BOARD_OFFSET.x, BOARD_OFFSET.y, BOARD_PRIORITY_COLS);
		this.scrollPositions = new BoardPositioner(1, BOARD_COLS - 1, POSITION_SCROLL.add(this.position),
				this.hitbox.getTopRightCorner(), Scroll.SCROLL_HITBOX.width * 1.1f, Scroll.SCROLL_HITBOX.height * 1.1f);

		this.initAnimations();

		this.counter = new EssenceCounter(this, 2.f, EssenceCounter.BOXES_POOL, true, false);
		// Start essences
		this.counter.add(EssenceSelection.allEssencesOnce());
		this.counter.stopAnimations();
	}

	private void initHitboxes() {
		this.hitbox = this.hitbox.modifyCenter(this.position);
		this.hitbox_deck = Artifact.ARTIFACT_HITBOX.modifyCenter(this.position.add(POSITION_DECK));
		this.hitbox_discard = Artifact.ARTIFACT_HITBOX.modifyCenter(this.position.add(POSITION_DISCARD));
		this.hitbox_hand = PLAYER_HAND_HITBOX.modifyCorner(Engine.getInstance().getWidth() - PLAYER_HAND_HITBOX.width,
				Engine.getInstance().getHeight() - PLAYER_HAND_HITBOX.height);
	}

	private void initAnimations() {
		this.activityAnimation = new AnimationBundle(
				new GlowAnimation(this.hitbox.addToWidthAndHeight(this.hitbox.height * 0.3f, this.hitbox.height * 0.2f),
						4.0f, Color.yellow.darker(0.1f), 10.0f, Color.orange.darker(0.1f)));
		this.victoryAnimation = new Tracer(
				new RandomParticleGenerator(PLAYER_WINNER_HITBOX.scale(0.9f).modifyCenter(this.position),
						new ColorVariationGenerator(ParticleColorScheme.YELLOW_ORANGE), 2.f, 150.f, 200.f));

		this.transferGen = new TransferGenerator(2.0f, 0.5f, -1, 0.075f);
		this.animationList.add(new Tracer(this.transferGen));

		float padding = 0.2f;
		float time = 3;
		PathGenerator path1 = PathFactory.getTranslatingPath(this.hitbox.getTopLeftCorner(),
				this.hitbox.getBottomRightCorner(), 0);
		PathGenerator path2 = PathFactory.getTranslatingPath(this.hitbox.getTopLeftCorner(),
				this.hitbox.getBottomRightCorner(), padding);
		this.dragonShadow = new AnimationBundle(
				new SmokeAnimation("animation/smoke.png", 2, path1, time / (1 + 2 * padding), 200,
						time / (1 + 2 * padding) * 0.9f),
				new SweepAnimation(this.hitbox,
						new Rectangle(Vector.ZERO, this.hitbox.height * 0.9f, this.hitbox.height * 0.9f), path2,
						"animation/dragon.png", time),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playAttackDragon();
					}
				});
		this.animationList.add(this.dragonShadow);
		this.demonShadow = new AnimationBundle(new SweepAnimation(this.hitbox,
				new Rectangle(Vector.ZERO, this.hitbox.height * 0.7f, this.hitbox.height * 0.7f),
				PathFactory.getDemonPath(
						this.hitbox.addToWidthAndHeight(-this.hitbox.height * 0.7f, -this.hitbox.height * 0.7f), 1,
						1.5f, 1.5f, 40, 2),
				"animation/demon.png", 3), new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playAttackDemon();
					}
				});
		this.animationList.add(this.demonShadow);
		this.arrowAttack = new AnimationBundle(
				new ArrowAnimation(this.hitbox,
						new Rectangle(Vector.ZERO, this.hitbox.height * 1.4f, this.hitbox.height * 1.4f),
						"animation/arrow_2.png", this.position.add(POSITION_DISCARD).y, 2.2f, 1.0f),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playAttackBow();
					}
				});
		this.animationList.add(this.arrowAttack);
		this.growingTree = new MaskAnimation(new Circle(Vector.ZERO, this.hitbox.height),
				new Rectangle(this.position, this.hitbox.height * 5 / 7, this.hitbox.height),
				PathFactory.getScalingPath(this.position.add(0, this.hitbox.height / 2), 0.01f, 1.2f),
				"animation/protect_tree.png", 3);
		this.animationList.add(this.growingTree);

		float angle = new Vector(this.hitbox.width / 4, this.hitbox.height / 4).clockWiseAng();
		Rectangle demonHitbox = new Rectangle(this.position.add(this.hitbox.width / 8, 0), this.hitbox.height * 0.7f,
				this.hitbox.height * 0.7f);
		WarpGenerator warpUp = WarpFactory
				.addProgressPlateaus(
						WarpFactory
								.modifySpeedSmooth(WarpFactory.getWedgeWarper(
										new Vector(0,
												demonHitbox.getPositionAtAng(angle + (float) Math.PI).y
														/ demonHitbox.height),
										new Vector(1, demonHitbox.getPositionAtAng(angle).y / demonHitbox.height), 0.1f,
										0.0f, false), 2, 0),
						0.1f, 0.3f);
		WarpGenerator warpDown = WarpFactory
				.addProgressPlateaus(
						WarpFactory
								.modifySpeedSmooth(WarpFactory.getWedgeWarper(
										new Vector(0,
												demonHitbox.getPositionAtAng(angle + (float) Math.PI).y
														/ demonHitbox.height),
										new Vector(1, demonHitbox.getPositionAtAng(angle).y / demonHitbox.height), 0.1f,
										0.0f, true), 2, 0),
						0.1f, 0.3f);
		this.demonSlayer = new AnimationBundle(
				new SweepAnimation(this.hitbox,
						new Rectangle(Vector.ZERO, this.hitbox.height / 2 * 0.7f, this.hitbox.height * 0.7f),
						PathFactory
								.addProgressPlateaus(
										PathFactory
												.modifySpeedSmooth(
														PathFactory.getTranslatingPath(
																this.position.add(-this.hitbox.width / 8 * 3,
																		-this.hitbox.height / 2),
																this.position.add(this.hitbox.width / 8, 0), -0.2f),
														2, 0),
										0.1f, 0.6f),
						"animation/demon_slayer.png", 7.0f).setFixedColor(Color.white),
				new DestroyAnimation(demonHitbox, "animation/demon.png", 7.0f,
						PathFactory.getTranslatingPath(demonHitbox.getPositionAtAng(angle + (float) Math.PI),
								demonHitbox.getPositionAtAng(angle), 0),
						0, warpUp, warpDown, true),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playDemonDyingLong();
					}
				});
		this.animationList.add(this.demonSlayer);

		time = 2;
		Vector shieldPos = this.position.add(this.hitbox.width / 4, 0);
		this.shield = new AnimationBundle(new SweepAnimation(this.hitbox, demonHitbox,
				PathFactory.getStaticPath(shieldPos), "animation/shield.png", time), new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playProtect();
					}
				});
		this.animationList.add(this.shield);

		time = 1;
		this.shieldVsArrow = new AnimationBundle(
				new SweepAnimation(this.hitbox, demonHitbox, PathFactory.getStaticPath(shieldPos),
						"animation/shield.png", time).setFixedColor(Color.white),
				new SweepAnimation(this.hitbox,
						new Rectangle(Vector.ZERO, this.hitbox.height * 1.4f, this.hitbox.height * 1.4f),
						PathFactory.getDropPath(
								this.hitbox.getTopLeftCorner().add(this.hitbox.getBottomLeftCorner()).div(2), 0.3f,
								-0.05f, shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f), 0.3f,
								shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f)
										.add(this.hitbox.height * 0.25f, this.hitbox.height * 0.5f),
								shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f)
										.add(this.hitbox.height * 0.35f, this.hitbox.height)),
						"animation/arrow_2.png", time),
				0, new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playProtect();
					}
				}, time * 0.3f);
		this.animationList.add(this.shieldVsArrow);

		time = 2;
		this.shieldVsDragon = new AnimationBundle(
				new SweepAnimation(this.hitbox, demonHitbox, PathFactory.getStaticPath(shieldPos),
						"animation/shield.png", time).setFixedColor(Color.white),
				new SweepAnimation(this.hitbox,
						new Rectangle(Vector.ZERO, this.hitbox.height * 1.4f, this.hitbox.height * 1.4f),
						PathFactory.getDropPath(
								this.hitbox.getTopLeftCorner().add(this.hitbox.getBottomLeftCorner()).div(2), 0.3f,
								-0.05f, shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f), 0.3f,
								shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f)
										.add(this.hitbox.height * 0.25f, this.hitbox.height * 0.5f),
								shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f)
										.add(this.hitbox.height * 0.35f, this.hitbox.height)),
						"animation/dragon.png", time),
				0, new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playProtect();
					}
				}, time * 0.3f);
		this.animationList.add(this.shieldVsDragon);

		time = 2;
		path1 = PathFactory.rotateBy(PathFactory.getArcPathBetweenPoints(
				this.hitbox.getTopLeftCorner().add(this.hitbox.getBottomLeftCorner()).div(2),
				shieldPos.add(-this.hitbox.height * 0.15f, -this.hitbox.height * 0.1f), 0, -0.05f), -90, -90);
		ArrayList<WarpGenerator> gens = new ArrayList<WarpGenerator>();
		gens.add(WarpFactory.compositeWarp(WarpFactory.getScalingWarp(1.0f, 1.5f),
				WarpFactory.getRotatingWarp(new Vector(0.5f, 0.5f), 0, 90)));
		gens.add(WarpFactory.compositeWarp(WarpFactory.getScalingWarp(1.0f, 0.5f),
				WarpFactory.getRotatingWarp(new Vector(0.5f, 0.5f), 0, -90)));
		gens.add(WarpFactory.compositeWarp(WarpFactory.getScalingWarp(1.0f, 1.5f),
				WarpFactory.getRotatingWarp(new Vector(0.5f, 0.5f), 0, -90)));
		gens.add(WarpFactory.compositeWarp(WarpFactory.getScalingWarp(1.0f, 0.5f),
				WarpFactory.getRotatingWarp(new Vector(0.5f, 0.5f), 0, 90)));
		Distributor<WarpGenerator> generators = DistributorFactory.getModuloDistributor(gens);
		WarpGenerator warp = WarpFactory.getMeltingWarp(1.25f, 2.5f, 0.2f);
		this.shieldVsDemon = new AnimationBundle(
				new SweepAnimation(this.hitbox, demonHitbox, PathFactory.getStaticPath(shieldPos),
						"animation/shield.png", time).setFixedColor(Color.white),
				new AnimationBundle(
						new SweepAnimation(this.hitbox, demonHitbox, path1, "animation/demon.png", time * 0.3f),
						new SplinterAnimation(demonHitbox,
								PathFactory.compositePath(PathFactory.getScalingPath(Vector.ZERO, 1, 1.25f),
										PathFactory.getTranslatingPath(path1.getPosition(1),
												path1.getPosition(1).add(0, this.hitbox.height / 4), 0),
										PathFactory.getStaticPath(Vector.ZERO, 1, path1.getAngle(1))),
								"animation/demon.png", warp, generators, 10, 10, time * 0.7f),
						time * 0.3f),
				0, new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playProtect();
						SoundManager.getInstance().playDemonDyingShort();
					}
				}, time * 0.3f);
		this.animationList.add(this.shieldVsDemon);

		time = 3;
		path1 = PathFactory.compositePath(
				PathFactory.loopPath(PathFactory.getScalingPath(Vector.ZERO, 0.9f, 1.1f), 10, true),
				PathFactory.getArcPathBetweenPoints(
						new Vector(this.hitbox.getLeftEnd(), this.hitbox.center.y - this.hitbox.height / 3),
						new Vector(this.hitbox.getRightEnd(), this.hitbox.center.y + this.hitbox.height / 4), 0.25f,
						-0.02f),
				PathFactory.loopPath(PathFactory.getRotatingPath(Vector.ZERO, -5, 5), 5, true));
		this.guardDog = new AnimationBundle(
				new SweepAnimation(this.hitbox, demonHitbox, path1, "animation/guard_dog.png", time),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playGuardDog();
					}
				});
		this.animationList.add(this.guardDog);

		time = 3;
		path1 = PathFactory.modifySpeedSmooth(PathFactory.getScalingPath(this.position, 1.0f, 3.5f), 1, 3);
		this.lion = new AnimationBundle(new SweepAnimation(this.hitbox, demonHitbox, path1, "animation/lion.png", time),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playLionRoar();
					}
				});
		this.animationList.add(this.lion);

		time = 7.5f;
		path1 = PathFactory.getDancingSwordPath(this.position, demonHitbox.width / 3, false);
		path2 = PathFactory.getDancingSwordPath(this.position, demonHitbox.width / 3, true);
		this.dancingSword = new AnimationBundle(
				new SweepAnimation(this.hitbox, demonHitbox, path1, "animation/dancing_sword.png", time),
				new SweepAnimation(this.hitbox, demonHitbox, path2, "animation/dancing_sword.png", time),
				new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playDancingSword();
					}
				}).add(new SoundAnimation() {
					@Override
					public void playSound() {
						SoundManager.getInstance().playSwordDropping();
					}
				}, time - 1);
		this.animationList.add(this.dancingSword);
	}

	public void initArtifactDeck(List<Artifact> deck) {
		this.deck = new ArrayList<Artifact>(deck);
		for (Artifact artifact : this.deck) {
			artifact.assignPlayer(this);
		}
		this.numberUnknown = this.deck.size();
	}

	public void initMage(Mage mage) {
		if (mage == null) {
			Log.error("Setting null mage");
		} else {
			if (this.mage == null) {
				Log.info("Setting " + mage + " for " + this);
				this.mage = mage;
				this.mage.setPosition(this.position.add(POSITION_MAGE));
				this.mage.assignPlayer(this);
				this.putCardInPlay(this.mage);
				if (this.isClientPlayer()) {
					ScrollingManager.getInstance().addListener(this);
				}
			} else {
				Log.warn("Setting mage while mage is already set for " + this);
			}
		}
	}

	@SuppressWarnings("unused")
	private final static int SETTER______________ = 0;

	public boolean setName(String newName) {
		if (newName != null) {
			if (!newName.isEmpty()) {
				this.name = newName;
				return true;
			}
		}
		return false;
	}

	public void setItem(MagicItem item) {
		MagicItem out = null;
		if (item == null) {
			Log.error("Setting null magicItem");
		} else {
			Log.info("Giving " + item + " to " + this);
			out = this.item;
			if (out != null) {
				this.getGame().swapItems(out, item);
				this.removeCardFromPlay(out);
			}
			this.item = item;
			this.item.setPosition(this.position.add(POSITION_ITEM));
			this.item.assignPlayer(this);
			this.putCardInPlay(this.item);
		}
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public void setIncomeFinished(boolean finished) {
		this.incomeFinished = finished;
	}

	public void makeWinner() {
		this.winner = true;
		this.victoryAnimation.start();
	}

	@SuppressWarnings("unused")
	private final static int UPDATE______________ = 0;

	public void drawHand(Graphics g) {
		g.pushTransform();
		g.resetTransform();

		// Card-playability glow
		if (this.isActive()) {
			if (this.isAnyCardInHandPlayable()) {
				if (this.mouseOverHand) {
					GraphicUtils.drawImage(g, this.hitbox_hand,
							ResourceManager.getInstance().getImage("misc/glow_raw.png"), Ability.COLOR_SELECTED, 20,
							25);
				} else {
					GraphicUtils.drawImage(g, this.hitbox_hand,
							ResourceManager.getInstance().getImage("misc/glow_raw.png"), Ability.COLOR_AVAILABLE, 20,
							25);
				}
			}
		}

		// Background texture
		if (this.mouseOverHand || this.isActive()) {
			GraphicUtils.drawImage(g, this.hitbox_hand,
					ResourceManager.getInstance().getImage("background/background_hand.png"));
		} else {
			GraphicUtils.drawImage(g, this.hitbox_hand,
					ResourceManager.getInstance().getImage("background/background_hand.png"), HAND_TRANSPARENCY);
		}

		// Cards in hand
		for (Artifact artifact : this.hand) {
			artifact.draw(g);
		}

		// Indicators for turn and income
		if (this.isActive()) {
			GraphicUtils.drawImage(g, this.hitbox_hand, ResourceManager.getInstance().getImage("misc/your_turn.png"));
		} else if (this.getGame().isIncomePending()) {
			GraphicUtils.drawImage(g, this.hitbox_hand,
					ResourceManager.getInstance().getImage("misc/income_pending.png"));
		}

		// Player name
		g.setFont(INDICATOR_FONT);
		GraphicUtils.drawString(g,
				this.hitbox_hand.getBottomLeftCorner().add(30, -FontManager.getInstance().getLineHeight(g.getFont())),
				this.getName(), Color.black);

		g.popTransform();
	}

	@Override
	public void draw(Graphics g) {
		// Active player glow
		if ((this.getGame().isActivePlayer(this) || !this.hasIncomeFinished()) && this.activityAnimation != null) {
			this.activityAnimation.draw(g);
		}
		// Player background mat
		GraphicUtils.drawImage(g, this.hitbox,
				ResourceManager.getInstance().getImage("background/background_parchment.png"));

		// All scrolls of this player
		for (Scroll scroll : this.scrolls) {
			scroll.draw(g);
		}

		// Font for indicators
		g.setFont(INDICATOR_FONT);

		// Deck pile with size indicator
		if (this.deck.size() > 0) {
			GraphicUtils.drawImage(g, this.hitbox_deck,
					ResourceManager.getInstance().getImage("misc/artifact_back.png"));
			GraphicUtils.drawString(g, this.hitbox_deck.getTopRightCorner().add(OFFSET_CARD_COUNTER),
					"" + this.deck.size());
		} else {
			GraphicUtils.drawStringCentered(g, this.hitbox_deck.getCenter(), "Deck", Color.black);
		}

		// Discard pile with size indicator
		if (this.discard.size() > 0) {
			GraphicUtils.drawImage(g, this.hitbox_discard,
					ResourceManager.getInstance().getImage(this.discard.get(this.discard.size() - 1).getImage()));
			GraphicUtils.drawString(g, this.hitbox_discard.getTopRightCorner().add(OFFSET_CARD_COUNTER),
					"" + this.discard.size());
		} else {
			GraphicUtils.drawStringCentered(g, this.hitbox_discard.getCenter(), "Discard", Color.black);
		}

		// Cards in reverse order, this is important for the overlapping tapped cards
		for (int i = this.inplay.size() - 1; i >= 0; i--) {
			this.inplay.get(i).draw(g);
		}
		// All cards in play
//		for (Tappable tappable : this.inplay) {
//			tappable.draw(g);
//		}

		// Reset font because the following can only be drawn after the Tappables
		g.setFont(INDICATOR_FONT);

		// Hand size at mage
		if (this.mage != null) {
			GraphicUtils.drawString(g, Artifact.ARTIFACT_HITBOX.modifyCenter(this.mage.getPosition())
					.getTopRightCorner().add(OFFSET_CARD_COUNTER), "" + this.hand.size());
		}

		// Player name
		GraphicUtils.drawString(g, this.position.add(POSITION_NAME), this.getName(), Color.black);

		// Essence counter
		this.counter.drawAt(g, this.position.add(POSITION_ESSENCES));

		// Start player token
		if (this.getGame().isNextStartingPlayer(this)) {
			if (this.hasPassed()) {
				GraphicUtils.drawImage(g, PLAYER_TOKEN_HITBOX.modifyCenter(POSITION_TOKEN.add(this.position)),
						ResourceManager.getInstance().getImage("misc/token_passed.png"));
			} else {
				GraphicUtils.drawImage(g, PLAYER_TOKEN_HITBOX.modifyCenter(POSITION_TOKEN.add(this.position)),
						ResourceManager.getInstance().getImage("misc/token_firstplayer.png"));
			}
		}

		// Point counter
		GraphicUtils.drawImage(g, PLAYER_TOKEN_HITBOX.scale(0.5f).modifyCenter(this.position.add(POSITION_POINTS)),
				ResourceManager.getInstance().getImage("misc/point_token.png"));
		g.setFont(POINT_FONT);
		GraphicUtils.drawStringCentered(g, this.position.add(POSITION_POINTS).add(0, -5), "" + this.getTotalPoints());

		// Gray out if player has passed
		if (this.hasPassed()) {
			GraphicUtils.drawImage(g, this.hitbox,
					ResourceManager.getInstance().getImage("background/background_parchment.png"), PASSED_COLOR);
		}
		// Red overlay if player is disconnected and game is not finished
		if (this.hasDisconnected() && !this.getGame().hasGameFinished()) {
			GraphicUtils.drawImage(g, this.hitbox,
					ResourceManager.getInstance().getImage("background/background_parchment.png"), DISCONNECT_COLOR);
		}

		// Action animations, these will only render while they are active
		for (Drawable ani : this.animationList) {
			ani.draw(g);
		}

		// Winner banner and animation
		if (this.winner) {
			if (this.victoryAnimation != null) {
				this.victoryAnimation.draw(g);
			}
			GraphicUtils.drawImage(g, PLAYER_WINNER_HITBOX.modifyCenter(this.position),
					ResourceManager.getInstance().getImage("misc/winner_banner.png"));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void poll(Input input, float secounds) {
		// Check mouse position for hand interaction
		this.mouseOverHand = this.hitbox_hand.isPointInThis(new Vector(input.getMouseX(), input.getMouseY()));
		if (this.handMoved) { // Check and advance delay for hand moving
			this.handDelay += secounds;
			if (this.handDelay > 0.3f) {
				this.handMoved = false;
			}
		}
		// Play card from hand
		if (this.mouseOverHand && this.isAnyCardInHandPlayable() && this.isActive()
				&& !this.getGame().isWaitingForAbility()) {
			if (input.isMousePressed(Input.MOUSE_LEFT_BUTTON)) {
				this.getGame().getGameClient().addSelector(new ImageSelector<Artifact>(this, this.getPlayableCards(),
						"Choose an artifact to play from hand"));
				this.getGame().markPlayingCard(this);
				this.status = STATE_PLAY_CARD;
			}
		}
		// Poll all tappables in play
		for (Tappable tappable : (ArrayList<Tappable>) this.inplay.clone()) {
			tappable.poll(input, secounds);
		}
		// Poll cards in hand in reverse order
		for (int i = this.hand.size() - 1; i >= 0; i--) {
			this.hand.get(i).pollDetailedView(input, secounds);
		}
		// Poll deck-pile for enlargement of known information
		if (!this.getGame().getGameClient().getGameState().isMouseBlockedByGUI()) {
			Vector mouse = this.getGame().getCamera().getTransformedMousePos(input);
			if (this.known.size() > 0 && this.hitbox_deck.isPointInThis(mouse) && input.isKeyDown(Input.KEY_LALT)) {
				this.getGame().getGameClient().setDetailedDeck(this.known, "Known cards of " + this.getName());
			}
			// Poll discard-pile for enlargement
			if (this.discard.size() > 0 && this.hitbox_discard.isPointInThis(mouse)
					&& input.isKeyDown(Input.KEY_LALT)) {
				this.getGame().getGameClient().setDetailedDeck(this.discard, "Discard pile of " + this.getName());
			}
		}
		// Essencecounter animation
		this.counter.poll(input, secounds);
		// Winning animation
		this.victoryAnimation.poll(input, secounds);
		// Activity animation
		this.activityAnimation.poll(input, secounds);
		// Action animations
		for (Pollable ani : this.animationList) {
			ani.poll(input, secounds);
		}

		// Single player test mode
		if (this.getGame().isSinglePlayerGame()) {
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_Z)) {
				this.playDragonAnimation(new Color(0.7f, 0, 0, 0.7f));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_T)) {
				this.playDemonAnimation(new Color(0.7f, 0, 0, 0.7f));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_H)) {
				this.playTransferAnimation(Essences.ELAN, 10, this.position,
						this.getGame().getCamera().getTransformedMousePos(input));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_B)) {
				this.playArrowAttack();
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_G)) {
				this.playGrowingTreeAnimation();
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_U)) {
				if (this.mage != null) {
					this.mage.playDestroyAnimation();
				}
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_J)) {
				this.playDemonSlayerAnimation(new Color(0.7f, 0, 0, 0.7f));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_P)) {
				this.playShieldVsArrowAnimation(Color.white);
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_O)) {
				this.playShieldVsDragonAnimation(new Color(0.7f, 0, 0, 0.7f));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_L)) {
				this.playShieldVsDemonAnimation(new Color(0.7f, 0, 0, 0.7f));
			}
			if (input.isKeyDown(Input.KEY_LCONTROL) && input.isKeyPressed(Input.KEY_I)) {
				this.playGuardDogAnimation();
			}
			if (input.isKeyDown(Input.KEY_RCONTROL) && input.isKeyPressed(Input.KEY_L)) {
				this.playLionAnimation();
			}
			if (input.isKeyDown(Input.KEY_RCONTROL) && input.isKeyPressed(Input.KEY_K)) {
				this.playDancingSwordAnimation();
			}
		}
	}

	@SuppressWarnings("unused")
	private final static int STATUS______________ = 0;

	public boolean hasPassed() {
		return this.getGame().hasPassed(this);
	}

	public boolean hasDisconnected() {
		return this.getGame().hasDisconnected(this);
	}

	public boolean isClientPlayer() {
		return this.id == this.getGame().getPlayerId();
	}

	public boolean isPayable(Tappable tappable) {
		return this.isPayable(tappable.getCost(), tappable.getRawCost());
	}

	public boolean isPayable(EssenceSelection cost) {
		return this.getEssenceCounter().isPayable(cost);
	}

	public boolean isPayable(EssenceSelection cost, EssenceSelection maxAllowed) {
		return this.getEssenceCounter().isPayable(cost, maxAllowed);
	}

	private boolean isAnyCardInHandPlayable() {
		for (Artifact artifact : this.hand) {
			if (this.isPayable(artifact)) {
				return true;
			}
		}
		return false;
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isInPlay(Tappable card) {
		return this.inplay.contains(card);
	}

	public boolean isInHand(Tappable card) {
		return this.hand.contains(card);
	}

	public boolean canDrawCard() {
		return this.deck.size() + this.discard.size() > 0;
	}

	public boolean hasIncomeFinished() {
		return this.incomeFinished;
	}

	public boolean isMouseOverHand() {
		return this.mouseOverHand;
	}

	@SuppressWarnings("unused")
	private final static int USER_ACTIONS______________ = 0;

	public void placeCardFromDiscard(Artifact card, EssenceSelection cost) {
		if (card.getPlayer() != this) {
			if (!card.getPlayer().discard.contains(card)) {
				Log.error("Card " + card + " not in discard of player " + card.getPlayer());
			}
			if (!cost.isDetermined()) {
				Log.warn("Indetermined card cost " + cost + " for card " + card);
			}
			Log.info("Placing " + card + " from discard of " + card.getPlayer() + " for " + this);
			card.getPlayer().discard.remove(card);
			card.assignPlayer(this);
			this.board.add(card);
			card.setPosition(this.boardPositions.getNextEmptyPosition());
			this.putCardInPlay(card);
			this.counter.sub(cost.getValues());
			SoundManager.getInstance().playCardFromHand();
			this.getGame().addToHistory(new HistoryElement(card)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this)).setOptionalOne("", "Revive"));
		} else {
			if (!this.discard.contains(card)) {
				Log.error("Card " + card + " not in discard of player " + this);
			}
			if (!cost.isDetermined()) {
				Log.warn("Indetermined card cost " + cost + " for card " + card);
			}
			Log.info("Placing " + card + " from discard for " + this);
			this.board.add(card);
			card.setPosition(this.boardPositions.getNextEmptyPosition());
			card.setAnimation(AnimationMode.PLACING);
			this.putCardInPlay(card);
			this.discard.remove(card);
			this.counter.sub(cost.getValues());
			SoundManager.getInstance().playCardFromHand();
			this.getGame().addToHistory(new HistoryElement(card)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this)).setOptionalOne("", "Revive"));
		}
	}

	public void placeCardFromHand(Artifact card, EssenceSelection cost) {
		if (!this.hand.contains(card)) {
			Log.error("Card " + card + " not in hand of player " + this);
		}
		if (!cost.isDetermined()) {
			Log.warn("Indetermined card cost " + cost + " for card " + card);
		}
		Log.info("Placing " + card + " from hand for " + this);
		this.board.add(card);
		card.setPosition(this.boardPositions.getNextEmptyPosition());
		card.setAnimation(AnimationMode.PLACING);
		this.putCardInPlay(card);
		this.hand.remove(card);
		this.counter.sub(cost.getValues());
		this.updateHandPositions();
		SoundManager.getInstance().playCardFromHand();
		this.getGame().addToHistory(new HistoryElement(card)
				.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this)).setOptionalOne("", "Play"));
	}

	public boolean buyPowerPlace(PowerPlace place) {
		if (place.getCost().isDetermined()) {
			if (!this.isPayable(place)) {
				Log.warn("Cannot pay PowerPlace cost " + place.getCost());
			}
			Log.info("Buying " + place + " for " + this);
			this.counter.sub(place.getCost().getValues());
			place.setPosition(this.position.add(POSITION_PLACES[this.places.size()]));
			place.untap();
			place.assignPlayer(this);
			this.places.add(place);
			this.putCardInPlay(place);
			this.getGame().removePlace(place);
			SoundManager.getInstance().playBuyPowerPlace();
			if (this.isActive()) {
				this.getGame().getGameClient()
						.informAllClients_Action(new UserInputOverwrite(this, "BuyPowerPlace", place));
			}
			this.getGame().addToHistory(new HistoryElement(place)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this)).setOptionalOne("", "Buy"));
			return true;
		} else {
			Log.warn("Indetermined PowerPlace cost " + place.getCost());
			return false;
		}
	}

	public boolean buyMonument(Monument monument) {
		if (monument.getCost().isDetermined()) {
			if (!this.isPayable(monument)) {
				Log.warn("Cannot pay Monument cost " + monument.getCost());
			}
			Log.info("Buying " + monument + " for " + this);
			this.counter.sub(monument.getCost().getValues());
			monument.setPosition(this.boardPositions.getNextEmptyPosition(BOARD_MONUMENT_OFFSET));
			monument.untap();
			monument.assignPlayer(this);
			this.monuments.add(monument);
			this.putCardInPlay(monument);
			this.getGame().removeMonument(monument);
			SoundManager.getInstance().playBuyMonument();
			if (this.isActive() && !(monument instanceof Obelisk)) {
				this.getGame().getGameClient()
						.informAllClients_Action(new UserInputOverwrite(this, "BuyMonument", monument));
			}
			this.getGame().addToHistory(new HistoryElement(monument)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this)).setOptionalOne("", "Buy"));
			return true;
		} else {
			Log.warn("Indetermined Monument cost " + monument.getCost());
			return false;
		}
	}

	public void pass(MagicItem newItem, Artifact draw) {
		this.setItem(newItem);
		if (draw != null && !this.isActive()) {
			this.drawCard(draw);
		}
		this.getGame().passPlayer(this);
	}

	public void userAction(UserInputOverwrite action) {
		// For the player the first entry will be a description of the action
		String description = action.getParts().get(0);
		if (description.equalsIgnoreCase("PlayCard")) {
			Artifact artifact = (Artifact) this.getGame().getTappable(action.getParts().get(1));
			this.placeCardFromHand(artifact, artifact.getCost());
		} else if (description.equalsIgnoreCase("PlayCardCost")) {
			this.placeCardFromHand((Artifact) this.getGame().getTappable(action.getParts().get(1)),
					new EssenceSelection(action.getParts().get(2)));
		} else if (description.equalsIgnoreCase("AutomaticIncome")) {
			Log.info("Automatic income for " + this);
			for (Tappable tappable : UtilFunctions.StringArrayToTappables(
					action.getParts().get(1).split(CommunicationKeys.SEPERATOR_VALUES), this.getGame())) {
				Log.info("Income from " + tappable);
				tappable.automaticCollectIncome();
			}
		} else if (description.equalsIgnoreCase("BuyPowerPlace")) {
			PowerPlace place = (PowerPlace) this.getGame().getTappable(action.getParts().get(1));
			if (!this.places.contains(place)) {
				this.buyPowerPlace(place);
			}
		} else if (description.equalsIgnoreCase("BuyMonument")) {
			Monument monument = (Monument) this.getGame().getTappable(action.getParts().get(1));
			if (!this.monuments.contains(monument)) {
				this.buyMonument(monument);
			}
		} else if (description.equalsIgnoreCase("BuyObelisk")) { // Special case with essence reward on buy
			Monument monument = (Monument) this.getGame().getTappable(action.getParts().get(1));
			if (!this.monuments.contains(monument)) {
				this.buyMonument(monument);
			}
			this.modifyEssence(new EssenceSelection(action.getParts().get(2)), false);
		} else if (description.equalsIgnoreCase("VialOfLight")) {
			this.modifyEssence(new EssenceSelection(action.getParts().get(1)), false);
			this.getGame().resolveVialOfLightAction();
		} else {
			Log.warn("Player " + this + " could not evaluate action " + action);
		}
	}

	@SuppressWarnings("unused")
	private final static int INTERNAL_ACTIONS______________ = 0;

	private void moveHand(int change) {
		if (this.hand.size() > 1 && !this.handMoved) {
			if (change > 0) {
				this.hand.add(this.hand.get(0));
				this.hand.remove(0);
			} else {
				this.hand.add(0, this.hand.get(this.hand.size() - 1));
				this.hand.remove(this.hand.size() - 1);
			}
			this.updateHandPositions();
			this.handMoved = true;
		}
	}

	private void updateHandPositions() {
		for (int i = 0; i < this.hand.size(); i++) {
			this.hand.get(i).setPosition(this.hitbox_hand.getTopLeftCorner()
					.add(Artifact.ARTIFACT_HITBOX.getLowerRightCorner().add(OFFSET_HAND_STEP.mul(i + 0.5f))));
		}
	}

	public void drawCard(Artifact card) {
		if (!this.deck.contains(card)) {
			Log.error("Could not draw " + card + " because it is not in deck of " + this);
		}
		Log.info("Drawing " + card + " for " + this);
		this.hand.add(card);
		this.deck.remove(card);
		this.updateHandPositions();
		SoundManager.getInstance().playDraw();
	}

	public void retrieve(Artifact card) {
		if (!this.discard.contains(card)) {
			Log.error("Could not retrieve " + card + " because it is not in discard of " + this);
		}
		Log.info("Drawing " + card + " for " + this);
		this.hand.add(card);
		this.discard.remove(card);
		this.updateHandPositions();
		SoundManager.getInstance().playDraw();
	}

	public ArrayList<Artifact> drawTopCards(int number) {
		Log.info("Draw " + number + " cards " + " for " + this);
		ArrayList<Artifact> draws = new ArrayList<Artifact>();
		// Mark first cards in deck to be drawn
		for (int i = 0; i < number && i < this.deck.size(); i++) {
			draws.add(this.deck.get(i));
		}
		// Refill deck if necessary, this has to be done with unchanged deck/discard to
		// properly sync the shuffle
		if (draws.size() < number) {
			if (this.discard.size() > 0) {
				this.refillAndShuffleDeck();
			}
		}
		// Draw cards
		for (Artifact card : draws) {
			this.drawCard(card);
		}
		// Draw remaining cards from top of deck
		while (draws.size() < number && this.deck.size() > 0) {
			draws.add(this.deck.get(0));
			this.drawCard(this.deck.get(0));
		}
		// Info if not all cards could be drawn
		if (draws.size() < number) {
			Log.info("Could only draw " + draws.size() + " cards of " + number + " cards requested for player " + this
					+ " because deck and discard are empty");
		}
		return draws;
	}

	public void refillAndShuffleDeck() {
		Log.info("Self refilling deck of " + this);
		this.deck.addAll(this.discard);
		this.discard.clear();
		Collections.shuffle(this.deck);
		this.getGame().getGameClient()
				.informAllClients_Shuffle(CommunicationKeys.VALUE_REFILL_DECK + CommunicationKeys.SEPERATOR_VALUES
						+ this + CommunicationKeys.SEPERATOR_PARTS + UtilFunctions.ListToString(this.deck));
	}

	public void refillAndOrderDeck(ArrayList<Tappable> refilled) {
		this.deck.addAll(this.discard);
		// Check if deck matches refill
		if (this.deck.size() != refilled.size() || !this.deck.containsAll(refilled)) {
			Log.error("Refilled deck: " + UtilFunctions.ListToString(refilled) + " does not match deck: "
					+ UtilFunctions.ListToString(this.deck));
		}
		Log.info("Refilling deck of " + this);
		this.discard.clear();
		this.deck.clear();
		for (int i = 0; i < refilled.size(); i++) {
			this.deck.add((Artifact) refilled.get(i));
		}
	}

	private void putCardInPlay(Tappable card) {
		this.inplay.add(card);
		if (card.canCostReduce()) {
			this.costReducers.add(card);
		}
		if (card.canProtect()) {
			this.protections.add(card);
		}
		if (card instanceof Artifact) {
			if (this.numberUnknown > 0) {
				if (!this.known.contains(card)) {
					this.known.add((Artifact) card);
					this.numberUnknown--;
				}
			}
			if (card.isDemon()) {
				this.demons.add((Artifact) card);
			}
			if (card instanceof VialOfLight) {
				this.getGame().registerVialOfLightInPlay((VialOfLight) card);
			}
		}
	}

	public void destroyArtifact(Artifact toDestroy) {
		if (!this.board.contains(toDestroy)) {
			Log.error("Trying to destroy " + toDestroy + " while it is not in play for " + this);
		}
		Log.info("Destroying " + toDestroy + " for " + this);
		this.boardPositions.freePosition(toDestroy.getPosition());
		this.board.remove(toDestroy);
		this.discard.add(toDestroy);
		if (!toDestroy.setAnimation(AnimationMode.DESTROY)) {
			this.removeDestroyed(toDestroy);
		}
		if (!(toDestroy instanceof VialOfLight) && this.getGame().isVialOfLightInPlay()) {
			this.getGame().registerVialOfLightAction();
		}
	}

	public void discardCard(Artifact result) {
		if (!this.hand.contains(result)) {
			Log.error("Trying to discard " + result + " which is not in hand of " + this);
		}
		Log.info("Discarding " + result + " for " + this);
		this.hand.remove(result);
		this.discard.add(result);
		this.updateHandPositions();
		SoundManager.getInstance().playDiscard();
		if (this.numberUnknown > 0) {
			if (!this.known.contains(result)) {
				this.known.add(result);
				this.numberUnknown--;
			}
		}
	}

	private void removeCardFromPlay(Tappable card) {
		this.inplay.remove(card);
		this.costReducers.remove(card);
		this.protections.remove(card);
		this.demons.remove(card);
		if (card instanceof VialOfLight) {
			this.getGame().removeVialOfLightFromPlay();
		}
	}

	public void removeDestroyed(Tappable card) {
		this.removeCardFromPlay(card);
		card.untap();
		card.clearEssences();
	}

	public void modifyEssence(EssenceSelection sel, boolean subtract) {
		if (subtract) {
			this.counter.sub(sel.getValues());
		} else {
			this.counter.add(sel.getValues());
		}
	}

	public void prepareNewRound() {
		this.incomeFinished = false;
		for (Tappable tappable : this.inplay) {
			tappable.untap();
		}
		SoundManager.getInstance().playUntapAll();
	}

	public void processIncome() {
		this.incomePending.clear();
		this.incomeAutomatic.clear();
		for (Tappable tappable : this.inplay) {
			if (!tappable.askCollectEssencesAndIncome()) { // User input needed
				this.incomePending.add(tappable);
			} else { // Income automatically calculated
				this.incomeAutomatic.add(tappable);
			}
		}
		if (!this.incomeAutomatic.isEmpty()) {
			this.getGame().getGameClient().informAllClients_Action(
					new UserInputOverwrite(this, "AutomaticIncome", UtilFunctions.ListToString(this.incomeAutomatic)));
		}
		if (this.incomePending.isEmpty()) {
			this.getGame().incomeFinished(this);
		}
	}

	public void incomeFinished(Tappable tappable, UserInputOverwrite action) {
		this.incomePending.remove(tappable);
		this.getGame().getGameClient().informAllClients_Action(action);
		Log.info(this.incomePending.size() + " incomes left for " + this);
		if (this.incomePending.isEmpty()) {
			this.getGame().incomeFinished(this);
		}
	}

	public void reorderDeck(ArrayList<Tappable> reorder) {
		if (this.deck.subList(0, reorder.size()).containsAll(reorder)) {
			for (int i = 0; i < reorder.size(); i++) {
				this.deck.set(i, (Artifact) reorder.get(i));
			}
		} else {
			Log.warn("Trying to reorder " + UtilFunctions.ListToString(reorder)
					+ " while some elements are not in the front of the deck " + UtilFunctions.ListToString(this.deck));
		}
	}

	public void claimScroll(Scroll scroll) {
		this.getGame().claimScroll(scroll);
		scroll.setPosition(this.scrollPositions.getNextEmptyPosition());
		scroll.assignPlayer(this);
		this.scrolls.add(scroll);
		this.putCardInPlay(scroll);
	}

	public void returnScroll(Scroll scroll) {
		this.scrolls.remove(scroll);
		this.scrollPositions.freePosition(scroll.getPosition());
		this.removeCardFromPlay(scroll);
		this.getGame().returnScroll(scroll);
	}

	@Override
	public void mouseWheelMoved(Input input, int change) {
		this.moveHand(change);
	}

	@Override
	public boolean isAcceptingScrollingInput() {
		return this.mouseOverHand && this.getGame().getGameClient().isActiveClient()
				&& !this.getGame().getGameClient().getGameState().isMouseBlockedByGUI();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof ImageSelector && this.status == STATE_PLAY_CARD) {
			this.getGame().getGameClient().unsetSelector(sel);
			Artifact card = ((ImageSelector<Artifact>) sel).getResult();
			EssenceSelection cost = card.getCost();
			if (cost.isDetermined()) {
				this.status = STATE_IDLE;
				this.getGame().cardFromHandPlayed(this, new UserInputOverwrite(this, "PlayCard", card));
			} else {
				this.status = STATE_PLAY_CARD_COST;
				this.getGame().getGameClient()
						.addSelector(new EssenceSelector(this, cost, this.getEssenceCounter().getCount(),
								card.getRawCost().getValues(), "Select essences to pay for " + card.getName()));
				this.cardToPlay = card;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_PLAY_CARD_COST) {
			this.getGame().getGameClient().unsetSelector(sel);
			EssenceSelection selection = ((EssenceSelector) sel).getSelection();
			this.status = STATE_IDLE;
			this.getGame().cardFromHandPlayed(this,
					new UserInputOverwrite(this, "PlayCardCost", this.cardToPlay, selection));
		} else {
			Log.warn("Player " + this + " could not process selector: " + sel);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		this.getGame().getGameClient().unsetSelector(sel);
		this.getGame().cancelCardPlay(this);
	}

	@SuppressWarnings("unused")
	private final static int ANIMATION______________ = 0;

	public void playDragonAnimation(Color color) {
		this.dragonShadow.start(color);
	}

	public void playDemonAnimation(Color color) {
		this.demonShadow.start(color);
	}

	public void playArrowAttack() {
		this.arrowAttack.start(Color.white);
	}

	public void playGrowingTreeAnimation() {
		this.growingTree.start(new Color(1, 1, 1, 0.7f));
	}

	public void playTransferAnimation(Essences ess, int amount, Vector start, Vector end) {
		this.transferGen.start(ess.getImage(), amount, start, end);
	}

	public void playDemonSlayerAnimation(Color color) {
		this.demonSlayer.start(color);
	}

	public void playShieldVsArrowAnimation(Color color) {
		this.shieldVsArrow.start(color);
	}

	public void playShieldVsDragonAnimation(Color color) {
		this.shieldVsDragon.start(color);
	}

	public void playShieldVsDemonAnimation(Color color) {
		this.shieldVsDemon.start(color);
	}

	public void playShieldAnimation(Color color) {
		this.shield.start(color);
	}

	public void playGuardDogAnimation() {
		this.guardDog.start(Color.white);
	}

	public void playLionAnimation() {
		this.lion.start(Color.white);
	}

	public void playDancingSwordAnimation() {
		this.dancingSword.start(Color.white);
	}

	@SuppressWarnings("unused")
	private final static int GETTER______________ = 0;

	public Game getGame() {
		return this.parent;
	}

	private ArrayList<Artifact> getPlayableCards() {
		ArrayList<Artifact> out = new ArrayList<Artifact>();
		for (Artifact artifact : this.hand) {
			if (this.isPayable(artifact)) {
				out.add(artifact);
			}
		}
		return out;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public EssenceCounter getEssenceCounter() {
		return this.counter;
	}

	public int getTotalPoints() {
		int points = 0;
		for (Tappable tappable : this.inplay) {
			points += tappable.getPoints();
		}
		if (this.getGame().isNextStartingPlayer(this)) {
			points++;
		}
		return points;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Tappable> getCostReducers() {
		return (ArrayList<Tappable>) this.costReducers.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Tappable> getProtection() {
		return (ArrayList<Tappable>) this.protections.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Artifact> getHand() {
		return (ArrayList<Artifact>) this.hand.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Tappable> getTappablesInPlay() {
		return (ArrayList<Tappable>) this.inplay.clone();
	}

	public ArrayList<Artifact> getArtifactsInPlay() {
		ArrayList<Artifact> artifacts = new ArrayList<Artifact>();
		for (Tappable tappable : this.inplay) {
			if (tappable instanceof Artifact) {
				artifacts.add((Artifact) tappable);
			}
		}
		return artifacts;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Artifact> getDiscard() {
		return (ArrayList<Artifact>) this.discard.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Artifact> getDeck() {
		return (ArrayList<Artifact>) this.deck.clone();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Artifact> getDemons() {
		return (ArrayList<Artifact>) this.demons.clone();
	}

	public MagicItem getMagicItem() {
		return this.item;
	}

	public Rectangle getHitbox() {
		return this.hitbox;
	}

	public StatisticsElement getStats() {
		StatisticsElement stats = new StatisticsElement();
		stats.setValue(StatisticProperties.POINTS, this.getTotalPoints());
		stats.setValue(StatisticProperties.POWER_PLACES, this.places.size());
		stats.setValue(StatisticProperties.MONUMENTS, this.monuments.size());
		stats.setValue(StatisticProperties.VALUE_IN_PLAY, this.getValueInPlay()); // GOLD counts double
		stats.setValue(StatisticProperties.CARDS_IN_PLAY, this.inplay.size() - 2); // No mage and item
		stats.setValue(StatisticProperties.CARDS_IN_HAND, this.hand.size());
		stats.setValue(StatisticProperties.NON_GOLD, this.getEssenceCounter().getTotalCount()
				- this.getEssenceCounter().getCount()[Essences.GOLD.ordinal()]);
		stats.setValue(StatisticProperties.GOLD, this.getEssenceCounter().getCount()[Essences.GOLD.ordinal()]);
		stats.setValue(StatisticProperties.HIGHEST_ESSENCE, this.getHighestEssence());
		stats.setValue(StatisticProperties.TOTAL_ESSENCES, this.getEssenceCounter().getTotalCount());
		return stats;
	}

	private int getValueInPlay() {
		int value = 0;
		for (Tappable tappable : this.inplay) {
			value += tappable.getRawCost().getTotal() + tappable.getRawCost().getValue(Essences.GOLD);
		}
		return value;
	}

	private int getHighestEssence() {
		int[] ess = this.getEssenceCounter().getCount();
		int max = 0;
		for (int i = 0; i < ess.length; i++) {
			if (ess[i] > max) {
				max = ess[i];
			}
		}
		return max;
	}

	@SuppressWarnings("unchecked")
	public ArrayList<Scroll> getScrolls() {
		return (ArrayList<Scroll>) this.scrolls.clone();
	}

	public Vector getEssencePosition(Essences ess) {
		return this.position.add(POSITION_ESSENCES).add(this.counter.getPosition(ess.ordinal()));
	}

	public Vector getMagePosition() {
		return this.position.add(POSITION_MAGE);
	}
}
