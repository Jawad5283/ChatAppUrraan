package com.destructors.chatappqb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.QBAuth;
import com.quickblox.auth.session.QBSession;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class RegisterActivity extends AppCompatActivity {

    Button btn_register;
    TextView already_have_account;
    EditText et_user_name,et_password,et_full_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        
        registerSession();

        btn_register = findViewById(R.id.register_btn1);
        et_user_name = findViewById(R.id.et_user_name1);
        et_password =findViewById(R.id.et_password1);
        et_full_name = findViewById(R.id.et_full_name1);
        already_have_account = findViewById(R.id.already_have_account);
        already_have_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                startActivity(intent);
            }
        });

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog dialog = new ProgressDialog(RegisterActivity.this);
                dialog.setTitle("Register User");
                dialog.setMessage("Please wait...");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                String user = et_user_name.getText().toString();
                String password = et_password.getText().toString();

                QBUser qbUser =  new QBUser(user,password);
                qbUser.setFullName(et_full_name.getText().toString());

                QBUsers.signUp(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(RegisterActivity.this, "register Successful", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void registerSession() {
        QBAuth.createSession().performAsync(new QBEntityCallback<QBSession>() {
            @Override
            public void onSuccess(QBSession qbSession, Bundle bundle) {
                Toast.makeText(RegisterActivity.this, "session created", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(QBResponseException e) {
                Toast.makeText(RegisterActivity.this, "session error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
