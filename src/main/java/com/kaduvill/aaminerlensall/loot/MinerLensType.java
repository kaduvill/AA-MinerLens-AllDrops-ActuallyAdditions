package com.kaduvill.aaminerlensall.loot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.Block;
import net.minecraft.block.BlockNetherrack;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** AA chooses its pool by block class, independently of dimension and ore dictionary. */
public enum MinerLensType {
    STONE("stone"),
    NETHERRACK("netherrack");

    private final String name;

    MinerLensType(String name) {
        this.name = name;
    }

    public String getUid() {
        return "aaminerlensall." + name;
    }

    public String getTitleKey() {
        return "jei.aaminerlensall.category." + name;
    }

    public ItemStack getLens() {
        Item lens = Item.getByNameOrId("actuallyadditions:item_mining_lens");
        return lens == null ? ItemStack.EMPTY : new ItemStack(lens);
    }

    public List<ItemStack> getInputVariants() {
        List<ItemStack> variants = new ArrayList<>();
        Map<Item, Set<Integer>> seen = new HashMap<>();
        for (Block block : ForgeRegistries.BLOCKS.getValuesCollection()) {
            if (!(this == STONE ? block instanceof BlockStone : block instanceof BlockNetherrack)) continue;
            for (IBlockState state : block.getBlockState().getValidStates()) {
                ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
                if (stack.isEmpty()) continue;
                if (seen.computeIfAbsent(stack.getItem(), item -> new HashSet<>()).add(stack.getItemDamage()))
                    variants.add(stack);
            }
        }
        return variants;
    }
}
