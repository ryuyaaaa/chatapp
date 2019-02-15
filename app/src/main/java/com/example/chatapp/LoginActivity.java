package com.example.chatapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText address;
    private EditText password;
    private Button loginButton;
    private TextView sign_up;

    private String userAddress;
    private String userPassword;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        address = findViewById(R.id.edit_email);
        password = findViewById(R.id.edit_password);
        loginButton = findViewById(R.id.button_login);
        sign_up = findViewById(R.id.text_signup);

        loginButton.setOnClickListener(this);
        sign_up.setOnClickListener(this);
    }

    public void onClick(View view) {

        handler = new Handler();

        userAddress = address.getText().toString();
        userPassword = password.getText().toString();

        switch(view.getId()) {
            case R.id.button_login:
                if (!userAddress.equals("") && !userPassword.equals("")) {
                    postUserInformation(userAddress, userPassword);
                } else {
                    showInformationError(userAddress, userPassword);
                }
                break;
            case R.id.text_signup:
                loadSignUpActivity();
                break;
            default:
                break;
        }
    }

    // POSTでアクセス
    public void postUserInformation(final String userAddress, String userPassword) {

        final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("uid", userAddress).put("email", userAddress).put("password", userPassword);
            RequestBody body = RequestBody.create(JSON, jsonObject.toString());

            Request request = new Request.Builder()
                    .url("http://eadc0792.ngrok.io/api/users/login")
                    .post(body)
                    .build();

            final OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response){
                    System.out.println(response.code());

                    // status codeについてはまた後で調整
                    if (response.code() == 401) {
                        // 別スレッドを実行
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                // Handlerを使用してメイン(UI)スレッドに処理を依頼する
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        showLoginError();
                                    }
                                });
                            }
                        }).start();
                    } else {

                        // 情報を端末に保存することで次回以降ログインを省略できる
                        SharedPreferences prefs = getSharedPreferences("UserInfo", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("Uid", userAddress);
                        editor.commit();
                        loadMainActivity();
                    }
                }
            });

        } catch(JSONException je) {
            je.printStackTrace();
        }


    }

    private void loadMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void loadSignUpActivity() {
        Intent intent = new Intent(this, SignUpActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // エラーメッセージ表示
    private void showInformationError(String userAddress, String userPassword) {
        if (userAddress.equals("")) {
            Toast toast = Toast.makeText(this, "メールアドレスを入力してください", Toast.LENGTH_SHORT);
            toast.show();
        } else if (userPassword.equals("")) {
            Toast toast = Toast.makeText(this, "パスワードを入力してください", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // エラーメッセージ表示
    private void showLoginError() {
        Toast toast = Toast.makeText(getApplicationContext(), "ユーザー情報が正しくありません", Toast.LENGTH_SHORT);
        toast.show();
    }


}
