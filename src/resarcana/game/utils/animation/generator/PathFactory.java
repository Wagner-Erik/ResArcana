package resarcana.game.utils.animation.generator;

import resarcana.math.Rectangle;
import resarcana.math.Vector;

public class PathFactory {

	public static PathGenerator getStaticPath(Vector position) {
		return getStaticPath(position, 1, 0);
	}

	public static PathGenerator getStaticPath(Vector position, float scale) {
		return getStaticPath(position, scale, 0);

	}

	public static PathGenerator getStaticPath(Vector position, float scale, int angle) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return scale;
			}

			@Override
			public Vector getPosition(float progress) {
				return position;
			}

			@Override
			public int getAngle(float progress) {
				return angle;
			}
		};
	}

	public static PathGenerator getScalingPath(Vector position, float initialScale, float finalScale) {
		float scaleDiff = finalScale - initialScale;
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return initialScale + progress * scaleDiff;
			}

			@Override
			public Vector getPosition(float progress) {
				return position;
			}

			@Override
			public int getAngle(float progress) {
				return 0;
			}
		};
	}

	public static PathGenerator getRotatingPath(Vector position, int startAngle, int endAngle) {
		int angleDiff = endAngle - startAngle;
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return 1;
			}

			@Override
			public Vector getPosition(float progress) {
				return position;
			}

			@Override
			public int getAngle(float progress) {
				return (int) (startAngle + progress * angleDiff);
			}
		};
	}

	public static PathGenerator getTranslatingPath(Vector p1, Vector p2, float paddingRatio) {
		Vector diff = p2.sub(p1);
		int angle = (int) (diff.clockWiseAng() * 180 / Math.PI);
		float factor = (1 + 2 * paddingRatio);
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return 1;
			}

			@Override
			public Vector getPosition(float progress) {
				return p1.add(diff.mul(factor * progress - paddingRatio));
			}

			@Override
			public int getAngle(float progress) {
				return angle;
			}
		};
	}

	public static PathGenerator getBalisitcPath(Vector start, Vector velocity, float velFactor) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return 1;
			}

			@Override
			public Vector getPosition(float progress) {
				return start.add(velocity.mul(progress * velFactor));
			}

			@Override
			public int getAngle(float progress) {
				return 0;
			}
		};
	}

	public static PathGenerator getArcPathBetweenPoints(Vector p1, Vector p2, float paddingRatio, float arcRatio) {
		Vector diff = p2.sub(p1);
		Vector norm = diff.rotateQuarterClockwise().mul(arcRatio);
		float baseAngle = (int) (diff.clockWiseAng() * 180 / Math.PI);
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return 1;
			}

			@Override
			public Vector getPosition(float progress) {
				float arc = progress * (1 - progress) * 4;
				return p1.add(p2.sub(p1).mul((1 + 2 * paddingRatio) * progress - paddingRatio)).add(norm.mul(arc));
			}

			@Override
			public int getAngle(float progress) {
				float angleArc = (float) (Math.atan((4 - 8 * progress) * arcRatio / (1 + 2 * paddingRatio)) * 180
						/ Math.PI);
				return (int) (baseAngle + angleArc);
			}
		};
	}

	public static PathGenerator getArcPathBetweenPoints(Vector p1, Vector p2, int startAngle) {
		Vector diff = p2.sub(p1);
		float baseAngle = (int) (diff.clockWiseAng() * 180 / Math.PI);
		return getArcPathBetweenPoints(p1, p2, 0, 0.25f * (float) Math.tan((startAngle - baseAngle) * Math.PI / 180));
	}

	public static PathGenerator getDropPath(Vector start, float startPart, float arcRatio, Vector dropStart,
			float dropLength, Vector dropEnd, Vector end) {
		PathGenerator one, two, three;
		one = getArcPathBetweenPoints(start, dropStart, 0, arcRatio);
		two = getArcPathBetweenPoints(dropStart, dropEnd, one.getAngle(1));
		three = getArcPathBetweenPoints(dropEnd, end, two.getAngle(1));
		return compositePath(one, startPart, compositePath(two, dropLength / (1 - startPart), three));
	}

	private static final float DEMON_X_RATE = 3;
	private static final float DEMON_Y_RATE = 4;
	private static final float DEMON_PHASE_SHIFT = 0;

	public static PathGenerator getDemonPath(Rectangle centerBox, float periods, float maxScale, float scalePeriodRatio,
			float maxAngle, float anglePeriodRatio) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				float p = (maxScale - 1) * (float) Math.sin(progress * Math.PI * 2 * scalePeriodRatio * periods);
				if (p > 0) {
					return 1 + p;
				} else if (p < 0) {
					return 1 / (1 - p);
				} else {
					return 1;
				}
			}

			@Override
			public Vector getPosition(float progress) {
				return centerBox.center.add(
						(float) (centerBox.width / 2 * Math.sin(Math.PI * 2 * DEMON_X_RATE * progress * periods)),
						(float) (centerBox.height / 2
								* Math.sin(Math.PI * 2 * DEMON_Y_RATE * progress * periods + DEMON_PHASE_SHIFT)));
			}

			@Override
			public int getAngle(float progress) {
				return (int) (maxAngle * Math.sin(progress * Math.PI * 2 * anglePeriodRatio * periods));
			}
		};
	}

	private static final int DANCING_SWORD_STEPS = 21;

	public static PathGenerator getDancingSwordPath(Vector center, float maxDist, boolean mirrored) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return 1;
			}

			@Override
			public Vector getPosition(float progress) {
				int n = (int) (progress * DANCING_SWORD_STEPS);
				progress = progress * DANCING_SWORD_STEPS - n;
				Vector start = Vector.ZERO, end = Vector.ZERO;
				switch (n) {
				case 0:
					if (progress < 0.1f) {
						progress *= 10;
						start = new Vector(-0.25f, 0.1f);
						end = Vector.ZERO;
					} else {
						progress -= 0.1;
						progress *= 10.0f / 9;
						start = Vector.ZERO;
						end = Vector.ZERO;
					}
					break;
				case 1:
					if (progress < 0.5f) {
						progress *= 2;
						start = Vector.ZERO;
						end = new Vector(-1, 0.5f);
					} else {
						progress -= 0.5f;
						progress *= 2;
						start = new Vector(-1, 0.5f);
						end = Vector.ZERO;
					}
					break;
				case 2:
					start = Vector.ZERO;
					end = new Vector(-1, 0.1f);
					break;
				case 3:
					if (progress < 0.5f) {
						progress *= 2;
						start = new Vector(-1, 0.1f);
						end = Vector.ZERO;
					} else {
						progress -= 0.5f;
						progress *= 2;
						start = Vector.ZERO;
						end = new Vector(-1, 0.2f);
					}
					break;
				case 4:
					start = new Vector(-1, 0.1f);
					end = new Vector(-1, 0.5f);
					break;
				case 5:
					start = new Vector(-1, 0.5f);
					end = Vector.ZERO;
					break;
				case 6:
					start = Vector.ZERO;
					end = new Vector(-1f, -1f);
					break;
				case 7:
					start = new Vector(-1f, -1f);
					end = Vector.ZERO;
					break;
				case 8:
					start = Vector.ZERO;
					end = new Vector(-1, 0.2f);
					break;
				case 9:
					start = new Vector(-1, 0.2f);
					end = Vector.ZERO;
					break;
				case 10:
					start = Vector.ZERO;
					end = new Vector(-1, 0.1f);
					break;
				case 11:
					if (progress < 0.5f) {
						progress *= 2;
						start = new Vector(-1, 0.1f);
						end = Vector.ZERO;
					} else {
						progress -= 0.5f;
						progress *= 2;
						start = Vector.ZERO;
						end = new Vector(-1, -0.5f);
					}
					break;
				case 12:
					start = new Vector(-1, -0.5f);
					end = Vector.ZERO;
					break;
				case 13:
					start = Vector.ZERO;
					end = new Vector(-0.5f, 0.75f);
					break;
				case 14:
					start = new Vector(-0.5f, 0.75f);
					end = new Vector(-0.8f, 0.65f);
					break;
				case 15:
					start = new Vector(-0.8f, 0.65f);
					end = new Vector(-0.65f, 0.65f);
					break;
				case 16:
					start = new Vector(-0.65f, 0.65f);
					end = new Vector(-0.5f, 0.75f);
					break;
				case 17:
					start = new Vector(-0.5f, 0.75f);
					end = Vector.ZERO;
					break;
				case 18:
					if (mirrored) {
						start = Vector.ZERO;
						end = new Vector(-1f, 0.25f);
					} else {
						start = Vector.ZERO;
						end = new Vector(-0.25f, 0.75f);
					}
					break;
				case 19:
					if (mirrored) {
						start = new Vector(-1f, 0.25f);
						end = new Vector(-1.25f, 0.f);
					} else {
						start = new Vector(-0.25f, 0.75f);
						end = new Vector(-0.25f, 1.5f);
					}
					break;
				case 20:
					if (mirrored) {
						start = new Vector(-1.25f, 0.f);
						end = new Vector(-1.25f, -1.5f);
					} else {
						start = new Vector(-0.25f, 1.5f);
						end = new Vector(-0.25f, 1.5f);
					}
					break;
				default:
					break;
				}
				Vector pos = start.add(end.sub(start).mul(progress)).add(-0.25f, 0).mul(maxDist);
				if (mirrored) {
					return center.add(-pos.x, pos.y);
				} else {
					// Log.debug(n + " - " + progress + " - " + start + " - " + end + " - " + pos);
					return center.add(pos);
				}
			}

			@Override
			public int getAngle(float progress) {
				float angle = 0;
				int n = (int) (progress * DANCING_SWORD_STEPS);
				progress = progress * DANCING_SWORD_STEPS - n;
				switch (n) {
				case 0:
					if (progress < 0.1f) {
						progress *= 10;
						angle = 0 + 20 * progress;
					} else {
						if (progress < 0.5f) {
							progress -= 0.1f;
							progress *= 2.5f;
							angle = 20 + 20 * progress;
						} else {
							progress -= 0.5f;
							progress *= 2;
							angle = 40 - 20 * progress;
						}
					}
					break;
				case 1:
					if (progress < 0.5f) {
						progress *= 2;
						angle = 20 - 20 * progress;
					} else {
						progress -= 0.5f;
						progress *= 2;
						angle = 0 + 10 * progress;
					}
					break;
				case 2:
					angle = 10 + 60 * progress;
					break;
				case 3:
					angle = 70 + 10 * progress;
					break;
				case 4:
					angle = 80 + 60 * progress;
					break;
				case 5:
					angle = 140 - 20 * progress;
					break;
				case 6:
					angle = 120 - 90 * progress;
					break;
				case 7:
					angle = 360 + 30 - 230 * progress;
					break;
				case 8:
					angle = 160 - 80 * progress;
					break;
				case 9:
					angle = 80 - 60 * progress;
					break;
				case 10:
					angle = 20 + 40 * progress;
					break;
				case 11:
					angle = 70 + 10 * progress;
					break;
				case 12:
					angle = 80 - 50 * progress;
					break;
				case 13:
					angle = 30;
					break;
				case 14:
					angle = 30;
					break;
				case 15:
					angle = 30;
					break;
				case 16:
					angle = 30;
					break;
				case 17:
					angle = 30 + 5 * progress;
					break;
				case 18:
					if (mirrored) {
						angle = 35 + 140 * progress;
					} else {
						angle = 35 + 120 * progress;
					}
					break;
				case 19:
					if (mirrored) {
						angle = 175 + 10 * progress;
					} else {
						angle = 155 - 40 * progress;
					}
					break;
				case 20:
					if (mirrored) {
						angle = 185 - 5 * progress;
					} else {
						angle = 115 - 25 * progress;
					}
					break;
				default:
					break;
				}
				if (mirrored) {
					return (int) -angle;
				} else {
					return (int) angle;
				}
			}
		};
	}

	public static PathGenerator scaleBy(PathGenerator base, float startScale, float endScale) {
		float scaleDiff = endScale - startScale;
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return base.getScale(progress) * (startScale + progress * scaleDiff);
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(progress);
			}

			@Override
			public int getAngle(float progress) {
				return base.getAngle(progress);
			}
		};
	}

	public static PathGenerator translateBy(PathGenerator base, Vector startTranslation, Vector endTranslation) {
		Vector diff = endTranslation.sub(startTranslation);
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return base.getScale(progress);
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(progress).add(startTranslation.add(diff.mul(progress)));
			}

			@Override
			public int getAngle(float progress) {
				return base.getAngle(progress);
			}
		};
	}

	public static PathGenerator rotateBy(PathGenerator base, int startAngle, int endAngle) {
		int angleDiff = endAngle - startAngle;
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return base.getScale(progress);
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(progress);
			}

			@Override
			public int getAngle(float progress) {
				return (int) (base.getAngle(progress) + startAngle + progress * angleDiff);
			}
		};
	}

	public static PathGenerator modifySpeedSmooth(PathGenerator base, float startSpeed, float endSpeed) {
		float a = -2 + startSpeed + endSpeed;
		float b = 3 - 2 * startSpeed - endSpeed;
		float c = startSpeed;
		return new PathGenerator() {

			private float adjustProgress(float progress) {
				return progress * (c + progress * (b + progress * a));
			}

			@Override
			public float getScale(float progress) {
				return base.getScale(this.adjustProgress(progress));
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(this.adjustProgress(progress));
			}

			@Override
			public int getAngle(float progress) {
				return base.getAngle(this.adjustProgress(progress));
			}
		};
	}

	public static PathGenerator addProgressPlateaus(PathGenerator base, float startPlateau, float endPlateau) {
		float slope = 1 / (1 - startPlateau - endPlateau);
		return new PathGenerator() {

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
			public float getScale(float progress) {
				return base.getScale(this.adjustProgress(progress));
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(this.adjustProgress(progress));
			}

			@Override
			public int getAngle(float progress) {
				return base.getAngle(this.adjustProgress(progress));
			}
		};
	}

	public static PathGenerator compositePath(PathGenerator scale, PathGenerator position, PathGenerator angle) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				return scale.getScale(progress);
			}

			@Override
			public Vector getPosition(float progress) {
				return position.getPosition(progress);
			}

			@Override
			public int getAngle(float progress) {
				return angle.getAngle(progress);
			}
		};
	}

	public static PathGenerator compositePath(PathGenerator first, float firstPart, PathGenerator second) {
		return new PathGenerator() {

			@Override
			public float getScale(float progress) {
				if (progress < firstPart) {
					return first.getScale(progress / firstPart);
				} else {
					return second.getScale((progress - firstPart) / (1 - firstPart));
				}
			}

			@Override
			public Vector getPosition(float progress) {
				if (progress < firstPart) {
					return first.getPosition(progress / firstPart);
				} else {
					return second.getPosition((progress - firstPart) / (1 - firstPart));
				}
			}

			@Override
			public int getAngle(float progress) {
				if (progress < firstPart) {
					return first.getAngle(progress / firstPart);
				} else {
					return second.getAngle((progress - firstPart) / (1 - firstPart));
				}
			}
		};
	}

	public static PathGenerator loopPath(PathGenerator base, int times, boolean backForth) {
		return new PathGenerator() {

			private float adjustProgress(float progress) {
				int n = (int) (times * progress);
				float p = progress * times - n;
				if (backForth && n % 2 == 1) {
					p = 1 - p;
				}
				return p;
			}

			@Override
			public float getScale(float progress) {
				return base.getScale(this.adjustProgress(progress));
			}

			@Override
			public Vector getPosition(float progress) {
				return base.getPosition(this.adjustProgress(progress));
			}

			@Override
			public int getAngle(float progress) {
				return base.getAngle(this.adjustProgress(progress));
			}
		};
	}
}
