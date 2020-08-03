package ru.somber;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = SomberUtilMod.MOD_ID, name = SomberUtilMod.MOD_NAME, version = SomberUtilMod.VERSION)
public class SomberUtilMod {
    public static final String MOD_ID = "somber_util";
    public static final String MOD_NAME = "Util for Somber mod";
    public static final String VERSION = "0.0.1";


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {

    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        System.out.println("post init " + event.description());
    }

}
