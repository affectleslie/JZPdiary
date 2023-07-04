package Fann.dairy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;



//添加照片：添加照片的功能键√ 点击之后 从系统取得照片后 显示在 日记本的日记内容中 √
// 并将选中的照片复制到日记本应用程序的内部存储目录下，将照片文件名保存入数据库中与对应的日记记录关联在一起 √
//拍照：拍照的功能键√  将照片显示时将其等比例缩小√ 拍照后将所得照片存在内部存储目录√。
//打开某条日记时，通过文件名去内部存储目录下查找对应照片 并将其显示在Imageview √

// 删除日记时除了删除数据库中的文件名之外，还得删除内部路径下的文件
//编辑时，点击照片可以选择是否删除还是替换，替换则会选择从相机拍摄还是从图库选择
//替换后，图片显示区先被清空，再被修改，点击确认后会将数据库的图片名修改，√
//编辑时，点击提交，如果无图片，则会将图片名置空√
//显示时 如果无照片 则不会显示√
//照片单独用一个ImageView显示√
public class DiaryProcess extends AppCompatActivity {
    private EditText Title;
    private EditText Date;
    private EditText Author;
    private EditText Content;
    private FloatingActionButton submit;
    private FloatingActionButton delete;
    private FloatingActionButton photo;
    private DiaryDB dbHelper;
    private ImageView photoView;
    private Uri imageUri;
    private int photoNum=0;

    private Bitmap bitmapToSave;
    private String bitmapName;
    public static final int TAKE_PHOTO=1;
    public static final int SELECT_PHOTO=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newdiary);
        StrictMode.VmPolicy.Builder builder0 = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder0.build());
        builder0.detectFileUriExposure();
        //调用Diary数据库
        dbHelper=new DiaryDB(this,"Diary01.db",null,1);
        dateSelector();//初始化日期选择器
        //初始化各布局
        Title=findViewById(R.id.title_input);
        Date=findViewById(R.id.date_input);
        Author=findViewById(R.id.author_input);
        Content=findViewById(R.id.content_input);
        submit=findViewById(R.id.submit);
        delete=findViewById(R.id.delete);
        photo=findViewById(R.id.photo);
        photoView=findViewById(R.id.photoView);
        //读取SharedPreferences中的作者信息
        SharedPreferences prefs=getSharedPreferences("author",MODE_PRIVATE);
        Author.setFocusable(false);//作者信息只能在信息界面修改
        String ProcessInfo=getIntent().getStringExtra("intent_type");//从主活动传递过来的处理信息 修改日记或者新增日记
        //如果是新增日记的操作则不允许删除功能 且作者名使用默认的名字
        if(ProcessInfo.equals("newDiary")) {
            Author.setText(prefs.getString("name","User"));
            delete.setEnabled(false);
        }
        ArrayList<String> diaryInfo1 = new ArrayList<String>();
        //如果处理信息为修改日记，那么就将传过来的日记数据显示
        if(ProcessInfo.equals("editDiary")) {
            diaryInfo1 = getIntent().getStringArrayListExtra("diaryInfo");
            if (!diaryInfo1.isEmpty()) {
                Title.setText(diaryInfo1.get(1));
                Date.setText(diaryInfo1.get(2));
                Author.setText(diaryInfo1.get(3));
                Content.setText(diaryInfo1.get(4));

                //如果日记有照片 则通过照片名打开照片并将其转化为bitmap并bitmapName修改然后显示在photoview
                if(!diaryInfo1.get(5).isEmpty())
                {
                    Bitmap bitmap=null;
                    try {
                        FileInputStream in=openFileInput(diaryInfo1.get(5));
                        bitmap=BitmapFactory.decodeStream(in);
                        bitmapName=diaryInfo1.get(5);
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    photoView.setImageBitmap(bitmap);
                }

            }
            //如果点击作者栏修改 则自动修改为当前作者信息
            Author.setOnClickListener(v->{
                Author.setText(prefs.getString("name","User"));
            });

            //如果点击图片，则展示菜单栏，选择是否删除图片
            photoView.setOnClickListener(v->
            {
                showMenu();
            });
        }

        SQLiteDatabase db=dbHelper.getWritableDatabase();

        submit.setOnClickListener(v-> {

            //必须将每一栏信息填满，加入照片是可选的
            if(!noBlanks())
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("输入不能有空");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

                builder.show();
                return;
            }

            ContentValues values=new ContentValues();
            //将新增的日记信息加入数据库
            if(ProcessInfo.equals("newDiary"))
            {
                values.put("title",EtoS(Title.getText()));
                values.put("date",EtoS(Date.getText()));
                values.put("author",EtoS(Author.getText()));
                values.put("content",EtoS(Content.getText()));

                //将所拍的照片的缩略图保存，并将文件名存入数据库
                if(photoView.getDrawable()!=null){
                    savePhoto(bitmapToSave);
                    values.put("photoName",bitmapName);
                }
                else
                    values.put("photoName","");//如果输入区没有图片，则置空
                db.insert("Diary01",null,values);
                values.clear();
                finish();
            }

            //将后的编辑日记信息更新到数据库中
            if(ProcessInfo.equals("editDiary"))
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("确认是否修改日记");

                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        builder.show();

                        String id=getIntent().getStringArrayListExtra("diaryInfo").get(0);

                        values.put("title",EtoS(Title.getText()));
                        values.put("date",EtoS(Date.getText()));
                        values.put("author",EtoS(Author.getText()));
                        values.put("content",EtoS(Content.getText()));
                        //如果此时没有图片，即在编辑时将图片删除了，则将对应数据库记录的照片名置空
                        if(photoView.getDrawable()==null)
                            values.put("photoName","");

                        //否则如果图片被修改了,这里的修改是得到相册或相机所传文件后修改的，由bitmapName是否改变表示
                        else if(!getIntent().getStringArrayListExtra("diaryInfo").get(5).equals(bitmapName))
                        {
                            savePhoto(bitmapToSave);
                            values.put("photoName",bitmapName);//将所拍的照片的缩略图保存，并将文件名存入数据库
                        }
                        db.update("Diary01",values,"id=?",new String[]{id});
                        values.clear();
                        finish();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.show();
            }
        });

        delete.setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确认是否删除日记");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    //将当前日记信息从数据库删除
                    String id=getIntent().getStringArrayListExtra("diaryInfo").get(0);
                    db.delete("Diary01","id=?",new String[]{id});
                    finish();
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
        });
        photo.setOnClickListener(v->{
            //从相册中选取图片显示
            select_photo();
        });
    }

    //选取照片 并显示的函数
    /*public void select_photo() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);//选取相册中的照片
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");//获取所有图片信息
        if (intent.resolveActivity(getPackageManager()) != null)//判断是否有符合条件的活动
            startActivityForResult(intent, SELECT_PHOTO);
    }*/
    public void select_photo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择照片来源");
        builder.setItems(new CharSequence[]{"相册", "相机"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    // 从相册中选取照片
                    Intent intent = new Intent(Intent.ACTION_PICK);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, SELECT_PHOTO);
                } else {
                    // 调用相机拍摄照片
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, TAKE_PHOTO);
                    } else {
                        Toast.makeText(getApplicationContext(), "无法启动相机", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //将相册所选取的照片转化为bitmap再将其缩小显示
        switch (requestCode){
            case SELECT_PHOTO:
                if(resultCode==RESULT_OK) {
                    if (data != null) {
                        Bitmap bitmap = null;
                        Uri uri = data.getData();
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        //将bitmap缩小并将所得bitmap赋值给bitmapToSave
                        bitmapToSave=Zoom(bitmap);
                    }
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Bundle extras = data.getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            // 保存图片到本地
                            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File imageFile = new File(storageDir, (System.currentTimeMillis())+".jpg");

                            try {
                                // 创建输出流，将照片保存到文件
                                FileOutputStream outputStream = new FileOutputStream(imageFile);
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                                outputStream.flush();
                                outputStream.close();
                                Toast.makeText(getApplicationContext(), "照片已保存", Toast.LENGTH_SHORT).show();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getApplicationContext(), "保存照片失败", Toast.LENGTH_SHORT).show();
                            }
                            bitmapToSave=Zoom(bitmap);
                        }
                    }
                }
                break;
            default:break;
        }
        bitmapName=String.valueOf(System.currentTimeMillis())+".jpg";//调用了相册已将图片显示区的图片修改 并用当前时间给照片文件命名
    }
    //将图片缩放并显示
    Bitmap Zoom(Bitmap bitmap)
    {
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth*1.0/imgHeight;
        double sqrtLength = Math.sqrt(partion*partion + 1);
        //新的缩略图大小
        double newImgW = 480*(partion / sqrtLength);
        double newImgH = 480*(1 / sqrtLength);
        float scaleW = (float) (newImgW/imgWidth);
        float scaleH = (float) (newImgH/imgHeight);
        Matrix mx = new Matrix();
        //对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
        if(photoView.getDrawable()!=null)
            photoView.setImageDrawable(null);//如果原本有图片则清除
        photoView.setImageBitmap(bitmap);
        return bitmap;
    }
    //将照片的缩小后的bitmap保存在内部路径
    void savePhoto(Bitmap bitmap) {
        File file=new File(getFilesDir().getAbsoluteFile(),bitmapName);
        try {
            FileOutputStream out=new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean noBlanks() {
        if(EtoS(Title.getText()).isEmpty())
            return false;
        if(EtoS(Date.getText()).isEmpty())
            return false;
        if(EtoS(Author.getText()).isEmpty())
            return false;
        if(EtoS(Content.getText()).isEmpty())
            return false;
        return true;
    }


    protected String EtoS(Editable text)
    {
        return String.valueOf(text);
    }

    protected void dateSelector() {
        Date=findViewById(R.id.date_input);
        Calendar cal;
        cal=Calendar.getInstance();
        int year=cal.get(Calendar.YEAR);       //获取年月日时分秒
        int month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        int day=cal.get(Calendar.DAY_OF_MONTH);
        Date.setFocusable(false);//点击时不会产生软键盘
        Date.setOnClickListener(v->{

            DatePickerDialog.OnDateSetListener listener= (arg0, year1, month1, day1) -> {
                Date.setText(year1 +"-"+(++month1)+"-"+ day1);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
            };
            DatePickerDialog dialog=new DatePickerDialog(DiaryProcess.this, DatePickerDialog.THEME_HOLO_LIGHT,listener,year,month,day);//主题在这里！后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
            dialog.show();

        });
    }
    public void showMenu()
    {
        Dialog mCameraDialog = new Dialog(this, R.style.BottomDialog);
        View root =  LayoutInflater.from(this).inflate(
                R.layout.photo_menu, null);
        //初始化视图
        root.findViewById(R.id.del_photo).setOnClickListener(v->{
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("提示");
            builder.setMessage("确认是否删除该图片");

            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    photoView.setImageDrawable(null);
                }
            });
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.show();
            mCameraDialog.dismiss();
        });

        root.findViewById(R.id.btn_cancel).setOnClickListener(v->
        {
            mCameraDialog.dismiss();
        });
        mCameraDialog.setContentView(root);
        Window dialogWindow = mCameraDialog.getWindow();
        dialogWindow.setGravity(Gravity.BOTTOM);
        // dialogWindow.setWindowAnimations(R.style.BottomDialog); // 添加动画
        WindowManager.LayoutParams lp = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        lp.x = 0; // 新位置X坐标
        lp.y = 0; // 新位置Y坐标
        lp.width = (int) getResources().getDisplayMetrics().widthPixels; // 宽度
        root.measure(0, 0);
        lp.height = root.getMeasuredHeight();

        lp.alpha = 9f; // 透明度
        dialogWindow.setAttributes(lp);
        mCameraDialog.show();
    }


}