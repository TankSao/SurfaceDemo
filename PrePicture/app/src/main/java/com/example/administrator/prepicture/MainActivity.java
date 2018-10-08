package com.example.administrator.prepicture;

import android.content.Intent;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;
    @BindView(R.id.take_pic)
    Button takePicBtn;
    private Camera camera;
    private boolean takePic = false;//是否拍照
    private String savePath = "/testPic/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initView();
        initListener();
    }

    private void initListener() {
        takePicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camera != null) {
                    takePic = true;
                } else {
                    Toast.makeText(MainActivity.this, "相机初始化失败，请联系管理员!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initView() {
        surfaceView.getHolder()
                .setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.getHolder().setFixedSize(150, 150); // 设置Surface分辨率
        surfaceView.getHolder().setKeepScreenOn(true);// 屏幕常亮
    }
    /**
     * 重构相机照相回调类
     *
     * @author pc
     */
    private final class SurfaceCallback implements SurfaceHolder.Callback {

        @SuppressWarnings("deprecation")
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            try {
                try {
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                }catch (Exception ee){
                    camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }// 打开摄像头
                camera.setPreviewDisplay(holder); // 设置用于显示拍照影像的SurfaceHolder对象
                camera.setDisplayOrientation(0);//0\90\180\270
                camera.setPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        try {
                            if (takePic) {
                                int w = camera.getParameters().getPreviewSize().width;
                                int h = camera.getParameters().getPreviewSize().height;
                                if (isHaveSDCard()) {
                                    saveToSD(w, h, data);
                                } else {
                                    saveToRoot(w, h, data);
                                }
                            }
                        } catch (Exception ee) {
                        } finally {
                            takePic = false;
                        }
                    }
                });
                camera.startPreview(); // 开始预览
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            // TODO Auto-generated method stub
            if (camera != null) {
                holder.removeCallback(this);
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.lock();
                camera.release(); // 释放照相机
                camera = null;

            }
        }

    }

    //判断是否有SD卡
    public static boolean isHaveSDCard() {
        return Environment.MEDIA_MOUNTED.equals(Environment
                .getExternalStorageState());
    }
    //预览保存
    public void saveToSD(int w, int h, byte[] data) {
        FileOutputStream outStream = null;
        //图片保存到sdcard
        try {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
            String filename = format.format(date) + ".jpg";
            File fileFolder = new File(Environment.getExternalStorageDirectory()
                    + savePath);
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"rujiaowang"的目录
                fileFolder.mkdir();
            }
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos);

            outStream = new FileOutputStream(Environment.getExternalStorageDirectory()
                    + savePath + filename);
            outStream.write(baos.toByteArray());
            outStream.close();
            Intent intent = new Intent(MainActivity.this,ImageActivity.class);
            intent.putExtra("imgurl",Environment.getExternalStorageDirectory()
                    + savePath + filename);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveToRoot(int w, int h, byte[] data) {
        FileOutputStream outStream = null;
        //图片保存到root
        try {
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
            String filename = format.format(date) + ".jpg";
            File fileFolder = new File(Environment.getExternalStorageDirectory()
                    + savePath);
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"rujiaowang"的目录
                fileFolder.mkdir();
            }
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, w, h, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, w, h), 100, baos);

            outStream = new FileOutputStream(Environment.getRootDirectory()
                    + savePath + filename);
            outStream.write(baos.toByteArray());
            outStream.close();
            Intent intent = new Intent(MainActivity.this,ImageActivity.class);
            intent.putExtra("imgurl",Environment.getRootDirectory()
                    + savePath + filename);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.getHolder().addCallback(new SurfaceCallback());
    }

}
