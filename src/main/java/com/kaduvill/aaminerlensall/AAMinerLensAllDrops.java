package com.kaduvill.aaminerlensall;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = AAMinerLensAllDrops.MOD_ID, name = AAMinerLensAllDrops.MOD_NAME,
        version = AAMinerLensAllDrops.VERSION, acceptedMinecraftVersions = "[" + Tags.MC_VERSION + "]",
        dependencies = "required-after:actuallyadditions@[1.12.2],[1.12.2-r152,);required-after:jei@[4.15,5);",
        clientSideOnly = true, acceptableRemoteVersions = "*")
public final class AAMinerLensAllDrops {
    public static final String MOD_ID = "aaminerlensall";
    public static final String MOD_NAME = "AA Miner Lens - All Drops";
    public static final String VERSION = Tags.VERSION;
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
}
