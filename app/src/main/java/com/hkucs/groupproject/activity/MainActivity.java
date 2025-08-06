package com.hkucs.groupproject.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hkucs.groupproject.adapter.ChatAdapter;
import com.hkucs.groupproject.ChatMessage;
import com.hkucs.groupproject.R;
import com.hkucs.groupproject.database.ChatHistoryManager;
import com.hkucs.groupproject.database.ImageTaskManager;
import com.hkucs.groupproject.database.UserManager;
import com.hkucs.groupproject.handler.LlmHandler;
import com.hkucs.groupproject.handler.MaskTypeHandler;
import com.hkucs.groupproject.model.DeepseekR1;
import com.hkucs.groupproject.model.DoubaoLite32k;
import com.hkucs.groupproject.model.DoubaoPro32k;
import com.hkucs.groupproject.model.DoubaoVisionPro32k;
import com.hkucs.groupproject.model.Model;
import com.hkucs.groupproject.response.LlmResponse;
import com.hkucs.groupproject.response.MaskTypeResponse;
import com.hkucs.groupproject.utils.ImageProcessor;
import com.hkucs.groupproject.util.AccountInitializer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.opencv.android.OpenCVLoader;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int REQUEST_CODE_SELECT_IMAGE = 1001;

    private static final int REQUEST_CODE_PERMISSIONS = 1002;

    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;
    private RecyclerView rvChatHistory;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private Spinner spinnerModels;

    private ImageButton cameraButton;
    private ImageButton btnNewChat;

    private Model model;

    private String base64Image;

    private ChatHistoryManager chatHistoryManager;

    private String currentChatId = null;

    private String maskType;

    // 新增 member variable, save original image and face box
    private Bitmap originalBitmapForObfuscation = null;
    private List<Rect> faceBoxesForObfuscation = null;

    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化manager账号
        AccountInitializer.initializeManagerAccount(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_CODE_PERMISSIONS);
        }

        checkAndRequestPermissions();

        if (!OpenCVLoader.initDebug()) {
            Log.e("OpenCV", "OpenCV initialization failed!");
        } else {
            Log.d("OpenCV", "OpenCV initialization succeeded!");
        }

        rvChatHistory = findViewById(R.id.rvChatHistory);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        spinnerModels = findViewById(R.id.spinnerModels);
        cameraButton = findViewById(R.id.btnCamera);
        btnNewChat = findViewById(R.id.btnNewChat);

        rvChatHistory.setClickable(false);
        rvChatHistory.setFocusable(false);
        rvChatHistory.setFocusableInTouchMode(false);

        ChatHistoryManager chatHistoryManager = new ChatHistoryManager(this);
        ImageTaskManager imageTaskManager = new ImageTaskManager(this);



        DeepseekR1 deepseekR1 = new DeepseekR1();
        DoubaoLite32k doubaoLite32k = new DoubaoLite32k();
        DoubaoPro32k doubaoPro32k = new DoubaoPro32k();
        DoubaoVisionPro32k doubaoVisionPro32k = new DoubaoVisionPro32k();

        // Initialize model dropdown box
        String[] modelNames = {deepseekR1.getModelName(), doubaoLite32k.getModelName(), doubaoPro32k.getModelName(), doubaoVisionPro32k.getModelName()};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                modelNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerModels.setAdapter(adapter);

        // Optional: Listen for model selection changes
        spinnerModels.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedModel = parent.getItemAtPosition(position).toString();
                Toast.makeText(MainActivity.this, "Model selected: " + selectedModel, Toast.LENGTH_SHORT).show();

                // TODO: Switch behavior based on the selected model
                switch (selectedModel) {
                    case "deepseek-r1":
                        model = deepseekR1;
                        break;
                    case "doubao-1.5-lite-32k":
                        model = doubaoLite32k;
                        break;
                    case "doubao-1.5-pro-32k":
                        model = doubaoPro32k;
                        break;
                    case "doubao-vision-pro-32k":
                        model = doubaoVisionPro32k;
                        break;
                    default:
                        model = deepseekR1;
                        break;
                }

                if ("doubao-vision-pro-32k".equals(selectedModel)) {
                    cameraButton.setVisibility(View.VISIBLE);
                } else {
                    cameraButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Initialize chat history list
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        rvChatHistory.setLayoutManager(new LinearLayoutManager(this));
        rvChatHistory.setAdapter(chatAdapter);


        // Receive the first message from WelcomeActivity (if any)
        String firstMessage = getIntent().getStringExtra("first_message");
        if (firstMessage != null && !firstMessage.isEmpty()) {
            chatMessages.add(new ChatMessage(firstMessage, ChatMessage.Sender.USER));
            chatMessages.add(new ChatMessage("Hello, I'm your LLM helper who can secure your personal information", ChatMessage.Sender.LLM));
        }

        chatAdapter.notifyDataSetChanged();
        rvChatHistory.scrollToPosition(chatMessages.size() - 1);


        cameraButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("image/*");
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        });

        // 新增聊天按钮点击事件
        btnNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清空聊天消息
                chatMessages.clear();
                chatAdapter.notifyDataSetChanged();
                // 生成新的chatId
                currentChatId = "chat" + System.currentTimeMillis();
                Toast.makeText(MainActivity.this, "Create a new chat", Toast.LENGTH_SHORT).show();
            }
        });

//        btnSend.setOnClickListener(v -> {
//            String text = etMessage.getText().toString().trim();
//            UserManager userManager = new UserManager(this);
//
//            if ("doubao-vision-pro-32k".equals(model.getModelName())) {
//                if (!text.isEmpty() && originalBitmapForObfuscation != null && faceBoxesForObfuscation != null && !faceBoxesForObfuscation.isEmpty()) {
//
//                    if (!userManager.hasImageCredits()) {
//                        Toast.makeText(MainActivity.this, "您的图片处理次数已用完", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    Toast.makeText(this, "prompt: " + text, Toast.LENGTH_SHORT).show();
//                    MaskTypeResponse maskTypeResponse;
//                    try {
//                        maskTypeResponse = MaskTypeHandler.chooseMaskType(text);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                        maskTypeResponse = new MaskTypeResponse("default", true, 0, "", "SOLID");
//                    }
//
//                    maskType = maskTypeResponse.getMaskType();
//                    Log.d(TAG, maskType);
//
//                    ChatMessage userMsg = new ChatMessage(text, ChatMessage.Sender.USER);
//
//                    String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
//                    String imagePath = null;
//                    try {
//                        imagePath = saveBitmapToFile(this, originalBitmapForObfuscation, fileName);
//                        userMsg.setImagePath(imagePath);
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//
//                    chatMessages.add(userMsg);
//                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                    etMessage.setText("");
//                    rvChatHistory.scrollToPosition(chatMessages.size() - 1);
//
//                    new Thread(() -> {
//                        try {
//                            ImageProcessor.ObfuscationParams chosenParams;
//                            switch (maskType) {
//                                case "PIXELATE":
//                                    chosenParams = new ImageProcessor.ObfuscationParams(0.3f, ImageProcessor.MaskType.PIXELATE, 10);
//                                    Log.d(TAG, "Mask type: PIXELATE");
//                                    break;
//                                case "TRANSLUCENT":
//                                    chosenParams = new ImageProcessor.ObfuscationParams(0.5f, ImageProcessor.MaskType.TRANSLUCENT, 10);
//                                    Log.d(TAG, "Mask type: TRANSLUCENT");
//                                    break;
//                                case "REPLACE_FACE":
//                                    chosenParams = new ImageProcessor.ObfuscationParams(1.2f, ImageProcessor.MaskType.REPLACE_FACE, 0);
//                                    Log.d(TAG, "Mask type: REPLACE_FACE");
//                                    break;
//                                case "SOLID":
//                                    Log.d(TAG, "Mask type: SOLID");
//                                default:
//                                    chosenParams = new ImageProcessor.ObfuscationParams(0.2f, ImageProcessor.MaskType.SOLID, 10);
//                                    Log.d(TAG, "Mask type: SOLID");
//                                    break;
//                            }
//                            Bitmap replaceFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.replaceimage);
//                            Bitmap processedBitmap = ImageProcessor.applyFaceObfuscation(originalBitmapForObfuscation, faceBoxesForObfuscation, chosenParams, replaceFaceBitmap);
//                            // 保存打码图片
//                            String processedFileName = "IMG_" + System.currentTimeMillis() + "_processed.jpg";
//                            String processedImagePath = saveBitmapToFile(this, processedBitmap, processedFileName);
//                            // 编码为base64
//                            base64Image = encodeBitmapToBase64(processedBitmap);
//                            // 消耗一次图片处理机会
//                            userManager.consumeImageCredit();
//                            // 发送给LLM
//                            DoubaoVisionPro32k visionModel = new DoubaoVisionPro32k();
//                            LlmResponse llmResponse = visionModel.chat(base64Image, text);
//                            new Handler(Looper.getMainLooper()).post(() -> {
//                                if (!llmResponse.getSuccess()) {
//                                    Log.d(TAG, "Model request failed!");
//                                    Toast.makeText(MainActivity.this, "Error！", Toast.LENGTH_SHORT).show();
//                                    return;
//                                }
//                                String reply = llmResponse.getReply();
//                                chatMessages.add(new ChatMessage("LLM's reply: " + reply, ChatMessage.Sender.LLM));
//                                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                                rvChatHistory.scrollToPosition(chatMessages.size() - 1);
//                                if (currentChatId != null) {
//                                    ImageTaskManager imageTaskManager1 = new ImageTaskManager(MainActivity.this);
//                                    imageTaskManager1.updateTaskAndReply(currentChatId, text, reply);
//                                }
//                                // 清空图片缓存，防止重复发送
//                                originalBitmapForObfuscation = null;
//                                faceBoxesForObfuscation = null;
//                                base64Image = null;
//                            });
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            runOnUiThread(() -> Toast.makeText(this, "Image processing failed!", Toast.LENGTH_SHORT).show());
//                        }
//                    }).start();
//                }
//            } else {
//                if (!text.isEmpty()) {
//                    // 检查token余额
//                    int estimatedTokens = text.length() / 4 + 50; // 粗略估算token数量
//                    if (!userManager.hasEnoughTokens(estimatedTokens)) {
//                        Toast.makeText(MainActivity.this, "您的token余额不足", Toast.LENGTH_SHORT).show();
//                        return;
//                    }
//
//                    // Add user message to chat history
//                    chatMessages.add(new ChatMessage(text, ChatMessage.Sender.USER));
//                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//                    rvChatHistory.scrollToPosition(chatMessages.size() - 1);
//                    etMessage.setText("");
//
//                    // Generate a unique chatId
//                    String chatId = "chat" + System.currentTimeMillis();
//
//                    new Thread(() -> {
//                        // Call the LLM interface to get a response
//                        LlmResponse llmResponse = LlmHandler.sendRequestAndGetResponse(model.getModelId(), text);
//
//                        // Return to the UI thread to update the page
//                        new Handler(Looper.getMainLooper()).post(() -> {
//                            if (!llmResponse.getSuccess()) {
//                                Log.d(TAG, "Model request failed!");
//                                Toast.makeText(MainActivity.this, "Model request failed!", Toast.LENGTH_SHORT).show();
//                                return;
//                            }
//
//                            // 消耗token
//                            int tokensUsed = llmResponse.getTokenConsumed();
//                            userManager.consumeTokens(tokensUsed);
//
//                            Toast.makeText(MainActivity.this, "Model request success!", Toast.LENGTH_SHORT).show();
//                            // Update the model's token consumption
//                            model.setTotalTokenConsumed(model.getTotalTokenConsumed() + llmResponse.getTokenConsumed());
//
//                            // Add desensitized text (first part) to user message
//                            chatMessages.add(new ChatMessage("Desensitized content:" + llmResponse.getReply(), ChatMessage.Sender.USER));
//                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//
//                            // Add reply to instructions (second part) to model message
//                            chatMessages.add(new ChatMessage("LLM's reply:" + llmResponse.getAdditionalInfo(), ChatMessage.Sender.LLM));
//                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//
//                            // Add information restored on the device side (third part) to model message
//                            chatMessages.add(new ChatMessage("The information restored on the device side:" + llmResponse.getRestoredInfo(), ChatMessage.Sender.LLM));
//                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
//
//                            rvChatHistory.scrollToPosition(chatMessages.size() - 1);
//
//                            // Save chat history to database
//                            String summary = "User message: " + text + " | Desensitized content: " + llmResponse.getReply() +
//                                    " | LLM's reply: " + llmResponse.getAdditionalInfo() +
//                                    " | Restored information: " + llmResponse.getRestoredInfo();
//                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
//
//                            chatHistoryManager.saveChatHistory(chatId, summary, timestamp);
//                        });
//                    }).start();
//                }
//            }
//        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            UserManager userManager = new UserManager(this);

            if ("doubao-vision-pro-32k".equals(model.getModelName())) {
                if (!text.isEmpty() && originalBitmapForObfuscation != null && faceBoxesForObfuscation != null && !faceBoxesForObfuscation.isEmpty()) {

                    if (!userManager.hasImageCredits()) {
                        Toast.makeText(this, "You have run out of image processing times.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Toast.makeText(this, "prompt: " + text, Toast.LENGTH_SHORT).show();

                    new Thread(() -> {
                        try {
                            MaskTypeResponse maskTypeResponse = MaskTypeHandler.chooseMaskType(text);
                            runOnUiThread(() -> processMaskTypeAndContinue(maskTypeResponse, text, userManager));
                        } catch (Exception e) {
                            e.printStackTrace();
                            MaskTypeResponse fallback = new MaskTypeResponse("default", true, 0, "", "SOLID");
                            runOnUiThread(() -> {
                                Toast.makeText(MainActivity.this, "Use the default mask type", Toast.LENGTH_SHORT).show();
                                processMaskTypeAndContinue(fallback, text, userManager);
                            });
                        }
                    }).start();
                }
            } else {
                if (!text.isEmpty()) {
                    int estimatedTokens = text.length() / 4 + 50;
                    if (!userManager.hasEnoughTokens(estimatedTokens)) {
                        Toast.makeText(MainActivity.this, "You don't have enough tokens.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    chatMessages.add(new ChatMessage(text, ChatMessage.Sender.USER));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    rvChatHistory.scrollToPosition(chatMessages.size() - 1);
                    etMessage.setText("");

                    String chatId = "chat" + System.currentTimeMillis();

                    new Thread(() -> {
                        LlmResponse llmResponse = LlmHandler.sendRequestAndGetResponse(model.getModelId(), text);
                        runOnUiThread(() -> {
                            if (!llmResponse.getSuccess()) {
                                Log.d(TAG, "Model request failed!");
                                Toast.makeText(MainActivity.this, "Model request failed!", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int tokensUsed = llmResponse.getTokenConsumed();
                            userManager.consumeTokens(tokensUsed);

                            Toast.makeText(MainActivity.this, "Model request success!", Toast.LENGTH_SHORT).show();
                            model.setTotalTokenConsumed(model.getTotalTokenConsumed() + tokensUsed);

                            chatMessages.add(new ChatMessage("Desensitized content:" + llmResponse.getReply(), ChatMessage.Sender.USER));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                            chatMessages.add(new ChatMessage("LLM's reply:" + llmResponse.getAdditionalInfo(), ChatMessage.Sender.LLM));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                            chatMessages.add(new ChatMessage("The information restored on the device side:" + llmResponse.getRestoredInfo(), ChatMessage.Sender.LLM));
                            chatAdapter.notifyItemInserted(chatMessages.size() - 1);

                            rvChatHistory.scrollToPosition(chatMessages.size() - 1);

                            String summary = "User message: " + text + " | Desensitized content: " + llmResponse.getReply() +
                                    " | LLM's reply: " + llmResponse.getAdditionalInfo() +
                                    " | Restored information: " + llmResponse.getRestoredInfo();
                            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

                            chatHistoryManager.saveChatHistory(chatId, summary, timestamp);
                        });
                    }).start();
                }
            }
        });



        // History button navigation
        ImageButton btnHistory = findViewById(R.id.btnHistory);
        Log.d("DEBUG", "btnHistory = " + btnHistory);
        btnHistory.setOnClickListener(v -> {
            Log.d("BUTTON", "History clicked");
            Intent intent = new Intent(MainActivity.this, HistoryNavigateActivity.class);
            startActivity(intent);
        });

        // Login/User info button
        ImageButton btnLogin = findViewById(R.id.btnLogin);
        Log.d("DEBUG", "btnLogin = " + btnLogin);
        btnLogin.setOnClickListener(v -> {
            Log.d("BUTTON", "Login clicked");
            UserManager userManager = new UserManager(MainActivity.this);
            if (userManager.getCurrentUserId() != null) {
                // Already logged in, navigate to user profile page
                Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else {
                // Not logged in, navigate to login page
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            new Thread(() -> {
                try {
                    if (imageUri == null) {
                        Log.e("ImageUpload", "Image URI is null!");
                        return;
                    }
                    Log.d("FaceDetection", "The uploaded image has been read. Detecting faces...");
                    Bitmap originalBitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                    if (originalBitmap == null) {
                        Log.e("ImageUpload", "Failed to decode Bitmap from URI: " + imageUri);
                        runOnUiThread(() -> Toast.makeText(this, "The image failed to load", Toast.LENGTH_SHORT).show());
                        return;
                    }
                    String timestamp1 = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                    String fileName1 = "IMG_" + timestamp1 + "_original.jpg";
                    String imagePath1 = saveBitmapToFile(this, originalBitmap, fileName1);
                    Log.d("saveImage", "Original image is successfully saved at" + imagePath1);
                    Log.d("BitmapSuccess", "Bitmap successfully loaded.");
                    List<Rect> faceBoxes = ImageProcessor.detectFaces(originalBitmap, this);
                    int faceCount = faceBoxes.size();
                    runOnUiThread(() -> {
                        if (faceCount > 0) {
                            Toast.makeText(this,  faceCount + "face(s) detected", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "No face detected", Toast.LENGTH_SHORT).show();
                        }
                    });

                    originalBitmapForObfuscation = originalBitmap;
                    faceBoxesForObfuscation = faceBoxes;
                    currentChatId = "chat" + System.currentTimeMillis();

                    ChatMessage imageMessage = new ChatMessage("", ChatMessage.Sender.USER);
                    imageMessage.setImagePath(imagePath1);
                    chatMessages.add(imageMessage);
                    runOnUiThread(() -> {
                        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                        rvChatHistory.scrollToPosition(chatMessages.size() - 1);
                        Toast.makeText(this, "The image has been successfully uploaded.", Toast.LENGTH_SHORT).show();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Image processing failed!", Toast.LENGTH_SHORT).show());
                }
            }).start();
        }
    }

    public static String saveBitmapToFile(Context context, Bitmap bitmap, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        }
        return file.getAbsolutePath();
    }

    public static String encodeImageToBase64(Context context, Uri imageUri) throws Exception {
        InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        byte[] buffer = new byte[4096];
        int bytesRead;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        byte[] imageBytes = outputStream.toByteArray();
        inputStream.close();
        outputStream.close();

        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }
    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        byte[] imageBytes = outputStream.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    public static String saveBase64ImageToFile(Context context, String base64Image, String fileName) throws IOException {
        byte[] decodedBytes = Base64.decode(base64Image, Base64.DEFAULT);
        File file = new File(context.getFilesDir(), fileName);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(decodedBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file.getAbsolutePath();
    }

    private String copyImageUriToDir(Context context, Uri imageUri, String prefix) throws Exception {

        File dir = new File(context.getFilesDir(), "chat_images");
        if (!dir.exists()) dir.mkdirs();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = prefix + timestamp + ".jpg";
        File destFile = new File(dir, fileName);
        try (
                InputStream in = context.getContentResolver().openInputStream(imageUri);
                OutputStream out = new FileOutputStream(destFile)
        ) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        }
        return destFile.getAbsolutePath();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {

                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void processMaskTypeAndContinue(MaskTypeResponse maskTypeResponse, String text, UserManager userManager) {
        maskType = maskTypeResponse.getMaskType();
        Log.d(TAG, maskType);

        ChatMessage userMsg = new ChatMessage(text, ChatMessage.Sender.USER);

        String fileName = "IMG_" + System.currentTimeMillis() + ".jpg";
        String imagePath = null;
        try {
            imagePath = saveBitmapToFile(this, originalBitmapForObfuscation, fileName);
            //userMsg.setImagePath(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        chatMessages.add(userMsg);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        etMessage.setText("");
        rvChatHistory.scrollToPosition(chatMessages.size() - 1);

        String finalImagePath = imagePath;
        new Thread(() -> {
            try {
                ImageProcessor.ObfuscationParams chosenParams;
                switch (maskType) {
                    case "the_second_most_strict":
                        chosenParams = new ImageProcessor.ObfuscationParams(0.7f, ImageProcessor.MaskType.PIXELATE, 10);
                        Log.d(TAG,"Mask type: 0.7_PIXELATE_10");
                        break;
                    case "the_second_most_lenient":
                        chosenParams = new ImageProcessor.ObfuscationParams(0.7f, ImageProcessor.MaskType.TRANSLUCENT, 10);
                        Log.d(TAG, "Mask type: 0.7_TRANSLUCENT_10");
                        break;
                    case "the_most_strict":
                        chosenParams = new ImageProcessor.ObfuscationParams(1.2f, ImageProcessor.MaskType.REPLACE_FACE, 0);
                        Log.d(TAG, "Mask type: 1.2_REPLACE_FACE_0");
                        break;
                    case "the_most_lenient":
                        chosenParams = new ImageProcessor.ObfuscationParams(0.3f, ImageProcessor.MaskType.TRANSLUCENT, 10);
                        Log.d(TAG, "Mask type: 0.3_TRANSLUCENT_10");
                        break;
                    default:
                        chosenParams = new ImageProcessor.ObfuscationParams(0.3f, ImageProcessor.MaskType.SOLID, 10);
                        Log.d(TAG, "Mask type: 0.3_SOLID_10");
                        break;
                }

                Bitmap replaceFaceBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.replaceimage);
                Bitmap processedBitmap = ImageProcessor.applyFaceObfuscation(originalBitmapForObfuscation, faceBoxesForObfuscation, chosenParams, replaceFaceBitmap);
                String processedFileName = "IMG_" + System.currentTimeMillis() + "_processed.jpg";
                String processedImagePath = saveBitmapToFile(this, processedBitmap, processedFileName);
                base64Image = encodeBitmapToBase64(processedBitmap);
                userManager.consumeImageCredit();

                DoubaoVisionPro32k visionModel = new DoubaoVisionPro32k();
                LlmResponse llmResponse = visionModel.chat(base64Image, text);

                runOnUiThread(() -> {
                    ChatMessage processedImageMsg = new ChatMessage("", ChatMessage.Sender.USER); // Or system
                    processedImageMsg.setImagePath(processedImagePath);
                    chatMessages.add(processedImageMsg);
                    if (!llmResponse.getSuccess()) {
                        Toast.makeText(MainActivity.this, "The model request failed", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String reply = llmResponse.getReply();
                    chatMessages.add(new ChatMessage("LLM's reply: " + reply, ChatMessage.Sender.LLM));
                    chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                    rvChatHistory.scrollToPosition(chatMessages.size() - 1);

                    if (currentChatId != null) {
                        ImageTaskManager imageTaskManager1 = new ImageTaskManager(MainActivity.this);
                        imageTaskManager1.updateTaskAndReply(currentChatId, text, reply);
                        saveImageTaskToDatabase(currentChatId, text, finalImagePath, processedImagePath, reply);
                    }

                    originalBitmapForObfuscation = null;
                    faceBoxesForObfuscation = null;
                    base64Image = null;
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Image processing failed!", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void saveImageTaskToDatabase(String chatId, String prompt, String imagePathOriginal, String imagePathProcessed, String reply) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
        String userId = new UserManager(this).getCurrentUserId();
        ImageTaskManager imageTaskManager = new ImageTaskManager(this);
        imageTaskManager.saveImageTask(chatId, prompt, imagePathOriginal, imagePathProcessed, reply, timestamp, userId);
    }
}




