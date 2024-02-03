package resarcana.game.utils.animation.generator;

import java.util.Random;

import org.newdawn.slick.Color;

public class CycleColorGenerator implements ColorGenerator {

	private final Random random;

	public CycleColorGenerator() {
		this.random = new Random();
	}

	@Override
	public Color getColor(float parameter, float offset) {
		float r = 1.f, g = 1.f, b = 1.f;
		switch ((int) offset) {
		case 0:
			r = offset % 1.0f;
			g = 1;
			b = 0;
			break;
		case 1:
			r = 1;
			g = offset % 1.0f;
			b = 0;
			break;
		case 2:
			r = 1;
			g = 0;
			b = offset % 1.0f;
			break;
		case 3:
			r = offset % 1.0f;
			g = 0;
			b = 1;
			break;
		case 4:
			r = 0;
			g = offset % 1.0f;
			b = 1;
			break;
		case 5:
			r = 0;
			g = 1;
			b = offset % 1.0f;
			break;
		default:
			break;
		}
		return new Color(r, g, b, parameter);
	}

	@Override
	public float nextOffset() {
		return this.random.nextInt(6) + this.random.nextFloat();
	}

}
