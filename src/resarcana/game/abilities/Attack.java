package resarcana.game.abilities;

import java.util.ArrayList;

import org.newdawn.slick.Color;
import org.newdawn.slick.util.Log;

import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Player;
import resarcana.game.core.Tappable;
import resarcana.game.utils.EssenceSelection;
import resarcana.game.utils.HistoryElement;
import resarcana.game.utils.userinput.EssenceSelector;
import resarcana.game.utils.userinput.ImageHolder;
import resarcana.game.utils.userinput.ImageSelector;
import resarcana.game.utils.userinput.Selecting;
import resarcana.game.utils.userinput.Selector;
import resarcana.game.utils.userinput.UserInputOverwrite;
import resarcana.graphics.utils.SoundManager;
import resarcana.math.Vector;

public class Attack extends Ability implements Selecting {

	public enum AttackIgnoreMode {
		NONE, ESSENCE, SACRIFICE, DISCARD
	};

	public static final int STATE_IDLE = 0;
	public static final int STATE_INITIAL_COST = 1;
	public static final int STATE_PROTECTION = 2;
	public static final int STATE_COST = 3;

	private final AttackIgnoreMode mode;
	private final Essences ignore;
	private final int hitpoints;
	private final EssenceSelection cost;

	private Color effectColor = Color.white;

	private int status = STATE_IDLE;
	private Player attackedPlayer = null;

	public Attack(Tappable parent, Vector relPos, EssenceSelection cost, int hitpoints, Essences ignore) {
		super(parent, relPos);
		this.cost = cost;
		this.mode = AttackIgnoreMode.ESSENCE;
		this.ignore = ignore;
		this.hitpoints = hitpoints;
	}

	public Attack(Tappable parent, Vector relPos, int hitpoints, Essences ignore) {
		this(parent, relPos, new EssenceSelection(), hitpoints, ignore);
	}

	public Attack(Tappable parent, Vector relPos, EssenceSelection cost, int hitpoints, AttackIgnoreMode mode) {
		super(parent, relPos);
		this.cost = cost;
		this.ignore = null;
		this.mode = mode;
		this.hitpoints = hitpoints;
	}

	public Attack(Tappable parent, Vector relPos, int hitpoints, AttackIgnoreMode mode) {
		this(parent, relPos, new EssenceSelection(), hitpoints, mode);
	}

	@Override
	protected boolean isActivable() {
		return super.isActivable() && this.getPlayer().isPayable(this.cost);
	}

	public Attack setEffectColor(Color color) {
		if (this.effectColor != null) {
			this.effectColor = color;
		}
		return this;
	}

	public Color getEffectColor() {
		return this.effectColor;
	}

	private void doEffects() {
		if (this.getTappable().isDragon()) { // Dragon attack
			this.getGame().playDragonAnimations(this.getPlayer(), this.effectColor);
		} else if (this.getTappable().isDemon()) { // Demon attack
			this.getGame().playDemonAnimations(this.getPlayer(), this.effectColor);
		} else if (this.hitpoints == 1) { // Bow attack
			this.getGame().playBowAttackAnimations(this.getPlayer());
		}
	}

	private void attack(EssenceSelection payedCost) {
		this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Attack", payedCost));
	}

	@Override
	public boolean activate() {
		if (this.cost.getTotal() == 0) {
			this.attack(this.cost);
		} else {
			this.getGameClient()
					.addSelector(new EssenceSelector(this, this.cost, this.getPlayer().getEssenceCounter().getCount(),
							this.cost.getValues(), "Pay for an attack with " + this.getTappable().getName()));
			this.status = STATE_INITIAL_COST;
		}
		return false;
	}

	@Override
	public HistoryElement activateOverwrite(UserInputOverwrite overwrite) {
		String type = overwrite.getParts().get(0);
		Player attacked = this.getGame().getPlayer(overwrite.getParts().get(1));
		if (type.equalsIgnoreCase("Essence")) {
			EssenceSelection sel = new EssenceSelection(overwrite.getParts().get(2));
			attacked.modifyEssence(sel, true);
			return new HistoryElement(this)
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()));
		} else if (type.equalsIgnoreCase("Protection")) {
			Tappable prot = this.getGame().getTappable(overwrite.getParts().get(2));
			prot.protect(this);
			return new HistoryElement(prot.getProtection(), attacked.getName())
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(this.getTappable());
		} else if (type.equalsIgnoreCase("Artifact")) {
			Artifact artifact = (Artifact) this.getGame().getTappable(overwrite.getParts().get(2));
			switch (this.mode) {
			case DISCARD:
				attacked.discardCard(artifact);
				break;
			case SACRIFICE:
				attacked.destroyArtifact(artifact);
				break;
			default:
				Log.error("Unknown IgnoreMode for discard/sacrifice: " + this.mode);
				break;
			}
			return new HistoryElement(this, attacked.getName())
					.setOptionalTwo(this.getGame().getCurrentEssenceDifference(this.getPlayer()))
					.setOptionalOne(artifact);
		} else if (type.equalsIgnoreCase("Passed")) {
			// Nothing to do, because attackedPlayer is unaffected
			return new HistoryElement(this, attacked.getName()).setOptionalOne("misc/artifact_back.png");
		} else if (type.equalsIgnoreCase("Attacker")) {
			// Nothing to do, because attacking player is unaffected
			this.doEffects();
			return new HistoryElement(this);
		} else {
			return new HistoryElement(this);
		}
	}

	public void activateAttack(EssenceSelection cost, Player defender) {
		this.attackedPlayer = defender;
		// Pay cost for attack now as this is done for every client
		this.getPlayer().modifyEssence(cost, true);
		this.getTappable().tap();
		// A passed player can't be attacked and thus has no cost to pay
		if (this.attackedPlayer.hasPassed()) {
			this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Passed", this.attackedPlayer));
			return;
		}
		// The attacking player does not need to defend
		if (defender == this.getPlayer()) {
			this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Attacker", this.attackedPlayer));
			return;
		}
		this.prepareProtectionChoice(this.attackedPlayer);
	}

	private void prepareProtectionChoice(Player defender) {
		ArrayList<ImageHolder> images = new ArrayList<ImageHolder>();
		switch (this.mode) {
		case ESSENCE:
			if (this.ignore != null) {
				if (this.attackedPlayer.isPayable(new EssenceSelection(this.ignore, 1))) {
					images.add(this.getTappable());
				}
			}
			break;
		case DISCARD:
			if (this.attackedPlayer.getHand().size() > 0) {
				images.add(this.getTappable());
			}
			break;
		case SACRIFICE:
			if (this.attackedPlayer.getArtifactsInPlay().size() > 0) {
				images.add(this.getTappable());
			}
			break;
		case NONE:
			break;
		default:
			Log.error("Unknown IgnoreMode: " + this.mode);
			break;
		}
		ArrayList<Tappable> protections = this.attackedPlayer.getProtection();
		for (Tappable tappable : protections) {
			if (tappable.canProtectFrom(this.getTappable())) {
				images.add(tappable);
			}
		}
		images.add(Essences.LIFE);
		this.getGameClient().addSelector(new ImageSelector<ImageHolder>(this, images,
				"Choose protection against attack of " + this.getTappable().getName()).disableCancel());
		this.status = STATE_PROTECTION;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void processSelection(Selector sel) {
		if (sel instanceof EssenceSelector && this.status == STATE_INITIAL_COST) {
			this.getGameClient().unsetSelector(sel);
			this.attack(((EssenceSelector) sel).getSelection()); // attack with selected cost
			this.status = STATE_IDLE;
		} else if (sel instanceof ImageSelector && this.status == STATE_PROTECTION) {
			this.getGameClient().unsetSelector(sel);
			ImageHolder result = ((ImageSelector<ImageHolder>) sel).getResult();
			if (result == Essences.LIFE) {
				int life = this.attackedPlayer.getEssenceCounter().getCount()[Essences.LIFE.ordinal()];
				if (life >= this.hitpoints) {
					SoundManager.getInstance().playAttackHit();
					this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Essence",
							this.attackedPlayer, new EssenceSelection(Essences.LIFE, this.hitpoints)));
					this.status = STATE_IDLE;
					return;
				} else {
					this.getGameClient()
							.addSelector(new EssenceSelector(this,
									new EssenceSelection(Essences.LIFE, life, null,
											Math.min(2 * (this.hitpoints - life),
													this.attackedPlayer.getEssenceCounter().getTotalCount() - life)),
									this.attackedPlayer.getEssenceCounter().getCount(), "Choose tribute to attack"));
					this.status = STATE_COST;
					return;
				}
			} else {
				if (result == this.getTappable()) {
					SoundManager.getInstance().playProtect();
					switch (this.mode) {
					case ESSENCE:
						this.status = STATE_IDLE;
						this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Essence",
								this.attackedPlayer, new EssenceSelection(this.ignore, 1)));
						return;
					case DISCARD:
						this.getGameClient().addSelector(new ImageSelector<Artifact>(this,
								this.attackedPlayer.getHand(), "Discard an artifact for protection"));
						this.status = STATE_COST;
						return;
					case SACRIFICE:
						this.getGameClient().addSelector(new ImageSelector<Artifact>(this,
								this.attackedPlayer.getArtifactsInPlay(), "Destroy an artifact for protection"));
						this.status = STATE_COST;
						return;
					default:
						Log.error("Unknown IgnoreMode: " + this.mode);
						return;
					}
				}
				this.getGameClient().informAllClients_Attack(
						new UserInputOverwrite(this, "Protection", this.attackedPlayer, result));
				this.status = STATE_IDLE;
				return;
			}
		} else if (sel instanceof EssenceSelector && this.status == STATE_COST) {
			SoundManager.getInstance().playAttackHit();
			this.getGameClient().unsetSelector(sel);
			this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Essence", this.attackedPlayer,
					((EssenceSelector) sel).getSelection()));
			this.status = STATE_IDLE;
			return;
		} else if (sel instanceof ImageSelector && this.status == STATE_COST) {
			this.getGameClient().unsetSelector(sel);
			this.getGameClient().informAllClients_Attack(new UserInputOverwrite(this, "Artifact", this.attackedPlayer,
					((ImageSelector<Artifact>) sel).getResult()));
		} else {
			Log.warn("Unknown selector " + sel + " for " + this);
		}
	}

	@Override
	public void cancelSelection(Selector sel) {
		if (this.status == STATE_INITIAL_COST) {
			this.getGameClient().unsetSelector(sel);
			this.getGame().cancelAbility(this);
			this.status = STATE_IDLE;
		} else if (this.status == STATE_COST) {
			this.getGameClient().unsetSelector(sel);
			this.prepareProtectionChoice(this.attackedPlayer);
		} else {
			Log.warn("Cancel should not be possible for " + this + " " + sel + " in status " + this.status);
		}
	}

	public boolean isDragonAttack() {
		return this.getTappable().isDragon();
	}

	public boolean isDemonAttack() {
		return this.getTappable().isDemon();
	}

	public boolean isBowAttack() {
		return this.hitpoints == 1 && !this.getTappable().isDragon() && !this.getTappable().isDemon();
	}
}
