package com.android.camera.hip.dragonfly;

/**
 * @author duzc
 */
public interface HipParamters {

    public static final int CAMERA_ID = 0;
    public static final String REQUEST_ACTION = "com.android.mycamera.command";
    public static final String RESULT_ACTION = "com.android.mycamera.command.result";
    public static final String ENNABLE_DETECT_ACTION = "com.android.camera.hip.dragonfly.detect.action";
    public static final String SURVEILLANCE_SHORT_ACTION = "com.android.surveillance.detect.short";
    public static final String COM_KEY = "command_key";
    public static final String TAKE_PICTURE = "take_picture";
    public static final String S_TAKE_PICTURE = "s_take_picture";
    public static final String RECORD = "record";
    public static final String S_RECORD = "s_record";
    public static final String GET_CAMERA_MODE = "get_camera_mode";
    public static final String SET_CAMERA_MODE = "set_camera_mode";
    public static final String GET_RECORD_STATE = "get_record_state";
    public static final String RECORD_STATE = "record_state";
    public static final String RECORDING_TIME = "recording_time";
    public static final String SET_PICTURE_LAPSE = "set_picture_lapse";
    public static final String GET_PICTURE_LAPSE = "get_picture_lapse";
    public static final String PICTURE_LAPSE = "picture_lapse";
    public static final String PICTURE_LAPSE_TIME = "picture_lapse_time";
    public static final String SET_PICTURE_BURST = "set_picture_burst";
    public static final String GET_PICTURE_BURST = "get_picture_burst";
    public static final String PICTURE_BURST = "picture_burst";
    public static final String PICTURE_BURST_RATE = "picture_burst_rate";
    public static final String SET_PICTURE_LAPSE_BURST = "set_picture_lapse_burst";
    public static final String GET_PICTURE_LAPSE_BURST = "get_picture_lapse_burst";
    public static final String PICTURE_LAPSE_BURST = "picture_lapse_burst";
    public static final String SET_VIDEO_LAPSE = "set_video_lapse";
    public static final String GET_VIDEO_LAPSE = "get_video_lapse";
    public static final String VIDEO_LAPSE = "video_lapse";
    public static final String VIDEO_LAPSE_TIME = "video_lapse_time";
    public static final String REMAINING_VIDEO = "remaining_video";
    public static final String REMAINING_PICTURE = "remaining_picture";
    public static final String SWITCH_MODE = "switch_mode";
    public static final String GENERAL = "general";
    public static final String STILL = "still";
    public static final String BASSTA = "bassta";
    
    public static final String GET_CAMERA_PRIVIEW = "get_camera_preview";
    public static final String GET_SWITCH_MODE ="get_switch_mode";

    public static final String SETTING = "settings";
    public static final String OFFSET = "offset";
    public static final String GET_OFFSET = "get_offset";
    public static final String VIDEOSETING = "video_setting";
    public static final String SET_AUTO_FLIP = "set_auto_flip";
    public static final String AUTO_FLIP = "auto_flip";
    public static final String GET_AUTO_FLIP = "get_auto_flip";
    public static final String MEMPIC = "mempic";
    public static final String MEMVIO = "memvio";
    public static final String GET_GPS = "get_gps";

    public static final String SET_PARAMETERS = "set_parameters";
    public static final String GET_PARAMETERS = "get_parameters";

    public static final String PICTURE_SIZE = "picture_size";
    public static final String VIDEO_SIZE = "video_size";
    public static final String PREVIEW_SIZE = "preview-size";
    public static final String WHITE_BALANCE = "white_balance";
    public static final String GPS = "gps";
    public static final String EXPOSURE = "exposure";
    public static final String COLOR_EFFECT = "color_effect";
    public static final String SWTMOD = "swtmod";

    public static final String KEY_MEMPIC = "mempic";
    public static final String KEY_MEMVIO = "memvid";

    public static final String PIC_LAPSE_ENABLE = "picture_lapse_enable";
    public static final String PIC_LAPSE_TIME = "picture_lapse_time";
    public static final String PIC_LAPSE_BURST_TIME = "picture_lapse_burst_time";
    public static final String VID_LAPSE_ENABLE = "video_lapse_enable";
    public static final String VID_LAPSE_TIME = "video_lapse_time";
    public static final String AUTO_FLIP_STATE = "Auto_flip_state";
    public static final String PIC_BURST_RATE = "picture_burst_rate";
    public static final String PIC_LAPSE_BURST_RATE = "picture_lapse_burst_rate";

    public static final String KEY_PICTURE_SIZE = "picture-size";
    public static final String KEY_VIDEO_SIZE = "video-size";
    public static final String KEY_CAPTURE_MODE = "capture-mode";
    public static final String KEY_PREVIEW_SIZE = "preview-size";

    public static final String PIC_SIZE_OPT_0 = "6480x1080";
    public static final String PIC_SIZE_OPT_1 = "4320x720";
    public static final String PIC_SIZE_OPT_2 = "3840x640";
    public static final String PIC_SIZE_OPT_3 = "2880x480";
    public static final String PIC_SIZE_OPT_4 = "1920x320";

    public static final String VIDEO_SIZE_OPT_1 = "3840x640";
    public static final String VIDEO_SIZE_OPT_2 = "2880x480";
    public static final String VIDEO_SIZE_OPT_3 = "1920x320";
    public static final String VIDEO_SIZE_OPT_0 = "6480x1080";

    public static final String PRE_SIZE_OPT_0 = "3840x640";
    public static final String PRE_SIZE_OPT_1 = "2880x480";
    public static final String PRE_SIZE_OPT_2 = "1920x320";

    // exposure 0: +2 1: +1 2: 0 3: -1 4: -2
    public static final int EX_OPT_0 = -2;
    public static final int EX_OPT_1 = -1;
    public static final int EX_OPT_2 = 0;
    public static final int EX_OPT_3 = 1;
    public static final int EX_OPT_4 = 2;

    // interval: 0:2 1: 5 2: 10 3:30 4: 60
    public static final int LAP_OPT_0 = 2 * 1000;
    public static final int LAP_OPT_1 = 5 * 1000;
    public static final int LAP_OPT_2 = 10 * 1000;
    public static final int LAP_OPT_3 = 30 * 1000;
    public static final int LAP_OPT_4 = 60 * 1000;

    public static final int OPT_0 = 0;
    public static final int OPT_1 = 1;
    public static final int OPT_2 = 2;
    public static final int OPT_3 = 3;
    public static final int OPT_4 = 4;
    public static final int OPT_5 = 5;
    public static final int OPT_6 = 6;
    public static final int OPT_7 = 7;
    public static final int OPT_INV = -1;

    // color effect 0:none, 1:mono, 2:sepia, 3:solarize, 4:negative,
    // 5:posterize, 6:whiteboard, 7:blackboard, 8:aqua, 9:emboss,10:sketch,
    // 11:neon
    public static final String EFFECT_OPT_0 = "none";
    public static final String EFFECT_OPT_1 = "mono";
    public static final String EFFECT_OPT_2 = "sepia";
    public static final String EFFECT_OPT_3 = "solarize";
    public static final String EFFECT_OPT_4 = "negative";
    public static final String EFFECT_OPT_5 = "posterize";
    public static final String EFFECT_OPT_6 = "whiteboard";
    public static final String EFFECT_OPT_7 = "blackboard";
    public static final String EFFECT_OPT_8 = "aqua";
    public static final String EFFECT_OPT_9 = "emboss";
    public static final String EFFECT_OPT_10 = "sketch";
    public static final String EFFECT_OPT_11 = "neon";

    public static final String WB_OPT_0 = "auto";
    public static final String WB_OPT_1 = "warm-fluorescent";// Tungsten
    public static final String WB_OPT_2 = "fluorescent";
    public static final String WB_OPT_3 = "daylight";// Sunny
    public static final String WB_OPT_4 = "cloudy-daylight";// Cloudy

    // request action intent
    public static final String CAMERA_ACTION = "com.android.camera.hip.dragonfly.action";
    public static final String REQUEST_ACTION_COMMAND = "com.android.hip.dragonfly.command";
    public static final String RESPONE_ACTION_COMMAND = "com.android.hip.dragonfly.command.result";
    public static final String CAMERA_START_ACTION = "com.android.camera.hip.dragonfly.start";
    public static final String VIDEO_STOP_RECORDING = "com.android.camera.hip.dragonfly.stop.recording";
    public static final String CAMERA_RESPONE_STATUS = "com.android.camera.hip.dragonfly.status";
    public static final String EVENT_CONTIF_CREATE = "com.hip.dragonfly.action.EVENT_CONTIF_CREATE";
    public static final String EVENT_CONTIF_DELETE = "com.hip.dragonfly.action.EVENT_CONTIF_DELETE";

    public static final int MEDIA_TYPE_FOLDER = 0;
    public static final int MEDIA_TYPE_PICTRUE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    // respone action intent
    public static final String HIP_DRAGONFLY_CAMERA_ACTION_MEMPIC = "com.hip.dragonfly.camera.action.EVENT_MEMPIC";
    public static final String HIP_DRAGONFLY_CAMERA_ACTION_MEMVIO = "com.hip.dragonfly.camera.action.EVENT_MEMVID";
    public static final String HIP_DRAGONFLY_CAMERA_ACTION_RECSTA= "com.hip.dragonfly.camera.action.EVENT_RECSTA";

    public static final String  HIP_CAMRRA_SWITCHMOD_EVENT = "com.hip.dragonfly.camera.action.EVENT_SWTMOD";
    public static final String HIP_EVENT_CONTIF_INIT = "com.hip.dragonfly.action.EVENT_CONTIF_INIT";

    public static final String HIP_CAMRRA_SERVER_ONREADY = "com.hip.dragonfly.action.camera.onready";

    // Camera mode
    public static final int INVLID_MODE = -0x01;
    public static final int CAMERA_MODE = 0x00;
    public static final int VIDEO_MODE = 0x01;
    public static final int PIC_BURST_MODE = 0x02;
    public static final int PIC_TIME_LAPSE_MODE = 0x03;
    public static final int PIC_BURST_TIME_LAPSE_MODE = 0x04;
    public static final int VID_TIME_LAPSE_MODE = 0x05;
    public static final int SURVEILLANCE_MODE = 0x06;

    // Camera Activity mode
    public static final int CAMERA_ACTIVITY_STATUS_START = 0X00;
    public static final int CAMERA_ACTIVITY_STATUS_PAUSE = 0X01;
    public static final int CAMERA_ACTIVITY_STATUS_ACTIVE = 0X02;
    public static final int CAMERA_ACTIVITY_STATUS_STOP = 0X03;
    public static final int CAMERA_ACTIVITY_STATUS_DESTROY = 0X04;
    
    public static final String MODE = "mode";
    public static final String START = "start";
    public static final String STOP = "stop";
    public static final String RESULT_KEY = "result";
    public static final String RESULT_OK = "ok";
    public static final String RESULT_FAIL = "fail";
    public static final String RESULT_ERROR = "error";
    public static final String RESULT_TRUE = "true";
    public static final String RESULT_FALSE = "false";

    public static final String STOP_PREVIEW = "stop_preview";
    public static final String START_PREVIEW = "start_preview";
    public static final String AUTO_STOP_PREVIEW = "auto_stop_preview";
    public static final String AUTO_START_PREVIEW = "auto_start_preview";
    public static final String CAMERA_LOGIN = "open_camera";
    public static final String CAMERA_LOGOUT = "close_camera";
    public static final String ZSL = "zsl";
    public static final String UNWRAP = "unwrap";
    public static final String AUTO_START = "auto_start";
    public static final String AUTO_PREVIEW = "auto_preview";
    public static final String COUNT_DOWN = "count_down";
    public static final String RECORDING = "recording";
    public static final String CAMERA_COMMAND = "command";
    public static final String MOTION_DETECTOR = "motion_detector";
    public static final String KEYS_MOTDET      = "motdet";

    public static final int NO_ERROR_CODE = -0x01; //invalid command
    public static final int ERROR_CODE0 = 0x00; //invalid command
    public static final int ERROR_CODE1 = 0x01; //during recording to stop privew
    public static final int ERROR_CODE2 = 0x02; //Camera device isn't opened
    public static final int ERROR_CODE3 = 0x03; //Lack of space
    public static final int ERROR_CODE4 = 0x04; //recording or taking pictrue, set command is invlid.
    public static final int ERROR_CODE5 = 0x05; //record command don't start ,record =true
    public static final int ERROR_CODE6 = 0x06; //invlid paramters
    public static final int ERROR_CODE7 = 0x07; //media record failture
    public static final int ERROR_CODE8 = 0x08; //survillance take_pictrue and record is invlid

    public static final int SPACE_MAX_SIZE = 0; // space max szie

    public static final int MEDIARECORDER_SPACE_MAX_SIZE = 150 * 1024 * 1024; // MediaRecorder space max szie

    public static final int SPACE_RESERVED_SIZE = 50 * 1024 * 1024; // space max
                                                                    // szie

    public static final long SPACE_MAX_VIDEO_SIZE_FAT32 = (long) (3.8 * 1024 * 1024 * 1024);

    public static final int LAP_COUNT_DOWN_STEP = 500;

    public static final int INVILD_PREFENCE_VALUE = 100;

    public static final long TAKE_PIC_AND_RECORD_INTERVAL = 1500;

    public static enum VIDEO_RECORD_STATE {
        NOT, START, ONINFO_CONTINUATION, STOP
    };

    // handler event
    public static final int UPDATE_RECORD_TIME = 1;
    public static final int TAKE_PICTURE_DALAY = 2;
    public static final int STOP_VIDEO_RCORDING = 3;
    public static final int STOP_PREVIEW_MSG = 4;
    public static final int START_PREVIEW_MSG = 5;
    public static final int START_VIDEO_RCORDING = 6;
    public static final int VIDEO_CORDING = 8;
    public static final int TOP_TRIGGER = 9;
    public static final int BOTTOM_TRIGGER = 10;
    public static final int TAKE_PICTURE_DALAY_BURST = 11;
    public static final int START_TAKE_PICTURE_LAPSE = 12;
    public static final int STOP_TAKE_PICTURE_LAPSE = 13;
    public static final int ENABLE_DETECT_COMMAND = 14;
    public static final int DISABLE_DETECT_COMMAND = 15;
    public static final int TAKE_PICTRUE_VIDEO_THUMB = 16;
    public static final int REFRSH_CAMERA_DEVICE = 17;
    public static final int STOP_CAMERA_DEVICE = 18;
    public static final int SET_ENABLE_CAMERA_ACTION = 19;
    public static final int TAKE_PICTRUE_CALIBRATION = 20;
    public static final int CAMERA_ACTION_DELAY = 23;
    public static final int CAMERA_ACTION_START = 24;
    public static final int SET_ENABLE_CAMERA_ACTION_RECORD = 27;

    public static final int EVENT_ONREADY = 0x01; // Camera 准备状态
    public static final int EVENT_TAKE_PICTURE_START = 0x02; // 触发快门按键
    public static final int EVENT_VIDEO_RECORD_START = 0x04; // 开始录像
    public static final int EVENT_VIDEO_RECORD_STOP = 0x05; // 停止录像
    public static final int EVENT_TAKE_PICTURE_START_ON_VIDEO_RECORD = 0x06;

    public static final int BT_STATUS_TAKE_PICTURE = 5; // 开始拍照
    public static final int BT_STATUS_VIDEO_RECORD_START = 6; // 开始录像
    public static final int BT_STATUS_VIDEO_RECORD_STOP = 7; // 停止录像

    public static final String PREF_KEY_AUDIO_TRIGGER     = "audio_tigger";
    public static final String PREF_KEY_AUDIO_LEVEL       = "audio_level";
    public static final String PREF_KEY_AUDIO_ACTION      = "audio_action";
    public static final String PREF_KEY_MOTION_TRIGGER    = "motion_tigger";
    public static final String PREF_KEY_MOTION_LEVEL      = "motion_level";
    public static final String PREF_KEY_MOTION_ACTION     = "motion_action";
    public static final String PREF_KEY_AUDIO_RECLEN      = "audio_reclen";
    public static final String PREF_KEY_MOTION_RECLEN     = "motion_reclen";
    public static final String PREF_KEY_AUDIO_INTERVAL    = "audio_interval";
    public static final String PREF_KEY_MOTION_INTERVAL   = "motion_interval";
    public static final String PREF_KEY_AUTDEL            = "autdel";
    public static final String PREF_KEY_MODE              = "detect_mode";
}
