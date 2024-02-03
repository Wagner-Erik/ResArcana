package resarcana.game.utils;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.core.Artifact;
import resarcana.math.Vector;

public class BoardPositioner {

	public static float DEFAULT_DISTANCE_COLUMN = Artifact.ARTIFACT_HITBOX.width * 1.1f;
	public static float DEFAULT_DISTANCE_ROW = Artifact.ARTIFACT_HITBOX.height * 1.1f;

	private ArrayList<Vector> positions;
	private boolean[] taken;

	private final Vector defaultPos;
	private final Vector distCol, distRow;

	public BoardPositioner(int rows, int cols, Vector topLeft, Vector defaultPos, float dWidth, float dHeight,
			int prioriyCols) {
		this.defaultPos = defaultPos;
		this.distCol = new Vector(dWidth, 0);
		this.distRow = new Vector(0, dHeight);
		this.positions = new ArrayList<Vector>();
		this.taken = new boolean[rows * cols];
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < prioriyCols; j++) {
				this.positions.add(topLeft.add(this.distCol.mul(j)).add(this.distRow.mul(i)));
				this.taken[i * cols + j] = false;
			}
		}
		for (int i = 0; i < rows; i++) {
			for (int j = prioriyCols; j < cols; j++) {
				this.positions.add(topLeft.add(this.distCol.mul(j)).add(this.distRow.mul(i)));
				this.taken[i * cols + j] = false;
			}
		}
	}

	public BoardPositioner(int rows, int cols, Vector topLeft, Vector defaultPos, float dWidth, float dHeight) {
		this(rows, cols, topLeft, defaultPos, dWidth, dHeight, cols);
	}

	public BoardPositioner(int rows, int cols, Vector topLeft, Vector defaultPos, int priorityCols) {
		this(rows, cols, topLeft, defaultPos, DEFAULT_DISTANCE_COLUMN, DEFAULT_DISTANCE_ROW, priorityCols);
	}

	public BoardPositioner(int rows, int cols, Vector topLeft, Vector defaultPos) {
		this(rows, cols, topLeft, defaultPos, cols);
	}

	public Vector getNextEmptyPosition() {
		return this.getNextEmptyPosition(0);
	}

	public Vector getNextEmptyPosition(int offset) {
		for (int i = offset; i < this.positions.size(); i++) {
			if (!this.taken[i]) {
				this.taken[i] = true;
				return this.positions.get(i);
			}
		}
		for (int i = offset; i < offset; i++) {
			if (!this.taken[i]) {
				this.taken[i] = true;
				return this.positions.get(i);
			}
		}
		return this.defaultPos;
	}

	public void freePosition(Vector pos) {
		if (!pos.equals(this.defaultPos)) {
			int idx = this.positions.indexOf(pos);
			if (idx != -1) {
				this.taken[idx] = false;
			} else {
				Log.warn("Freeing position " + pos + " which is neither in the position list nor the default position");
			}
		}
	}

	public void reset() {
		for (int i = 0; i < this.taken.length; i++) {
			this.taken[i] = false;
		}
	}
}