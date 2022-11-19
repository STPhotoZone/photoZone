package com.example.stphotozone;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.TokenData;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {

    // 구글로그인 result 상수
    private static final int RC_SIGN_IN = 900;

    // 구글api클라이언트
    private GoogleSignInClient googleSignInClient;

    // 파이어베이스 인증 객체 생성
    private FirebaseAuth firebaseAuth = null;

    // 구글  로그인 버튼
    private SignInButton buttonGoogle;

    ActivityResultLauncher<Intent> someActivityResultLauncher;

    //Firestore
    FirebaseFirestore db;

    Boolean hasAccount = false;

    TextView txtNickname;
    EditText editNickname;
    Button btnNickname;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState
                            ) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);


        db = FirebaseFirestore.getInstance();

        firebaseAuth = FirebaseAuth.getInstance();

        buttonGoogle = (SignInButton) findViewById(R.id.btn_googleSignIn);

        txtNickname = (TextView) findViewById(R.id.txtNick);
        btnNickname = (Button) findViewById(R.id.btnSubmit);
        editNickname = (EditText) findViewById(R.id.editNickName);


        if(firebaseAuth.getCurrentUser() != null)
        {
            Intent intent = new Intent(getApplication(), ChallengeActivity.class);
            startActivity(intent);
            finish();
        }

        // Google 로그인을 앱에 통합
        // GoogleSignInOptions 개체를 구성할 때 requestIdToken을 호출
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.ClientId))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);

            }
        });

        btnNickname.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addUser(editNickname.getText().toString());
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //구글로그인 버튼 응답
        if(requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            System.out.println(task);
            try {
                //구글 로그인 성공
                GoogleSignInAccount account = task.getResult(ApiException.class);
                System.out.println(task.getResult(ApiException.class));
                firebaseAuthWithGoogle(account);
            } catch (ApiException e ) {

                System.out.println(e);
        }


  }
  }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            // 로그인 성공
                            // db 체크 해서 있으면 로그인 없으면 새로 생성
                            checkFireStore();
                        } else {
                            // 로그인 실패
                            Toast.makeText(LoginActivity.this, "Login fail", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    public void checkFireStore() {
        hasAccount = false;
        CollectionReference productRef = db.collection("users");
        productRef
                .whereEqualTo("email", firebaseAuth.getCurrentUser().getEmail())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            hasAccount = true;
                        }
                    }
                });

        if (!hasAccount)
        {
            //닉네임 정하는 dialog
            txtNickname.setVisibility(View.VISIBLE);
            editNickname.setVisibility(View.VISIBLE);
            btnNickname.setVisibility(View.VISIBLE);
            buttonGoogle.setVisibility(View.INVISIBLE);

        } else
        {
            Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void addUser(String str) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", firebaseAuth.getCurrentUser().getEmail());
        user.put("nickname", str);
        user.put("mission_num", 0);

        db.collection("users")
                .add(user)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                        Intent intent = new Intent(getApplicationContext(), ChallengeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


}
