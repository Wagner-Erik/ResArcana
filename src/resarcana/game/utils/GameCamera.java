package resarcana.game.utils;

import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.graphics.Camera;
import resarcana.graphics.Engine;
import resarcana.graphics.Pollable;
import resarcana.graphics.gui.Interfaceable;
import resarcana.graphics.utils.ScrollingListener;
import resarcana.graphics.utils.ScrollingManager;
import resarcana.math.Rectangle;
import resarcana.math.Shape;
import resarcana.math.Vector;

public class GameCamera implements Camera, Pollable, ScrollingListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4081109857640074973L;

	private static final float BORDER_VEL_MULTIPLIER = 0.5f;

	private Vector oldPos, curPos, lastMousePosWithoutClick = Vector.ZERO, mousePos = Vector.ZERO,
			mousePosDif = Vector.ZERO;

	private final float vel, borderVel;
	private float zoom, zoomFactor;
	private final Shape blockedArea;
	private final Rectangle minCameraRoom, noBorderCameraRoom, gameHitbox;
	private final float minZoom, maxZoom;

	private final Vector borderMoveTL;
	private final float borderMoveAngTR, borderMoveAngBL;

	private boolean mouseOverCameraRoom = false;

	private Interfaceable gui;

	private static GameCamera activeGameCamera = null;

	/**
	 * Defines a camera for the game with some blocked Area in the bottom-right
	 * corner
	 * 
	 * @param startPos
	 * @param blockedArea
	 * @param gameHitbox
	 * @param movingVelocity
	 * @param maxZoom
	 */
	public GameCamera(Vector startPos, Shape blockedArea, Rectangle gameHitbox, float movingVelocity, float maxZoom) {
		this.vel = movingVelocity;
		this.zoom = 1.f;
		this.zoomFactor = 1.2f;
		this.blockedArea = new Rectangle(Engine.getInstance().getWidth() - blockedArea.getXRange(),
				Engine.getInstance().getHeight() - blockedArea.getYRange(), blockedArea.getXRange(),
				blockedArea.getYRange());
		this.gameHitbox = gameHitbox;
		// Limits the zoom to not show black borders inside the cameraRoom when zooming
		// out too far
		this.minCameraRoom = new Rectangle(0, 0, Engine.getInstance().getWidth() - this.blockedArea.getXRange(),
				Engine.getInstance().getHeight());
		this.minZoom = Math.min(this.minCameraRoom.width / this.gameHitbox.width,
				this.minCameraRoom.height / this.gameHitbox.height);
		this.maxZoom = maxZoom;
		// Setup for movement at border of the cameraRoom
		this.noBorderCameraRoom = Engine.getInstance().getScreenBox().scale(0.98f);
		this.borderMoveTL = this.noBorderCameraRoom.getTopLeftCorner().sub(this.noBorderCameraRoom.getCenter());
		this.borderMoveAngBL = this.borderMoveTL
				.clockWiseAng(this.noBorderCameraRoom.getBottomLeftCorner().sub(this.noBorderCameraRoom.getCenter()));
		this.borderMoveAngTR = this.borderMoveTL
				.clockWiseAng(this.noBorderCameraRoom.getTopRightCorner().sub(this.noBorderCameraRoom.getCenter()));
		this.borderVel = this.vel * BORDER_VEL_MULTIPLIER * Math.min(this.gameHitbox.width, this.gameHitbox.height);

		ScrollingManager.getInstance().addListener(this);

		Log.info("Camera information:");
		Log.info("Start:" + startPos);
		Log.info("min-room: " + this.minCameraRoom);
		Log.info("hitbox:" + this.gameHitbox);
		this.curPos = this.limitCameraToGameHitbox(startPos);
		this.oldPos = this.curPos;
		Log.info("cur: " + this.curPos);
	}

	public void setGUI(Interfaceable gui) {
		this.gui = gui;
	}

	public void toZero() {
		this.curPos = Vector.ZERO;
	}

	public Shape getBlockedArea() {
		return this.blockedArea;
	}

	/**
	 * Sets the position of the camera
	 * 
	 * @param newPos the new position
	 * @return false if the postion had to be limited by limitCameraToGameHitbox(),
	 *         true otherwise
	 */
	public boolean setPosition(Vector newPos) {
		this.curPos = this.limitCameraToGameHitbox(newPos);
		return this.curPos.equals(newPos);
	}

	/**
	 * Sets the position of the camera to center on the given position
	 * 
	 * @param newPos the new position
	 * @return false if the postion had to be limited by limitCameraToGameHitbox(),
	 *         true otherwise
	 */
	public boolean setCenter(Vector newPos) {
		this.curPos = this.limitCameraToGameHitbox(newPos.sub(this.blockedArea.getCenter()));
		return this.curPos.equals(newPos);
	}

	@Override
	public void poll(Input input, float secounds) {
		activeGameCamera = this;

		this.mousePos = new Vector(input.getMouseX(), input.getMouseY());
		this.mouseOverCameraRoom = !this.blockedArea.isPointInThis(this.mousePos);
		if (input.isMouseButtonDown(Input.MOUSE_RIGHT_BUTTON) && this.mouseOverCameraRoom
				&& !this.isMouseBlockedByGUI()) {
			this.mousePosDif = this.lastMousePosWithoutClick.sub(this.mousePos);
			this.curPos = this.limitCameraToGameHitbox(this.oldPos.add(this.mousePosDif.mul(this.vel / this.zoom)));
		} else {
			// Movement at border of cameraRoom if in fullscreen mode
			if (Engine.getInstance().isFullscreen() && this.mouseOverCameraRoom
					&& !this.noBorderCameraRoom.isPointInThis(this.mousePos) && !this.isMouseBlockedByGUI()) {
				this.moveAtBorder(secounds);
			}
			this.lastMousePosWithoutClick = this.mousePos;
			this.oldPos = this.curPos;
		}
	}

	private void moveAtBorder(float secounds) {
		float ang = this.borderMoveTL.clockWiseAng(this.mousePos.sub(this.noBorderCameraRoom.getCenter()));
		if (ang < Math.PI) {
			if (ang < this.borderMoveAngTR) {
				this.move(Vector.UP, secounds);
			} else {
				this.move(Vector.RIGHT, secounds);
			}
		} else {
			if (ang > this.borderMoveAngBL) {
				this.move(Vector.LEFT, secounds);
			} else {
				this.move(Vector.DOWN, secounds);
			}
		}
	}

	private void move(Vector dir, float secounds) {
		this.curPos = this.limitCameraToGameHitbox(this.oldPos.add(dir.mul(this.borderVel / this.zoom * secounds)));
	}

	private void zoom(int change) {
		if (this.mouseOverCameraRoom) {
			if (change > 0 && this.zoom * this.zoomFactor <= this.maxZoom) {
				this.zoom *= this.zoomFactor;
				this.curPos = this
						.limitCameraToGameHitbox(this.curPos.add(this.mousePos.mul((this.zoomFactor - 1) / this.zoom)));
			} else if (change < 0 && this.zoom / this.zoomFactor >= this.minZoom) {
				this.zoom /= this.zoomFactor;
				this.curPos = this.limitCameraToGameHitbox(
						this.curPos.add(this.mousePos.mul((1 / this.zoomFactor - 1) / this.zoom)));
			}
			this.lastMousePosWithoutClick = this.mousePos;
			this.oldPos = this.curPos;
		}
	}

	private Vector limitCameraToGameHitbox(Vector pos) {
		float x = 0, y = 0;
		if (pos != null) {
			x = pos.x;
			y = pos.y;
		}
		if (this.gameHitbox.width < this.minCameraRoom.width / this.zoom) {
			// Keep table in view
			if (x > this.gameHitbox.x) {
				x = this.gameHitbox.x;
			}
			if (x < this.gameHitbox.x + this.gameHitbox.width - this.minCameraRoom.width / this.zoom) {
				x = this.gameHitbox.x + this.gameHitbox.width - this.minCameraRoom.width / this.zoom;
			}
		} else {
			// Keep view in table
			if (x < this.gameHitbox.x) {
				x = this.gameHitbox.x;
			}
			if (x > this.gameHitbox.x + this.gameHitbox.width - this.minCameraRoom.width / this.zoom) {
				x = this.gameHitbox.x + this.gameHitbox.width - this.minCameraRoom.width / this.zoom;
			}
		}
		if (this.gameHitbox.height < this.minCameraRoom.height / this.zoom) {
			// Keep table in view
			if (y < this.gameHitbox.y + this.gameHitbox.height - this.minCameraRoom.height / this.zoom) {
				y = this.gameHitbox.y + this.gameHitbox.height - this.minCameraRoom.height / this.zoom;
			}
			if (y > this.gameHitbox.y) {
				y = this.gameHitbox.y;
			}
		} else {
			// Keep view in table
			if (y < this.gameHitbox.y) {
				y = this.gameHitbox.y;
			}
			if (y > this.gameHitbox.y + this.gameHitbox.height - this.minCameraRoom.height / this.zoom) {
				y = this.gameHitbox.y + this.gameHitbox.height - this.minCameraRoom.height / this.zoom;
			}
		}
		return new Vector(x, y);
	}

	public float getVelocity() {
		return this.vel;
	}

	public float getMaxZoom() {
		return this.maxZoom;
	}

	private boolean isMouseBlockedByGUI() {
		if (this.gui != null) {
			if (this.gui.isMouseBlockedByGUI()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void mouseWheelMoved(Input input, int change) {
		if (!this.isMouseBlockedByGUI()) {
			this.zoom(change);
		}
	}

	@Override
	public boolean isAcceptingScrollingInput() {
		return this.mouseOverCameraRoom && activeGameCamera == this;
	}

	@Override
	public Vector getPosition() {
		return this.curPos;
	}

	@Override
	public float getZoom() {
		return this.zoom;
	}

	@Override
	public void applyCamera(Graphics g) {
		g.scale(this.zoom, this.zoom);
		g.translate(-this.curPos.x, -this.curPos.y);
	}

	public Vector getTransformedMousePos(Input input) {
		return this.curPos.add(input.getMouseX() / this.zoom, input.getMouseY() / this.zoom);
	}
}
