package com.kaduvill.aaminerlensall.jei;

import com.kaduvill.aaminerlensall.loot.MinerLensDrop;
import com.kaduvill.aaminerlensall.loot.MinerLensType;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public final class MinerLensRecipeCategory implements IRecipeCategory<MinerLensRecipeWrapper> {
    private final MinerLensType type;
    private final MinerLensRecipeLayout layout;
    private final IDrawable background;
    private final IDrawable slot;
    private final IDrawableStatic arrowBackground;
    private final IDrawableAnimated arrow;

    public MinerLensRecipeCategory(IGuiHelper guiHelper, MinerLensType type, int dropCount) {
        this.type = type;
        this.layout = new MinerLensRecipeLayout(dropCount);
        this.background = guiHelper.createBlankDrawable(MinerLensRecipeLayout.WIDTH, layout.height);
        this.slot = guiHelper.getSlotDrawable();
        ResourceLocation texture = new ResourceLocation("jei", "textures/gui/gui_vanilla.png");
        this.arrowBackground = guiHelper.createDrawable(texture, 24, 132, 24, 17);
        this.arrow = guiHelper.createAnimatedDrawable(guiHelper.createDrawable(texture, 82, 128, 24, 17),
                200, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return type.getUid();
    }

    @Override
    public String getTitle() {
        return I18n.format(type.getTitleKey());
    }

    @Override
    public String getModName() {
        return I18n.format("jei.aaminerlensall.mod_name");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        slot.draw(minecraft, MinerLensRecipeLayout.INPUT_X, MinerLensRecipeLayout.INPUT_Y);
        for (int index = 0; index < layout.pageSize; index++)
            slot.draw(minecraft, layout.outputX(index), layout.outputY(index));
        GlStateManager.pushMatrix();
        try {
            // Rotate both textures so the progress animation runs down from the lens.
            GlStateManager.translate((MinerLensRecipeLayout.WIDTH + arrowBackground.getHeight()) / 2.0F,
                    MinerLensRecipeLayout.ARROW_Y, 0);
            GlStateManager.rotate(90, 0, 0, 1);
            arrowBackground.draw(minecraft, 0, 0);
            arrow.draw(minecraft, 0, 0);
        } finally {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, MinerLensRecipeWrapper recipe, IIngredients ingredients) {
        IGuiItemStackGroup items = recipeLayout.getItemStacks();
        recipe.bindLayout(items, layout);
        List<MinerLensDrop> drops = recipe.getDrops();
        items.addTooltipCallback((slotIndex, input, ingredient, tooltip) -> {
            if (input) {
                if (slotIndex == 0) tooltip.add(I18n.format("jei.aaminerlensall.lens"));
                return;
            }
            int dropIndex = slotIndex - MinerLensRecipeLayout.OUTPUT_OFFSET;
            if (dropIndex < 0 || dropIndex >= drops.size()) return;
            MinerLensDrop drop = drops.get(dropIndex);
            tooltip.add(I18n.format("jei.aaminerlensall.chance", formatPercentage(drop.getProbability())));
        });
    }

    static String formatPercentage(double probability) {
        double percentage = probability * 100;
        if (percentage > 0 && percentage < 0.1)
            return BigDecimal.valueOf(probability).movePointRight(2)
                    .round(new MathContext(2, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString();
        return String.format(Locale.ROOT, "%.1f", percentage);
    }
}
