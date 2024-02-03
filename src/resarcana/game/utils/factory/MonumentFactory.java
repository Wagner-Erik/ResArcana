package resarcana.game.utils.factory;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.CardDrawer;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reanimator;
import resarcana.game.abilities.Reorder;
import resarcana.game.abilities.specials.DarkCathedral;
import resarcana.game.abilities.specials.GoldenStatue;
import resarcana.game.abilities.specials.Obelisk;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Monument;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class MonumentFactory {

	private static final int NUMBER_MONUMENTS = 14;
	private static int[] monuments_used = UtilFunctions.getRange(0, NUMBER_MONUMENTS);

	private static final Vector POSITION_ABILITY_MONUMENT = new Vector(0, 80);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Monuments");
		for (int i = 0; i < NUMBER_MONUMENTS; i++) {
			Scheduler.getInstance().scheduleResource("monument/monument_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setMonumentsUsed(int[] used) {
		monuments_used = used.clone();
	}

	public static ArrayList<Monument> createAll(Game parent) {
		ArrayList<Monument> out = new ArrayList<Monument>();
		Monument a;
		for (int i : monuments_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static Monument create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_MONUMENTS) {
			Log.warn("Requesting unknown Monument " + i + " in " + parent);
			return null;
		}
		Log.info("Creating Monument " + i + " in " + parent);
		Monument out = new Monument(parent, "monument/monument_" + (i < 10 ? "0" : "") + i + ".png");
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Colossus
			out.setName("Colossus");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MONUMENT, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(null, 1), new EssenceSelection(Essences.GOLD, 1)));
			out.setPoints(2);
			break;
		case 1: // Golden Statue
			out = new GoldenStatue(parent);
			break;
		case 2: // Great Pyramid
			out.setName("Great Pyramid");
			out.setPoints(3);
			break;
		case 3: // Hanging Gardens
			out.setName("Hanging Gardens");
			out.setIncome(new EssenceSelection(3, Essences.GOLD));
			out.setPoints(1);
			break;
		case 4: // Library
			out.setName("Library");
			abilities.add(new CardDrawer(out, POSITION_ABILITY_MONUMENT, new EssenceSelection()));
			out.setPoints(1);
			break;
		case 5: // Mausoleum
			out.setName("Mausoleum");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MONUMENT, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(null, 1), new EssenceSelection(Essences.DEATH, 1)));
			out.setPoints(2);
			break;
		case 6: // Obelisk
			out = new Obelisk(parent);
			break;
		case 7: // Oracle
			out.setName("Oracle");
			abilities.add(new Reorder(out, POSITION_ABILITY_MONUMENT));
			out.setPoints(2);
			break;
		case 8: // Solomon's Mine
			out.setName("Solomon's Mine");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MONUMENT, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.GOLD, 1)));
			out.setPoints(1);
			break;
		case 9: // Temple
			out.setName("Temple");
			out.setIncome(new EssenceSelection(Essences.LIFE, 1));
			abilities.add(new Protection(out, POSITION_ABILITY_MONUMENT, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			out.setPoints(2);
			break;
		case 10: // Alchemical Lab
			out.setName("Alchemical Lab");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MONUMENT, EssencePlacer.MODE_IN_PLAY, true,
					new EssenceSelection(null, 1), new EssenceSelection(), EssencePlacer.OUTPUT_AS_INPUT));
			out.setPoints(1);
			break;
		case 11: // Dark Cathedral
			out = new DarkCathedral(parent);
			break;
		case 12: // Demon Workshop
			out.setName("Demon Workshop");
			out.setIncome(new EssenceSelection(1, Essences.GOLD, Essences.LIFE, Essences.CALM));
			abilities.add(new Reanimator(out, POSITION_ABILITY_MONUMENT, new EssenceSelection(Essences.GOLD, 1)));
			out.setPoints(1);
			break;
		case 13: // Warrior's Hall
			out.setName("Warriors Hall");
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MONUMENT, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(), Essences.ELAN));
			out.setPoints(2);
			break;
		default:
			Log.info("Could not create Monument " + i);
			return null;
		}
		out.setRawCosts(new EssenceSelection(Essences.GOLD, 4));
		out.setAbilities(abilities);
		return out;
	}

}
