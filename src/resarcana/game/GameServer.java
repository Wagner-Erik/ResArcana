package resarcana.game;

import java.io.IOException;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import resarcana.communication.ClientInfo;
import resarcana.communication.Server;
import resarcana.graphics.Engine;
import resarcana.graphics.gui.InterfaceContainer;
import resarcana.graphics.gui.InterfaceFunction;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.container.GridContainer;
import resarcana.graphics.gui.objects.Mousestatus;
import resarcana.graphics.gui.objects.TileableBackgroundButton;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.math.Vector;

/**
 * GUI-Wrapper for {@link Server}
 * 
 * @author Erik
 *
 */
public class GameServer implements DrawPollInterface {

	public final Server server;

	private InterfaceContainer container;

	/**
	 * Creates a GUI-wrapper for a {@link Server}
	 * 
	 * @throws IOException if the server could not be initialized
	 */
	public GameServer() throws IOException {
		this.server = new Server(true, 1);
		this.initGui();
		// Start Server in seperate Thread
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				GameServer.this.server.startServer();
			}
		});
		t.start();
	}

	/**
	 * Initializes all GUI parts for the server
	 */
	private void initGui() {
		GridContainer con = new GridContainer(3, 2);
		con.add(new TileableBackgroundButton(InterfaceFunctions.SERVER_START_GAME, "Start game", 1.5f, 3)
				.addInformable(this), 2, 0);
		con.maximizeSize();
		this.container = con;
	}

	@Override
	public void draw(Graphics g) {
		this.drawClients(g);
	}

	/**
	 * Draw information about all connected clients
	 * 
	 * @param g the context to draw on
	 */
	private void drawClients(Graphics g) {
		g.pushTransform();
		ClientInfo client;
		Vector pos;
		for (int i = 0; i < this.server.getClients().size(); i++) {
			client = this.server.getClients().get(i);
			pos = new Vector(50, Engine.getInstance().getHeight() / 4).add(new Vector(0, 100).mul(i));
			g.setFont(FontManager.getInstance().getDefaultFont());
			GraphicUtils.drawString(g, pos, client.id + ": " + client.getName());
			GraphicUtils.drawString(g,
					pos.add(Vector.DOWN.mul(1 * FontManager.getInstance().getLineHeight(g.getFont()) + 4)),
					"Ready: " + client.isReady());
		}
		g.popTransform();
	}

	@Override
	public void poll(Input input, float secounds) {
		// Nichts tun
	}

	@Override
	public InterfaceContainer getInterfaceContainer() {
		return this.container;
	}

	@Override
	public void mouseButtonAction(InterfaceObject object) {
		InterfaceFunction function = object.getFunction();
		if (function == InterfaceFunctions.SERVER_START_GAME
				&& object.getStatus() == Mousestatus.STATUS_LEFT_RELEASED) {
			this.server.startGame();
		}
	}

	@Override
	public void mouseOverAction(InterfaceObject object) {
		// Nothing to do
	}

	@Override
	public void objectIsSelected(InterfaceObject object) {
		// Nothing to do
	}
}
