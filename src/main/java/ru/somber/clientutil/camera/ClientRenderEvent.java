package ru.somber.clientutil.camera;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderWorldEvent;

public class ClientRenderEvent {

    @SubscribeEvent
    public void renderEvent(RenderWorldEvent.Pre event) {
        CameraPositionUtil.getInstance().updateRender();
    }

}
