package com.sales.numax.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sales.numax.R;
import com.sales.numax.common.FirebaseTables;
import com.sales.numax.model.UserDetail;
import com.sales.numax.utility.AppStatus;
import com.sales.numax.utility.Global;
import com.sales.numax.utility.KeyboardUtil;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by bms0013 on 19/04/17.
 */

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_SIGNUP = 0;
    @BindView(R.id.input_email)
    TextInputEditText _emailText;

    @BindView(R.id.input_password)
    TextInputEditText _passwordText;

    @BindView(R.id.btn_login)
    MaterialButton _loginButton;

    @BindView(R.id.link_reset_password)
    TextView _reset_password;

    @BindView(R.id.link_signup)
    TextView link_signup;


    @BindView(R.id.tvBuild)
    TextView tvBuild;

    private FirebaseAuth mFirebaseAuth;
    View parentLayout;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPrefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        parentLayout = findViewById(android.R.id.content);
        ButterKnife.bind(this);

        /*if (!Global.FORCE_SIGNUP) {
            if (!CheckInstallation()) {
                Intent iSignUp = new Intent(LoginActivity.this, SignupInfo.class);
                iSignUp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(iSignUp);
            }
        }*/

        _emailText.setText("rahu@gmail.com");
        _passwordText.setText("test@123");

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (Global.CheckInternetConnection(parentLayout, getApplicationContext())) {
                    FireBaseLogin();
                }
            }
        });

//        link_signup.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Global.SIGNUP_FROM = 1;
//                Intent iSignup = new Intent(LoginActivity.this, NewUserSignUp.class);
//                iSignup.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                startActivity(iSignup);
//
//            }
//        });


        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right);
    }



    @SuppressLint("WrongConstant")
    public void FireBaseLogin() {

        KeyboardUtil.hideKeyboard(this);

        if (!AppStatus.getInstance(this).isOnline()) {
            Global.ShowSnackMessage(LoginActivity.this,"No internet connection..Please check");
        } else {
            if (!validate()) {
                return;
            } else {

                final ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, null, "Authenticating..", true);
                try {
                    mFirebaseAuth = FirebaseAuth.getInstance();
                    // Global.SetBmsProgressBar("Authenticating...", dialog);
                    mFirebaseAuth.signInWithEmailAndPassword(_emailText.getText().toString(), _passwordText.getText().toString())
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @SuppressLint("WrongConstant")
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        dialog.dismiss();
                                        Global.USER_CODE = task.getResult().getUser().getUid();
                                        GetUserDetail(Global.USER_CODE);
                                    } else {
                                        Global.ShowSnackMessage(LoginActivity.this, "Invalid user id or password");
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                            dialog.dismiss();
                        }
                    });
                } catch (Exception ex) {
                    Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                }
            }
        }
    }

    private void RememberMe(String userid, int usertype) {
        try {
            loginPreferences = getSharedPreferences("NUMAX_REMEMBER_ME", MODE_PRIVATE);
            loginPrefsEditor = loginPreferences.edit();
            loginPrefsEditor.putBoolean("saveLogin", true);
            loginPrefsEditor.putString("loginid", userid);
            loginPrefsEditor.putInt("usertype", usertype);
            loginPrefsEditor.apply();
            loginPrefsEditor.commit();

        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(), ex.getMessage(), Toast.LENGTH_LONG).show();
        }

    }


    public void onLoginFailed() {
        //Snackbar snackbar = Snackbar.make(parentLayout, "Login failed", Snackbar.LENGTH_LONG);
       // snackbar.setDuration(Snackbar.LENGTH_LONG);
       // snackbar.show();
        _loginButton.setEnabled(true);
    }

    private void GetUserDetail(String userkey) {

        final ProgressDialog dialog = ProgressDialog.show(this, null, "Verifying information..", true);
        dialog.show();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child(FirebaseTables.TBL_USER_DETAIL).orderByChild("userkey").equalTo(userkey).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    for (DataSnapshot userSnapshot : task.getResult().getChildren()) {
                        UserDetail userDetail = userSnapshot.getValue(UserDetail.class);
                        if (userDetail!=null){
                            Global.LOGIN_USER_DETAIL = userDetail;
                            if (userDetail.getUsertype()==1) { // Not equal to web user
                                if (userDetail.getIsactive()) {
                                    Global.USER_TYPE = Global.LOGIN_USER_DETAIL.getUsertype();
                                    RememberMe(Global.USER_CODE, Global.USER_TYPE);
                                    //RegisterBrodcaster();
                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Global.ShowSnackMessage(LoginActivity.this,"You are currently deactivated by Admin, Please contact Admin");
                                }
                            }else{
                                Global.ShowSnackMessage(LoginActivity.this,"You are not valid user..Please verify");
                            }
                        }
                    }
                }
                else {
                    dialog.dismiss();
                    Toast.makeText(getApplicationContext(),task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    Log.d("firebase", task.getException().getMessage());
                }
            }
        });
//        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
//        mDatabase.child(FirebaseTables.TBL_USER_DETAIL).orderByChild("userkey").equalTo(userkey).addValueEventListener(new ValueEventListener() {
//            @SuppressLint("WrongConstant")
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                //dialog.dismiss();
//                Toast.makeText(getApplicationContext(),"user key:",Toast.LENGTH_LONG).show();
//                if (dataSnapshot.exists()) {
//                   // UserDetail userDetail = dataSnapshot.getValue(UserDetail.class);
//                    //Global.USER_TYPE = Global.LOGIN_USER_DETAIL.getUsertype();
//                   Toast.makeText(getApplicationContext(),"user key:",Toast.LENGTH_LONG).show();
//                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
//                        UserDetail userDetail = userSnapshot.getValue(UserDetail.class);
//                        if (userDetail != null) {
//                            Global.LOGIN_USER_DETAIL = userDetail;
//                            //User Type: 1 is Customer
//                            //User Type: 3 is Sales Person
//                            Toast.makeText(getApplicationContext(),"user type:" + String.valueOf(userDetail.getUsertype()),Toast.LENGTH_LONG).show();
//                            if (userDetail.getUsertype()==1) { // Not equal to web user
//                                if (userDetail.getIsactive() == 1) {
//                                    Global.USER_TYPE = Global.LOGIN_USER_DETAIL.getUsertype();
//                                    RememberMe(Global.USER_CODE, Global.USER_TYPE);
//                                    //RegisterBrodcaster();
//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    startActivity(intent);
//                                    finish();
//                                } else {
//                                    Global.ShowSnackMessage(LoginActivity.this,"You are currently deactivated by Admin, Please contact Admin");
//                                }
//                            }else{
//                                Global.ShowSnackMessage(LoginActivity.this,"You are not valid user..Please verify");
//                            }
//                        }
//                    }
//
//
//                } else {
//                    Snackbar snackbar = Snackbar.make(parentLayout, "User detail doest not exist!!. Please verify", Snackbar.LENGTH_LONG);
//                    snackbar.setDuration(Snackbar.LENGTH_LONG);
//                    snackbar.show();
//                }
//                Toast.makeText(getApplicationContext(),"HAI",Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                //dialog.dismiss();
//                Toast.makeText(getApplicationContext(),databaseError.getMessage(),Toast.LENGTH_LONG).show();
//
//            }
//        });

    }
     public boolean validate() {
        boolean valid = true;


        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        if (email.isEmpty() ||  password.isEmpty()){
            Global.ShowSnackMessage(LoginActivity.this,"Login ID and Password cannot be empty");
            valid = false;
            return valid;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("Enter valid email address");

        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 15) {
            _passwordText.setError("between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

//    public void RegisterBrodcaster() {
//
//        Global.DEVICE_REGISTRATION_ID = Global.getRegitrationDeviceId(getApplicationContext());
//        if (Global.LOGIN_USER_DETAIL != null) {
//            if (!Global.DEVICE_REGISTRATION_ID.isEmpty() && !Global.LOGIN_USER_DETAIL.getKey().isEmpty()) {
//                //Toast.makeText(getApplicationContext(),Global.DEVICE_REGISTRATION_ID,Toast.LENGTH_LONG).show();
//                if (!Global.DEVICE_REGISTRATION_ID.equalsIgnoreCase("NA")) {
//                    DatabaseReference mDataReference = FirebaseDatabase.getInstance().getReference(FirebaseTables.TBL_MOBILE_DEVICE_REG);
//                    MobileDeviceInfo storesDeviceInfo = new MobileDeviceInfo();
//                    storesDeviceInfo.setUsertype(Global.LOGIN_USER_DETAIL.getUsertype());
//                    storesDeviceInfo.setIsactive(true);
//                    storesDeviceInfo.setDeviceid(Global.DEVICE_REGISTRATION_ID);
//                    storesDeviceInfo.setDeviceinfo("MANUFACTURER:" + Build.MANUFACTURER + ",DEVICEID" + Build.ID + ",DEVICE:" + Build.DEVICE);
//                    mDataReference.child(Global.LOGIN_USER_DETAIL.getKey()).setValue(storesDeviceInfo).addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void aVoid) {
//                            Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
//                        }
//                    }).addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                        }
//                    });
//                }
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        finish();
    }


}
