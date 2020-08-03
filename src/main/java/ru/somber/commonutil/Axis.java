package ru.somber.commonutil;

/**
 * Перечисление для представления различных состояний,
 * связанных с осями координат.
 */
public enum Axis {

    /** Ось абсцисс. (ось OX) */
    ABSCISSA_AXIS,
    /** Ось ординат. (ось OY) */
    ORDINATE_AXIS,
    /** Ось аппликат. (ось OZ) */
    APPLICATE_AXIS,
    /** Все оси сразу. */
    ALL_AXIS,
    /** Не для какой из осей. (т.е. от оси не зависит) */
    NONE_AXIS;

}
