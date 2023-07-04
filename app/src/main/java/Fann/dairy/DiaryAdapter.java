package Fann.dairy;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

public class DiaryAdapter extends ArrayAdapter {
    private int resourceId;
    private TextView diaryInfo;
    private Context context;
    //DiaryAdapter的构造函数
    public DiaryAdapter(Context context, int textViewResourceId, List<Diary> objects) {
        super(context,textViewResourceId,objects);
        this.resourceId=textViewResourceId;
        this.context=context;
    }
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Diary diary = (Diary) getItem(position);//从日记列表中取出一个日记对象
        View view;
        //旧布局为空则用LayoutInflater加载布局
        if(convertView==null)
            view = LayoutInflater.from(getContext()).inflate(resourceId,null);
            //否则直接对旧布局重用
        else
            view=convertView;
        //初始化日记信息textview
        diaryInfo=view.findViewById(R.id.diary_title);
        //从所取日记提取标题、日期和作者信息
        String Info=diary.getTitle()+"\n"+diary.getDate()+" "+diary.getAuthor();
        //将日记标题、日记日期、日记作者分行以不同格式显示
        SpannableString ss = new SpannableString(Info);
        //第一行为日记标题
        ss.setSpan(new TextAppearanceSpan(context, R.style.tv_style1), 0, diary.getTitle().length(),
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //第二行为日期和作者
        ss.setSpan(new TextAppearanceSpan(context, R.style.tv_style2), diary.getTitle().length(),
                Info.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        diaryInfo.setText(ss, TextView.BufferType.SPANNABLE);
        return view;
    }

}
