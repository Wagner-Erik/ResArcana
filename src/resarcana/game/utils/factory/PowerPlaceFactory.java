package resarcana.game.utils.factory;

import java.util.ArrayList;
import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.ArtifactSacrificer;
import resarcana.game.abilities.CostReduction;
import resarcana.game.abilities.CreatureTapper;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reviver;
import resarcana.game.abilities.VictoryChecker;
import resarcana.game.abilities.specials.CreatureReanimator;
import resarcana.game.abilities.specials.CreatureSacrificer;
import resarcana.game.abilities.specials.CrystalKeep;
import resarcana.game.abilities.specials.CursedForge;
import resarcana.game.abilities.specials.GateOfHell;
import resarcana.game.abilities.specials.SorcerersBestiary;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.PowerPlace;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class PowerPlaceFactory {

	private static final int NUMBER_PLACES = 14;
	private static int[] places_used = UtilFunctions.getRange(0, NUMBER_PLACES);

	public static final int[] OTHER_SIDES = new int[] { /* 0 */6, /* 1 */7, /* 2 */9, /* 3 */5, /* 4 */8, /* 5 */3,
			/* 6 */0, /* 7 */1, /* 8 */4, /* 9 */2, /* 10 */13, /* 11 */12, /* 12 */11, /* 13 */10 };

	private static final Vector POSITION_ABILITY_TOP = new Vector(0, 20);
	private static final Vector POSITION_ABILITY_MIDDLE = new Vector(0, 55);
	private static final Vector POSITION_ABILITY_BOTTOM = new Vector(0, 90);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Places of Power");
		for (int i = 0; i < NUMBER_PLACES; i++) {
			Scheduler.getInstance().scheduleResource("place/place_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setPlacesUsed(int[] used) {
		places_used = used.clone();
	}

	public static ArrayList<PowerPlace> createAll(Game parent) {
		ArrayList<PowerPlace> out = new ArrayList<PowerPlace>();
		PowerPlace a;
		for (int i : places_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static PowerPlace create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_PLACES) {
			Log.warn("Requesting unknown PowerPlace " + i + " in " + parent);
			return null;
		}
		Log.info("Creating PowerPlace " + i + " in " + parent);
		PowerPlace out = new PowerPlace(parent, "place/place_" + (i < 10 ? "0" : "") + i + ".png", i);
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Alchemist's Tower
			out.setName("Alchemist's Tower");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 3));
			out.setIncome(new EssenceSelection(3, Essences.GOLD));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.DEATH, 1, Essences.ELAN, 1, Essences.CALM, 1, Essences.LIFE, 1),
					new EssenceSelection(Essences.GOLD, 1)));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.GOLD, 1);
			break;
		case 1: // Catacombs of the Dead
			out.setName("Catacombs of the Dead");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 9));
			out.setIncome(new EssenceSelection(Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.DEATH, 5), new EssenceSelection(Essences.DEATH, 1)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(), new EssenceSelection(Essences.DEATH, 1)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.DEATH, 1);
			break;
		case 2: // Coral Castle
			out.setName("Coral Castle");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 5, Essences.LIFE, 5, Essences.CALM, 5));
			abilities.add(new VictoryChecker(out, POSITION_ABILITY_MIDDLE));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			out.setPoints(3);
			break;
		case 3: // Cursed Forge
			out = new CursedForge(parent);
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.ELAN, 2, Essences.GOLD, 1), new EssenceSelection(Essences.GOLD, 1)));
			break;
		case 4: // Dragon's Lair
			out.setName("Dragon's Lair");
			out.setRawCosts(
					new EssenceSelection(Essences.ELAN, 3, Essences.LIFE, 3, Essences.CALM, 3, Essences.DEATH, 3));
			abilities.add(new CostReduction(out, POSITION_ABILITY_TOP, CostReduction.MODE_DRAGON, 3,
					EnumSet.noneOf(Essences.class)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.GOLD, 2)));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_BOTTOM, CreatureTapper.TAP_MODE_DRAGON, true,
					CreatureTapper.OUTPUT_MODE_SELF, new EssenceSelection(Essences.GOLD, 2)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.GOLD, 1);
			break;
		case 5: // Dwarven Mines
			out.setName("Dwarven Mines");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 4, Essences.LIFE, 2, Essences.GOLD, 1));
			out.setIncome(new EssenceSelection(Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.ELAN, 5), new EssenceSelection(Essences.GOLD, 3)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.DEATH, 3, Essences.ELAN, 3), new EssenceSelection(Essences.GOLD, 2)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.GOLD, 1);
			break;
		case 6: // Sacred Grove
			out.setName("Sacred Grove");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 8, Essences.CALM, 4));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.CALM, 1), new EssenceSelection(Essences.LIFE, 5)));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_BOTTOM, CreatureTapper.TAP_MODE_BEAST, true,
					CreatureTapper.OUTPUT_MODE_SELF, new EssenceSelection(Essences.LIFE, 1)));
			out.setPoints(2);
			out.setPointsPerEssence(Essences.LIFE, 1);
			break;
		case 7: // Sacrifical Pit
			out.setName("Sacrifical Pit");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 8, Essences.DEATH, 4));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.LIFE, 3), new EssenceSelection(Essences.DEATH, 1)));
			abilities.add(new CreatureSacrificer(out, POSITION_ABILITY_BOTTOM, ArtifactSacrificer.MODE_PLAYER,
					CreatureSacrificer.MODE_BEAST_DRAGON, 0, new EssenceSelection(Essences.DEATH, 1), Essences.GOLD));
			out.setPoints(2);
			out.setPointsPerEssence(Essences.DEATH, 1);
			break;
		case 8: // Sorcerers Bestiary
			out = new SorcerersBestiary(parent);
			abilities.add(new VictoryChecker(out, POSITION_ABILITY_MIDDLE));
			abilities.add(new Reviver(out, POSITION_ABILITY_BOTTOM, Reviver.MODE_DRAGON_REVIVE_ALL, null, 4, 0,
					EnumSet.noneOf(Essences.class)));
			break;
		case 9: // Sunken Reef
			out.setName("Sunken Reef");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 5, Essences.ELAN, 2, Essences.LIFE, 2));
			out.setIncome(new EssenceSelection(Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.CALM, 2, Essences.LIFE, 1), new EssenceSelection(Essences.CALM, 1)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.CALM, 1);
			break;
		case 10: // Dragon Aerie
			out.setName("Dragon Aerie");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 8, Essences.LIFE, 4));
			out.setIncome(new EssenceSelection(Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.LIFE, 4), new EssenceSelection(Essences.LIFE, 1)));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_BOTTOM, CreatureTapper.TAP_MODE_DRAGON, false,
					CreatureTapper.OUTPUT_MODE_SELF, new EssenceSelection(Essences.LIFE, 1)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.LIFE, 1);
			break;
		case 11: // Gate of Hell
			out = new GateOfHell(parent);
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_TOP, CreatureTapper.TAP_MODE_DEMON, true,
					CreatureTapper.OUTPUT_MODE_SELF, new EssenceSelection(Essences.DEATH, 1)));
			abilities.add(new CreatureSacrificer(out, POSITION_ABILITY_MIDDLE, ArtifactSacrificer.MODE_SELF,
					CreatureSacrificer.MODE_BEAST, 0, new EssenceSelection(), Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.ELAN, 4), new EssenceSelection(Essences.DEATH, 1)));
			break;
		case 12: // Temple of the Abyss
			out.setName("Temple of the Abyss");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 6, Essences.DEATH, 3));
			abilities.add(new CreatureReanimator(out, POSITION_ABILITY_TOP, CreatureReanimator.MODE_DEMON,
					new EssenceSelection(Essences.LIFE, 2)).makeReanimateAll());
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MIDDLE, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.CALM, 2, Essences.DEATH, 2), new EssenceSelection(Essences.CALM, 1)));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_BOTTOM, CreatureTapper.TAP_MODE_DEMON, false,
					CreatureTapper.OUTPUT_MODE_SELF, new EssenceSelection(Essences.CALM, 1)));
			out.setPoints(0);
			out.setPointsPerEssence(Essences.CALM, 1);
			break;
		case 13: // Crystal Keep
			out = new CrystalKeep(parent);
			abilities.add(new VictoryChecker(out, POSITION_ABILITY_BOTTOM));
			break;
		default:
			Log.info("Could not create PowerPlace " + i);
			return null;
		}
		out.setAbilities(abilities);
		return out;
	}

}
