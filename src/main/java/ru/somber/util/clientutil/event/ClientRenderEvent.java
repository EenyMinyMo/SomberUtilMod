package ru.somber.util.clientutil.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import ru.somber.util.clientutil.PlayerPositionUtil;

/**
 * Класс для события рендера мира.
 * Сюда добавлять действия для событий утилитных класов.
 */
public class ClientRenderEvent {

    /**
     * Событие обновления утилитного класса позиций игрока.
     */
    @SubscribeEvent(
            //высочайший приоритет, чтобы позиции обновились до всех рендеров.
            priority = EventPriority.HIGHEST
    )
    public void renderEvent(RenderWorldLastEvent event) {
        PlayerPositionUtil.getInstance().updateRender(event.partialTicks);
    }

}
