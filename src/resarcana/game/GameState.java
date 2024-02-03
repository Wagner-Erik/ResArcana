package resarcana.game;

import java.awt.SplashScreen;
import java.io.IOException;
import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.util.Log;

import resarcana.game.core.Game;
import resarcana.game.utils.animation.Tracer;
import resarcana.game.utils.factory.ArtifactFactory;
import resarcana.game.utils.factory.MageFactory;
import resarcana.game.utils.factory.MagicItemFactory;
import resarcana.game.utils.factory.MonumentFactory;
import resarcana.game.utils.factory.PowerPlaceFactory;
import resarcana.game.utils.factory.ScrollFactory;
import resarcana.graphics.AbstractState;
import resarcana.graphics.Camera;
import resarcana.graphics.Engine;
import resarcana.graphics.SlickEngine;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.Interfaceable;
import resarcana.graphics.gui.MainGUI;
import resarcana.graphics.gui.ThemesGUI;
import resarcana.graphics.gui.container.AdvancedGridContainer;
import resarcana.graphics.gui.container.CardContainer;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.graphics.gui.container.HeadlineContainer;
import resarcana.graphics.gui.objects.ImageButton;
import resarcana.graphics.gui.objects.Label;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TileableBackgroundButton;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.Scheduler;
import resarcana.graphics.utils.ScrollingManager;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.JarHandler;
import resarcana.utils.Parameter;

public class GameState extends AbstractState implements Interfaceable {

	private MainGUI gui;
	private Camera camera;
	private ArrayList<GameClient> clients = new ArrayList<GameClient>();
	private DrawPollInterface active = null;
	private GameServer server = null;
	private CardContainer cardContainer;
	private Rectangle loadingBarSpace;

	private Tracer trace;
	private boolean mouseOverGUI = false;

	public GameState() {
		// Initialization will be done in init(GameContainer, StateBasedGame)
	}

	private void initGui() {
		// Set scheduler box
		this.loadingBarSpace = new Rectangle(
				Engine.getInstance().getScreenBox().x + Engine.getInstance().getScreenBox().width * 0.1f,
				Engine.getInstance().getScreenBox().y + Engine.getInstance().getScreenBox().height * 0.4f,
				Engine.getInstance().getScreenBox().width * 0.3f, Engine.getInstance().getScreenBox().height * 0.3f);
		Scheduler.getInstance().setSpace(this.loadingBarSpace);

		this.gui = new MainGUI(this);

		GridContainer head = new GridContainer(1, 1, GridContainer.MODUS_X_RIGHT, GridContainer.MODUS_Y_UP);
		head.add(new ImageButton(InterfaceFunctions.BACK_TO_MAIN, new Rectangle(Vector.ZERO, 150, 70),
				"interface-icons/TextBackground_down.png", "Menu\n").addInformable(this), 0, 0);
		head.maximizeXRange();

		AdvancedGridContainer mainMenu = new AdvancedGridContainer(4, 1, AdvancedGridContainer.MODUS_DEFAULT,
				AdvancedGridContainer.MODUS_DEFAULT, 20, 20);
		mainMenu.add(
				new TileableBackgroundButton(InterfaceFunctions.JOIN_GAME, "Join game", 1.5f, 3).addInformable(this), 0,
				0);
		mainMenu.add(
				new TileableBackgroundButton(InterfaceFunctions.ADD_CLIENT, "Add player", 1.5f, 3).addInformable(this),
				1, 0);
		mainMenu.add(new TileableBackgroundButton(InterfaceFunctions.CREATE_SERVER, "Host local server", 1.5f, 3)
				.addInformable(this), 2, 0);
		mainMenu.add(new TileableBackgroundButton(InterfaceFunctions.GAME_EXIT, "Quit", 1.5f, 3).addInformable(this), 3,
				0);

		GridContainer menuCon = new GridContainer(3, 2);
		menuCon.add(mainMenu, 1, 0, GridContainer.MODUS_DEFAULT, GridContainer.MODUS_DEFAULT);
		menuCon.add(new Label(JarHandler.getJarName(), 2, Color.white), 2, 1, GridContainer.MODUS_X_RIGHT,
				GridContainer.MODUS_Y_DOWN);
		menuCon.maximizeSize();

		this.cardContainer = new CardContainer();
		this.cardContainer.add(menuCon);
		this.cardContainer.maximizeSize();

		HeadlineContainer main = new HeadlineContainer(head, this.cardContainer);
		main.maximizeSize();

		this.gui.setMainContainer(main);

//		this.trace = new Tracer(
//				new MouseTraceGenerator(new ColorVariationGenerator(ParticleColorScheme.WHITE), 1.0f, 80));
	}

	public void setActive(DrawPollInterface active) {
		this.active = active;
		this.cardContainer.switchTo(this.active.getInterfaceContainer());
	}

	@Override
	public void poll(Input input, float secounds) {
		this.mouseOverGUI = false;
		// poll sound first to allow new sounds to be played
		SoundManager.getInstance().poll(input, secounds);
		// Process server communication for the client
		if (this.clients.size() > 0) {
			for (GameClient client : this.clients) {
				client.processPackets();
			}
		}
		// Don't poll GUI etc. while loading resources
		if (!Scheduler.getInstance().hasItemsScheduled()) {
			// Poll the GUI first for player interaction
			this.gui.poll(input, secounds);
			// Poll the active component for userinput
			if (this.active != null) {
				this.active.poll(input, secounds);
			}
			// If requested change the active component
			this.checkControlInput(input);
		}
		if (this.trace != null) {
			this.trace.poll(input, secounds);
		}
		// Clear the input record after everything has been processed
		input.clearKeyPressedRecord();
		input.clearControlPressedRecord();
		input.clearMousePressedRecord();
	}

	private void checkControlInput(Input input) {
		if (input.isKeyDown(Input.KEY_LCONTROL) || input.isKeyDown(Input.KEY_RCONTROL)) {
			if (input.isKeyPressed(Input.KEY_M)) {
				this.switchTo(null);
			} else if (input.isKeyPressed(Input.KEY_C)) { // Next client
				if (this.clients.size() > 0) {
					this.switchTo(this.clients.get((this.clients.indexOf(this.active) + 1) % this.clients.size()));
				} else {
					this.switchTo(null);
				}
			} else if (input.isKeyPressed(Input.KEY_S)) {
				Log.info("Switching");
				this.switchTo(this.server);
			} else if (input.isKeyDown(Input.KEY_LALT) && input.isKeyPressed(Input.KEY_R)) {
				ResourceManager.getInstance().reloadImages();
			}
		} else {
			if (input.isKeyPressed(Input.KEY_ESCAPE)) {
				this.switchTo(null);
			}
		}
	}

	private void switchTo(DrawPollInterface state) {
		if (state != null && (state == this.server || this.clients.contains(state))) {
			Log.info("Switching to: " + state);
			this.active = state;
			this.cardContainer.switchTo(state.getInterfaceContainer());
		} else {
			// Remove GameClient if not connected to a server and going back to the main
			// menu
			if (this.active instanceof GameClient) {
				if (!((GameClient) this.active).isConnected()) {
					this.clients.remove(this.active);
					this.cardContainer.remove(this.active.getInterfaceContainer());
				}
			}
			this.active = null;
			this.cardContainer.switchTo(0);
		}
	}

	@Override
	public void draw(Graphics g) {
		g.setFont(FontManager.getInstance().getDefaultFont());
		g.setColor(Color.white);
		GraphicUtils.drawImage(g, Engine.getInstance().getScreenBox(),
				ResourceManager.getInstance().getImage("background/main_menu_1_name.png"));

		if (Scheduler.getInstance().hasItemsScheduled()) {
			Scheduler.getInstance().draw(g);
			Scheduler.getInstance().loadNextScheduledItem();
		} else {
			if (this.active != null) {
				this.active.draw(g);
			}
			this.gui.draw(g);
			if (this.clients.contains(this.active)) {
				((GameClient) this.active).drawDetailedCard(g);
			}
		}
		if (this.trace != null) {
			this.trace.draw(g);
		}
		g.resetFont();
	}

	@Override
	public int getHeight() {
		return Engine.getInstance().getHeight();
	}

	@Override
	public int getWidth() {
		return Engine.getInstance().getWidth();
	}

	@Override
	public Camera getCamera() {
		return this.camera;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		if (object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			if (object.getFunction() == InterfaceFunctions.GAME_EXIT) {
				SlickEngine.getInstance().exit();
			} else if (object.getFunction() == InterfaceFunctions.CREATE_SERVER) {
				if (this.server == null) {
					try {
						Log.info("Starting server");
						this.server = new GameServer();
						this.active = this.server;
						this.cardContainer.add(this.server.getInterfaceContainer());
					} catch (IOException e) {
						Log.error("Can't create Server");
						e.printStackTrace();
					}
				} else {
					this.switchTo(this.server);
				}
			} else if (object.getFunction() == InterfaceFunctions.JOIN_GAME) {
				if (this.clients.size() > 0) {
					this.switchTo(this.clients.get(0));
				} else {
					this.addGameClient();
				}
			} else if (object.getFunction() == InterfaceFunctions.ADD_CLIENT) {
				this.addGameClient();
			} else if (object.getFunction() == InterfaceFunctions.BACK_TO_MAIN) {
				this.switchTo(null);
			}
		}
	}

	private void addGameClient() {
		Log.info("Creating GameClient");
		this.clients.add(new GameClient(this));
		this.cardContainer.add(this.clients.get(this.clients.size() - 1).getInterfaceContainer());
		this.active = this.clients.get(this.clients.size() - 1);
	}

	public GameClient getClient() {
		if (this.active instanceof GameClient) {
			return (GameClient) this.active;
		} else {
			return null;
		}
	}

	public boolean isActive(DrawPollInterface inter) {
		return this.active == inter;
	}

	@Override
	public boolean isShown(InterfaceContainer interfaceContainer) {
		return interfaceContainer == this.gui;
	}

	@Override
	public boolean isMouseBlockedByGUI() {
		return this.mouseOverGUI;
	}

	@Override
	public void registerMouseBlockedByGUI() {
		this.mouseOverGUI = true;
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void init(GameContainer container, StateBasedGame game) throws SlickException {
		// Load mouse cursor
		Image cursor = ResourceManager.getInstance().getImage("misc/cursor.png");
		if (cursor != ResourceManager.getInstance().getDefaultImage()) {
			container.setMouseCursor(cursor, 0, 0);
		}
		// Preload menu background
		ResourceManager.getInstance().getImage("background/main_menu_1_name.png");

		// Set theme for the GUI
		ThemesGUI.setDefaultTheme(ThemesGUI.SCROLL_THEME);

		if (Launcher.preloadImages) {
			// Schedule card images
			ArtifactFactory.scheduleImages();
			MageFactory.scheduleImages();
			MagicItemFactory.scheduleImages();
			MonumentFactory.scheduleImages();
			PowerPlaceFactory.scheduleImages();
			ScrollFactory.scheduleImages();

			// Schedule misc images, grouped in GameClient, Game and MainGUI
			GameClient.scheduleImages();
			Game.scheduleImages();
			MainGUI.scheduleImages();
		}

		// Create FontManager
		FontManager.getInstance().setDefaultFont(Parameter.GUI_STANDARD_FONT_SIZE);
		Engine.getInstance().setDefaultFont(FontManager.getInstance().getDefaultFontNoAlias());
		if (Launcher.preloadFonts) {
			FontManager.getInstance().scheduleFonts();
		}

		// Load sounds
		SoundManager.getInstance().setBaseVolume(0.5f);
		SoundManager.getInstance().setSoundVolume(0.25f);
		SoundManager.getInstance().setMusicVolume(0.04f);
		SoundManager.getInstance().startMusic();

		// Set input for ScrollingManager
		ScrollingManager.getInstance().setInput(container.getInput());

		// Initialize GUI
		this.initGui();

		// Close SplashScreen
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			try {
				splash.close();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}
}
