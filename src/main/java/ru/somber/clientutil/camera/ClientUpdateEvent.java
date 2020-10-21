package ru.somber.clientutil.camera;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

public class ClientUpdateEvent {

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            CameraPositionUtil.getInstance().updateTick();
        }
    }

}
