package com.kaduvill.aaminerlensall.jei;

import com.kaduvill.aaminerlensall.AAMinerLensAllDrops;
import com.kaduvill.aaminerlensall.loot.MinerLensDrops;
import com.kaduvill.aaminerlensall.loot.MinerLensDrops.Snapshot;
import com.kaduvill.aaminerlensall.loot.MinerLensType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public final class MinerLensJeiPlugin implements IModPlugin {
    private final Map<MinerLensType, Snapshot> snapshots = new EnumMap<>(MinerLensType.class);

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        snapshots.clear();
        for (MinerLensType type : MinerLensType.values()) {
            Snapshot snapshot;
            try {
                snapshot = MinerLensDrops.read(type);
            } catch (RuntimeException | LinkageError failure) {
                AAMinerLensAllDrops.LOGGER.warn("Mining-lens JEI page disabled for {}: cannot read the installed table",
                        type, failure);
                snapshot = Snapshot.empty();
            }
            snapshots.put(type, snapshot);
            registry.addRecipeCategories(new MinerLensRecipeCategory(registry.getJeiHelpers().getGuiHelper(),
                    type, snapshot.getDrops().size()));
        }
    }

    @Override
    public void register(IModRegistry registry) {
        Item machine = Item.getByNameOrId("actuallyadditions:block_atomic_reconstructor");
        for (MinerLensType type : MinerLensType.values()) {
            Snapshot snapshot = snapshots.get(type);
            if (snapshot == null || snapshot.isEmpty()) continue;
            registry.addRecipes(Collections.singletonList(new MinerLensRecipeWrapper(type, snapshot)), type.getUid());
            ItemStack lens = type.getLens();
            if (!lens.isEmpty()) registry.addRecipeCatalyst(lens, type.getUid());
            if (machine != null) registry.addRecipeCatalyst(new ItemStack(machine), type.getUid());
        }
    }
}
