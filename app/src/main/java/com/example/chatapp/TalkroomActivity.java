package com.example.chatapp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TalkroomActivity extends AppCompatActivity {

    private String Uid;
    private String toUid;
    private final String MESSAGE_URL = "http://38133334.ngrok.io/api/messages";

    public static ArrayList<String> messageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_talkroom);

        Intent intent = getIntent();
        toUid = intent.getStringExtra("to_uid");
        System.out.println("toUid = " + toUid);

        SharedPreferences pref = getSharedPreferences("UserInfo", MODE_PRIVATE);
        Uid = pref.getString("Uid", "0");

        // トーク一覧を読み込むため
        AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(Uid, toUid, MESSAGE_URL, getApplication(), findViewById(android.R.id.content));
        asyncJsonLoader.execute();

        final EditText editMessage = findViewById(R.id.edit_message);
        final Button sendButton = findViewById(R.id.button_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = editMessage.getText().toString();

                if (!message.equals("")) {
                    // POSTアクションで送信
                    AsyncJsonPoster asyncJsonPoster = new AsyncJsonPoster(Uid, toUid, message, MESSAGE_URL, getApplication(), findViewById(android.R.id.content));
                    asyncJsonPoster.execute();
                    editMessage.setText("");
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }

    // 非同期でGETアクションを行うクラス
    public static class AsyncJsonLoader extends AsyncTask<Void, Void, String> {

        private String uri;
        private Application context;
        private View view;
        private String Uid;
        private String toUid;

        private AsyncJsonLoader(String Uid, String toUid, String uri, Application context, View view) {
            this.uri = uri;
            this.context = context;
            this.view = view;
            this.Uid = Uid;
            this.toUid = toUid;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(uri)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Uid", Uid)
                    .addHeader("toUid", toUid)
                    .build();

            // クライアントオブジェクトを作って
            OkHttpClient client = new OkHttpClient();

            // リクエストして結果を受け取って
            try {
                Response response = client.newCall(request).execute();
                assert response.body() != null;
                result = response.body().string();
                System.out.println(result);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 返す
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

            try {
                messageList = new ArrayList<>();
                JSONObject json = new JSONObject(result);
                JSONArray messages = json.getJSONArray("results");

                for (int i = 0; i < messages.length(); i++) {
                    JSONObject messageObj = messages.getJSONObject(i);
                    String from = messageObj.getString("from_uid");
                    String message = messageObj.getString("content");
                    System.out.println("[" + from + "]: " + message);
                    messageList.add("[" + from + "]: " + message);
                }

            } catch (JSONException je) {
                je.printStackTrace();
                showLoadError();
            }


            ListView listView = view.findViewById(R.id.message_list);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, messageList);
            listView.setAdapter(adapter);
        }

        // エラーメッセージ表示
        private void showLoadError() {
            Toast toast = Toast.makeText(context, "データを取得できませんでした。", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // 非同期でPOSTアクションを行うクラス
    public static class AsyncJsonPoster extends AsyncTask<Void, Void, String> {

        private String uri;
        private Application context;
        private View view;
        private String Uid;
        private String toUid;
        private String message;

        private AsyncJsonPoster(String Uid, String toUid, String message, String uri, Application context, View view) {
            this.uri = uri;
            this.context = context;
            this.view = view;
            this.Uid = Uid;
            this.toUid = toUid;
            this.message = message;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", message);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                // リクエストオブジェクトを作って
                Request request = new Request.Builder()
                        .url(uri)
                        .addHeader("Uid", Uid)
                        .addHeader("toUid", toUid)
                        .post(body)
                        .build();

                // クライアントオブジェクトを作って
                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                assert response.body() != null;
                result = response.body().string();
                System.out.println(result);
            } catch (JSONException je) {
                je.printStackTrace();
            } catch (IOException ie) {
                ie.printStackTrace();
            }

            // 返す
            return result;
        }

        @Override
        protected void onPostExecute(String result) {

        }

        // エラーメッセージ表示
        private void showLoadError() {
            Toast toast = Toast.makeText(context, "データを取得できませんでした。", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
