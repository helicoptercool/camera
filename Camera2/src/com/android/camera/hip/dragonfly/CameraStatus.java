
package com.android.camera.hip.dragonfly;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author gaoyl
 */
public class CameraStatus implements Parcelable {
    /** */
    public int swtmod;

    /** */
    public int mempic;

    /** */
    public int memvid;

    /** */
    public boolean recstaRec;

    /** */
    public int recstaTime;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(swtmod);
        dest.writeInt(mempic);
        dest.writeInt(memvid);
        dest.writeInt(recstaRec ? 1 : 0);
        dest.writeInt(recstaTime);
    }

    /**
     * @param source Parcel
     */
    public void readFromParcel(Parcel source) {
        swtmod = source.readInt();
        mempic = source.readInt();
        memvid = source.readInt();
        recstaRec = source.readInt() != 0;
        recstaTime = source.readInt();
    }

    /** */
    public static final Creator<CameraStatus> CREATOR = new Parcelable.Creator<CameraStatus>() {
        @Override
        public CameraStatus createFromParcel(Parcel source) {
            CameraStatus status = new CameraStatus();
            status.readFromParcel(source);
            return status;
        }

        @Override
        public CameraStatus[] newArray(int size) {
            return new CameraStatus[size];
        }
    };
}
