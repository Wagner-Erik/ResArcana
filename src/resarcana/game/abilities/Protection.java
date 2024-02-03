package resarcana.game.abilities;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Vector;

public class Protection extends Ability {

	public enum ProtectionEffect {
		NONE, SHIELD, TREE, DEMON_SLAYER, GUARD_DOG, LION, SWORD
	};

	private final EssenceSelection cost;
	private final boolean tapping;

	private ProtectionEffect effect = ProtectionEffect.NONE;

	public Protection(Tappable parent, Vector relPos, EssenceSelection cost, boolean tapping) {
		super(parent, relPos);
		this.cost = cost;
		this.tapping = tapping;
	}

	public Protection setEffect(ProtectionEffect effect) {
		this.effect = effect;
		return this;
	}

	private void playEffect(Attack attack) {
		switch (this.effect) {
		case DEMON_SLAYER:
			this.getPlayer().playDemonSlayerAnimation(attack.getEffectColor());
			break;
		case LION:
			this.getPlayer().playLionAnimation();
			break;
		case SWORD:
			this.getPlayer().playDancingSwordAnimation();
			break;
		case SHIELD:
			if (attack.isBowAttack()) {
				this.getPlayer().playShieldVsArrowAnimation(Color.white);
			} else if (attack.isDragonAttack()) {
				this.getPlayer().playShieldVsDragonAnimation(attack.getEffectColor());
			} else if (attack.isDemonAttack()) {
				this.getPlayer().playShieldVsDemonAnimation(attack.getEffectColor());
			} else {
				this.getPlayer().playShieldAnimation(Color.white);
			}
			break;
		case TREE:
			this.getPlayer().playGrowingTreeAnimation();
			break;
		case GUARD_DOG:
			this.getPlayer().playGuardDogAnimation();
			break;
		case NONE:
			SoundManager.getInstance().playProtect();
			break;
		default:
			SoundManager.getInstance().playProtect();
			Log.warn("Unknown ProtectionEffect: " + this.effect);
			break;
		}
	}

	public void protect(Attack attack) {
		if (this.tapping) {
			this.getTappable().tap();
		}
		this.getPlayer().modifyEssence(this.cost, true);
		this.playEffect(attack);
	}

	@Override
	public boolean activate() {
		Log.error("Protection " + this + "should never be directly activated!");
		return true;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		Log.error("Protection " + this + "should never be activated by direct overwrite!");
		return new HistoryElement(this);
	}

	@Override
	protected boolean isActivable() {
		return false;
	}

	public boolean canProtectFrom(Tappable origin) {
		Log.info(this + ((!this.getTappable().isTapped() && this.getPlayer().isPayable(this.cost)) ? "can" : "can't")
				+ " protect from " + origin);
		return (!this.tapping || !this.getTappable().isTapped()) && this.getPlayer().isPayable(this.cost);
	}

}
