package resarcana.game.utils.animation.generator;

import resarcana.math.Vector;

public class WarpFactory {

	private WarpFactory() {
	}

	public static ConvexWarpGenerator getStaticWarper() {
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return point;
			}

			@Override
			public Vector getStarCenter(float progress) {
				return new Vector(0.5f, 0.5f);
			}
		};
	}

	public static ConvexWarpGenerator getSlidingWarper(Vector totalSlide) {
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return point.add(totalSlide.mul(progress));
			}

			@Override
			public Vector getStarCenter(float progress) {
				return totalSlide.mul(progress).add(0.5f, 0.5f);
			}
		};
	}

	public static ConvexWarpGenerator getScalingWarp(float startScale, float endScale) {
		float scaleDiff = endScale - startScale;
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return point.sub(0.5f, 0.5f).mul(startScale + progress * scaleDiff).add(0.5f, 0.5f);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return new Vector(0.5f, 0.5f);
			}
		};
	}

	public static WarpGenerator getWedgeWarper(Vector p1, Vector p2, float maxOpeningP1, float maxOpeningP2,
			boolean down) {
		Vector diff = p2.sub(p1).getDirection();
		float diffAbs = p2.sub(p1).abs();
		Vector norm = diff.rotateQuarterAnticlockwise();
		return new WarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				Vector d = point.sub(p1);
				float sdiff = d.mul(diff);
				float snorm = d.mul(norm);
				return p1.add(diff.mul(sdiff)).add(norm.mul(snorm + progress
						* (maxOpeningP1 + (maxOpeningP2 - maxOpeningP1) * sdiff / diffAbs) / 2 * (down ? -1 : 1)));
			}
		};
	}

	public static WarpGenerator getDestroyWarpUp() {
		return new WarpGenerator() {
			@Override
			public Vector warpPoint(Vector point, float progress) {
				float xr = point.x;
				return point.add(1.0f / 4 * progress * (1 - xr), -1.0f / 4 * progress * ((1 - xr) * (1 - xr) + 0.25f));
			}
		};
	}

	public static ConvexWarpGenerator getCircleWarp(float scaleStart, float scaleEnd) {
		float scaleDiff = scaleEnd - scaleStart;
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				point = point.sub(0.5f, 0.5f);
				return point.getDirection()
						.mul(Math.max(Math.abs(point.x), Math.abs(point.y)) * (scaleStart + progress * scaleDiff))
						.add(0.5f, 0.5f);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return new Vector(0.5f, 0.5f);
			}
		};
	}

	public static StarWarpGenerator getQuadFlowerWarp(float scaleStart, float scaleEnd) {
		float scaleDiff = scaleEnd - scaleStart;
		return new StarWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				point = point.sub(0.5f, 0.5f);
				return point.getDirection()
						.mul((Math.abs(point.x) + Math.abs(point.y)) * (scaleStart + progress * scaleDiff))
						.add(0.5f, 0.5f);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return new Vector(0.5f, 0.5f);
			}
		};
	}

	public static ConvexWarpGenerator getMeltingWarp(float endTopScale, float endBottomScale, float endYscale) {
		float xtopscale = endTopScale - 1, yscale = endYscale - 1;
		float xscaleDiff = endBottomScale - endTopScale;
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				float x = (point.x - 0.5f) * (1 + progress * (xtopscale + point.y * xscaleDiff)) + 0.5f;
				float y = 1 - (1 - point.y) * (1 + progress * yscale);
				return new Vector(x, y);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return this.warpPoint(new Vector(0.5f, 0.5f), progress);
			}
		};
	}

	public static ConvexWarpGenerator getRotatingWarp(Vector rotationCenter, int startAngle, int endAngle) {
		float angle = (float) (Math.PI / 180 * startAngle);
		float diff = (float) (Math.PI / 180 * (endAngle - startAngle));
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				point = point.sub(rotationCenter);
				return point.rotate(angle + diff * progress).add(rotationCenter);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return rotationCenter;
			}
		};
	}

	public static StarWarpGenerator modifySpeedSmooth(StarWarpGenerator base, float startSpeed, float endSpeed) {
		float a = -2 + startSpeed + endSpeed;
		float b = 3 - 2 * startSpeed - endSpeed;
		float c = startSpeed;
		return new StarWarpGenerator() {

			private float adjustProgress(float progress) {
				return progress * (c + progress * (b + progress * a));
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, this.adjustProgress(progress));
			}

			@Override
			public Vector getStarCenter(float progress) {
				return base.getStarCenter(this.adjustProgress(progress));
			}
		};
	}

	public static ConvexWarpGenerator modifySpeedSmooth(ConvexWarpGenerator base, float startSpeed, float endSpeed) {
		float a = -2 + startSpeed + endSpeed;
		float b = 3 - 2 * startSpeed - endSpeed;
		float c = startSpeed;
		return new ConvexWarpGenerator() {

			private float adjustProgress(float progress) {
				return progress * (c + progress * (b + progress * a));
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, this.adjustProgress(progress));
			}

			@Override
			public Vector getStarCenter(float progress) {
				return base.getStarCenter(this.adjustProgress(progress));
			}
		};
	}

	public static WarpGenerator modifySpeedSmooth(WarpGenerator base, float startSpeed, float endSpeed) {
		float a = -2 + startSpeed + endSpeed;
		float b = 3 - 2 * startSpeed - endSpeed;
		float c = startSpeed;
		return new WarpGenerator() {

			private float adjustProgress(float progress) {
				return progress * (c + progress * (b + progress * a));
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, this.adjustProgress(progress));
			}
		};
	}

	public static StarWarpGenerator setFixedProgress(StarWarpGenerator base, float fixedProgress) {
		return new StarWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, fixedProgress);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return base.getStarCenter(fixedProgress);
			}
		};
	}

	public static ConvexWarpGenerator setFixedProgress(ConvexWarpGenerator base, float fixedProgress) {
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, fixedProgress);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return base.getStarCenter(fixedProgress);
			}
		};
	}

	public static WarpGenerator setFixedProgress(WarpGenerator base, float fixedProgress) {
		return new WarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, fixedProgress);
			}
		};
	}

	public static WarpGenerator addProgressPlateaus(WarpGenerator base, float startPlateau, float endPlateau) {
		float slope = 1 / (1 - startPlateau - endPlateau);
		return new WarpGenerator() {

			private float adjustProgress(float progress) {
				if (progress < startPlateau) {
					return 0;
				} else {
					progress = (progress - startPlateau) * slope;
				}
				if (progress > 1) {
					return 1;
				} else {
					return progress;
				}
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return base.warpPoint(point, this.adjustProgress(progress));
			}
		};
	}

	public static WarpGenerator compositeWarp(WarpGenerator firstWarp, WarpGenerator secondWarp) {
		return new WarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return secondWarp.warpPoint(firstWarp.warpPoint(point, progress), progress);
			}
		};
	}

	public static StarWarpGenerator compositeWarp(StarWarpGenerator firstWarp, StarWarpGenerator secondWarp) {
		return new StarWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return secondWarp.warpPoint(firstWarp.warpPoint(point, progress), progress);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return secondWarp.getStarCenter(progress);
			}
		};
	}

	public static ConvexWarpGenerator compositeWarp(ConvexWarpGenerator firstWarp, ConvexWarpGenerator secondWarp) {
		return new ConvexWarpGenerator() {

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return secondWarp.warpPoint(firstWarp.warpPoint(point, progress), progress);
			}

			@Override
			public Vector getStarCenter(float progress) {
				return secondWarp.getStarCenter(progress);
			}
		};
	}

	public static WarpGenerator loopWarp(WarpGenerator warp, int times, boolean backForth) {
		return new WarpGenerator() {

			private float adjustProgress(float progress) {
				int n = (int) (times * progress);
				float p = progress * times - n;
				if (backForth && n % 2 == 1) {
					p = 1 - p;
				}
				return p;
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return warp.warpPoint(point, this.adjustProgress(progress));
			}
		};
	}

	public static StarWarpGenerator loopWarp(StarWarpGenerator warp, int times, boolean backForth) {
		return new StarWarpGenerator() {

			private float adjustProgress(float progress) {
				int n = (int) (times * progress);
				float p = progress * times - n;
				if (backForth && n % 2 == 1) {
					p = 1 - p;
				}
				return p;
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return warp.warpPoint(point, this.adjustProgress(progress));
			}

			@Override
			public Vector getStarCenter(float progress) {
				return warp.getStarCenter(this.adjustProgress(progress));
			}
		};
	}

	public static ConvexWarpGenerator loopWarp(ConvexWarpGenerator warp, int times, boolean backForth) {
		return new ConvexWarpGenerator() {

			private float adjustProgress(float progress) {
				int n = (int) (times * progress);
				float p = progress * times - n;
				if (backForth && n % 2 == 1) {
					p = 1 - p;
				}
				return p;
			}

			@Override
			public Vector warpPoint(Vector point, float progress) {
				return warp.warpPoint(point, this.adjustProgress(progress));
			}

			@Override
			public Vector getStarCenter(float progress) {
				return warp.getStarCenter(this.adjustProgress(progress));
			}
		};
	}
}
