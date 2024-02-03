package resarcana.graphics;

import org.newdawn.slick.Color;
import org.newdawn.slick.Image;
import org.newdawn.slick.opengl.renderer.SGL;

import resarcana.game.utils.animation.generator.StarWarpGenerator;
import resarcana.game.utils.animation.generator.WarpGenerator;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Distributor;
import resarcana.utils.PolygonTriangulation;
import resarcana.utils.UtilFunctions;

public class AdvancedImage extends Image {

	public AdvancedImage(Image other) {
		super(other);
	}

	public void drawWarped(float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4, float srcx1,
			float srcy1, float srcx2, float srcy2, float srcx3, float srcy3, float srcx4, float srcy4, Color filter) {
		init();
		if (filter != null) {
			filter.bind();
		} else {
			Color.white.bind();
		}
		texture.bind();

		float texx1 = (((srcx1) / (width)) * textureWidth) + textureOffsetX;
		float texy1 = (((srcy1) / (height)) * textureHeight) + textureOffsetY;
		float texx2 = (((srcx2) / (width)) * textureWidth) + textureOffsetX;
		float texy2 = (((srcy2) / (height)) * textureHeight) + textureOffsetY;
		float texx3 = (((srcx3) / (width)) * textureWidth) + textureOffsetX;
		float texy3 = (((srcy3) / (height)) * textureHeight) + textureOffsetY;
		float texx4 = (((srcx4) / (width)) * textureWidth) + textureOffsetX;
		float texy4 = (((srcy4) / (height)) * textureHeight) + textureOffsetY;

		GL.glTranslatef(x1, y1, 0);
		if (angle != 0) {
			GL.glTranslatef(centerX, centerY, 0.0f);
			GL.glRotatef(angle, 0.0f, 0.0f, 1.0f);
			GL.glTranslatef(-centerX, -centerY, 0.0f);
		}

		GL.glBegin(SGL.GL_QUADS);

		GL.glTexCoord2f(texx1, texy1);
		GL.glVertex3f(0, 0, 0);
		GL.glTexCoord2f(texx2, texy2);
		GL.glVertex3f(x2 - x1, y2 - y1, 0);
		GL.glTexCoord2f(texx3, texy3);
		GL.glVertex3f(x3 - x1, y3 - y1, 0);
		GL.glTexCoord2f(texx4, texy4);
		GL.glVertex3f(x4 - x1, y4 - y1, 0);
		GL.glEnd();

		if (angle != 0) {
			GL.glTranslatef(centerX, centerY, 0.0f);
			GL.glRotatef(-angle, 0.0f, 0.0f, 1.0f);
			GL.glTranslatef(-centerX, -centerY, 0.0f);
		}
		GL.glTranslatef(-x1, -y1, 0);
	}

	public void drawWarped(Rectangle unwarpedBox, WarpGenerator warper, float progress, int numPointsPerSide,
			Color filter) {
		float src[] = new float[numPointsPerSide * 4 * 2];
		for (int i = 0; i < 2 * numPointsPerSide; i = i + 2) {
			src[i] = 0 + i * 1.f / numPointsPerSide / 2;
			src[i + 1] = 0;
			src[i + 2 * numPointsPerSide] = 1;
			src[i + 2 * numPointsPerSide + 1] = 0 + i * 1.f / numPointsPerSide / 2;
			src[i + 4 * numPointsPerSide] = 1 - i * 1.f / numPointsPerSide / 2;
			src[i + 4 * numPointsPerSide + 1] = 1;
			src[i + 6 * numPointsPerSide] = 0;
			src[i + 6 * numPointsPerSide + 1] = 1 - i * 1.f / numPointsPerSide / 2;
		}
		float warp[] = new float[src.length];
		Vector vec;
		for (int i = 0; i < src.length; i = i + 2) {
			vec = warper.warpPoint(new Vector(src[i], src[i + 1]), progress);
			warp[i] = unwarpedBox.x + vec.x * unwarpedBox.width;
			warp[i + 1] = unwarpedBox.y + vec.y * unwarpedBox.height;
		}

		if (warper instanceof StarWarpGenerator) {
			Vector starC = ((StarWarpGenerator) warper).getStarCenter(progress);
			this.drawStar(unwarpedBox.x + unwarpedBox.width * starC.x, unwarpedBox.y + unwarpedBox.height * starC.y,
					starC.x, starC.y, warp, src, filter);
		} else {
			this.drawAsTriangulatedPolygon(warp, src, UtilFunctions.toIntArray(PolygonTriangulation.earcut(warp)),
					filter);
		}
	}

	public void drawStar(float cx, float cy, float srcRelcx, float srcRelcy, float[] polygon, float[] relSrcPolygon,
			Color filter) {
		int len = polygon.length;
		if (relSrcPolygon.length == len && len > 2) { // only draw if correct point
														// lists are given
			init();
			if (filter != null) {
				filter.bind();
			} else {
				Color.white.bind();
			}
			texture.bind();

			GL.glBegin(SGL.GL_TRIANGLE_FAN);

			GL.glTexCoord2f(srcRelcx * textureWidth + textureOffsetX, srcRelcy * textureHeight + textureOffsetY);
			GL.glVertex3f(cx, cy, 0);

			for (int i = 0; i < len; i = i + 2) {
				GL.glTexCoord2f(relSrcPolygon[i] * textureWidth + textureOffsetX,
						relSrcPolygon[i + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[i], polygon[i + 1], 0);
			}

			// the 1st polygon point closes the shape
			GL.glTexCoord2f(relSrcPolygon[0] * textureWidth + textureOffsetX,
					relSrcPolygon[1] * textureHeight + textureOffsetY);
			GL.glVertex3f(polygon[0], polygon[1], 0);

			GL.glEnd();
		}
	}

	public void drawStarQuads(float cx, float cy, float srcRelcx, float srcRelcy, float[] polygon,
			float[] relSrcPolygon, Color filter) {
		int len = polygon.length;
		if (relSrcPolygon.length == len && (len % 4 == 0 || len % 4 == 2) && len > 0) { // only draw if correct point
																						// lists are given
			init();
			if (filter != null) {
				filter.bind();
			} else {
				Color.white.bind();
			}
			texture.bind();

			GL.glBegin(SGL.GL_QUADS);

			float srcSCx = srcRelcx * textureWidth + textureOffsetX;
			float srcSCy = srcRelcy * textureHeight + textureOffsetY;

			// last "i" used will point to the x-coordinate of the 3rd or 4th last polygon
			// point
			for (int i = 0; i < len - 5; i = i + 4) {
				GL.glTexCoord2f(srcSCx, srcSCy);
				GL.glVertex3f(cx, cy, 0);

				GL.glTexCoord2f(relSrcPolygon[i] * textureWidth + textureOffsetX,
						relSrcPolygon[i + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[i], polygon[i + 1], 0);

				GL.glTexCoord2f(relSrcPolygon[i + 2] * textureWidth + textureOffsetX,
						relSrcPolygon[i + 3] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[i + 2], polygon[i + 3], 0);

				GL.glTexCoord2f(relSrcPolygon[i + 4] * textureWidth + textureOffsetX,
						relSrcPolygon[i + 5] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[i + 4], polygon[i + 5], 0);
			}
			// Last quad
			GL.glTexCoord2f(srcSCx, srcSCy);
			GL.glVertex3f(cx, cy, 0);

			if (len % 4 == 0) {
				// "len - 4" points to the x-coordinate of the 2nd last polygon point
				GL.glTexCoord2f(relSrcPolygon[len - 4] * textureWidth + textureOffsetX,
						relSrcPolygon[len - 3] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[len - 4], polygon[len - 3], 0);

				GL.glTexCoord2f(relSrcPolygon[len - 2] * textureWidth + textureOffsetX,
						relSrcPolygon[len - 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[len - 2], polygon[len - 1], 0);
			} else { // len % 4 == 2
				// "len - 2" points to the x-coordinate of the last polygon point
				GL.glTexCoord2f(relSrcPolygon[len - 2] * textureWidth + textureOffsetX,
						relSrcPolygon[len - 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[len - 2], polygon[len - 1], 0);

				// auxilliary point in the middle of the last polygon line
				GL.glTexCoord2f((relSrcPolygon[0] + relSrcPolygon[len - 2]) / 2 * textureWidth + textureOffsetX,
						(relSrcPolygon[1] + relSrcPolygon[len - 1]) / 2 * textureHeight + textureOffsetY);
				GL.glVertex3f((polygon[0] + polygon[len - 2]) / 2, (polygon[1] + polygon[len - 1]) / 2, 0);
			}

			// the 1st polygon point closes the shape
			GL.glTexCoord2f(relSrcPolygon[0] * textureWidth + textureOffsetX,
					relSrcPolygon[1] * textureHeight + textureOffsetY);
			GL.glVertex3f(polygon[0], polygon[1], 0);

			GL.glEnd();
		}
	}

	public void drawAsTriangulatedPolygon(float[] polygon, float[] relSrcPolygon, int[] triangleVertices,
			Color filter) {
		int len = triangleVertices.length;
		if (len % 3 == 0 && len > 0) { // only draw if correct point lists are given
			init();
			if (filter != null) {
				filter.bind();
			} else {
				Color.white.bind();
			}
			texture.bind();

			GL.glBegin(SGL.GL_TRIANGLES);

			for (int i = 0; i < len; i = i + 3) {
				GL.glTexCoord2f(relSrcPolygon[2 * triangleVertices[i]] * textureWidth + textureOffsetX,
						relSrcPolygon[2 * triangleVertices[i] + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[2 * triangleVertices[i]], polygon[2 * triangleVertices[i] + 1], 0);

				GL.glTexCoord2f(relSrcPolygon[2 * triangleVertices[i + 1]] * textureWidth + textureOffsetX,
						relSrcPolygon[2 * triangleVertices[i + 1] + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[2 * triangleVertices[i + 1]], polygon[2 * triangleVertices[i + 1] + 1], 0);

				GL.glTexCoord2f(relSrcPolygon[2 * triangleVertices[i + 2]] * textureWidth + textureOffsetX,
						relSrcPolygon[2 * triangleVertices[i + 2] + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(polygon[2 * triangleVertices[i + 2]], polygon[2 * triangleVertices[i + 2] + 1], 0);
			}
			GL.glEnd();
		}
	}

	public void drawMultiQuads(Rectangle box, int rows, int cols, WarpGenerator globalWarp,
			Distributor<WarpGenerator> generators, float progress, Color filter) {
		float colDiv = 1.0f / cols, rowDiv = 1.0f / rows;
		float[] srcQuads = new float[2 * 4 * rows * cols];
		int idx = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				idx = 8 * (i * cols + j);
				srcQuads[idx] = j * colDiv;
				srcQuads[idx + 1] = i * rowDiv;
				srcQuads[idx + 2] = (j + 1) * colDiv;
				srcQuads[idx + 3] = i * rowDiv;
				srcQuads[idx + 4] = (j + 1) * colDiv;
				srcQuads[idx + 5] = (i + 1) * rowDiv;
				srcQuads[idx + 6] = j * colDiv;
				srcQuads[idx + 7] = (i + 1) * rowDiv;
			}
		}
		float[] quads = new float[2 * 4 * rows * cols];
		Vector pos, topLeft, relDownRight = new Vector(1, 1);
		WarpGenerator warper;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				idx = 8 * (i * cols + j);
				warper = generators.getObject(i * cols + j);
				topLeft = globalWarp.warpPoint(new Vector((j + 0.5f) * colDiv, (i + 0.5f) * rowDiv), progress)
						.sub(0.5f * colDiv, 0.5f * colDiv);
				pos = warper.warpPoint(Vector.ZERO, progress).mulAsLists(colDiv, rowDiv).add(topLeft);
				quads[idx] = box.x + box.width * pos.x;
				quads[idx + 1] = box.y + box.height * pos.y;
				pos = warper.warpPoint(Vector.RIGHT, progress).mulAsLists(colDiv, rowDiv).add(topLeft);
				quads[idx + 2] = box.x + box.width * pos.x;
				quads[idx + 3] = box.y + box.height * pos.y;
				pos = warper.warpPoint(relDownRight, progress).mulAsLists(colDiv, rowDiv).add(topLeft);
				quads[idx + 4] = box.x + box.width * pos.x;
				quads[idx + 5] = box.y + box.height * pos.y;
				pos = warper.warpPoint(Vector.DOWN, progress).mulAsLists(colDiv, rowDiv).add(topLeft);
				quads[idx + 6] = box.x + box.width * pos.x;
				quads[idx + 7] = box.y + box.height * pos.y;
			}
		}
		this.drawMultiQuads(quads, srcQuads, filter);
	}

	public void drawMultiQuads(float[] quads, float[] srcQuads, Color filter) {
		int len = quads.length;
		if (srcQuads.length == len && len % 4 == 0 && len > 0) { // only draw if correct point
																	// lists are given
			init();
			if (filter != null) {
				filter.bind();
			} else {
				Color.white.bind();
			}
			texture.bind();

			GL.glBegin(SGL.GL_QUADS);

			// last "i" used will point to the x-coordinate of the 3rd or 4th last polygon
			// point
			for (int i = 0; i < len - 5; i = i + 8) {
				GL.glTexCoord2f(srcQuads[i] * textureWidth + textureOffsetX,
						srcQuads[i + 1] * textureHeight + textureOffsetY);
				GL.glVertex3f(quads[i], quads[i + 1], 0);

				GL.glTexCoord2f(srcQuads[i + 2] * textureWidth + textureOffsetX,
						srcQuads[i + 3] * textureHeight + textureOffsetY);
				GL.glVertex3f(quads[i + 2], quads[i + 3], 0);

				GL.glTexCoord2f(srcQuads[i + 4] * textureWidth + textureOffsetX,
						srcQuads[i + 5] * textureHeight + textureOffsetY);
				GL.glVertex3f(quads[i + 4], quads[i + 5], 0);

				GL.glTexCoord2f(srcQuads[i + 6] * textureWidth + textureOffsetX,
						srcQuads[i + 7] * textureHeight + textureOffsetY);
				GL.glVertex3f(quads[i + 6], quads[i + 7], 0);
			}
			GL.glEnd();
		}
	}
}
