package com.dam2.m08;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.projecte_maps.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity
{
    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextView tvLoginHere;
    Button btnRegister;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_signup);

            etRegEmail = findViewById(R.id.etRegEmail);
            etRegPassword = findViewById(R.id.etRegPass);
            tvLoginHere = findViewById(R.id.tvLoginHere);
            btnRegister = findViewById(R.id.btnRegister);

            mAuth = FirebaseAuth.getInstance();

            btnRegister.setOnClickListener(view -> createUser());

            tvLoginHere.setOnClickListener(view -> startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
        } catch (Exception e) {
            Messages.showMessage(this, e.getMessage());
        }
    }

    private void createUser()
    {
        try {
            String email = etRegEmail.getText().toString();
            String password = etRegPassword.getText().toString();

            if (TextUtils.isEmpty(email))
            {
                etRegEmail.setError("L'email no pot estar buit");
                etRegEmail.requestFocus();
            }
            else if (TextUtils.isEmpty(password))
            {
                etRegPassword.setError("La contrasenya no pot estar buida");
                etRegPassword.requestFocus();
            }
            else
            {
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(task -> {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(SignupActivity.this, "L'usuari ha iniciat sessió correctament", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    }
                    else { Toast.makeText(SignupActivity.this, "Error de registre: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show(); }
                });
            }
        } catch (Exception e) {
            Messages.showMessage(this, e.getMessage());
        }
    }
}