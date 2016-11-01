
package com.android.camera.hip.dragonfly;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author gaoyl
 */
public class CameraSettings implements Parcelable {
    /** */
    public int cammod;

    /** */
    public boolean camflp;

    /** */
    public int picres;

    /** */
    public int picexp;

    /** */
    public int picwbl;

    /** */
    public int piccef;

    /** */
    public int picburRate;

    /** */
    public int pictlpInterval;

    /** */
    public int pictwbInterval;

    /** */
    public int pictwbRate;

    /** */
    public int vdores;

    /** */
    public int vdowbl;

    /** */
    public int vdocef;

    /** */
    public int vdotlpInterval;

    /** */
    public int svlmod;

    /** */
    public boolean autdel;

    /** */
    public boolean auddetTrigger;

    /** */
    public int auddetLevel;

    /** */
    public int auddetMode;

    /** */
    public int auddetReclen;

    /** */
    public int auddetInterval;

    /** */
    public boolean motdetTrigger;

    /** */
    public int motdetLevel;

    /** */
    public int motdetMode;

    /** */
    public int motdetReclen;

    /** */
    public int motdetInterval;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        // camera
        dest.writeInt(cammod);
        dest.writeInt(camflp ? 1 : 0);
        dest.writeInt(picres);
        dest.writeInt(picexp);
        dest.writeInt(picwbl);
        dest.writeInt(piccef);
        dest.writeInt(picburRate);
        dest.writeInt(pictlpInterval);
        dest.writeInt(pictwbInterval);
        dest.writeInt(pictwbRate);
        dest.writeInt(vdores);
        dest.writeInt(vdowbl);
        dest.writeInt(vdocef);
        dest.writeInt(vdotlpInterval);
        // surveillance
        dest.writeInt(svlmod);
        dest.writeInt(autdel ? 1 : 0);
        dest.writeInt(auddetTrigger ? 1 : 0);
        dest.writeInt(auddetLevel);
        dest.writeInt(auddetMode);
        dest.writeInt(auddetReclen);
        dest.writeInt(auddetInterval);
        dest.writeInt(motdetTrigger ? 1 : 0);
        dest.writeInt(motdetLevel);
        dest.writeInt(motdetMode);
        dest.writeInt(motdetReclen);
        dest.writeInt(motdetInterval);
    }

    /**
     * @param source Parcel
     */
    public void readFromParcel(Parcel source) {
        // camera
        cammod = source.readInt();
        camflp = source.readInt() != 0;
        picres = source.readInt();
        picexp = source.readInt();
        picwbl = source.readInt();
        piccef = source.readInt();
        picburRate = source.readInt();
        pictlpInterval = source.readInt();
        pictwbInterval = source.readInt();
        pictwbRate = source.readInt();
        vdores = source.readInt();
        vdowbl = source.readInt();
        vdocef = source.readInt();
        vdotlpInterval = source.readInt();
        // surveillance
        svlmod = source.readInt();
        autdel = source.readInt() != 0;
        auddetTrigger = source.readInt() != 0;
        auddetLevel = source.readInt();
        auddetMode = source.readInt();
        auddetReclen = source.readInt();
        auddetInterval = source.readInt();
        motdetTrigger = source.readInt() != 0;
        motdetLevel = source.readInt();
        motdetMode = source.readInt();
        motdetReclen = source.readInt();
        motdetInterval = source.readInt();
    }

    /** */
    public static final Creator<CameraSettings> CREATOR = new Parcelable.Creator<CameraSettings>() {
        @Override
        public CameraSettings createFromParcel(Parcel source) {
            CameraSettings settings = new CameraSettings();
            settings.readFromParcel(source);
            return settings;
        }

        @Override
        public CameraSettings[] newArray(int size) {
            return new CameraSettings[size];
        }
    };
}
