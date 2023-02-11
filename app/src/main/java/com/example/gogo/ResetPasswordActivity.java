package com.example.gogo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPasswordActivity extends AppCompatActivity {

    EditText etEmail;
    Button btReset;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        etEmail = findViewById(R.id.etEmail);
        btReset = findViewById(R.id.btReset);
        Toolbar toolbar  = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();

        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getText().toString();
                if (email.equals("")){
                    Toast.makeText(ResetPasswordActivity.this, "All fileds are required!", Toast.LENGTH_SHORT).show();
                }else{
                    firebaseAuth.sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ResetPasswordActivity.this, "Please check your email "+email, Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(ResetPasswordActivity.this, LoginActivity.class));
                                    }else   {
                                        String  error = task.getException().getMessage();
                                        Toast.makeText(ResetPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });




    }
}