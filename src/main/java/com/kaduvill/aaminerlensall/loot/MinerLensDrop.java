package com.kaduvill.aaminerlensall.loot;

import net.minecraft.item.ItemStack;

public final class MinerLensDrop {
    private final ItemStack stack;
    private final double probability;

    public MinerLensDrop(ItemStack stack, double probability) {
        this.stack = stack.copy();
        this.stack.setCount(1);
        // LensMining passes metadata to block placement, never the ore stack's NBT.
        this.stack.setTagCompound(null);
        this.probability = probability;
    }

    public ItemStack getStack() { return stack.copy(); }
    public double getProbability() { return probability; }
}
