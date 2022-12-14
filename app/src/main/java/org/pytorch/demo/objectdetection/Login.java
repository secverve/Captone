package org.pytorch.demo.objectdetection;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Login extends AppCompatActivity {
    private static final String TAG ="Login";
    private static final int RC_SIGN_IN = 10;
    private TextView nickName;
    private ImageView profileImage;

//    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        Log.d("KeyHash", getKeyHash());

        // Hiding actionBar
//        ActionBar actionBar = getSupportActionBar();
//        actionBar.hide();

        // database
        myDBHelper myDBHelper;
        SQLiteDatabase sqlDB;

        // login layout
        EditText editId1, editId2;
        Button signupBtn, findBtn, loginBtn;
        ImageButton kakaoLoginBtn, googleBtn;

        editId1 = findViewById(R.id.loginId);
        editId2 = findViewById(R.id.loginPassword);

        signupBtn = findViewById(R.id.signupBtn);
        loginBtn = findViewById(R.id.loginBtn);
        kakaoLoginBtn = findViewById(R.id.kakaoLoginBtn);
        googleBtn = findViewById(R.id.googleLoginBtn);

        // kakao login
//        nickName = findViewById(R.id.kakaoNickname);
//        profileImage = findViewById(R.id.kakaoProfile);

//        Log.d("getKeyHash", "" + getKeyHash(Login.this));

        // ?????? ?????? ??? ??????
        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Signup.class);
                startActivity(intent);
            }
        });

        // ????????? ?????? ???
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String id = editId1.getText().toString();
                String pswd = editId2.getText().toString();

                if (id.equals("") && pswd.equals("")){
                    Toast.makeText(getApplicationContext(), "????????? ???????????? ??????", Toast.LENGTH_LONG).show();
                }

                else if (id.equals("")){
                    Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_LONG).show();
                }

                else if (pswd.equals("")) {
                    Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_LONG).show();
                }

                else {
                    // ?????? ????????? ???????????? ??????????????? ??????
                    Toast.makeText(getApplicationContext(), "????????? ??????", Toast.LENGTH_LONG).show();
                }
            }
        });

        // ????????? ?????????
        kakaoLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(Login.this)) {
                    kakao_login();
                }else {
                    kakao_accountLogin();
                }
            }
        });

        // ?????? ?????????
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestId()
                .requestEmail()
                .requestProfile()
                .build();
        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(Login.this, gso);

        googleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });
    }

    // ???????????? ?????? ????????? method()
    public void kakao_login(){
        String TAG = "login()";
        UserApiClient.getInstance().loginWithKakaoTalk(Login.this,(oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "????????? ??????", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "????????? ??????(??????) : " + oAuthToken.getAccessToken());
                getUserInfo();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            return null;
        });
    }

    // ????????? ?????? ?????? ????????? method()
    public void kakao_accountLogin(){
        String TAG = "accountLogin()";
        UserApiClient.getInstance().loginWithKakaoAccount(Login.this,(oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "????????? ??????", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "????????? ??????(??????) : " + oAuthToken.getAccessToken());
                getUserInfo();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
            return null;
        });
    }

    public void getUserInfo(){
        String TAG = "getUserInfo()";
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Log.e(TAG, "????????? ?????? ?????? ??????", meError);
            } else {
                System.out.println("????????? ??????");
                Log.i(TAG, user.toString());
                {
                    Log.i(TAG, "????????? ?????? ?????? ??????" +
                            "\n????????????: "+user.getId() +
                            "\n?????????: "+user.getKakaoAccount().getEmail());
                }
                Account user1 = user.getKakaoAccount();
                System.out.println("????????? ??????" + user1);
            }
            return null;
        });
    }
    
    // ????????? ??????
    public String getKeyHash(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }
    
    // ?????? ????????? Method()
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount acct = task.getResult(ApiException.class);
            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                Log.d(TAG, "handleSignInResult:personName "+personName);
                Log.d(TAG, "handleSignInResult:personGivenName "+personGivenName);
                Log.d(TAG, "handleSignInResult:personEmail "+personEmail);
                Log.d(TAG, "handleSignInResult:personId "+personId);
                Log.d(TAG, "handleSignInResult:personFamilyName "+personFamilyName);
                Log.d(TAG, "handleSignInResult:personPhoto "+personPhoto);

                // ???????????? ???
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }
}

