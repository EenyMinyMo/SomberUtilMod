package ru.somber;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.MinecraftForge;
import ru.somber.clientutil.camera.ClientRenderEvent;
import ru.somber.clientutil.camera.ClientUpdateEvent;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {

    private ClientUpdateEvent updateEvent;
    private ClientRenderEvent renderEvent;

    public ClientProxy() {
        super();

        updateEvent = new ClientUpdateEvent();
        renderEvent = new ClientRenderEvent();
    }

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        FMLCommonHandler.instance().bus().register(updateEvent);
        MinecraftForge.EVENT_BUS.register(renderEvent);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);

    }

}

