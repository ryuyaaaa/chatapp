package com.example.chatapp;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class TalksFragment extends Fragment {

    private final String ROOM_URL = "http://07f8129f.ngrok.io/api/rooms";
    private String uid;

    public static ArrayList<String> talkRoomList = null;

    // Fragmentで表示するViewを作成するメソッド
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        // 先ほどのレイアウトをここでViewとして作成します
        return inflater.inflate(R.layout.fragment_talks, container, false);
    }

    // Viewが生成し終わった時に呼ばれるメソッド
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferences pref = getActivity().getSharedPreferences("UserInfo", MODE_PRIVATE);
        uid = pref.getString("Uid", "0");

        try {
            // トーク一覧を読み込むため
            AsyncJsonLoader asyncJsonLoader = new AsyncJsonLoader(uid, ROOM_URL, getActivity().getApplication(), getView());
            asyncJsonLoader.execute();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    // 非同期処理を行うクラス
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

            try {
                talkRoomList = new ArrayList<>();
                JSONObject json = new JSONObject(result);
                JSONArray talks = json.getJSONArray("results");

                for (int i = 0; i < talks.length(); i++) {
                    JSONObject toObj = talks.getJSONObject(i);
                    String toId = toObj.getString("to_id");
                    System.out.println(toId);
                    talkRoomList.add(toId);
                }

            } catch (JSONException je) {
                je.printStackTrace();
                showLoadError();
            }


            final ListView listView = view.findViewById(R.id.room_list);
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_list_item_1, talkRoomList);
            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = adapter.getItem(position);

                    Intent intent = new Intent(context, TalkroomActivity.class);
                    intent.putExtra("to_uid", item);
                    context.startActivity(intent);
                }
            });
        }

        // エラーメッセージ表示
        private void showLoadError() {
            Toast toast = Toast.makeText(context, "データを取得できませんでした。", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
