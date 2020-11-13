package ru.somber.util;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SomberUtilMod.MOD_ID, name = SomberUtilMod.MOD_NAME, version = SomberUtilMod.MOD_VERSION)
public class SomberUtilMod {
    public static final String MOD_ID = "somber_util";
    public static final String MOD_NAME = "Util for Somber mod";
    public static final String MOD_VERSION = "0.0.1";

    @SidedProxy(clientSide = "ru.somber.util.ClientProxy",
            serverSide = "ru.somber.util.ServerProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }

}
