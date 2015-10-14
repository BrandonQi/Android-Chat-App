package edu.stevens.cs522.chat.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import edu.stevens.cs522.chat.R;

public class activityLogin extends Activity implements View.OnClickListener {
    public static final String KEY_USERNAME = activityLogin.class.getSimpleName() + ".user_name";

    public void onClick(View view) {
        String s = ((EditText)findViewById(R.id.user_name)).getText().toString();

        Intent intent = new Intent(this, activityChat.class);

        intent.putExtra(KEY_USERNAME, s);

        startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_login);

        Button button = (Button)findViewById(R.id.loggin_button);

        button.setOnClickListener(this);
    }
}
