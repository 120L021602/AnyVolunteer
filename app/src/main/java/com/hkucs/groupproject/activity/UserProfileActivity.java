package com.hkucs.groupproject.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.hkucs.groupproject.R;
import com.hkucs.groupproject.database.UserManager;

public class UserProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvRemainingTokens, tvImageCredits;
    private Button btnBackToMain, btnLogout;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        // Initialize controls
        tvUsername = findViewById(R.id.tvUsername);
        tvRemainingTokens = findViewById(R.id.tvRemainingTokens);
        tvImageCredits = findViewById(R.id.tvImageCredits);
        btnBackToMain = findViewById(R.id.btnBackToMain);
        btnLogout = findViewById(R.id.btnLogout);
        userManager = new UserManager(this);

        // Display current logged-in username and token information
        String username = userManager.getCurrentUsername();
        if (username != null) {
            tvUsername.setText(username);
            // 显示剩余token
            int remainingTokens = userManager.getRemainingTokens();
            tvRemainingTokens.setText(remainingTokens == Integer.MAX_VALUE ? "无限制" : String.valueOf(remainingTokens));
            
            // 显示剩余图片处理次数
            int imageCredits = userManager.getRemainingImageCredits();
            tvImageCredits.setText(imageCredits == Integer.MAX_VALUE ? "无限制" : String.valueOf(imageCredits));
        } else {
            // If no user is logged in, return to login page
            Toast.makeText(this, "请先登录", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Back to main page button click event
        btnBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserProfileActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userManager.logout();
                Toast.makeText(UserProfileActivity.this, "已成功登出", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}