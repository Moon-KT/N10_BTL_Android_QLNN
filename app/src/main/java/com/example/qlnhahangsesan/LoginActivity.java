package com.example.qlnhahangsesan;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.qlnhahangsesan.database.DatabaseHelper;
import com.example.qlnhahangsesan.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    
    private DatabaseHelper databaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database helper
        databaseHelper = DatabaseHelper.getInstance(this);
        
        // Initialize session manager
        sessionManager = new SessionManager(this);
        
        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            // User is already logged in, go to MainActivity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);

        // Set click listener for login button
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Set click listener for register text
        textViewRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to RegisterActivity
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        
        // Insert default admin user if not exists
        insertDefaultUser();
    }

    private void insertDefaultUser() {
        if (!databaseHelper.isUserExists("admin")) {
            databaseHelper.addUser("Administrator", "admin", "admin123");
        }
    }

    private void loginUser() {
        // Get input values
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(username)) {
            editTextUsername.setError("Vui lòng nhập tên đăng nhập");
            editTextUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Vui lòng nhập mật khẩu");
            editTextPassword.requestFocus();
            return;
        }

        // Check user credentials
        if (databaseHelper.checkUser(username, password)) {
            // Get user data
            SQLiteDatabase db = databaseHelper.getReadableDatabase();
            String[] columns = {"id", "fullname"};
            String selection = "username = ?";
            String[] selectionArgs = {username};
            Cursor cursor = db.query("users", columns, selection, selectionArgs, null, null, null);
            
            if (cursor.moveToFirst()) {
                String userId = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                String fullname = cursor.getString(cursor.getColumnIndexOrThrow("fullname"));
                
                // Create session
                sessionManager.createLoginSession(userId, username, fullname);
                
                // Navigate to MainActivity
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
            cursor.close();
        } else {
            Toast.makeText(LoginActivity.this, "Tên đăng nhập hoặc mật khẩu không chính xác", Toast.LENGTH_SHORT).show();
        }
    }
} 