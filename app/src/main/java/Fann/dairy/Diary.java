package Fann.dairy;


public class Diary {
    private String title="";//日记标题
    private String content="";//日记内容
    private String author="";//日记作者
    private int Id=0;//日记编号
    private String photoName="";//照片名
    private String date;//日期
    //Constructor构造函数
    public Diary(int id,String title, String date, String author, String content,String photoName) {
        this.title = title;
        this.content = content;
        this.author = author;
        this.date = date;
        this.Id=id;
        this.photoName=photoName;
    }

    //各变量的getter和setter函数
    public String getTitle() {
        return title;
    }

    public int getDiaryId() {
        return Id;
    }

    public void setDiaryId(int Id) {
        this.Id = Id;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPhotoName() {
        return photoName;
    }

    public void setPhotoName(String pictureName) {
        this.photoName = pictureName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


}
