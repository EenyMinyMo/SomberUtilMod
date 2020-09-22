package ru.somber.clientutil.textureatlas.stitcher;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class StitcherException extends RuntimeException {
    private final Stitcher.Holder holder;

    public StitcherException(Stitcher.Holder holder, String exceptionMessage) {
        super(exceptionMessage);
        this.holder = holder;
    }

}