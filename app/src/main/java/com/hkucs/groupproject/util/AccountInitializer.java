package com.hkucs.groupproject.util;

import android.content.Context;
import com.hkucs.groupproject.database.UserManager;

public class AccountInitializer {
    
    public static void initializeManagerAccount(Context context) {
        UserManager userManager = new UserManager(context);
        // 尝试创建manager账号
        userManager.registerUser("manager", "123456");
    }
} 