package com.example.administrator.takepicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
    private String savePath = "/testPic/";
    private Bundle bundle = null;
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
                    camera.takePicture(null, null, new MyPictureCallback());
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
    //保存至SD卡
    public void saveToSDCard(byte[] data) throws IOException {
        if(data!=null){
            Toast.makeText(MainActivity.this, "拍照成功，正在上传。。。", Toast.LENGTH_SHORT).show();
            Bitmap b = byteToBitmap(data);
            Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight() );
            //生成文件
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
            String filename = format.format(date) + ".jpg";
            File fileFolder = new File(Environment.getExternalStorageDirectory()
                    + savePath);
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"rujiaowang"的目录
                fileFolder.mkdir();
            }
            File jpgFile = new File(fileFolder, filename);
            FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close(); // 关闭输出流

            //照片路径
            //Environment.getExternalStorageDirectory() + savePath + filename
            Intent intent = new Intent(MainActivity.this,ImageActivity.class);
            intent.putExtra("imgurl",Environment.getExternalStorageDirectory()
                    + savePath + filename);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "拍照失败!", Toast.LENGTH_SHORT).show();
        }
    }

    public void saveToRoot(byte[] data) throws IOException {
        //剪切为正方形
        if(data!=null) {
            Toast.makeText(MainActivity.this, "拍照成功，正在上传。。。", Toast.LENGTH_SHORT).show();
            Bitmap b = byteToBitmap(data);
            Bitmap bitmap = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight());
            //生成文件
            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); // 格式化时间
            String filename = format.format(date) + ".jpg";
            File fileFolder = new File(Environment.getRootDirectory()
                    + savePath);
            if (!fileFolder.exists()) { // 如果目录不存在，则创建一个名为"finger"的目录
                fileFolder.mkdir();
            }
            File jpgFile = new File(fileFolder, filename);
            FileOutputStream outputStream = new FileOutputStream(jpgFile); // 文件输出流
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close(); // 关闭输出流

            //照片路径
            //Environment.getExternalStorageDirectory() + savePath + filename
            Intent intent = new Intent(MainActivity.this,ImageActivity.class);
            intent.putExtra("imgurl",Environment.getRootDirectory()
                    + savePath + filename);
            startActivity(intent);
        }else{
            Toast.makeText(MainActivity.this, "拍照失败!", Toast.LENGTH_SHORT).show();
        }

    }

    //byte转bitmap
    private Bitmap byteToBitmap(byte[] data){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap b = BitmapFactory.decodeByteArray(data, 0, data.length,options);
        int i = 0;
        while (true) {
            if ((options.outWidth >> i <= 1000)
                    && (options.outHeight >> i <= 1000)) {
                options.inSampleSize = (int) Math.pow(2.0D, i);
                options.inJustDecodeBounds = false;
                b = BitmapFactory.decodeByteArray(data, 0, data.length,options);
                break;
            }
            i += 1;
        }
        return b;

    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                bundle = new Bundle();
                bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换
                if (bundle == null) {
                    Toast.makeText(MainActivity.this, "bundle is null",
                            Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (isHaveSDCard())
                            saveToSDCard(bundle.getByteArray("bytes"));
                        else
                            saveToRoot(bundle.getByteArray("bytes"));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        Log.e("error1", e.getMessage());
                        e.printStackTrace();
                    }
                }
                camera.startPreview(); // 拍完照后，重新开始预览
            } catch (Exception e) {
                Log.e("error", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        surfaceView.getHolder().addCallback(new SurfaceCallback());
    }
}
