package Fann.dairy;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class DiaryDB extends SQLiteOpenHelper {

    //初始化数据库，新建日记表包含日记id 日记标题 日记日期 日记作者 日记内容和日记所包含照片的文件名
    public static final String CREATE_DIARY="create table Diary01("
            +"id integer primary key autoincrement,title text,date text,"
            +"author text, content text,photoName text)";
    private Context mContext;
    public DiaryDB(Context context, String name, SQLiteDatabase.CursorFactory factory,int version)
    {
        super(context,name,factory,version);
        mContext=context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_DIARY);
        Toast.makeText(mContext,"数据库创建成功",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
