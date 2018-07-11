package vn.edu.hcmus.fit.cntn15.bookswap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmailPassword";

    Button btnSignup, btnLogin;
    EditText input_email, input_pass;

    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        btnSignup = findViewById(R.id.email_register_button);
        btnLogin = findViewById(R.id.register_login);

        input_email = findViewById(R.id.register_email_field);
        input_pass = findViewById(R.id.register_password_field);

        btnSignup.setOnClickListener(this);
        btnLogin.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();

    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.email_register_button) {
            signUpUser(input_email.getText().toString(), input_pass.getText().toString());
        } else if (i == R.id.register_login) {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        }
    }

    private void signUpUser(String email, String password) {

        Log.d(TAG, "createAccount:" + email + " " + password);
        if (!validateForm()) {
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            Log.v("SIGNUP", "ERROR");
                        } else {
                            Log.v("SIGNUP", "REGISTER SUCCESSFULLY");
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                        }
                    }
                });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = input_email.getText().toString();
        if (TextUtils.isEmpty(email)) {
            input_email.setError("Required.");
            valid = false;
        } else {
            input_email.setError(null);
        }

        String password = input_pass.getText().toString();
        if (TextUtils.isEmpty(password)) {
            input_pass.setError("Required.");
            valid = false;
        } else {
            input_pass.setError(null);
        }

        return valid;
    }

}
