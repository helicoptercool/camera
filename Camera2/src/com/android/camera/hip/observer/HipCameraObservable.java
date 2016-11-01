/**
 * 
 */

package com.android.camera.hip.observer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import com.android.camera.CameraLog;

/**
 * @author duzc
 */
public class HipCameraObservable {

    private final HashSet<HipCameraObserver> mObservers;
    private Iterator<HipCameraObserver> mIterator;

    public HipCameraObservable() {
        mObservers = new HashSet<HipCameraObserver>();
    }

    public void attach(HipCameraObserver observer) {
        CameraLog.e("HipCameraObservable", "refreshCamera   observer  " + " : " + observer);
        if (observer != null) {
            mObservers.add(observer);
        }
    }

    public void detach(HipCameraObserver observer) {
        mObservers.remove(observer);
    }
    public void detachAll() {
        mObservers.clear();
    }

    public void notifyObservers(int status) {
        CameraLog.e("HipCameraObservable","mObservers:" + mObservers + ",status:" + status);
        if(mObservers != null){
            CameraLog.e("HipCameraObservable","mObservers size:" + mObservers.size());
        }
        if (mObservers == null || mObservers.size() == 0)
            return;
        CameraLog.e("HipCameraObservable", "notifyObservers   mObservers  " + " : " + mObservers.size());
        mIterator = mObservers.iterator();
        try {
            while (mIterator.hasNext()) {
                mIterator.next().notifyCameraStatus(status);
            }
        } finally {
            mIterator = null;
        }
    }
}
