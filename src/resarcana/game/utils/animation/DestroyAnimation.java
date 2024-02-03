package resarcana.game.utils.animation;

import java.util.Random;

import org.newdawn.slick.Color;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;

import javafx.util.Pair;
import resarcana.game.utils.animation.generator.PathGenerator;
import resarcana.game.utils.animation.generator.WarpGenerator;
import resarcana.graphics.AdvancedImage;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.PolygonTriangulation;
import resarcana.utils.UtilFunctions;

public class DestroyAnimation implements Animation {

	private static final int CUT_POINTS = 50;

	private final Rectangle rawBox;
	private final String image;
	private final float lifetime, cutDeltaY;
	private final PathGenerator cut;
	private final WarpGenerator warpUp, warpDown;
	private final boolean drawUpFirst;

	private float[] pointsSrcUp, pointsSrcDown, pointsLineRaw, pointsUp, pointsDown;
	private int[] triangleVerticesUp, triangleVerticesDown;

	private Color color = Color.white, fixedColor = null;
	private float progress;
	private boolean autoLooping = false;

	public DestroyAnimation(Rectangle rawBox, String image, float lifetime, PathGenerator cut, float cutDeltaY,
			WarpGenerator warpUp, WarpGenerator warpDown, boolean drawUpFirst) {
		this.rawBox = rawBox;
		this.image = image;

		this.lifetime = lifetime;
		this.drawUpFirst = drawUpFirst;

		this.cut = cut;
		this.cutDeltaY = cutDeltaY;

		this.warpUp = warpUp;
		this.warpDown = warpDown;

		this.progress = 0;
		this.calcPoints();
		this.progress = 2;
	}

	private void calcPoints() {
		int len = 2 * 4 * CUT_POINTS;
		float x = 0, y = 0;
		if (this.pointsLineRaw == null) {
			this.pointsLineRaw = new float[2 * (CUT_POINTS + 1)];
			Random random = new Random();
			Vector pos = this.cut.getPosition(0);
			this.pointsLineRaw[0] = pos.x;
			this.pointsLineRaw[1] = pos.y;
			for (int i = 1; i < CUT_POINTS; i++) {
				pos = this.cut.getPosition(i * 1.0f / CUT_POINTS);
				this.pointsLineRaw[i * 2] = pos.x;
				this.pointsLineRaw[i * 2 + 1] = pos.y + this.cutDeltaY * (2 * random.nextFloat() - 1);
			}
			pos = this.cut.getPosition(1);
			this.pointsLineRaw[2 * CUT_POINTS] = pos.x;
			this.pointsLineRaw[2 * CUT_POINTS + 1] = pos.y;

			this.pointsSrcUp = new float[len];
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcUp[i] = (this.pointsLineRaw[i] - this.rawBox.x) / this.rawBox.width;
				this.pointsSrcUp[i + 1] = (this.pointsLineRaw[i + 1] - this.rawBox.y) / this.rawBox.height;
			}
			x = (pos.x - this.rawBox.x) / this.rawBox.width;
			y = (pos.y - this.rawBox.y) / this.rawBox.height;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcUp[i + (2 * CUT_POINTS)] = x + (1 - x) * i / (2 * CUT_POINTS);
				this.pointsSrcUp[i + (2 * CUT_POINTS) + 1] = y + (0 - y) * i / (2 * CUT_POINTS);
			}
			x = 1;
			y = 0;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcUp[i + 2 * (2 * CUT_POINTS)] = x + (0 - x) * i / (2 * CUT_POINTS);
				this.pointsSrcUp[i + 2 * (2 * CUT_POINTS) + 1] = 0;
			}
			x = (this.pointsLineRaw[0] - this.rawBox.x) / this.rawBox.width;
			y = (this.pointsLineRaw[1] - this.rawBox.y) / this.rawBox.height;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcUp[i + 3 * (2 * CUT_POINTS)] = 0 + (x - 0) * i / (2 * CUT_POINTS);
				this.pointsSrcUp[i + 3 * (2 * CUT_POINTS) + 1] = 0 + (y - 0) * i / (2 * CUT_POINTS);
			}

			this.pointsSrcDown = new float[len];
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcDown[i] = (this.pointsLineRaw[i] - this.rawBox.x) / this.rawBox.width;
				this.pointsSrcDown[i + 1] = (this.pointsLineRaw[i + 1] - this.rawBox.y) / this.rawBox.height;
			}
			x = (pos.x - this.rawBox.x) / this.rawBox.width;
			y = (pos.y - this.rawBox.y) / this.rawBox.height;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcDown[i + (2 * CUT_POINTS)] = x + (1 - x) * i / (2 * CUT_POINTS);
				this.pointsSrcDown[i + (2 * CUT_POINTS) + 1] = y + (1 - y) * i / (2 * CUT_POINTS);
			}
			x = 1;
			y = 1;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcDown[i + 2 * (2 * CUT_POINTS)] = x + (0 - x) * i / (2 * CUT_POINTS);
				this.pointsSrcDown[i + 2 * (2 * CUT_POINTS) + 1] = 1;
			}
			x = (this.pointsLineRaw[0] - this.rawBox.x) / this.rawBox.width;
			y = (this.pointsLineRaw[1] - this.rawBox.y) / this.rawBox.height;
			for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
				this.pointsSrcDown[i + 3 * (2 * CUT_POINTS)] = 0 + (x - 0) * i / (2 * CUT_POINTS);
				this.pointsSrcDown[i + 3 * (2 * CUT_POINTS) + 1] = 1 + (y - 1) * i / (2 * CUT_POINTS);
			}

			this.pointsUp = new float[len];
			this.pointsDown = new float[len];
		}
		Pair<Float, Float> buf;
		for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
			buf = this.warpUp(this.pointsLineRaw[i], this.pointsLineRaw[i + 1]);
			this.pointsUp[i] = buf.getKey();
			this.pointsUp[i + 1] = buf.getValue();
			buf = this.warpDown(this.pointsLineRaw[i], this.pointsLineRaw[i + 1]);
			this.pointsDown[i] = buf.getKey();
			this.pointsDown[i + 1] = buf.getValue();
		}
		x = this.pointsLineRaw[2 * CUT_POINTS];
		y = this.pointsLineRaw[2 * CUT_POINTS + 1];
		for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
			buf = this.warpUp(x + (this.rawBox.x + this.rawBox.width - x) * i / (2 * CUT_POINTS),
					y + (this.rawBox.y - y) * i / (2 * CUT_POINTS));
			this.pointsUp[i + 2 * CUT_POINTS] = buf.getKey();
			this.pointsUp[i + 2 * CUT_POINTS + 1] = buf.getValue();
			buf = this.warpDown(x + (this.rawBox.x + this.rawBox.width - x) * i / (2 * CUT_POINTS),
					y + (this.rawBox.y + this.rawBox.height - y) * i / (2 * CUT_POINTS));
			this.pointsDown[i + 2 * CUT_POINTS] = buf.getKey();
			this.pointsDown[i + 2 * CUT_POINTS + 1] = buf.getValue();
		}
		x = this.rawBox.x + this.rawBox.width;
		y = this.rawBox.y;
		for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
			buf = this.warpUp(x + (this.rawBox.x - x) * i / (2 * CUT_POINTS),
					y + (this.rawBox.y - y) * i / (2 * CUT_POINTS));
			this.pointsUp[i + 4 * CUT_POINTS] = buf.getKey();
			this.pointsUp[i + 4 * CUT_POINTS + 1] = buf.getValue();
			buf = this.warpDown(x + (this.rawBox.x - x) * i / (2 * CUT_POINTS), y + this.rawBox.height
					+ (this.rawBox.y + this.rawBox.height - (y + this.rawBox.height)) * i / (2 * CUT_POINTS));
			this.pointsDown[i + 4 * CUT_POINTS] = buf.getKey();
			this.pointsDown[i + 4 * CUT_POINTS + 1] = buf.getValue();
		}
		x = this.pointsLineRaw[0];
		y = this.pointsLineRaw[1];
		for (int i = 0; i < 2 * CUT_POINTS; i = i + 2) {
			buf = this.warpUp(this.rawBox.x + (x - this.rawBox.x) * i / (2 * CUT_POINTS),
					this.rawBox.y + (y - this.rawBox.y) * i / (2 * CUT_POINTS));
			this.pointsUp[i + 6 * CUT_POINTS] = buf.getKey();
			this.pointsUp[i + 6 * CUT_POINTS + 1] = buf.getValue();
			buf = this.warpDown(this.rawBox.x + (x - this.rawBox.x) * i / (2 * CUT_POINTS), this.rawBox.y
					+ this.rawBox.height + (y - (this.rawBox.y + this.rawBox.height)) * i / (2 * CUT_POINTS));
			this.pointsDown[i + 6 * CUT_POINTS] = buf.getKey();
			this.pointsDown[i + 6 * CUT_POINTS + 1] = buf.getValue();
		}

		this.triangleVerticesUp = UtilFunctions.toIntArray(PolygonTriangulation.earcut(this.pointsUp));
		this.triangleVerticesDown = UtilFunctions.toIntArray(PolygonTriangulation.earcut(this.pointsDown));
	}

	private Pair<Float, Float> warpUp(float x, float y) {
		Vector out = this.warpUp.warpPoint(
				new Vector((x - this.rawBox.x) / this.rawBox.width, (y - this.rawBox.y) / this.rawBox.height),
				this.progress);
		return new Pair<Float, Float>(this.rawBox.x + out.x * this.rawBox.width,
				this.rawBox.y + out.y * this.rawBox.height);
	}

	private Pair<Float, Float> warpDown(float x, float y) {
		Vector out = this.warpDown.warpPoint(
				new Vector((x - this.rawBox.x) / this.rawBox.width, (y - this.rawBox.y) / this.rawBox.height),
				this.progress);
		return new Pair<Float, Float>(this.rawBox.x + out.x * this.rawBox.width,
				this.rawBox.y + out.y * this.rawBox.height);
	}

	public void setAutolooping(boolean autoLooping) {
		this.autoLooping = autoLooping;
	}

	@Override
	public Animation setFixedColor(Color color) {
		this.fixedColor = color;
		return this;
	}

	@Override
	public void start(Color color) {
		if (this.fixedColor != null) {
			color = this.fixedColor;
		}
		if (color != null) {
			this.color = color;
		}
		this.progress = 0;
		this.calcPoints();
	}

	@Override
	public boolean isRunning() {
		return this.progress <= 1;
	}

	@Override
	public void draw(Graphics g) {
		if (this.isRunning()) {
			AdvancedImage img = ResourceManager.getInstance().getImage(this.image);
			if (this.drawUpFirst) {
				img.drawAsTriangulatedPolygon(this.pointsUp, this.pointsSrcUp, this.triangleVerticesUp, this.color);
				img.drawAsTriangulatedPolygon(this.pointsDown, this.pointsSrcDown, this.triangleVerticesDown,
						this.color);
			} else {
				img.drawAsTriangulatedPolygon(this.pointsDown, this.pointsSrcDown, this.triangleVerticesDown,
						this.color);
				img.drawAsTriangulatedPolygon(this.pointsUp, this.pointsSrcUp, this.triangleVerticesUp, this.color);
			}
		}
	}

	@Override
	public void poll(Input input, float secounds) {
		if (this.isRunning()) {
			this.progress += secounds / this.lifetime;
			this.calcPoints();
		} else {
			if (this.autoLooping) {
				this.start(this.color);
			}
		}
	}

}
