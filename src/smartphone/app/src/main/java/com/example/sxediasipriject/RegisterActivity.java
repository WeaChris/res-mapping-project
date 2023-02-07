package com.example.sxediasipriject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView banner, registerUser;
    private EditText email, password;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        banner = (TextView) findViewById(R.id.banner);
        banner.setOnClickListener(this);

        registerUser = (Button) findViewById(R.id.registerUser);
        registerUser.setOnClickListener(this);


        password = (EditText) findViewById(R.id.password);
        email= (EditText) findViewById(R.id.email);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.banner:
                startActivity(new Intent(this, FirstActivity.class));
                break;
            case R.id.registerUser:
                registerUser();
                progressBar.setVisibility(View.GONE);
                startActivity(new Intent(this, FirstActivity.class));
                break;
        }
    }

    private void registerUserToFirestore(){
        String userID = mAuth.getCurrentUser().getUid();
        DocumentReference documentReference = db.collection("Users").document(userID);
        Map<Object,Object> user = new HashMap<>();
        user.put("email",email.getText().toString().trim());
        documentReference.set(user).addOnSuccessListener( task->{
            Log.d(TAG, "User Created");
        });
    }

    private void registerUser(){

        String password2 = password.getText().toString().trim();
        String email2 = email.getText().toString().trim();


        if(password2.isEmpty()){
            password.setError("Password is required!");
            password.requestFocus();
            return;
        }
        if(email2.isEmpty()){
            email.setError("Email is required!");
            email.requestFocus();
            return;
        }


        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email2,password2).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                registerUserToFirestore();
            }else{

            }
        });

    }
}