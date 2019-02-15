package com.example.chatapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;


public class SettingsFragment extends Fragment {

    private String Uid;
    EditText edit_name;
    EditText edit_comment;

    private final String USER_URL = "http://07f8129f.ngrok.io/api/users";

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // 先ほどのレイアウトをここでViewとして作成します
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("UserInfo", MODE_PRIVATE);
        Uid = pref.getString("Uid", "0");

        Button button_save = view.findViewById(R.id.button_save);

        edit_name = view.findViewById(R.id.edit_name);
        edit_comment = view.findViewById(R.id.edit_comment);

        try {
            // トーク一覧を読み込むため
            AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(Uid, USER_URL, getActivity().getApplication(), getView());
            asyncJsonLoader.execute();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = edit_name.getText().toString();
                String comment = edit_comment.getText().toString();
                // 情報を更新する
                AsyncJsonPutter asyncJsonPutter = new AsyncJsonPutter(Uid, name, comment, USER_URL, getActivity().getApplication(), getView());
                asyncJsonPutter.execute();
            }
        });


    }

    // 非同期でGETアクションを行うクラス
    public static class AsyncJsonLoader extends AsyncTask<Void, Void, String> {

        private String uri;
        private Application context;
        private View view;
        private String uid;

        private AsyncJsonLoader(String uid, String uri, Application context, View view) {
            this.uri = uri;
            this.context = context;
            this.view = view;
            this.uid = uid;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            // リクエストオブジェクトを作って
            Request request = new Request.Builder()
                    .url(uri)
                    .addHeader("Content-Type", "application/json; charset=utf-8")
                    .addHeader("Uid", uid)
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

            String name = "unknown";
            String comment = "";

            try {
                JSONObject json = new JSONObject(result);
                JSONArray users = json.getJSONArray("results");

                JSONObject userObj = users.getJSONObject(0);
                name = userObj.getString("name");
                comment = userObj.getString("comment");

            } catch (JSONException je) {
                je.printStackTrace();
                showLoadError();
            }

            EditText edit_name = view.findViewById(R.id.edit_name);
            EditText edit_comment = view.findViewById(R.id.edit_comment);
            edit_name.setText(name);
            edit_comment.setText(comment);

        }

        // エラーメッセージ表示
        private void showLoadError() {
            Toast toast = Toast.makeText(context, "データを取得できませんでした。", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    // 非同期でPUTアクションを行うクラス
    public static class AsyncJsonPutter extends AsyncTask<Void, Void, String> {

        private String uri;
        private Application context;
        private View view;
        private String Uid;
        private String name;
        private String comment;
        private int status_code;

        private AsyncJsonPutter(String Uid, String name, String comment, String uri, Application context, View view) {
            this.uri = uri;
            this.context = context;
            this.view = view;
            this.Uid = Uid;
            this.name = name;
            this.comment = comment;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", name).put("comment", comment);
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                // リクエストオブジェクトを作って
                Request request = new Request.Builder()
                        .url(uri)
                        .addHeader("Uid", Uid)
                        .put(body)
                        .build();

                // クライアントオブジェクトを作って
                OkHttpClient client = new OkHttpClient();

                Response response = client.newCall(request).execute();
                assert response.body() != null;
                result = response.body().string();
                status_code = response.code();
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
            showPutAnswer(status_code);
        }

        // エラーメッセージ表示
        private void showPutAnswer(int status) {

            if (status == 200) {
                Toast toast = Toast.makeText(context, "保存が完了しました", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(context, "保存に失敗しました", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
