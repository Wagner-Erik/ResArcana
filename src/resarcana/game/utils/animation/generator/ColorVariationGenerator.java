package resarcana.game.utils.animation.generator;

import java.util.Random;

import org.newdawn.slick.Color;

public class ColorVariationGenerator implements ColorGenerator {

	public enum ParticleColorScheme {
		WHITE, YELLOW_ORANGE, LIGHT_BLUE, RED, GREEN, YELLOW, DARK_GRAY
	};

	private final ParticleColorScheme scheme;

	private final Random random;

	public ColorVariationGenerator(ParticleColorScheme scheme) {
		this.scheme = scheme;
		this.random = new Random();
	}

	@Override
	public Color getColor(float parameter, float offset) {
		float r = 1.f, g = 1.f, b = 1.f;
		switch (this.scheme) {
		case WHITE:
			break;
		case YELLOW_ORANGE:
			r = 1.f;
			g = 0.6f + 0.4f * (offset % 1.0f);
			b = 0.f;
			break;
		case LIGHT_BLUE:
			r = 0.f;
			g = 0.75f + 0.25f * (offset % 1.0f);
			b = 1.f;
			break;
		case DARK_GRAY:
			offset = offset % 1.0f;
			r = 0.3f * offset;
			g = 0.3f * offset;
			b = 0.3f * offset;
			break;
		case GREEN:
			r = Math.max(0, 0.4f * ((offset % 1.0f) - 0.5f));
			g = 1.f;
			b = Math.max(0, 0.4f * (0.5f - (offset % 1.0f)));
			break;
		case RED:
			r = 1.f;
			g = Math.max(0, 0.4f * ((offset % 1.0f) - 0.5f));
			b = Math.max(0, 0.4f * (0.5f - (offset % 1.0f)));	
			break;
		case YELLOW:
			r = 0.9f + Math.max(0, 0.1f * ((offset % 1.0f) - 0.5f));
			g = 0.9f + Math.max(0, 0.1f * (0.5f - (offset % 1.0f)));
			b = 0.f;
			break;
		default:
			break;
		}
		return new Color(r, g, b, parameter);
	}

	@Override
	public float nextOffset() {
		return this.random.nextFloat();
	}

}
