package ru.somber;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import ru.somber.clientutil.event.ClientRenderEvent;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private ClientRenderEvent renderEvent;

    public ClientProxy() {
        super();

        renderEvent = new ClientRenderEvent();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        MinecraftForge.EVENT_BUS.register(renderEvent);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

    }

}

