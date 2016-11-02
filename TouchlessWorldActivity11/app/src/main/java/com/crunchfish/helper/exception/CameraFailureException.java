/**
 * Copyright (c) 2013 Crunchfish AB. All rights reserved. All information herein is
 * or may be trade secrets of Crunchfish AB.
 *
 * This code is written for evaluation and educational purposes only.
 * The code is not an integral part of the licensed software libraries for Touchless A3D SDK
 * and is provided "AS IS" and "AS AVAILABLE" with all faults and without warranty of any kind.
 * Crunchfish is only providing limited documentation and no support related to this code.
 * The code might change at any time.
 */

package com.crunchfish.helper.exception;

/**
 *  Exception that occurs when the Camera could not be
 *  setup correctly.
 */
public class CameraFailureException extends Exception {

    private static final long serialVersionUID = 1L;

    public CameraFailureException(Throwable throwable) {
        super(throwable);
    }

}
