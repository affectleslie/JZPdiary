package Fann.dairy;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
//sharedPreference 主界面布局加一个浮动按钮，用于修改作者信息，作者信息包括姓名和年龄，都存入sp，每次新增日记时，都会从sp先读取作者信息，
//编辑日记内容时，也会从sp读取并，修改作者名
//作者信息页面可以修改作者信息，也可以删除作者信息 如果作者信息被删除或不存在，则进入页面时，会有一个默认的作者名和默认的年龄 即hint
//若为默认信息，点击输入框后默认信息将被清空，随之写入的信息为黑色hint属性
//点击删除浮动按钮可以删除当前作者信息，将该信息从sp中删除，然后输入框的信息会变为默认
//当输入框为默认值时，点击保存将直接退出 选择默认的名字，且点击删除无效
public class AuthorInfo extends AppCompatActivity {
    private FloatingActionButton saveButton;
    private FloatingActionButton delButton;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_author_info);
        //保存作者信息和删除作者信息的浮动按钮
        saveButton=findViewById(R.id.saveAuthor);
        delButton=findViewById(R.id.deleteAuthor);
        //首先查看SharedPreferences是否有作者信息 如果没有则获得默认姓名和默认年龄
        SharedPreferences prefs=getSharedPreferences("author",MODE_PRIVATE);
        String name=prefs.getString("name","User(Default)");
        int age=prefs.getInt("age",0);
        //初始化姓名和年龄的输入框
        EditText nameInput=findViewById(R.id.name_input);
        EditText ageInput=findViewById(R.id.age_input);
        //如果SharedPreferences中有作者信息，将信息分别显示在输入框中
        if(!name.equals("User(Default)")||age!=0)
        {
          //  Toast.makeText(AuthorInfo.this,"Not Default",Toast.LENGTH_SHORT).show();
            nameInput.setText(name);
            ageInput.setText(String.valueOf(age));
            nameInput.setText(prefs.getString("name",""));
            ageInput.setText(String.valueOf(prefs.getInt("age",0)));
        }
        saveButton.setOnClickListener(v->{
            //如果点击保存时，输入框没有输入则使用默认姓名，并将SharedPreferences所存的数据清除
            if(nameInput.getText().toString().isEmpty()) {
                Toast.makeText(AuthorInfo.this,"你选择默认的姓名",Toast.LENGTH_SHORT).show();
                //在sp中有数据时才会清除
                if(!prefs.getString("name","User(Default)").equals("User(Default)"))
                    prefs.edit().clear().commit();
            }
            //如果有输入，则将SharedPreferences中的数据修改为所输入的姓名以及年龄
            else{
                    SharedPreferences.Editor editor=getSharedPreferences("author",MODE_PRIVATE).edit();
                    editor.putString("name",String.valueOf(nameInput.getText()));
                    //年龄如没有输入，则默认为0
                    if(ageInput.getText().toString().isEmpty())
                        editor.putInt("age",0);
                    else
                        editor.putInt("age",Integer.parseInt(String.valueOf(ageInput.getText())));
                    editor.commit();
            }
            finish();
        });
        delButton.setOnClickListener(v->{
            //如果点击删除键时 输入框有空则函数返回不执行任何操作
            if(nameInput.getText().toString().isEmpty()||ageInput.getText().toString().isEmpty()) {
                return;
            }
            //如果有输入，但是并没有保存到SharedPreferences中，则函数也返回，点击删除按钮不执行任何操作
            if(prefs.getString("name","Fann(Default)").equals("Fann(Default)"))
                return;
                //当输入不为空 且SharedPreferences有数据时才执行操作操作
            else
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("提示");
                builder.setMessage("确认是否删除该作者信息");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        nameInput.setText("");
                        ageInput.setText("");
                        prefs.edit().clear().commit();
                        Toast.makeText(AuthorInfo.this,"已删除",Toast.LENGTH_SHORT).show();
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
    }
}