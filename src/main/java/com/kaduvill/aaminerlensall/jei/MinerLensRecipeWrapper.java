package com.kaduvill.aaminerlensall.jei;

import com.kaduvill.aaminerlensall.loot.MinerLensDrop;
import com.kaduvill.aaminerlensall.loot.MinerLensDrops.Snapshot;
import com.kaduvill.aaminerlensall.loot.MinerLensType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;

public final class MinerLensRecipeWrapper implements IRecipeWrapper {
    private static final int HIDDEN_POSITION = -10000;
    private static final int PAGE_BUTTON_WIDTH = 18;
    // Hardcoded by LensMining#getDistance in Actually Additions r152.
    private static final int BEAM_RANGE = 10;
    // JEI draws every registered slot, even off-screen. Keep its ingredient for
    // output helpers, but skip the expensive item-model render on hidden pages.
    private static final IIngredientRenderer<ItemStack> HIDDEN_RENDERER = (minecraft, x, y, stack) -> { };
    private final Snapshot snapshot;
    private final ItemStack lens;
    private final List<ItemStack> inputs;
    private IGuiItemStackGroup itemStacks;
    private MinerLensRecipeLayout layout;
    private int page;

    public MinerLensRecipeWrapper(MinerLensType type, Snapshot snapshot) {
        this.snapshot = snapshot;
        this.lens = type.getLens().copy();
        this.inputs = copyStacks(type.getInputVariants());
    }

    public List<MinerLensDrop> getDrops() {
        return snapshot.getDrops();
    }

    @Override
    public void getIngredients(IIngredients ingredients) {
        ingredients.setInputLists(VanillaTypes.ITEM,
                Arrays.asList(Collections.singletonList(lens.copy()), copyStacks(inputs)));
        List<ItemStack> outputs = new ArrayList<>(getDrops().size());
        for (MinerLensDrop drop : getDrops()) outputs.add(drop.getStack());
        // Every concrete product remains indexed, including those on later display pages.
        ingredients.setOutputs(VanillaTypes.ITEM, outputs);
    }

    @Override
    public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
        if (layout == null) return;
        FontRenderer font = minecraft.fontRenderer;
        String info = I18n.format("jei.aaminerlensall.range", BEAM_RANGE);
        font.drawString(info, (recipeWidth - font.getStringWidth(info)) / 2, layout.infoY + 1, 0x404040);
        if (layout.footerY < 0) return;
        int textY = layout.footerY + (MinerLensRecipeLayout.FOOTER_HEIGHT - font.FONT_HEIGHT) / 2;
        String pageText = I18n.format("jei.aaminerlensall.page", page + 1, layout.pageCount(getDrops().size()));
        font.drawString(pageText, (recipeWidth - font.getStringWidth(pageText)) / 2, textY, 0x606060);
        font.drawString("<", (PAGE_BUTTON_WIDTH - font.getStringWidth("<")) / 2, textY,
                page > 0 ? 0x404040 : 0xA0A0A0);
        font.drawString(">", MinerLensRecipeLayout.WIDTH - (PAGE_BUTTON_WIDTH + font.getStringWidth(">")) / 2,
                textY, page + 1 < layout.pageCount(getDrops().size()) ? 0x404040 : 0xA0A0A0);
    }

    @Override
    public List<String> getTooltipStrings(int mouseX, int mouseY) {
        if (layout == null || mouseX < 0 || mouseX >= MinerLensRecipeLayout.WIDTH
                || mouseY < layout.infoY || mouseY >= layout.infoY + MinerLensRecipeLayout.INFO_HEIGHT)
            return Collections.emptyList();
        return Collections.singletonList(I18n.format("jei.aaminerlensall.range_tooltip"));
    }

    @Override
    public boolean handleClick(Minecraft minecraft, int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0 || layout == null || layout.footerY < 0
                || mouseY < layout.footerY || mouseY >= layout.footerY + MinerLensRecipeLayout.FOOTER_HEIGHT)
            return false;
        if (mouseX >= 0 && mouseX < PAGE_BUTTON_WIDTH) {
            setPage(page - 1);
            return true;
        }
        if (mouseX >= MinerLensRecipeLayout.WIDTH - PAGE_BUTTON_WIDTH && mouseX < MinerLensRecipeLayout.WIDTH) {
            setPage(page + 1);
            return true;
        }
        return false;
    }

    void bindLayout(IGuiItemStackGroup itemStacks, MinerLensRecipeLayout layout) {
        this.itemStacks = itemStacks;
        this.layout = layout;
        this.page = Math.max(0, Math.min(page, layout.pageCount(getDrops().size()) - 1));
        itemStacks.init(0, true, MinerLensRecipeLayout.LENS_X, MinerLensRecipeLayout.LENS_Y);
        itemStacks.set(0, lens.copy());
        itemStacks.init(1, true, MinerLensRecipeLayout.INPUT_X, MinerLensRecipeLayout.INPUT_Y);
        itemStacks.set(1, copyStacks(inputs));
        int first = page * layout.pageSize;
        int last = Math.min(first + layout.pageSize, getDrops().size());
        for (int index = 0; index < getDrops().size(); index++)
            initSlot(index, index >= first && index < last ? index - first : -1);
    }

    private void setPage(int newPage) {
        if (newPage == page || newPage < 0 || newPage >= layout.pageCount(getDrops().size())) return;
        int oldPage = page;
        page = newPage;
        positionPage(oldPage, false);
        positionPage(page, true);
    }

    private void positionPage(int targetPage, boolean visible) {
        int first = targetPage * layout.pageSize;
        int last = Math.min(first + layout.pageSize, getDrops().size());
        for (int index = first; index < last; index++) initSlot(index, visible ? index - first : -1);
    }

    private void initSlot(int index, int visibleIndex) {
        int x = visibleIndex < 0 ? HIDDEN_POSITION : layout.outputX(visibleIndex);
        int y = visibleIndex < 0 ? HIDDEN_POSITION : layout.outputY(visibleIndex);
        // Permanent slot IDs preserve every output for whole-filter copy helpers.
        int slotIndex = index + MinerLensRecipeLayout.OUTPUT_OFFSET;
        if (visibleIndex < 0)
            itemStacks.init(slotIndex, false, HIDDEN_RENDERER, x, y, 18, 18, 1, 1);
        else itemStacks.init(slotIndex, false, x, y);
        itemStacks.set(slotIndex, getDrops().get(index).getStack());
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) copies.add(stack.copy());
        return copies;
    }
}
