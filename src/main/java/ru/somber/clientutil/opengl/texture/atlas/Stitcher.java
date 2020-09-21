package ru.somber.clientutil.opengl.texture.atlas;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.MathHelper;

import java.util.*;

@SideOnly(Side.CLIENT)
public class Stitcher {
    private final Set<Holder> stitchHolderSet = new HashSet<>(256);
    private final List<Slot> stitchSlotList = new ArrayList<>(256);

    /** Максимально возможная ширина. */
    private final int maxWidth;
    /** Максимально возможная высота. */
    private final int maxHeight;
    /** Max size (width or height) of a single tile. */
    private final int maxTileDimension;
    /** Нужно ли форсировать размеры отдельных текстур как степеней двойки. */
    private final boolean forcePowerOf2;

    /** Текущая ширина сшивателя атласа. */
    private int currentWidth;
    /** Текущая высота сшивателя атласа. */
    private int currentHeight;


    /**
     * Создает сшиватель для формирования текстурного атласа.
     *
     * @param maxWidth максимально возможная ширина будущего атласа.
     * @param maxHeight максимально возможная высота будущего атласа.
     * @param forcePowerOf2 нужно ли специально делать размеры отдельных текстур в атласа равными степеням двойки.
     * @param maxTileDimension максимально возможный размер и высоты отдельной текстуры в атласе.
     */
    public Stitcher(int maxWidth, int maxHeight, boolean forcePowerOf2, int maxTileDimension) {
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.forcePowerOf2 = forcePowerOf2;
        this.maxTileDimension = maxTileDimension;
    }

    /**
     * Возвращает текущую ширину будущего атласа.
     */
    public int getCurrentWidth() {
        return this.currentWidth;
    }

    /**
     * Возвращает текущую высота будущего атласа.
     */
    public int getCurrentHeight() {
        return this.currentHeight;
    }

    /**
     * Добавляет иконку для добавления в текстурный атлас.
     */
    public void addSprite(AtlasIcon particleIcon) {
        Holder holder = new Holder(particleIcon);

        if (maxTileDimension > 0) {
            holder.setNewDimension(maxTileDimension);
        }

        stitchHolderSet.add(holder);
    }

    /**
     * Производит сшивание атласа текстур.
     * Перед сшшиваением добавте в сшиватель все иконки.
     */
    public void doStitch() {
        Holder[] stitchHolderArray = stitchHolderSet.toArray(new Holder[stitchHolderSet.size()]);
        Arrays.sort(stitchHolderArray);

        for (Holder holder : stitchHolderArray) {
            if (! allocateSlot(holder)) {
                String exceptionMessage = String.format("Unable to fit: %s - size: %dx%d - Maybe try a lowerresolution resourcepack?", holder.getAtlasSprite().getIconName(), holder.getAtlasSprite().getIconWidth(), holder.getAtlasSprite().getIconHeight());
                throw new StitcherException(holder, exceptionMessage);
            }
        }

        if (forcePowerOf2) {
            this.currentWidth = MathHelper.roundUpToPowerOfTwo(this.currentWidth);
            this.currentHeight = MathHelper.roundUpToPowerOfTwo(this.currentHeight);
        }
    }

    /**
     * Возвращает лист слотов текстурного атласа.
     * Из слота сшивателя можно вытащить иконку и другие данные об ее положении в атласе.
     */
    public List<AtlasIcon> getStitchSlots() {
        ArrayList<Slot> arraylist = Lists.newArrayList();

        for (Slot stitchSlot : stitchSlotList) {
            stitchSlot.getAllStitchSlots(arraylist);
        }

        ArrayList<AtlasIcon> iconList = Lists.newArrayList();

        for (Slot stitchSlot : arraylist) {
            Holder holder = stitchSlot.getStitchHolder();
            AtlasIcon icon = holder.getAtlasSprite();
            icon.initSprite(currentWidth, currentHeight, stitchSlot.getOriginX(), stitchSlot.getOriginY(), holder.isRotated());
            iconList.add(icon);
        }

        return iconList;
    }

    /**
     * Пытается найти место в будущем атласе для переданного держателя иконки.
     */
    private boolean allocateSlot(Holder holder) {
        for (Slot slot : stitchSlotList) {
            if (slot.addSlot(holder)) {
                return true;
            }

            holder.rotate();

            if (slot.addSlot(holder)) {
                return true;
            }

            holder.rotate();
        }

        return this.expandAndAllocateSlot(holder);
    }

    /**
     * Расширият сшитую текстуру, чтобы освободить место для переданного держателя текстуры.
     */
    private boolean expandAndAllocateSlot(Holder holder) {
        int minHolderDimension = Math.min(holder.getWidth(), holder.getHeight());
        boolean flag = currentWidth == 0 && currentHeight == 0;
        boolean flag1;
        int maxHolderDimension;

        if (this.forcePowerOf2) {
            maxHolderDimension = MathHelper.roundUpToPowerOfTwo(currentWidth);
            int k = MathHelper.roundUpToPowerOfTwo(currentHeight);
            int l = MathHelper.roundUpToPowerOfTwo(currentWidth + minHolderDimension);
            int i1 = MathHelper.roundUpToPowerOfTwo(currentHeight + minHolderDimension);
            boolean flag2 = l <= maxWidth;
            boolean flag3 = i1 <= maxHeight;

            if (!flag2 && !flag3) {
                return false;
            }

            boolean flag4 = maxHolderDimension != l;
            boolean flag5 = k != i1;

            if (flag4 ^ flag5) {
                flag1 = !flag4;
            } else {
                flag1 = flag2 && maxHolderDimension <= k;
            }
        } else {
            boolean flag6 = currentWidth + minHolderDimension <= maxWidth;
            boolean flag7 = currentHeight + minHolderDimension <= maxHeight;

            if (!flag6 && !flag7) {
                return false;
            }

            flag1 = flag6 && (flag || currentWidth <= currentHeight);
        }

        maxHolderDimension = Math.max(holder.getWidth(), holder.getHeight());

        if (MathHelper.roundUpToPowerOfTwo((flag1 ? currentHeight : currentWidth) + maxHolderDimension) > (flag1 ? maxHeight : maxWidth)) {
            return false;
        } else {
            Slot slot;

            if (flag1) {
                if (holder.getWidth() > holder.getHeight()) {
                    holder.rotate();
                }

                if (currentHeight == 0) {
                    currentHeight = holder.getHeight();
                }

                slot = new Slot(currentWidth, 0, holder.getWidth(), currentHeight);
                currentWidth += holder.getWidth();
            } else {
                slot = new Slot(0, currentHeight, currentWidth, holder.getHeight());
                currentHeight += holder.getHeight();
            }

            slot.addSlot(holder);
            stitchSlotList.add(slot);
            return true;
        }
    }


    /**
     * Класс держателя иконки и дополнительных данных о ней.
     */
    @SideOnly(Side.CLIENT)
    public static class Holder implements Comparable<Holder> {
        /** Иконка, дополнительные данные о которой содержатся в объекте. */
        private final AtlasIcon particleIcon;

        /** Ширина самой иконки. */
        private final int width;
        /** Высота самой иконки. */
        private final int height;

        /** Хранится ли иконка в сшивателе (в последствии и в атласе текстур) в перевернутом виде. */
        private boolean rotated;
        /** Соотношение сторон у иконки. */
        private float scaleFactor = 1.0F;


        /**
         * Создает держатель иконки для переданной иконки.
         */
        public Holder(AtlasIcon icon) {
            this.particleIcon = icon;
            this.width = icon.getIconWidth();
            this.height = icon.getIconHeight();
            this.rotated = height > width;
        }

        /**
         * Возвращает соответствующиую иконку.
         */
        public AtlasIcon getAtlasSprite() {
            return particleIcon;
        }

        /**
         * Возвращает ширину иконки в будущем атласе текстур.
         * Возвращаемая ширина может отличаться от изначальной ширины иконки,
         * т.к. возвращаемая ширина учитвает перевернута ли иконка в текстурном атласе, изменено ли ее соотношение сторон.
         */
        public int getWidth() {
            return rotated ? (int) (height * scaleFactor) : (int) (width * scaleFactor);
        }

        /**
         * Возвращает высоту иконки в будущем атласе текстур.
         * Возвращаемая высота может отличаться от изначальной высоты иконки,
         * т.к. возвращаемая высота учитвает перевернута ли иконка в текстурном атласе, изменено ли ее соотношение сторон.
         */
        public int getHeight() {
            return rotated ? (int) (width * scaleFactor) : (int) (height * scaleFactor);
        }

        /**
         * Делает иконку в атласе текстур перевернутой.
         */
        public void rotate() {
            rotated = !rotated;
        }

        /**
         * Возвращает true, если соответствующая иконка в атласе текстур будет перевернута.
         */
        public boolean isRotated() {
            return rotated;
        }

        /**
         * Устанавливает новый размер сторон иконки в текстурном атласе.
         * (до конца не понял, но похоже размеры иконки в атласе всегда равны, не зависимо от изначального соотношения сторон)
         */
        public void setNewDimension(int newDimension) {
            if (width > newDimension && height > newDimension) {
                this.scaleFactor = (float) newDimension / Math.min(width, height);
            }
        }

        @Override
        public int compareTo(Holder otherHolder) {
            if (getHeight() == otherHolder.getHeight()) {   //в случае, когда высоты равны.
                if (getWidth() == otherHolder.getWidth()) { //в случае, когда ширины равны.
                    //если у какой то иконки нет названия, то она меньше.
                    //если у обеих иконок нет имени, то они равны.
                    if (this.particleIcon.getIconName() == null) {
                        return otherHolder.particleIcon.getIconName() == null ? 0 : -1;
                    }

                    //если у обеих иконок одинаковые размеры и есть имена, тогда сравниваем имена.
                    return particleIcon.getIconName().compareTo(otherHolder.particleIcon.getIconName());
                }

                //если ширины не равны, то большая будет у которой ширина меньше.
                return getWidth() < otherHolder.getWidth() ? 1 : -1;
            } else {
                //если высоты не равны, то большая будет у которой высота меньше.
                return getHeight() < otherHolder.getHeight() ? 1 : -1;
            }
        }

        @Override
        public String toString() {
            return "Holder{" +
                    "particleIcon=" + particleIcon +
                    ", width=" + width +
                    ", height=" + height +
                    ", rotated=" + rotated +
                    ", scaleFactor=" + scaleFactor +
                    '}';
        }

    }


    /**
     * Класс слота в сшивателе.
     * Слот может хранить один держатель иконки (если иконка имеет размер слота) или лист слотов,
     * размеры которых меньше размеров слота.
     */
    @SideOnly(Side.CLIENT)
    public static class Slot {
        /** Держатель иконки и ее данных в  */
        private Holder holder;
        /** Лист слотов меньшего размера, котрые хранятся в данном слоте. */
        private List<Slot> subSlots;

        /** Позиция начала текстурного слота в атласе по оси X в пикселях. */
        private final int originX;
        /** Позиция начала текстурного слота в атласе по оси Y в пикселях. */
        private final int originY;
        /** Ширина текстурного слота в атласе. */
        private final int width;
        /** Высота текстурного слота в атласе. */
        private final int height;


        public Slot(int originX, int originY, int width, int height) {
            this.originX = originX;
            this.originY = originY;
            this.width = width;
            this.height = height;
        }

        /**
         * Возвращает соответствующий держатель иконки.
         */
        public Holder getStitchHolder()
        {
            return holder;
        }

        /**
         * Возвращает начальную позицию X слота внутри сшивателя в пикселях.
         */
        public int getOriginX()
        {
            return originX;
        }

        /**
         * Возвращает начальную позицию Y слота внутри сшивателя в пикселях.
         */
        public int getOriginY()
        {
            return originY;
        }

        /**
         * Добавляет holder иконки на хранение и обработку в слот.
         * Если holder иконки с размером слота уже есть в слоте, то ничего не добавляется.
         * Если holder иконки с размерами слота нет, то holder иконки добавляется:
         *     -как holder иконки данного слота (если размеры иконки и слота равны)
         *     -как subSlot данного слота, если переданный holder имеет размеры меньше, чет сам слот.
         *
         * @param holder - holder, который нужно поместить в слот.
         *
         * @return true, если holder успешно помещен внутрь слота, иначе false.
         */
        public boolean addSlot(Holder holder) {
            if (this.holder != null) {
                //если holder у слота есть, то мы ничего не добавляем.
                return false;
            } else {
                //если holder'a у слота нет, то мы пытаемся добавить holder в слот.
                int width = holder.getWidth();
                int height = holder.getHeight();

                if (width <= this.width && height <= this.height) {
                    //если размеры holder'a меньше или равны, мы пытаемся добавить holder.
                    if (width == this.width && height == this.height) {
                        //если размеры holder'a равны размерам слота, то слот будет только один этот holder.
                        this.holder = holder;
                    } else {
                        //пытаемся добавить holder в subSlot.
                        if (this.subSlots == null) {
                            this.subSlots = new ArrayList<>(1);
                            this.subSlots.add(new Slot(this.originX, this.originY, width, height)); //добавляем сабслот с новыми размерами.
                            int deltaWidth = this.width - width;
                            int deltaHeight = this.height - height;

                            //добавляем еще один сабслот на пустое место, чтобы потом его использовать (или не использовать?)
                            //короче смысл в том, чтобы все простраснство слота было заполнено либо holder'ом, либо другими слотами.
                            if (deltaHeight > 0 && deltaWidth > 0) {
                                int i1 = Math.max(this.height, deltaWidth);
                                int j1 = Math.max(this.width, deltaHeight);

                                if (i1 >= j1) {
                                    this.subSlots.add(new Slot(this.originX, this.originY + height, width, deltaHeight));
                                    this.subSlots.add(new Slot(this.originX + width, this.originY, deltaWidth, this.height));
                                } else {
                                    this.subSlots.add(new Slot(this.originX + width, this.originY, deltaWidth, height));
                                    this.subSlots.add(new Slot(this.originX, this.originY + height, this.width, deltaHeight));
                                }
                            } else if (deltaWidth == 0) {
                                this.subSlots.add(new Slot(this.originX, this.originY + height, width, deltaHeight));
                            } else if (deltaHeight == 0) {
                                this.subSlots.add(new Slot(this.originX + width, this.originY, deltaWidth, height));
                            }
                        }

                        //проходимся по всем сабслотам в данном слоте и пытаемся пихнуть в них holder.
                        //если смогли пихнуть, то задача выполнена, иначе пытаемся пихнуть в следующий слот.
                        Iterator<Slot> iterator = this.subSlots.iterator();
                        Slot slot;

                        do {
                            if (! iterator.hasNext()) {
                                return false;
                            }

                            slot = iterator.next();
                        } while (! slot.addSlot(holder));

                    }

                    return true;
                } else {
                    return false;
                }
            }
        }

        /**
         * Записывает текующий слот или все слоты внутри этого слота в переданный лист.
         */
        public void getAllStitchSlots(List<Slot> stitchSlotList) {
            if (holder != null) {
                stitchSlotList.add(this);
            } else if (subSlots != null) {

                for (Slot slot : subSlots) {
                    slot.getAllStitchSlots(stitchSlotList);
                }
            }
        }

        @Override
        public String toString() {
            return "Slot{" +
                    "holder=" + holder +
                    ", subSlots=" + subSlots +
                    ", originX=" + originX +
                    ", originY=" + originY +
                    ", width=" + width +
                    ", height=" + height +
                    '}';
        }

    }

}
