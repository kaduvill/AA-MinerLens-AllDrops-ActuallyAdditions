package com.kaduvill.aaminerlensall.loot;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.api.recipe.WeightedOre;
import de.ellpeck.actuallyadditions.mod.config.values.ConfigStringListValues;
import de.ellpeck.actuallyadditions.mod.recipe.CrusherRecipeRegistry;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

/** Reads the final r152 tables without invoking the unbounded gameplay selection loop. */
public final class MinerLensDrops {
    private MinerLensDrops() { }

    public static Snapshot read(MinerLensType type) {
        List<WeightedOre> source = new ArrayList<>(type == MinerLensType.STONE
                ? ActuallyAdditionsAPI.STONE_ORES : ActuallyAdditionsAPI.NETHERRACK_ORES);
        // Copy mutable weights as well as the list before resolving any registrations.
        List<Entry> entries = new ArrayList<>(source.size());
        int total = 0;
        for (WeightedOre ore : source) {
            if (ore == null || ore.name == null || ore.itemWeight < 0)
                throw new IllegalArgumentException("Malformed mining-lens weighted entry");
            total = Math.addExact(total, ore.itemWeight);
            entries.add(new Entry(ore.name, ore.itemWeight));
        }
        String[] blacklist = ConfigStringListValues.MINING_LENS_BLACKLIST.getValue().clone();
        if (total == 0) return new Snapshot(Collections.emptyList(), total, 0);

        Map<String, ItemStack> resolved = new HashMap<>();
        Map<OutputKey, MutableDrop> grouped = new LinkedHashMap<>();
        long acceptedWeight = 0;
        long noResultWeight = 0;
        for (Entry entry : entries) {
            if (entry.weight == 0) continue;
            if (!resolved.containsKey(entry.name)) resolved.put(entry.name, resolve(entry.name, blacklist));
            ItemStack candidate = resolved.get(entry.name);
            if (candidate == null) continue; // AA retries these entries indefinitely.
            acceptedWeight += entry.weight;
            if (Block.getBlockFromItem(candidate.getItem()) == Blocks.AIR) {
                // AA stops retrying as soon as it has an eligible ItemBlock, even AIR-backed ones.
                noResultWeight += entry.weight;
                continue;
            }
            // The standard placement implementation decodes item metadata into
            // a state. Canonicalize it so wildcard/invalid metadata does not
            // expand into impossible JEI variants or split identical outputs.
            ItemStack display = placedDisplay(candidate);
            OutputKey key = new OutputKey(display);
            MutableDrop drop = grouped.get(key);
            if (drop == null) grouped.put(key, new MutableDrop(display, entry.weight));
            else drop.add(entry.weight);
        }
        List<MinerLensDrop> drops = new ArrayList<>(grouped.size());
        for (MutableDrop drop : grouped.values()) {
            drops.add(new MinerLensDrop(drop.stack, drop.weight / (double) acceptedWeight));
        }
        return new Snapshot(drops, total,
                acceptedWeight == 0 ? 0 : noResultWeight / (double) acceptedWeight);
    }

    private static ItemStack resolve(String name, String[] blacklist) {
        for (ItemStack stack : OreDictionary.getOres(name, false)) {
            if (stack != null && StackUtil.isValid(stack)
                    && !CrusherRecipeRegistry.hasBlacklistedOutput(stack, blacklist)
                    && stack.getItem() instanceof ItemBlock) return stack.copy();
        }
        return null;
    }

    private static ItemStack placedDisplay(ItemStack candidate) {
        Block block = Block.getBlockFromItem(candidate.getItem());
        IBlockState state = block.getStateFromMeta(candidate.getMetadata());
        ItemStack display = new ItemStack(block, 1, block.getMetaFromState(state));
        return display.isEmpty() ? candidate.copy() : display;
    }

    private static final class Entry {
        final String name;
        final int weight;
        Entry(String name, int weight) { this.name = name; this.weight = weight; }
    }

    private static final class OutputKey {
        final Item item;
        final int metadata;
        OutputKey(ItemStack stack) { item = stack.getItem(); metadata = stack.getItemDamage(); }
        @Override public boolean equals(Object other) {
            if (!(other instanceof OutputKey)) return false;
            OutputKey key = (OutputKey) other;
            return item == key.item && metadata == key.metadata;
        }
        @Override public int hashCode() { return 31 * System.identityHashCode(item) + metadata; }
    }

    private static final class MutableDrop {
        final ItemStack stack;
        long weight;
        MutableDrop(ItemStack stack, int weight) {
            this.stack = stack;
            this.weight = weight;
        }
        void add(int additionalWeight) {
            weight += additionalWeight;
        }
    }

    public static final class Snapshot {
        private final List<MinerLensDrop> drops;
        private final int totalWeight;
        private final double noResultProbability;

        private Snapshot(List<MinerLensDrop> drops, int totalWeight, double noResultProbability) {
            this.drops = Collections.unmodifiableList(new ArrayList<>(drops));
            this.totalWeight = totalWeight;
            this.noResultProbability = noResultProbability;
        }

        public static Snapshot empty() { return new Snapshot(Collections.emptyList(), 0, 0); }
        public List<MinerLensDrop> getDrops() { return drops; }
        public int getTotalWeight() { return totalWeight; }
        public double getNoResultProbability() { return noResultProbability; }
        public boolean isEmpty() { return drops.isEmpty(); }
    }
}
