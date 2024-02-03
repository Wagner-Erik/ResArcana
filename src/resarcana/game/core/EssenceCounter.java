package resarcana.game.core;

import java.util.Arrays;
import java.util.EnumSet;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.util.Log;

import resarcana.communication.CommunicationKeys;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.animation.EssenceAnimation;
import resarcana.graphics.Pollable;
import resarcana.graphics.PositionDrawable;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class EssenceCounter implements PositionDrawable, Pollable {

	public static final float BOX_SIZE = 32;
	public static final Rectangle[] BOXES_POOL = {
			new Rectangle(-1.5f * BOX_SIZE, -0.75f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(0, -0.75f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(1.5f * BOX_SIZE, -0.75f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(-0.75f * BOX_SIZE, 0.75f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(0.75f * BOX_SIZE, 0.75f * BOX_SIZE, BOX_SIZE, BOX_SIZE) };

	public static final Rectangle[] BOXES_COLUMN = { new Rectangle(0, 0, BOX_SIZE, BOX_SIZE),
			new Rectangle(0, 1.1f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(0, 2.2f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(0, 3.3f * BOX_SIZE, BOX_SIZE, BOX_SIZE),
			new Rectangle(0, 4.4f * BOX_SIZE, BOX_SIZE, BOX_SIZE) };

	static {
		assert Essences.values().length == BOXES_POOL.length : "Length of BOXES does not fit number of Essences";
	}

	private final Object parent;
	private final boolean staticPositions, showZero;

	private final float scale;

	private final Rectangle[] boxes;
	private final EssenceAnimation[] animations;

	/**
	 * Elan, Life, Calm, Death, Gold
	 */
	private int[] count;

	public EssenceCounter(Object parent, float scale, Rectangle[] boxesRaw, boolean staticPositions, boolean showZero) {
		this.parent = parent;
		this.staticPositions = staticPositions;
		this.showZero = showZero;
		this.scale = scale;
		this.count = new int[Essences.values().length];
		this.boxes = new Rectangle[boxesRaw.length];
		for (int i = 0; i < boxesRaw.length; i++) {
			this.boxes[i] = boxesRaw[i].scaleWithCenter(this.scale);
		}
		this.animations = new EssenceAnimation[Essences.values().length];
		for (Essences ess : Essences.values()) {
			this.animations[ess.ordinal()] = new EssenceAnimation(boxesRaw[ess.ordinal()], ess, this.scale);
		}
	}

	@Override
	public String toString() {
		return "Counter" + CommunicationKeys.SEPERATOR_NAME + this.parent.toString() + CommunicationKeys.SEPERATOR_NAME
				+ Arrays.toString(this.count).replace(", ", CommunicationKeys.SEPERATOR_VALUES);
	}

	@Override
	public void drawAt(Graphics g, Vector position) {
		g.pushTransform();
		g.translate(position.x, position.y);
		g.setFont(FontManager.getInstance().getFont((int) (this.scale * Parameter.GUI_STANDARD_FONT_SIZE)));
		g.setColor(Color.white);
		int box = 0;
		for (int i = 0; i < this.count.length; i++) {
			if (this.count[i] != 0 || this.animations[i].isRunning() || this.showZero) {
				GraphicUtils.drawImage(g, this.boxes[box],
						ResourceManager.getInstance().getImage(Essences.values()[i].getImage()));
				GraphicUtils.drawStringCentered(g, this.boxes[box].getCenter(), "" + this.count[i]);
				this.animations[i].drawAt(g, this.boxes[box].getCenter());
				box++;
			} else if (this.staticPositions) {
				box++;
			}
		}
		g.popTransform();
	}

	@Override
	public void poll(Input input, float secounds) {
		for (EssenceAnimation ani : this.animations) {
			ani.poll(input, secounds);
		}
	}

	public void stopAnimations() {
		for (EssenceAnimation ani : this.animations) {
			ani.stop();
		}
	}

	public int[] getCount() {
		return this.count.clone();
	}

	/**
	 * Adds the values of all "determined" Essences corresponding to "toAdd" to this
	 * values
	 * 
	 * @param toAdd
	 */
	public void add(int[] toAdd) {
		SoundManager.getInstance().playMoveEssences();
		if (toAdd.length >= this.count.length) {
			for (int i = 0; i < this.count.length; i++) {
				this.count[i] += toAdd[i];
				if (toAdd[i] > 0) {
					this.animations[i].start(true);
				}
			}
			Log.debug("Added " + Arrays.toString(toAdd) + " to " + this);
		} else {
			Log.error("Wrong size of array to add. Expected >=" + this.count.length + ", got " + toAdd.length);
		}
	}

	/**
	 * Subtracts the values of all "determined" Essences corresponding to "toAdd"
	 * from this values
	 * 
	 * @param toSub
	 */
	public void sub(int[] toSub) {
		SoundManager.getInstance().playMoveEssences();
		if (toSub.length >= count.length) {
			for (int i = 0; i < count.length; i++) {
				this.count[i] -= toSub[i];
				if (this.count[i] < 0) {
					Log.warn("Subtracting more " + Essences.values()[i].toString() + "(" + this.count[i]
							+ ") from counter " + this + " than is available.");
				}
				if (toSub[i] > 0) {
					this.animations[i].start(false);
				}
			}
			Log.debug("Subtracted " + Arrays.toString(toSub) + " from " + this);
		} else {
			Log.error("Wrong size of array to add. Expected >=" + this.count.length + ", got " + toSub.length);
		}
	}

	public void resetCounter() {
		for (int i = 0; i < count.length; i++) {
			if (this.count[i] > 0) {
				this.animations[i].start(false);
			}
			this.count[i] = 0;
		}
	}

	public boolean isPayable(EssenceSelection sel) {
		return this.isPayable(sel, null);
	}

	public boolean isPayable(EssenceSelection sel, EssenceSelection maxAllowed) {
		int[] buf = this.count.clone();
		if (maxAllowed != null) { // Limit available essences to those allowed
			for (Essences ess : Essences.values()) {
				buf[ess.ordinal()] = Math.min(buf[ess.ordinal()],
						maxAllowed.getValue(ess) + maxAllowed.getIndeterminedValue());
			}
		}
		int[] val = sel.getValues();
		int remain = 0;
		for (Essences ess : EnumSet.complementOf(sel.getExcludes())) {
			buf[ess.ordinal()] -= val[ess.ordinal()];
			if (val[ess.ordinal()] > 0 && buf[ess.ordinal()] < 0) {
				return false;
			}
			remain += buf[ess.ordinal()];
		}
		return remain >= val[val.length - 1];
	}

	public boolean isEmpty() {
		for (int i = 0; i < this.count.length; i++) {
			if (this.count[i] > 0) {
				return false;
			}
		}
		return true;
	}

	public int getTotalCount() {
		int total = 0;
		for (int i = 0; i < this.count.length; i++) {
			total += count[i];
		}
		return total;
	}

	public Vector getPosition(int num) {
		if (num < this.boxes.length) {
			return this.boxes[num].getCenter();
		} else {
			return Vector.ZERO;
		}
	}
}
