package resarcana.game.utils.factory;

import java.util.ArrayList;

import org.newdawn.slick.util.Log;

import resarcana.game.abilities.CardDrawer;
import resarcana.game.abilities.ClaimScroll;
import resarcana.game.abilities.DrawDiscarder;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Pass;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reanimator;
import resarcana.game.abilities.specials.Illusion;
import resarcana.game.core.Ability;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.MagicItem;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class MagicItemFactory {

	private static final int NUMBER_MAGIC_ITEMS = 10;
	private static int[] items_used = UtilFunctions.getRange(0, NUMBER_MAGIC_ITEMS);

	private static final Vector POSITION_ABILITY_ITEM = new Vector(0, 102);
	private static final Vector POSITION_PASS = new Vector(0, 0);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Magic Items");
		for (int i = 0; i < NUMBER_MAGIC_ITEMS; i++) {
			Scheduler.getInstance().scheduleResource("item/item_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setItemsUsed(int[] used) {
		items_used = used.clone();
	}

	public static ArrayList<MagicItem> createAll(Game parent) {
		ArrayList<MagicItem> out = new ArrayList<MagicItem>();
		MagicItem a;
		for (int i : items_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static MagicItem create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_MAGIC_ITEMS) {
			Log.warn("Requesting unknown MagicItem " + i + " in " + parent);
			return null;
		}
		Log.info("Creating MagicItem " + i + " in " + parent);
		MagicItem out = new MagicItem(parent, "item/item_" + (i < 10 ? "0" : "") + i + ".png");
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Alchemy
			out.setName("Alchemy");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_ITEM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(null, 4), new EssenceSelection(Essences.GOLD, 2)));
			break;
		case 1: // CalmElan
			out.setName("Calm Elan");
			out.setIncome(new EssenceSelection(1, Essences.DEATH, Essences.GOLD, Essences.LIFE));
			break;
		case 2: // DeathLife
			out.setName("Death Life");
			out.setIncome(new EssenceSelection(1, Essences.CALM, Essences.GOLD, Essences.ELAN));
			break;
		case 3: // Divination
			out.setName("Divination");
			abilities.add(new DrawDiscarder(out, POSITION_ABILITY_ITEM, 3));
			break;
		// break;
		case 4: // Protection
			out.setName("Protection");
			abilities.add(new Protection(out, POSITION_ABILITY_ITEM, new EssenceSelection(), true).setEffect(ProtectionEffect.SHIELD));
			break;
		// break;
		case 5: // Reanimate
			out.setName("Reanimate");
			abilities.add(new Reanimator(out, POSITION_ABILITY_ITEM, new EssenceSelection(null, 1)));
			break;
		case 6: // Research
			out.setName("Research");
			abilities.add(new CardDrawer(out, POSITION_ABILITY_ITEM, new EssenceSelection(null, 1)));
			break;
		case 7: // Transmutation
			out.setName("Transmutation");
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_ITEM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(null, 3), new EssenceSelection(3, Essences.GOLD)));
			break;
		case 8: // Inscription
			out.setName("Inscription");
			abilities.add(new ClaimScroll(out, POSITION_ABILITY_ITEM, new EssenceSelection(null, 1)));
			break;
		case 9: // Illusion
			out = new Illusion(parent);
			break;
		default:
			Log.info("Could not create MagicItem " + i);
			return null;
		}
		abilities.add(new Pass(out, POSITION_PASS));
		out.setAbilities(abilities);
		return out;
	}

}
