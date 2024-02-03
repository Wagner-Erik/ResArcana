package resarcana.game.utils.factory;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.ArtifactSacrificer;
import resarcana.game.abilities.CardDrawer;
import resarcana.game.abilities.EssenceConverter;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reanimator;
import resarcana.game.abilities.Retriever;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Scroll;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class ScrollFactory {

	private static final int NUMBER_SCROLLS = 8;
	private static int[] scrolls_used = UtilFunctions.getRange(0, NUMBER_SCROLLS);

	private static final Vector POSITION_ABILITY_SCROLL = new Vector(0, 25);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Scrolls");
		for (int i = 0; i < NUMBER_SCROLLS; i++) {
			Scheduler.getInstance().scheduleResource("scroll/scroll_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setScrollsUsed(int[] used) {
		scrolls_used = used.clone();
	}

	public static ArrayList<Scroll> createAll(Game parent) {
		ArrayList<Scroll> out = new ArrayList<Scroll>();
		Scroll a;
		for (int i : scrolls_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static Scroll create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_SCROLLS) {
			Log.warn("Requesting unknown Scroll " + i + " in " + parent);
			return null;
		}
		Log.info("Creating Scroll " + i + " in " + parent);
		Scroll out = new Scroll(parent, "scroll/scroll_" + (i < 10 ? "0" : "") + i + ".png");
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Transform
			out.setName("Transform");
			abilities.add(new EssenceConverter(out, POSITION_ABILITY_SCROLL, EssenceConverter.MODE_PRISM));
			break;
		case 1: // Vitality
			out.setName("Vitality");
			abilities.add(new Reanimator(out, POSITION_ABILITY_SCROLL, new EssenceSelection(Essences.ELAN, 2)));
			break;
		case 2: // Destruction
			out.setName("Destruction");
			abilities.add(new ArtifactSacrificer(out, POSITION_ABILITY_SCROLL, false, 0));
			break;
		case 3: // Revivify
			out.setName("Revivify");
			abilities.add(new Retriever(out, POSITION_ABILITY_SCROLL, new EssenceSelection(Essences.DEATH, 1)));
			break;
		case 4: // Augury
			out.setName("Augury");
			abilities.add(new CardDrawer(out, POSITION_ABILITY_SCROLL, new EssenceSelection(Essences.CALM, 1)));
			break;
		case 5: // Disjunction
			out.setName("Disjunction");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SCROLL, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.GOLD, 1),
					new EssenceSelection(Essences.DEATH, 1, Essences.CALM, 1, Essences.LIFE, 1, Essences.ELAN, 1)));
			break;
		case 6: // Projection
			out.setName("Projection");
			abilities.add(new EssenceConverter(out, POSITION_ABILITY_SCROLL, EssenceConverter.MODE_PROJECTION_3));
			break;
		case 7: // Shield
			out.setName("Shield");
			abilities.add(new Protection(out, POSITION_ABILITY_SCROLL, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			break;
		default:
			Log.info("Could not create Scroll " + i);
			return null;
		}
		out.setAbilities(abilities);
		return out;
	}

}
