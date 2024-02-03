package resarcana.game.utils.factory;

import java.util.ArrayList;
import java.util.EnumSet;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import resarcana.game.abilities.ArtifactSacrificer;
import resarcana.game.abilities.Attack;
import resarcana.game.abilities.Attack.AttackIgnoreMode;
import resarcana.game.abilities.CardDrawer;
import resarcana.game.abilities.CostReduction;
import resarcana.game.abilities.CostReplacer;
import resarcana.game.abilities.CreatureTapper;
import resarcana.game.abilities.DrawDiscarder;
import resarcana.game.abilities.EssenceCollector;
import resarcana.game.abilities.EssenceConverter;
import resarcana.game.abilities.EssenceCopier;
import resarcana.game.abilities.EssencePlacer;
import resarcana.game.abilities.Protection;
import resarcana.game.abilities.Protection.ProtectionEffect;
import resarcana.game.abilities.Reanimator;
import resarcana.game.abilities.Reorder;
import resarcana.game.abilities.Reviver;
import resarcana.game.abilities.SelfReanimate;
import resarcana.game.abilities.SelfSacrifice;
import resarcana.game.abilities.specials.CreatureReanimator;
import resarcana.game.abilities.specials.DemonProtection;
import resarcana.game.abilities.specials.DragonProtection;
import resarcana.game.abilities.specials.ProtectionProducer;
import resarcana.game.abilities.specials.Vault;
import resarcana.game.abilities.specials.VialOfLight;
import resarcana.game.abilities.specials.WindupMan;
import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Game;
import resarcana.game.core.Tappable.CollectMode;
import resarcana.game.utils.EssenceSelection;
import resarcana.graphics.utils.Scheduler;
import resarcana.math.Vector;
import resarcana.utils.UtilFunctions;

public class ArtifactFactory {

	private static final int NUMBER_ARTIFACTS = 52;
	private static int[] artifacts_used = UtilFunctions.getRange(0, NUMBER_ARTIFACTS);

	private static final Vector POSITION_ABILITY_SINGLE = new Vector(0, 112);
	private static final Vector POSITION_ABILITY_TOP = new Vector(0, 78);
	private static final Vector POSITION_ABILITY_BOTTOM = new Vector(0, 115);
	private static final Vector POSITION_ABILITY_TOP_WITH_POINT = new Vector(0, 51);
	private static final Vector POSITION_ABILITY_BOTTOM_WITH_POINT = new Vector(0, 86);

	public static void scheduleImages() {
		Scheduler.getInstance().addMarker("Artifacts");
		for (int i = 0; i < NUMBER_ARTIFACTS; i++) {
			Scheduler.getInstance().scheduleResource("artifact/artifact_" + (i < 10 ? "0" : "") + i + ".png");
		}
	}

	public static void setArtifactsUsed(int[] used) {
		artifacts_used = used.clone();
	}

	public static ArrayList<Artifact> createAll(Game parent) {
		ArrayList<Artifact> out = new ArrayList<Artifact>();
		Artifact a;
		for (int i : artifacts_used) {
			a = create(i, parent);
			if (a != null) {
				out.add(a);
			}
		}
		return out;
	}

	public static Artifact create(int i, Game parent) {
		if (i < 0 || i >= NUMBER_ARTIFACTS) {
			Log.warn("Requesting unknown Artifact " + i + " in " + parent);
			return null;
		}
		Log.info("Creating Artifact " + i + " in " + parent);
		Artifact out = new Artifact(parent, "artifact/artifact_" + (i < 10 ? "0" : "") + i + ".png");
		ArrayList<Ability> abilities = new ArrayList<Ability>();
		switch (i) {
		case 0: // Athanor
			out.setName("Athanor");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1, Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.ELAN, 1), new EssenceSelection(Essences.ELAN, 2)));
			abilities.add(new EssenceConverter(out, POSITION_ABILITY_BOTTOM, EssenceConverter.MODE_ATHANOR));
			out.setAutoCollect(CollectMode.NEVER);
			break;
		case 1: // Bone dragon
			out.setName("Bone dragon");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 4, Essences.LIFE, 1));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, Essences.DEATH)
					.setEffectColor(new Color(0.5f, 0.5f, 0.5f, 0.7f)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 2: // Celestial Horse
			out.setName("Celestial Horse");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 2, Essences.ELAN, 1));
			out.setIncome(new EssenceSelection(2, Essences.GOLD, Essences.DEATH));
			out.makeBeast();
			break;
		case 3: // Chalice of Fire
			out.setName("Chalice of Fire");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1, Essences.ELAN, 1));
			out.setIncome(new EssenceSelection(Essences.ELAN, 2));
			abilities.add(new Reanimator(out, POSITION_ABILITY_SINGLE, new EssenceSelection(Essences.ELAN, 1)));
			break;
		case 4: // Chalice of Life
			out.setName("Chalice of Life");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1, Essences.LIFE, 1, Essences.CALM, 1));
			out.setIncome(new EssenceSelection(Essences.CALM, 1, Essences.LIFE, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.CALM, 2), new EssenceSelection(Essences.CALM, 2, Essences.LIFE, 1)));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(), true)
					.setEffect(ProtectionEffect.SHIELD));
			break;
		case 5: // Corrupt Altar
			out.setName("Corrupt Altar");
			out.setRawCosts(new EssenceSelection(null, 3, Essences.DEATH, 2));
			out.setIncome(new EssenceSelection(Essences.LIFE, 1, Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.LIFE, 2), new EssenceSelection(Essences.ELAN, 3)));
			abilities.add(new ArtifactSacrificer(out, POSITION_ABILITY_BOTTOM, true, 2));
			break;
		case 6: // Crypt
			out.setName("Crypt");
			out.setRawCosts(new EssenceSelection(null, 3, Essences.DEATH, 2));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.DEATH, 2)));
			abilities.add(new Reviver(out, POSITION_ABILITY_BOTTOM, Reviver.MODE_DISCARD_REVIVE, Essences.DEATH, 1, 2,
					EnumSet.of(Essences.GOLD)));
			break;
		case 7: // Cursed Skull
			out.setName("Cursed Skull");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 2));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(3, Essences.GOLD, Essences.LIFE)));
			break;
		case 8: // Dancing Sword
			out.setName("Dancing Sword");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1, Essences.ELAN, 1));
			out.setIncome(new EssenceSelection(Essences.DEATH, 1, Essences.ELAN, 1));
			abilities.add(new ProtectionProducer(out, POSITION_ABILITY_SINGLE, new EssenceSelection(Essences.ELAN, 1),
					false, new EssenceSelection(Essences.DEATH, 1)).setEffect(ProtectionEffect.SWORD));
			break;
		case 9: // Dragon Bridle
			out.setName("Dragon Bridle");
			out.setRawCosts(
					new EssenceSelection(Essences.ELAN, 1, Essences.LIFE, 1, Essences.CALM, 1, Essences.DEATH, 1));
			abilities.add(new CostReduction(out, POSITION_ABILITY_TOP_WITH_POINT, CostReduction.MODE_DRAGON, 3,
					EnumSet.noneOf(Essences.class)));
			abilities.add(new DragonProtection(out, POSITION_ABILITY_BOTTOM_WITH_POINT, new EssenceSelection(), true));
			out.setPoints(1);
			break;
		case 10: // Dragon Egg
			out.setName("Dragon Egg");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1));
			abilities.add(
					new SelfSacrifice(out, POSITION_ABILITY_BOTTOM_WITH_POINT, SelfSacrifice.MODE_DRAGON_EGG, 4, null));
			out.setPoints(1);
			break;
		case 11: // Dragon Teeth
			out.setName("Dragon Teeth");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 1, Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.ELAN, 2), new EssenceSelection(Essences.ELAN, 3)));
			abilities.add(new CostReplacer(out, POSITION_ABILITY_BOTTOM, CostReplacer.MODE_DRAGON,
					new EssenceSelection(Essences.ELAN, 3)));
			break;
		case 12: // Dwarven Pickaxe
			out.setName("Dwarven Pickaxe");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.ELAN, 1), new EssenceSelection(Essences.GOLD, 1)));
			break;
		case 13: // Earth Dragon
			out.setName("Earth Dragon");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 4, Essences.LIFE, 3));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, Essences.GOLD)
					.setEffectColor(new Color(0.f, 1.0f, 0.f, 0.7f)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 14: // Elemental Spring
			out.setName("Elemental Spring");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 1, Essences.CALM, 1));
			out.setIncome(new EssenceSelection(Essences.ELAN, 1, Essences.LIFE, 1, Essences.CALM, 1));
			abilities.add(new Protection(out, POSITION_ABILITY_SINGLE, new EssenceSelection(Essences.CALM, 1), false)
					.setEffect(ProtectionEffect.SHIELD));
			break;
		case 15: // Elvish Bow
			out.setName("Elvish Bow");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 1));
			abilities.add(new Attack(out, POSITION_ABILITY_TOP, 1, AttackIgnoreMode.NONE)
					.setEffectColor(new Color(0.f, 1.0f, 0.f, 0.7f)));
			abilities.add(new CardDrawer(out, POSITION_ABILITY_BOTTOM, new EssenceSelection()));
			break;
		case 16: // Fiery Whip
			out.setName("Fiery Whip");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.DEATH, 2));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.ELAN, 2), Essences.ELAN));
			abilities.add(new ArtifactSacrificer(out, POSITION_ABILITY_BOTTOM, false, 2));
			break;
		case 17: // Fire Dragon
			out.setName("Fire Dragon");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 6));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, Essences.CALM)
					.setEffectColor(new Color(1.0f, 0.f, 0.f, 0.7f)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 18: // Flaming Pit
			out.setName("Flaming Pit");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2));
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.ELAN, 1, Essences.DEATH, 1)));
			break;
		case 19: // Fountain of Youth
			out.setName("Fountain of Youth");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 1, Essences.DEATH, 1));
			out.setIncome(new EssenceSelection(Essences.LIFE, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.DEATH, 2), new EssenceSelection(Essences.CALM, 2, Essences.LIFE, 1)));
			break;
		case 20: // Guard Dog
			out.setName("Guard Dog");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new SelfReanimate(out, POSITION_ABILITY_TOP, new EssenceSelection(Essences.ELAN, 1)));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(), true)
					.setEffect(ProtectionEffect.GUARD_DOG));
			out.makeBeast();
			break;
		case 21: // Hand of Glory
			out.setName("Hand of Glory");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1, Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.DEATH, 1), Essences.DEATH));
			break;
		case 22: // Hawk
			out.setName("Hawk");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1, Essences.CALM, 1));
			out.setIncome(new EssenceSelection(Essences.CALM, 1));
			abilities.add(new Reorder(out, POSITION_ABILITY_TOP));
			abilities.add(new CardDrawer(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(Essences.CALM, 2)));
			out.makeBeast();
			break;
		case 23: // Horn of Plenty
			out.setName("Horn of Plenty");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 2));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(3, Essences.GOLD)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.GOLD, 1)));
			break;
		case 24: // Hypnotic Basin
			out.setName("Hypnotic Basin");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 2, Essences.ELAN, 1, Essences.DEATH, 1));
			out.setIncome(new EssenceSelection(Essences.CALM, 2));
			abilities.add(new EssenceCopier(out, POSITION_ABILITY_SINGLE, Essences.ELAN, Essences.CALM));
			break;
		case 25: // Jeweled Statuette
			out.setName("Jeweled Statuette");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 2, Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP_WITH_POINT, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.DEATH, 2), Essences.DEATH));
			abilities.add(new SelfSacrifice(out, POSITION_ABILITY_BOTTOM_WITH_POINT,
					new EssenceSelection(Essences.GOLD, 2, Essences.ELAN, 1)));
			out.setPoints(1);
			break;
		case 26: // Magical Shard
			out.setName("Magical Shard");
			out.setRawCosts(new EssenceSelection());
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(1, Essences.GOLD)));
			break;
		case 27: // Mermaid
			out.setName("Mermaid");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 2, Essences.CALM, 2));
			out.setIncome(new EssenceSelection(Essences.CALM, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_IN_PLAY, true,
					new EssenceSelection(1, Essences.DEATH, Essences.ELAN), new EssenceSelection(),
					EssencePlacer.OUTPUT_AS_INPUT));
			out.makeBeast();
			break;
		case 28: // Nightingale
			out.setName("Nightingale");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1, Essences.CALM, 1));
			out.makeBeast();
			out.setPoints(1);
			break;
		case 29: // Philosopher's Stone
			out.setName("Philosopher's Stone");
			out.setRawCosts(
					new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 2, Essences.CALM, 2, Essences.DEATH, 2));
			abilities.add(new EssenceConverter(out, POSITION_ABILITY_BOTTOM_WITH_POINT,
					EssenceConverter.MODE_PHILOSOPHERS_STONE));
			out.setPoints(1);
			break;
		case 30: // Prism
			out.setName("Prism");
			out.setRawCosts(new EssenceSelection());
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(null, 1), new EssenceSelection(2, Essences.GOLD)));
			abilities.add(new EssenceConverter(out, POSITION_ABILITY_BOTTOM, EssenceConverter.MODE_PRISM));
			break;
		case 31: // Ring of Midas
			out.setName("Ring of Midas");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1, Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP_WITH_POINT, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.LIFE, 2), new EssenceSelection(Essences.GOLD, 1)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM_WITH_POINT, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(), new EssenceSelection(Essences.GOLD, 1)));
			out.setPoints(1);
			break;
		case 32: // Sacrifical Dagger
			out.setName("Sacrifical Dagger");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 1, Essences.GOLD, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.DEATH, 3)));
			abilities
					.add(new SelfSacrifice(out, POSITION_ABILITY_BOTTOM, SelfSacrifice.MODE_DISCARD, 0, Essences.GOLD));
			break;
		case 33: // Sea Serpent
			out.setName("Sea Serpent");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 6, Essences.LIFE, 3));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, AttackIgnoreMode.SACRIFICE)
					.setEffectColor(new Color(0.f, 0.6f, 0.4f, 0.7f)));
			out.makeBeast();
			out.makeDragon();
			out.setPoints(1);
			break;
		case 34: // Treant
			out.setName("Treant");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 3, Essences.ELAN, 2));
			out.setIncome(new EssenceSelection(Essences.LIFE, 2));
			abilities.add(new EssenceCopier(out, POSITION_ABILITY_SINGLE, Essences.DEATH, Essences.ELAN));
			out.makeBeast();
			break;
		case 35: // Tree of Life
			out.setName("Tree of Life");
			out.setRawCosts(new EssenceSelection(null, 2, Essences.LIFE, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(), new EssenceSelection(Essences.LIFE, 2), Essences.LIFE));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM, new EssenceSelection(Essences.LIFE, 1), false)
					.setEffect(ProtectionEffect.TREE));
			break;
		case 36: // Vault
			out = new Vault(parent);
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(), new EssenceSelection(Essences.GOLD, 1)));
			break;
		case 37: // Water Dragon
			out.setName("Water Dragon");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 6));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, Essences.ELAN)
					.setEffectColor(new Color(0.f, 0.f, 1.f, 0.7f)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 38: // Wind Dragon
			out.setName("Wind Dragon");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 4, null, 4));
			abilities.add(new Attack(out, POSITION_ABILITY_BOTTOM_WITH_POINT, 2, AttackIgnoreMode.DISCARD)
					.setEffectColor(new Color(0.f, 1.0f, 1.0f, 0.7f)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 39: // Windup Man
			out = new WindupMan(parent);
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_SINGLE, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(null, 1), new EssenceSelection(), EssencePlacer.OUTPUT_AS_INPUT));
			break;
		case 40: // Choas Imp
			out.setName("Chaos Imp");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 1, Essences.DEATH, 1));
			abilities.add(new CreatureReanimator(out, POSITION_ABILITY_TOP, CreatureReanimator.MODE_DEMON,
					new EssenceSelection(Essences.LIFE, 1)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.DEATH, 1, Essences.ELAN, 1), new EssenceSelection(Essences.DEATH, 3),
					EssencePlacer.OUTPUT_INDEPENDENT));
			out.makeDemon();
			break;
		case 41: // Cursed Dwarven King
			out.setName("Cursed Dwarven King");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1, Essences.DEATH, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, false,
					new EssenceSelection(Essences.DEATH, 1, Essences.ELAN, 1, Essences.LIFE, 1),
					new EssenceSelection(Essences.GOLD, 2), EssencePlacer.OUTPUT_INDEPENDENT));
			abilities.add(new CreatureTapper(out, POSITION_ABILITY_BOTTOM, CreatureTapper.TAP_MODE_DRAGON, true,
					CreatureTapper.OUTPUT_MODE_PLAYER, new EssenceSelection(Essences.GOLD, 1)));
			out.makeDemon();
			break;
		case 42: // Fire Demon
			out.setName("Fire Demon");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.DEATH, 2));
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new Attack(out, POSITION_ABILITY_TOP, new EssenceSelection(Essences.ELAN, 1), 2,
					AttackIgnoreMode.NONE).setEffectColor(new Color(1.0f, 0.f, 0.f, 0.7f)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.ELAN, 3)));
			out.makeDemon();
			break;
		case 43: // Golden Lion
			out.setName("Golden Lion");
			out.setRawCosts(
					new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 1, Essences.CALM, 1, Essences.GOLD, 1));
			out.setIncome(new EssenceSelection(Essences.CALM, 1, Essences.LIFE, 1, Essences.ELAN, 1));
			abilities.add(new Protection(out, POSITION_ABILITY_BOTTOM_WITH_POINT, new EssenceSelection(), true)
					.setEffect(ProtectionEffect.LION));
			out.makeBeast();
			out.setPoints(1);
			break;
		case 44: // Homunculus
			out.setName("Homunculus");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 1));
			abilities.add(new CostReduction(out, POSITION_ABILITY_TOP, CostReduction.MODE_DEMON, 2,
					EnumSet.noneOf(Essences.class)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(), new EssenceSelection(2, Essences.GOLD)));
			out.makeDemon();
			break;
		case 45: // Hound of Death
			out.setName("Hound of Death");
			out.setRawCosts(new EssenceSelection(Essences.LIFE, 3, Essences.DEATH, 2));
			out.setIncome(new EssenceSelection(Essences.DEATH, 2));
			abilities.add(new Attack(out, POSITION_ABILITY_TOP, new EssenceSelection(Essences.LIFE, 1), 2,
					AttackIgnoreMode.NONE).setEffectColor(new Color(0.f, 0.f, 0.6f, 0.7f)));
			abilities.add(new EssenceCopier(out, POSITION_ABILITY_BOTTOM, Essences.GOLD, Essences.DEATH));
			out.makeDemon();
			out.makeBeast();
			break;
		case 46: // Infernal Engine
			out.setName("Infernal Engine");
			out.setRawCosts(new EssenceSelection(Essences.DEATH, 1));
			out.setIncome(new EssenceSelection(Essences.ELAN, 1));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(null, -1), new EssenceSelection(), EssencePlacer.OUTPUT_AS_INPUT));
			abilities.add(new EssenceCollector(out, POSITION_ABILITY_BOTTOM));
			out.setAutoCollect(CollectMode.ASK);
			break;
		case 47: // Possessed Demon Slayer
			out.setName("Possessed Demon Slayer");
			out.setRawCosts(new EssenceSelection(Essences.GOLD, 1, Essences.ELAN, 1, Essences.DEATH, 1));
			abilities.add(new EssenceCopier(out, POSITION_ABILITY_TOP_WITH_POINT, EssenceCopier.MODE_DEMON, null,
					Essences.ELAN));
			abilities.add(new DemonProtection(out, POSITION_ABILITY_BOTTOM_WITH_POINT, new EssenceSelection(), false));
			out.makeDemon();
			out.setPoints(1);
			break;
		case 48: // Prismatic Dragon
			out.setName("Prismatic Dragon");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 2, Essences.CALM, 2));
			out.setIncome(new EssenceSelection(1, Essences.GOLD, Essences.DEATH));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM_WITH_POINT, EssencePlacer.MODE_SELF, true,
					new EssenceSelection(Essences.GOLD, 1), new EssenceSelection(4, Essences.GOLD)));
			out.makeDragon();
			out.setPoints(1);
			break;
		case 49: // Shadowy Figure
			out.setName("Shadowy Figure");
			out.setRawCosts(new EssenceSelection(Essences.CALM, 2, Essences.DEATH, 2));
			out.setIncome(new EssenceSelection(Essences.CALM, 1));
			abilities.add(new DrawDiscarder(out, POSITION_ABILITY_TOP, 2, 1, new EssenceSelection(Essences.CALM, 1)));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.CALM, 3)));
			out.makeDemon();
			break;
		case 50: // Vial of Light
			out = new VialOfLight(parent);
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_TOP, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.DEATH, 1), new EssenceSelection(Essences.LIFE, 1, Essences.ELAN, 1)));
			// 2nd ability implemented indirectly in Game and Player
			break;
		case 51: // Vortex of Destruction
			out.setName("Vortex of Destruction");
			out.setRawCosts(new EssenceSelection(Essences.ELAN, 2, Essences.LIFE, 2, Essences.DEATH, 1));
			out.setIncome(new EssenceSelection(Essences.ELAN, 1, Essences.DEATH, 1));
			abilities.add(new ArtifactSacrificer(out, POSITION_ABILITY_TOP, false, 2));
			abilities.add(new EssencePlacer(out, POSITION_ABILITY_BOTTOM, EssencePlacer.MODE_PLAYER, true,
					new EssenceSelection(Essences.LIFE, 1), new EssenceSelection(Essences.DEATH, 3)));
			out.makeDemon();
			break;
		default:
			Log.info("Could not create Artifact " + i);
			return null;
		}
		out.setAbilities(abilities);
		return out;
	}

}
