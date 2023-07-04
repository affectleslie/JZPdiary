package Fann.dairy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AddAuthor extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_author);

        TextView name=findViewById(R.id.name);
        TextView age=findViewById(R.id.age);
        EditText nameInput=findViewById(R.id.name_input);
        EditText ageInput=findViewById(R.id.age_input);
        Button submit=findViewById(R.id.submit);

        submit.setOnClickListener(v->{
            SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();

            editor.putString("name",nameInput.toString());
            editor.putInt("age",Integer.parseInt(ageInput.toString()));

            editor.commit();
            finish();
        });
    }
}