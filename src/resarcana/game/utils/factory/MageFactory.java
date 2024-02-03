package resarcana.game.utils.factory;

import java.util.ArrayList;
import java.util.EnumSet;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.CardDrawer;
import resarcana.game.abilities.CostReduction;
import resarcana.game.abilities.CreatureTapper;
import resarcana.game.abilities.Discard;
import resarcana.game.abilities.DrawDiscarder;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reanimator;
import resarcana.game.abilities.Reorder;
import resarcana.game.abilities.Retriever;
import resarcana.game.abilities.specials.CreatureDiscard;
import resarcana.game.abilities.specials.CreatureReanimator;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Mage;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class MageFactory {

	private static final int NUMBER_MAGES = 14;
	private static int[] mages_used = UtilFunctions.getRange(0, NUMBER_MAGES);

	private static final Vector POSITION_ABILITY_MAGE = new Vector(0, 112);
	private static final Vector POSITION_ABILITY_MAGE_BOTTOM = new Vector(0, 115);
	private static final Vector POSITION_ABILITY_MAGE_TOP = new Vector(0, 75);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Mages");
		for (int i = 0; i < NUMBER_MAGES; i++) {
			Scheduler.getInstance().scheduleResource("mage/mage_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setMagesUsed(int[] used) {
		mages_used = used.clone();
	}

	public static ArrayList<Mage> createAll(Game parent) {
		ArrayList<Mage> out = new ArrayList<Mage>();
		Mage a;
		for (int i : mages_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static Mage create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_MAGES) {
			Log.warn("Requesting unknown Mage " + i + " in " + parent);
			return null;
		}
		Log.info("Creating Mage " + i + " in " + parent);
		Mage out = new Mage(parent, "mage/mage_" + (i < 10 ? "0" : "") + i + ".png");
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Necromancer
			out.setName("Necromancer");
			out.setIncome(new EssenceSelection(Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.LIFE, 2), new EssenceSelection(Essences.DEATH, 3)));
			break;
		case 1: // Duelist
			out.setName("Duelist");
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.DEATH, 1), new EssenceSelection(Essences.GOLD, 1)));
			break;
		case 2: // Transmuter
			out.setName("Transmuter");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(null, 2), new EssenceSelection(3, Essences.GOLD)));
			break;
		case 3: // Druid
			out.setName("Druid");
			out.setIncome(new EssenceSelection(Essences.LIFE, 1));
			abilities.add(new CreatureReanimator(out, POSITION_ABILITY_MAGE, CreatureReanimator.MODE_BEAST,
					new EssenceSelection()));
			break;
		case 4: // Artificer
			out.setName("Artificer");
			abilities.add(new CostReduction(out, POSITION_ABILITY_MAGE, CostReduction.MODE_ARTIFACTS, 1,
					EnumSet.of(Essences.GOLD)));
			break;
		case 5: // Healer
			out.setName("Healer");
			out.setIncome(new EssenceSelection(1, Essences.DEATH, Essences.ELAN, Essences.GOLD));
			abilities.add(new Protection(out, POSITION_ABILITY_MAGE, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			break;
		case 6: // Alchemist
			out.setName("Alchemist");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(null, 4), new EssenceSelection(Essences.GOLD, 2)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(1, Essences.GOLD)));
			break;
		case 7: // Witch
			out.setName("Witch");
			out.setIncome(new EssenceSelection(1, Essences.CALM, Essences.ELAN, Essences.GOLD));
			abilities.add(new Reanimator(out, POSITION_ABILITY_MAGE, new EssenceSelection(null, 2)));
			break;
		case 8: // Scholar
			out.setName("Scholar");
			abilities.add(new CardDrawer(out, POSITION_ABILITY_MAGE, new EssenceSelection(null, 1)));
			break;
		case 9: // Seer
			out.setName("Seer");
			out.setIncome(new EssenceSelection(Essences.CALM, 1));
			abilities.add(new Reorder(out, POSITION_ABILITY_MAGE));
			break;
		case 10: // Bard
			out.setName("Bard");
			abilities.add(
					new CreatureDiscard(out, POSITION_ABILITY_MAGE_TOP, true, new EssenceSelection(Essences.GOLD, 2)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(1, Essences.GOLD)));
			break;
		case 11: // Beastmaster
			out.setName("Beastmaster");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_MAGE_TOP, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.LIFE, 3)));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_MAGE_BOTTOM, CreatureTapper.TAP_MODE_BEAST, true,
					CreatureTapper.OUTPUT_MODE_PLAYER, new EssenceSelection(2, Essences.GOLD)));
			break;
		case 12: // Demonlogist
			out.setName("Demonologist");
			abilities.add(new Retriever(out, POSITION_ABILITY_MAGE_TOP, new EssenceSelection(Essences.LIFE, 1)));
			abilities.add(new CreatureReanimator(out, POSITION_ABILITY_MAGE_BOTTOM, CreatureReanimator.MODE_DEMON,
					new EssenceSelection()));
			break;
		case 13: // Diviner
			out.setName("Diviner");
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new DrawDiscarder(out, POSITION_ABILITY_MAGE, 3));
			break;
		default:
			Log.info("Could not create Mage " + i);
			return null;
		}
		abilities.add(new Discard(out, Mage.POSITION_DISCARD_ALL, false, new EssenceSelection(2, Essences.GOLD)));
		abilities.add(new Discard(out, Mage.POSITION_DISCARD_GOLD, false, new EssenceSelection(Essences.GOLD, 1)));
		out.setAbilities(abilities);
		return out;
	}

}
