package com.linuxpara.agles20tutorials.camera.widget.video;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLExt;
import android.opengl.EGLSurface;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.linuxpara.agles20tutorials.camera.widget.CameraDrawer;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;


/**
 * Date: 2018/3/22
 * *************************************************************
 * Auther: 陈占洋
 * *************************************************************
 * Email: zhanyang.chen@gmail.com
 * *************************************************************
 * Description: 视频编码
 */

public class VideoEncoder extends HandlerThread {

    private static final String TAG = "VideoEncoder";

    private static final String H264 = MediaFormat.MIMETYPE_VIDEO_AVC;
    private static final int MSG_ENCODE_START = 0;
    private static final int MSG_ENCODE_FRAME = 1;
    private static final int MSG_ENCODE_EOS_FRAME = 2;
    private static final int MSG_ENCODE_RELEASE = 3;

    private static int FRAME_RATE = 30;//30fps
    private static int FRAME_INTERVAL = 5;

    private MediaCodec mEncoder;
    private MediaMuxer mMuxer;
    private Handler mHandler;
    private int mTrackIdx = -1;
    private boolean mMuxerStarted;
    private EncoderConfig mEncoderConfig;
    private EGLDisplay mEGLDisplay;
    private EGLSurface mEGLSurface;
    private WeakReference<CameraDrawer> mWeakRefCameraDrawer;


    public VideoEncoder(String name) {
        super(name);
        start();
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleAction(msg);
            }
        };
    }

    public VideoEncoder(String name, int priority) {
        super(name, priority);
        start();
        mHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleAction(msg);
            }
        };
    }

    private void handleAction(Message msg) {
        switch (msg.what) {
            case MSG_ENCODE_START:
                EncoderConfig encoderConfig = (EncoderConfig) msg.obj;
                mEncoder = createEncoder(encoderConfig);
                try {
                    mMuxer = new MediaMuxer(encoderConfig.getOutFile().getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Surface inputSurface = mEncoder.createInputSurface();
                createEGLEnvironment(inputSurface, encoderConfig);
                mEncoder.start();
                break;
            case MSG_ENCODE_FRAME:
                //在此EGL环境中绘制预览
                if (mWeakRefCameraDrawer != null && mWeakRefCameraDrawer.get() != null){
                    mWeakRefCameraDrawer.get().drawPreview();
                }
                //给当前帧添加时间戳
                long timestamp = (long) msg.obj;
                EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, timestamp);
                EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
                //编码帧数据
                encodeFrame(false);
                break;
            case MSG_ENCODE_EOS_FRAME:
                long timestamp_eos = (long) msg.obj;
                EGLExt.eglPresentationTimeANDROID(mEGLDisplay, mEGLSurface, timestamp_eos);
                EGL14.eglSwapBuffers(mEGLDisplay, mEGLSurface);
                //编码结束帧数据
                encodeEOSFrame();
                break;
            case MSG_ENCODE_RELEASE:
                releaseEncoder();
                break;
        }
    }

    /**
     * 设置编码配置
     *
     * @param encoderConfig
     */
    public void setEncoderConfig(EncoderConfig encoderConfig) {
        mEncoderConfig = encoderConfig;
    }

    /**
     * 创建解码器
     *
     * @param encoderConfig
     * @return
     */
    private MediaCodec createEncoder(EncoderConfig encoderConfig) {
        MediaFormat format = MediaFormat.createVideoFormat(H264, encoderConfig.getWidth(), encoderConfig.getHeight());
        //设置颜色格式从surface中获取
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //设置码率
        format.setInteger(MediaFormat.KEY_BIT_RATE, encoderConfig.getBitRate());
        //设置帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE);
        //设置I帧间隔
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, FRAME_INTERVAL);

        try {
            //根据MIME类型创建媒体编解码器
            MediaCodec encoder = MediaCodec.createEncoderByType(H264);
            //配置编解码器，从未初始化状态切换到初始化状态。
            //format 编码时为输出格式，解码时为输入格式
            //surface 如果不是raw输出、不是一个解码器，或者想配置为ByteBuffer时传入null
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            return encoder;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 创建EGL环境
     *
     * @param surface
     * @param encoderConfig
     */
    private void createEGLEnvironment(Surface surface, EncoderConfig encoderConfig) {
        //-------------------------eglDisplay--------------------------
        mEGLDisplay = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
        int[] version = new int[2];
        if (!EGL14.eglInitialize(mEGLDisplay, version, 0, version, 1)) {
            throw new RuntimeException("eglInitialize 初始化EGLDisplay失败!");
        }
        Log.i(TAG, "createEGLEnvironment: EGL Version = " + version[0] + "." + version[1]);
        //-------------------------eglConfig--------------------------
        EGLConfig eglConfig;
        int[] config_attrib_list = {
                EGL14.EGL_RED_SIZE, 8,
                EGL14.EGL_GREEN_SIZE, 8,
                EGL14.EGL_BLUE_SIZE, 8,
                EGL14.EGL_ALPHA_SIZE, 8,
                EGL14.EGL_DEPTH_SIZE, 16,
                EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                EGL14.EGL_NONE
        };
        EGLConfig[] configs = new EGLConfig[1];
        int[] num_config = new int[1];
        if (!EGL14.eglChooseConfig(mEGLDisplay, config_attrib_list, 0,
                configs, 0, configs.length,
                num_config, 0)) {
            throw new RuntimeException("eglChooseConfig 获取EGLConfig失败!");
        } else {
            if (num_config[0] == 0) {
                throw new RuntimeException("eglChooseConfig 获取EGLConfig失败，没有符合条件的EGLConfig");
            } else {
                eglConfig = configs[0];
            }
        }
        //-------------------------eglSurface--------------------------
        int[] surface_attrib_list = {
//                EGL14.EGL_WIDTH, encoderConfig.getWidth(),
//                EGL14.EGL_HEIGHT, encoderConfig.getHeight(),
                EGL14.EGL_NONE
        };
        mEGLSurface = EGL14.eglCreateWindowSurface(mEGLDisplay, eglConfig,
                surface, surface_attrib_list, 0);
        if (mEGLSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("eglCreateWindowSurface 创建EGLSurface失败!");
        }
        //-------------------------eglContext--------------------------
        int[] context_attrib_list = {
                EGL14.EGL_CONTEXT_CLIENT_VERSION, 2,
                EGL14.EGL_NONE
        };
        EGLContext eglContext = EGL14.eglCreateContext(mEGLDisplay, eglConfig, encoderConfig.getEGLContext(),
                context_attrib_list, 0);
        if (eglContext == EGL14.EGL_NO_CONTEXT) {
            throw new RuntimeException("eglCreateContext 创建EGLContext失败!");
        }


        EGL14.eglMakeCurrent(mEGLDisplay, mEGLSurface, mEGLSurface, eglContext);
    }

    /**
     * 编码一帧数据
     */
    private void encodeFrame(boolean isEOS) {
        ByteBuffer[] outputBuffers = mEncoder.getOutputBuffers();
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (true) {
            int status = mEncoder.dequeueOutputBuffer(bufferInfo, 10000);
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                Log.i(TAG, "encodeFrame: try-again-later");
                if (!isEOS) {
                    break;
                } else {
                    Log.i(TAG, "encodeFrame: 等待结束帧");
                }
            } else if (status == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                Log.i(TAG, "encodeFrame: output-buffers-changed " + status);
                outputBuffers = mEncoder.getOutputBuffers();
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                Log.i(TAG, "encodeFrame: output-format-changed " + status);
                if (mMuxerStarted) {
                    throw new RuntimeException("正在录制视频时，视频格式发生了改变!");
                }
                MediaFormat encodeFormat = mEncoder.getOutputFormat();
                mTrackIdx = mMuxer.addTrack(encodeFormat);
                mMuxer.start();
                mMuxerStarted = true;
            } else if (status < 0) {
                Log.i(TAG, "encodeFrame: 未知状态!");
            } else {
                //获取编码完成的数据
                ByteBuffer encodeData = outputBuffers[status];
                if (encodeData == null) {
                    throw new RuntimeException("编码器输出为空!");
                }
                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                    Log.i(TAG, "encodeFrame: BUFFER_FLAG_CODEC_CONFIG 配置数据，忽略此Buffer " + status);
                    bufferInfo.size = 0;
                }
                if (bufferInfo.size != 0) {
                    if (!mMuxerStarted) {
                        throw new RuntimeException("muxer 未开启!");
                    }
                    encodeData.position(bufferInfo.offset);
                    encodeData.limit(bufferInfo.offset + bufferInfo.size);
                    //将编码完成的数据写入到文件中
                    mMuxer.writeSampleData(mTrackIdx, encodeData, bufferInfo);
                    Log.i(TAG, "encodeFrame: 写入数据 " + bufferInfo.size + " bytes to muxer, 时间戳 = " + bufferInfo.presentationTimeUs);
                }
                //释放buffer
                mEncoder.releaseOutputBuffer(status, false);

                if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                    if (!isEOS) {
                        Log.i(TAG, "encodeFrame: 非正常结束帧！");
                    } else {
                        Log.i(TAG, "encodeFrame: 结束录制! " + status);
                    }
                    break;
                }
            }
        }
    }

    private void encodeEOSFrame() {
        Log.i(TAG, "encodeEOSFrame: sending EOS to encoder");
        if (mEncoder != null) {
            mEncoder.signalEndOfInputStream();
            encodeFrame(true);
        }
        mHandler.sendEmptyMessageDelayed(MSG_ENCODE_RELEASE,50);
    }

    /**
     * 释放编码器
     */
    private void releaseEncoder() {
        if (mEncoder != null) {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        }
        if (mMuxer != null) {
            mMuxer.stop();
            mMuxer.release();
            mMuxer = null;
        }
    }

    /**
     * 开始捕获视频
     *
     * @param outFile
     */
    public void startCaptureVideo(File outFile) {
        mEncoderConfig.setOutFile(outFile);
        Message msg = Message.obtain();
        msg.what = MSG_ENCODE_START;
        msg.obj = mEncoderConfig;
        mHandler.sendMessage(msg);
    }

    /**
     * 捕获一帧数据
     *
     * @param timestamp
     */
    public void CaptureFrame(long timestamp) {
        Message msg = Message.obtain();
        msg.what = MSG_ENCODE_FRAME;
        msg.obj = timestamp;
        mHandler.sendMessage(msg);
    }

    /**
     * 捕获结束帧数据
     *
     * @param timestamp
     */
    public void CaptureEOSFrame(long timestamp) {
        Message msg = Message.obtain();
        msg.what = MSG_ENCODE_EOS_FRAME;
        msg.obj = timestamp;
        mHandler.sendMessage(msg);
    }

    /**
     * 设置绘制效果的类
     * @param cameraDrawer
     */
    public void setCameraDrawer(CameraDrawer cameraDrawer) {
        mWeakRefCameraDrawer = new WeakReference<>(cameraDrawer);
    }

    /**
     * 编码配置
     */
    public static class EncoderConfig {

        private int mWidth;
        private int mHeight;
        private int mBitRate;
        private EGLContext mEGLContext;
        private File mOutFile;

        public EncoderConfig(int width, int height, int bitRate, EGLContext eglContext) {
            this.mWidth = width;
            this.mHeight = height;
            this.mBitRate = bitRate;
            this.mEGLContext = eglContext;
        }

        /**
         * 获取视频宽
         *
         * @return
         */
        public int getWidth() {
            return mWidth;
        }

        /**
         * 获取视频高
         *
         * @return
         */
        public int getHeight() {
            return mHeight;
        }

        /**
         * 获取编码率
         *
         * @return
         */
        public int getBitRate() {
            return mBitRate;
        }

        /**
         * 获取EGL上下文
         *
         * @return
         */
        public EGLContext getEGLContext() {
            return mEGLContext;
        }

        /**
         * 设置视频存储路径
         *
         * @param mOutFile
         */
        public void setOutFile(File mOutFile) {
            this.mOutFile = mOutFile;
        }

        /**
         * 获取视频存储路劲
         *
         * @return
         */
        public File getOutFile() {
            return mOutFile;
        }
    }

}
