package com.example.chatapp;

import android.app.Application;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class FriendsFragment extends Fragment {

    private final String FRIEND_URL = "http://38133334.ngrok.io/api/friends";
    private String Uid;

    Handler handler;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // 先ほどのレイアウトをここでViewとして作成します
        return inflater.inflate(R.layout.fragment_friends, container, false);
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("UserInfo", MODE_PRIVATE);
        Uid = pref.getString("Uid", "0");

        final EditText editId = view.findViewById(R.id.edit_id);
        Button addButton = view.findViewById(R.id.button_add);

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String targetId = editId.getText().toString();
                if (!targetId.equals("")) {

                    AsyncJsonPoster asyncJsonPoster = new AsyncJsonPoster(Uid, targetId, FRIEND_URL, getActivity().getApplication(), getView());
                    asyncJsonPoster.execute();
                    editId.setText("");
                }
            }
        });

    }

    // 非同期でPOSTアクションを行うクラス
    public static class AsyncJsonPoster extends AsyncTask<Void, Void, String> {

        private String uri;
        private Application context;
        private View view;
        private String Uid;
        private String toUid;
        private int status_code = 0;

        private AsyncJsonPoster(String Uid, String toUid, String uri, Application context, View view) {
            this.uri = uri;
            this.context = context;
            this.view = view;
            this.Uid = Uid;
            this.toUid = toUid;
        }

        @Override
        protected String doInBackground(Void... params) {
            String result = null;

            final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("content", "add new friend");
                RequestBody body = RequestBody.create(JSON, jsonObject.toString());

                // リクエストオブジェクトを作って
                Request request = new Request.Builder()
                        .url(uri)
                        .addHeader("Uid", Uid)
                        .addHeader("Target", toUid)
                        .post(body)
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
            showAnswerToast(status_code);
        }

        // エラーメッセージ等
        private void showAnswerToast(int code) {

            if (code == 200) {
                Toast toast = Toast.makeText(context, "追加しました", Toast.LENGTH_SHORT);
                toast.show();
            } else if (code == 421) {
                Toast toast = Toast.makeText(context, "ユーザが存在しません", Toast.LENGTH_SHORT);
                toast.show();
            } else if (code == 412) {
                Toast toast = Toast.makeText(context, "すでに登録済みです", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(context, "エラーが発生しました", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }
}
