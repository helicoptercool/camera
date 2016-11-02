
package com.crunchfish.touchless_a3d.gesture;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * This class represents a swipe. A swipe has a direction.
 */
public class Swipe extends Identifiable {
    private final Direction mDirection;

    /**
     * Direction of a swipe.
     */
    public enum Direction {
        SWIPE_LEFT,
        SWIPE_RIGHT;

        @Override
        public String toString() {
            switch(this) {
                case SWIPE_LEFT:
                    return "LEFT";
                case SWIPE_RIGHT:
                    return "RIGHT";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    Swipe(String id, Direction direction) {
        super(Identifiable.Type.SWIPE, id);
        mDirection = direction;
    }

    /**
     * Get the direction of the swipe.
     *
     * @return The direction.
     */
    public Direction getDirection() {
        return mDirection;
    }

    @Override
    public String toString() {
        return new StringBuilder("Swipe{id='")
                .append(getId())
                .append("', direction=")
                .append(mDirection)
                .append("}")
                .toString();
    }
}
