
package com.crunchfish.touchless_a3d.gesture;

/**
 * Copyright (c) 2014 Crunchfish AB. All rights reserved. All information herein
 * is or may be trade secrets of Crunchfish AB.
 * <p/>
 * Identifiable is an abstract base class for the identifiable objects in an
 * {@link Event}.
 */
public abstract class Identifiable {

    /**
     * Enumeration representing the different possible derived types of this
     * class.
     */
    public enum Type {
        POSE,
        SWIPE;

        @Override
        public String toString() {
            switch(this) {
                case POSE:
                    return "POSE";
                case SWIPE:
                    return "SWIPE";
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    private final Type mType;
    private final String mId;

    protected Identifiable(Type type, String id) {
        mType = type;
        mId = id;
    }

    /**
     * Get the derived type of this instance.
     *
     * @return The type of instance.
     */
    public final Type getType() {
        return mType;
    }

    /**
     * Get the identifier of this instance as described in the JSON description
     * of the gesture.
     *
     * @return Id of the object
     */
    public final String getId() {
        return mId;
    }

    @Override
    public String toString() {
        return new StringBuilder("Identifiable{id='")
                .append(mId)
                .append("', type=")
                .append(mType)
                .append("}")
                .toString();
    }
}
