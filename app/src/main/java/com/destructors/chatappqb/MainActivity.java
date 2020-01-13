package com.destructors.chatappqb;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.quickblox.auth.session.QBSettings;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

public class MainActivity extends AppCompatActivity {

    static final String App_ID = "79761";
    static final String AUTH_KEY = "dFpWhdZTNO3zFzY";
    static final String AUTH_SECRET = "cMLqjUG5CZNHr3S";
    static final String ACCOUNT_KEY = "sdZgyxs3T-RjWvsQ6vLL";

    Button btn_login;
    TextView btn_register;
    EditText et_user_name,et_password;

    private int STORAGE_PERMISSION_CODE=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initiallizeFrameWork();

        btn_register= findViewById(R.id.register_btn);
        btn_login = findViewById(R.id.login_btn);
        et_user_name = findViewById(R.id.et_user_name);
        et_password = findViewById(R.id.et_password);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("please Wait....");
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();

                final String user = et_user_name.getText().toString();
                final String password = et_password.getText().toString();

                QBUser qbUser = new QBUser(user,password);

                QBUsers.signIn(qbUser).performAsync(new QBEntityCallback<QBUser>() {
                    @Override
                    public void onSuccess(QBUser qbUser, Bundle bundle) {
                        Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();

                        Intent intent  = new Intent(MainActivity.this,ChatDialogsActivity.class);
                        intent.putExtra("user",user);
                        intent.putExtra("password",password);
                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void initiallizeFrameWork() {

        QBSettings.getInstance().init(getApplicationContext(), App_ID, AUTH_KEY, AUTH_SECRET);
        QBSettings.getInstance().setAccountKey(ACCOUNT_KEY);
    }

    @Override
    protected void onStart() {
        super.onStart();

       // requestStorageRuntimePermission();
    }

    private void requestStorageRuntimePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED ||
                            ContextCompat.checkSelfPermission(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    == PackageManager.PERMISSION_GRANTED){
            }else {
                requestLocationPermission();
            }
        }
    }
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("This Permission needed Otherwise you will not be able to use this app!")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]
                                            {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        }else {
            ActivityCompat.requestPermissions(this,new String[]
                    {Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE},STORAGE_PERMISSION_CODE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == STORAGE_PERMISSION_CODE){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "permission granted", Toast.LENGTH_SHORT).show();

            }else {
                Toast.makeText(this, "permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
