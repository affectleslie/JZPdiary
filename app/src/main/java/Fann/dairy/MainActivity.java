package Fann.dairy;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

//新增功能√ 如果有空 点保存则提醒框要求不为空√ 增加照片效果
//修改功能√ 如果有空 点保存则提醒框要求不为空√
//删除功能√
//为每个功能提交时都设置一个确认窗口√
//listview标题下显示时间√

public class MainActivity extends AppCompatActivity {
    //public String [] data={"Jordan","LeBron","Magic","Kareem","Russel","Wilt","Bird","Shaq","Dream"};
    private List<Diary> diaryList=new ArrayList<Diary>();
    private DiaryDB dbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper=new DiaryDB(this,"Diary01.db",null,1);
        setDiaries();//将所有记录显示
        //新增日记和修改作者信息的浮动按钮
        FloatingActionButton newButton=findViewById(R.id.newButton);
        FloatingActionButton authorButton=findViewById(R.id.authorButton);
        newButton.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this,DiaryProcess.class);
            //向DiaryProcess发送intent 表明此时是新增日记功能
            intent.putExtra("intent_type","newDiary");
            startActivity(intent);
        });
        authorButton.setOnClickListener(v->{
            Intent intent=new Intent(MainActivity.this,AuthorInfo.class);
            startActivity(intent);
        });
    }
    //将数据库中所有日记信息显示的函数
    public void setDiaries() {
        if(!diaryList.isEmpty())
            diaryList.clear();
        //查询所有日记
        SQLiteDatabase db=dbHelper.getReadableDatabase();
        //Cursor cursor=db.query("Diary01",null,null,null,null,null,null,null);
        String[] columns = {"id", "title", "date", "author", "content", "photoName"};
        String selection = "author = ?";
        SharedPreferences currentAuthor=getSharedPreferences("author",MODE_PRIVATE);
        String name=currentAuthor.getString("name","User(Default)");
        String[] selectionArgs = {name}; // 假设当前作者存储在 currentAuthor 变量中
        Cursor cursor = db.query("Diary01", columns, selection, selectionArgs, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                @SuppressLint("Range")
                int id=cursor.getInt(cursor.getColumnIndex("id"));
                @SuppressLint("Range")
                String title=cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range")
                String date=cursor.getString(cursor.getColumnIndex("date"));
                @SuppressLint("Range")
                String author=cursor.getString(cursor.getColumnIndex("author"));
                @SuppressLint("Range")
                String content=cursor.getString(cursor.getColumnIndex("content"));
                @SuppressLint("Range")
                String photoName=cursor.getString(cursor.getColumnIndex("photoName"));
                Diary diary=new Diary(id,title,date,author,content,photoName);
                //将每篇日记加入列表
                diaryList.add(diary);
            }while(cursor.moveToNext());

        }
        cursor.close();
        //将查询到的数据用于初始化adapter，并通过listview显示
        DiaryAdapter adapter=new DiaryAdapter(MainActivity.this, R.layout.diary_item,diaryList);
        ListView listview=findViewById(R.id.list_view);
        listview.setAdapter(adapter);
        //点击某条日记记录可进行对日记编辑
        listview.setOnItemClickListener((parent, view, position, id) -> {
            Diary diary = (Diary) adapter.getItem(position);
            Intent intent=new Intent(MainActivity.this,DiaryProcess.class);
            ArrayList<String> diaryInfo= new ArrayList<String>();
            diaryInfo.add(String.valueOf(diary.getDiaryId()));
            diaryInfo.add(diary.getTitle());
            diaryInfo.add(diary.getDate());
            diaryInfo.add(diary.getAuthor());
            diaryInfo.add(diary.getContent());
            diaryInfo.add(diary.getPhotoName());
            //发送intent给DiaryProcess表明此时功能为编辑日记
            intent.putExtra("intent_type","editDiary");
            //并将该条日记的信息发送
            intent.putStringArrayListExtra("diaryInfo",diaryInfo);
            startActivity(intent);
        });
    }
    //活动再次启动时，更新listview日记记录
    @Override
    protected void onRestart() {
        super.onRestart();
        setDiaries();
    }
}