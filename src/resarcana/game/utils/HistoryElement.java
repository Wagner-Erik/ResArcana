package resarcana.game.utils;

import org.newdawn.slick.Color;
import org.newdawn.slick.Font;
import org.newdawn.slick.Graphics;

import resarcana.game.abilities.Discard;
import resarcana.game.abilities.Pass;
import resarcana.game.core.Ability;
import resarcana.game.core.Artifact;
import resarcana.game.core.Essences;
import resarcana.game.core.Tappable;
import resarcana.graphics.gui.ImageDisplay;
import resarcana.graphics.gui.InterfaceFunctions;
import resarcana.graphics.gui.InterfaceObject;
import resarcana.graphics.gui.ScalableObject;
import resarcana.graphics.utils.FontManager;
import resarcana.graphics.utils.GraphicUtils;
import resarcana.graphics.utils.ResourceManager;
import resarcana.math.Rectangle;
import resarcana.math.Vector;
import resarcana.utils.Parameter;

public class HistoryElement extends InterfaceObject implements ScalableObject, ImageDisplay {

	private static final Rectangle HITBOX = Artifact.ARTIFACT_HITBOX;
	private static final Rectangle OVERLAY = new Rectangle(Vector.ZERO, Ability.ABILITY_HITBOX);

	private static final float NAME_MAX_SCALE = 2.f;
	private static final float NAME_HEIGHT = FontManager.getInstance().getLineHeight(
			FontManager.getInstance().getFont((int) (NAME_MAX_SCALE * Parameter.GUI_STANDARD_FONT_SIZE)));

	private static final float OPTIONAL_RATIO = 0.45f;

	private final String mainImage, overlay;
	private final Rectangle overlayHitbox;
	private final String player, mainText;
	private final float nameScale;

	private Font fontName, fontMain, font1, font2;

	private String opt1Image = "", opt2Image = "", opt1Text = "", opt2Text = "";

	private Rectangle scaledMainHitbox, scaledOpt1Hitbox, scaledOpt2Hitbox, scaledOverlayHitbox;
	private Vector scaledNamePosition;

	public HistoryElement(Tappable tappable) {
		super(InterfaceFunctions.HISTORY_ELEMENT);
		this.mainImage = tappable.getImage();
		this.player = tappable.getPlayer().getName();
		this.nameScale = CalcNameScale(this.player);
		this.overlay = null;
		this.overlayHitbox = null;
		this.mainText = "";

		this.fontName = FontManager.getInstance().getFont((int) (this.nameScale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.fontMain = FontManager.getInstance().getDefaultFont();

		this.scale(1);
	}

	public HistoryElement(Ability ability, String player) {
		super(InterfaceFunctions.HISTORY_ELEMENT);
		this.player = player;
		this.nameScale = CalcNameScale(this.player);
		this.mainText = "";

		this.fontName = FontManager.getInstance().getFont((int) (this.nameScale * Parameter.GUI_STANDARD_FONT_SIZE));
		this.fontMain = FontManager.getInstance().getDefaultFont();

		if (ability instanceof Pass) {
			this.mainImage = "misc/item_back.png";
			this.overlay = null;
			this.overlayHitbox = null;
		} else if (ability instanceof Discard) {
			if (((Discard) ability).reward.isDetermined()) {
				this.mainImage = "misc/history_discard_gold.png";
			} else {
				this.mainImage = "misc/history_discard_all.png";
			}
			this.overlay = "misc/ability_border_available.png";
			this.overlayHitbox = OVERLAY;
		} else {
			this.mainImage = ability.getTappable().getImage();
			this.overlay = "misc/ability_border_available.png";
			this.overlayHitbox = OVERLAY
					.modifyCenter(ability.getRelPos().mul(HITBOX.height / ability.getTappable().getRawHitbox().height));
		}
		this.scaledOverlayHitbox = this.overlayHitbox;

		this.scale(1);
	}

	public HistoryElement(Ability ability) {
		this(ability, ability.getPlayer().getName());
	}

	public HistoryElement(String player, EssenceSelection essences) {
		super(InterfaceFunctions.HISTORY_ELEMENT);
		this.player = player;
		this.nameScale = CalcNameScale(this.player);
		this.mainImage = "misc/history_income.png";
		this.overlay = null;
		this.overlayHitbox = null;
		this.scaledOverlayHitbox = this.overlayHitbox;
		this.mainText = CreateEssenceText(essences);

		this.fontName = FontManager.getInstance().getFont((int) (this.nameScale * Parameter.GUI_STANDARD_FONT_SIZE));
		float scale = HITBOX.height * 0.8f
				/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(), this.mainText);
		this.fontMain = FontManager.getInstance().getFont((int) (scale * Parameter.GUI_STANDARD_FONT_SIZE));

		this.scale(1);
	}

	public HistoryElement scale(float newScale) {
		float totalHeight = ((1 + OPTIONAL_RATIO + 0.05f) * HITBOX.height + NAME_HEIGHT);
		// Calculate sub hitboxes
		this.scaledMainHitbox = HITBOX.modifyCenter(0, HITBOX.height / 2 + NAME_HEIGHT - totalHeight / 2)
				.scaleWithCenter(newScale);
		this.scaledOpt1Hitbox = HITBOX.scale(OPTIONAL_RATIO)
				.modifyCenter(-HITBOX.width * (OPTIONAL_RATIO + 0.05f) / 2,
						HITBOX.height * (1 + OPTIONAL_RATIO / 2 + 0.05f) + NAME_HEIGHT - totalHeight / 2)
				.scaleWithCenter(newScale);
		this.scaledOpt2Hitbox = HITBOX.scale(OPTIONAL_RATIO)
				.modifyCenter(HITBOX.width * (OPTIONAL_RATIO + 0.05f) / 2,
						HITBOX.height * (1 + OPTIONAL_RATIO / 2 + 0.05f) + NAME_HEIGHT - totalHeight / 2)
				.scaleWithCenter(newScale);
		if (this.overlayHitbox != null) {
			this.scaledOverlayHitbox = this.overlayHitbox.moveBy(0, HITBOX.height / 2 + NAME_HEIGHT - totalHeight / 2)
					.scaleWithCenter(newScale);
		}
		this.scaledNamePosition = new Vector(0, (NAME_HEIGHT / 2 - totalHeight / 2) * newScale);
		// Set new hitbox
		this.setHitbox(HITBOX.width * newScale, totalHeight * newScale);
		// Request new fonts
		this.fontName = FontManager.getInstance()
				.getFont((int) (this.nameScale * Parameter.GUI_STANDARD_FONT_SIZE * newScale));
		float fontScale;
		if (!this.mainText.isEmpty()) {
			fontScale = Math.min(
					this.scaledMainHitbox.height * 0.8f
							/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(),
									this.mainText),
					this.scaledMainHitbox.width / FontManager.getInstance()
							.getWidth(FontManager.getInstance().getDefaultFont(), this.mainText));
			this.fontMain = FontManager.getInstance().getFont((int) (fontScale * Parameter.GUI_STANDARD_FONT_SIZE));
		}
		if (!this.opt1Text.isEmpty()) {
			fontScale = Math.min(
					this.scaledOpt1Hitbox.height * 0.8f
							/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(),
									this.opt1Text),
					this.scaledOpt1Hitbox.width / FontManager.getInstance()
							.getWidth(FontManager.getInstance().getDefaultFont(), this.opt1Text));
			this.font1 = FontManager.getInstance().getFont((int) (fontScale * Parameter.GUI_STANDARD_FONT_SIZE));
		}
		if (!this.opt2Text.isEmpty()) {
			fontScale = Math.min(
					this.scaledOpt2Hitbox.height * 0.8f
							/ FontManager.getInstance().getHeight(FontManager.getInstance().getDefaultFont(),
									this.opt2Text),
					this.scaledOpt2Hitbox.width / FontManager.getInstance()
							.getWidth(FontManager.getInstance().getDefaultFont(), this.opt2Text));
			this.font2 = FontManager.getInstance().getFont((int) (fontScale * Parameter.GUI_STANDARD_FONT_SIZE));
		}
		// Return this object which is now rendered with the newScale
		return this;
	}

	private static String CreateEssenceText(EssenceSelection essences) {
		String out = "";
		out += "   " + essences.getValue(Essences.ELAN) + "            " + essences.getValue(Essences.LIFE) + "   ";
		out += "\n";
		out += "    " + essences.getValue(Essences.CALM) + "           " + essences.getValue(Essences.DEATH) + "   ";
		out += "\n";
		out += "           " + essences.getValue(Essences.GOLD);
		return out;
	}

	private static float CalcNameScale(String name) {
		float scale = HITBOX.width
				/ FontManager.getInstance().getWidth(FontManager.getInstance().getDefaultFont(), name);
		return Math.min(0.9f * scale, NAME_MAX_SCALE);
	}

	public HistoryElement setOptionalOne(EssenceSelection essences) {
		return this.setOptionalOne("misc/history_income.png", CreateEssenceText(essences));
	}

	public HistoryElement setOptionalOne(ImageDisplay image) {
		return this.setOptionalOne(image.getImage());
	}

	public HistoryElement setOptionalOne(String image) {
		return this.setOptionalOne(image, "");
	}

	public HistoryElement setOptionalOne(String image, String text) {
		this.opt1Image = image;
		this.opt1Text = text;
		return this;
	}

	public HistoryElement setOptionalTwo(ImageDisplay image) {
		return this.setOptionalTwo(image.getImage());
	}

	public HistoryElement setOptionalTwo(EssenceSelection essences) {
		return this.setOptionalTwo("misc/history_income.png", CreateEssenceText(essences));
	}

	public HistoryElement setOptionalTwo(String image) {
		return this.setOptionalTwo(image, "");
	}

	public HistoryElement setOptionalTwo(String image, String text) {
		this.opt2Image = image;
		this.opt2Text = text;
		return this;
	}

	@Override
	public void draw(Graphics g) {
		g.pushTransform();
		Color c = g.getColor();
		g.setColor(Color.white);
		GraphicUtils.translate(g, this.getCenter());
		g.setFont(this.fontName);
		GraphicUtils.drawStringCentered(g, this.scaledNamePosition, this.player);
		GraphicUtils.drawImageUndistorted(g, this.scaledMainHitbox,
				ResourceManager.getInstance().getImage(this.mainImage));
		if (this.overlay != null) {
			GraphicUtils.drawImage(g, this.scaledOverlayHitbox, ResourceManager.getInstance().getImage(this.overlay));
		}
		if (!this.mainText.isEmpty()) {
			g.setFont(this.fontMain);
			GraphicUtils.drawStringCentered(g, this.scaledMainHitbox.getCenter(), this.mainText);
		}
		if (!this.opt1Image.isEmpty()) {
			GraphicUtils.drawImageUndistorted(g, this.scaledOpt1Hitbox,
					ResourceManager.getInstance().getImage(this.opt1Image));
		}
		if (!this.opt1Text.isEmpty()) {
			g.setFont(this.font1);
			GraphicUtils.drawStringCentered(g, this.scaledOpt1Hitbox.getCenter(), this.opt1Text);
		}
		if (!this.opt2Image.isEmpty()) {
			GraphicUtils.drawImageUndistorted(g, this.scaledOpt2Hitbox,
					ResourceManager.getInstance().getImage(this.opt2Image));
		}
		if (!this.opt2Text.isEmpty()) {
			g.setFont(this.font2);
			GraphicUtils.drawStringCentered(g, this.scaledOpt2Hitbox.getCenter(), this.opt2Text);
		}
		this.drawBorder(g);
		g.setColor(c);
		g.popTransform();
	}

	public boolean hasOverlay() {
		return this.overlay != null;
	}

	public String getOverlay() {
		return this.overlay;
	}

	public Rectangle getOverlayHitbox() {
		return this.overlayHitbox;
	}

	public String getImage() {
		return this.mainImage;
	}

	public String getPlayerName() {
		return this.player;
	}

	public float getNameScale() {
		return this.nameScale;
	}

	public String getText() {
		return this.mainText;
	}

}
