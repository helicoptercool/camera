package com.android.surveillance;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ActivityThread;
import android.content.pm.IPackageManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.StatFs;
import android.util.Log;
import android.util.Slog;

import com.android.surveillance.SurveillanceService.ServiceHandler;
import com.android.surveillance.detector.Worker;

public class DiskMonitor implements Worker {
    private static final boolean DEBUG = true;
    private static final String TAG = "Surveillance:DiskMonitor";

    volatile private boolean mStorageIsLow = false;
    private Context mContext;
    private static final int DEFAULT_CHECK_INTERVAL = 1000; //1000 ms

    private Handler mHandler;// = new MyHandler();

    public DiskMonitor(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
    }

    private void deleteOldFiles() {
        if ( DEBUG ) {
            Log.d(TAG, "begin delete files.");
        }

//        DeviceStorageMonitorService dsm = (DeviceStorageMonitorService)
//                ServiceManager.getService(DeviceStorageMonitorService.SERVICE);
        final IPackageManager pm = ActivityThread.getPackageManager();

        while ( mStorageIsLow ) {
            File f = getOldest();
            if ( f == null ) {
                if ( DEBUG ) {
                    Log.v(TAG, "No file it.");
                }
                break;
            }

            if (f.isFile() && f.exists() /*&& f.getName().endsWith(SurveillanceService.VIDEO_FILE_EXTENSION)*/) {
                if ( DEBUG ) { 
                    Log.d(TAG, "delete:"+f.getName());
                }
                f.delete();
            }

            try {
                if ( !pm.isStorageLow() ) {
                    break;
                }
            } catch (RemoteException e) {
                if ( DEBUG ) {
                    Log.e(TAG, "deleteOldFiles()", e);
                }
                break;
            }
        }

        if ( DEBUG ) {
            Log.d(TAG, "end delete");
        }
    }

    private File getOldest() {
 //     List<File> files = Arrays.asList(new File("/storage/sdcard1"+SurveillanceService.PATH_VIDEO).listFiles());
        List<File> files = Arrays.asList(new File("/storage/sdcard1/"+Environment.DIRECTORY_DCIM).listFiles());
        Log.d(TAG, "getOldest() files.size:"+files.size());
        if ( files == null ) {
            return null;
        } else if ( files.size() == 1 ) {
            return files.get(0);
        } else {
            Collections.sort(files, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    if (o1.isDirectory() && o2.isFile()) {
                        return -1;
                    } else if (o1.isFile() && o2.isDirectory()) {
                        return 1;
                    } else {
                        return (int)(o1.lastModified() - o2.lastModified());
                    }
                }
            });
        }
        Log.d(TAG, "getOldest():"+files.get(0).getName());
        return files.get(0);
    }
    private final static long MEMORY_BUFFER_SIZE = 150 *(1024 * 1024);     // 50M
    protected boolean isLowMemory() {
        File path = new File("/storage/sdcard1");//Environment.getExternalStorageDirectory(); 
        //File path =  Environment.getExternalStorageDirectory();
        Log.d(TAG,"File path = " + path.getPath());
        StatFs statfs = new StatFs(path.getPath());  
//        StatFs statfs = new StatFs(SurveillanceService.PATH_VIDEO);
//        long value = DEFAULT_THRESHOLD_PERCENTAGE;
//        long totalMemory = ((long) statfs.getBlockCount() * statfs
//                .getBlockSize()) / 100L;
//        long thresholdMemory = totalMemory * value + MEMORY_BUFFER_SIZE;
//        statfs.restat(SurveillanceService.PATH_VIDEO);
        long availMemory = (long) statfs.getAvailableBlocks()
                * statfs.getBlockSize();
        Log.d(TAG,"availMemory = " + availMemory + " MEMORY_BUFFER_SIZE:"+MEMORY_BUFFER_SIZE);
        return availMemory < MEMORY_BUFFER_SIZE;
    }

    @Override
    public void start() {
        Log.d(TAG,"DiskMonitor start()");
        checkMemory();
    }

    @Override
    public void stop() {
        Log.d(TAG, "DiskMonitor stop()");
        mHandler.removeMessages(SurveillanceService.EVENT_CHECK_MEMORY);
    }

    public final void checkMemory() {
        boolean b = isLowMemory();
        Log.d(TAG, "DiskMonitor checkMemory:" + b);
          if( b ){
              mStorageIsLow = true;
              deleteOldFiles(); 
          }else{
        	  mStorageIsLow = false;
          }
          mHandler.removeMessages(SurveillanceService.EVENT_CHECK_MEMORY);
          mHandler.sendEmptyMessageDelayed(SurveillanceService.EVENT_CHECK_MEMORY, DEFAULT_CHECK_INTERVAL);
	}
}
