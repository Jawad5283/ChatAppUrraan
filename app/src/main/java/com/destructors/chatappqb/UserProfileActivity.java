package com.destructors.chatappqb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.destructors.chatappqb.Common.Common;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class UserProfileActivity extends AppCompatActivity {

    EditText edtOldPassword, edtNewPassword, edtFullName, edtEmail, edtPhone;
    Button btnUpdate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        //add toolbar
        Toolbar toolbar = findViewById(R.id.user_update_toolbar);
        toolbar.setTitle("User Profile");
        setSupportActionBar(toolbar);

        initViews();
        loadUserProfile();


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = edtOldPassword.getText().toString();
                String newPassword = edtNewPassword.getText().toString();
                String fullName = edtFullName.getText().toString();
                String email = edtEmail.getText().toString();
                String phone = edtPhone.getText().toString();

                QBUser user = new QBUser();
                user.setId(QBChatService.getInstance().getUser().getId());

                if (!Common.isNullorEmptyString(oldPassword)) ;
                user.setOldPassword(oldPassword);

                if (!Common.isNullorEmptyString(newPassword)) ;
                user.setPassword(newPassword);

                if (!Common.isNullorEmptyString(fullName)) ;
                user.setFullName(fullName);

                if (!Common.isNullorEmptyString(email)) ;
                user.setEmail(email);

                if (!Common.isNullorEmptyString(phone)) ;
                user.setPhone(phone);

                final ProgressDialog mDialog = new ProgressDialog(UserProfileActivity.this);

                mDialog.setMessage("Please wait...");
                mDialog.show();

                QBUsers.updateUser(user).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, qbUser.getLogin()+" updated successfully", Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(UserProfileActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        mDialog.dismiss();
                    }
                });
            }
        });
    }

    private void loadUserProfile() {
        QBUser currentUser = QBChatService.getInstance().getUser();
        String fullName = currentUser.getFullName();
        String email = currentUser.getEmail();
        String phone = currentUser.getPhone();

        edtFullName.setText(fullName);
        edtEmail.setText(email);
        edtPhone.setText(phone);
    }

    private void initViews() {
        edtOldPassword = findViewById(R.id.update_edt_old_password);
        edtNewPassword = findViewById(R.id.update_edt_new_password);
        edtFullName = findViewById(R.id.update_edt_full_name);
        edtEmail = findViewById(R.id.update_edt_email);
        edtPhone = findViewById(R.id.update_edt_phone);
        btnUpdate = findViewById(R.id.update_user_profile_button);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.user_update_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.user_update_logout:
                logOUt();
                break;
                default:
                    break;
        }

        return true;
    }

    private void logOUt() {
        QBUsers.signOut().performAsync(new QBEntityCallback<Void>() {
            @Override
            public void onSuccess(Void aVoid, Bundle bundle) {
                QBChatService.getInstance().logout(new QBEntityCallback<Void>() {
                    @Override
                    public void onSuccess(Void aVoid, Bundle bundle) {
                        Toast.makeText(UserProfileActivity.this, "Logout Successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(UserProfileActivity.this,MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //remove all previous activities
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {

                    }
                });
            }

            @Override
            public void onError(QBResponseException e) {

            }
        });
    }
}
