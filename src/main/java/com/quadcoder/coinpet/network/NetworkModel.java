package com.quadcoder.coinpet.network;

import android.content.Context;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;
import com.quadcoder.coinpet.MyApplication;
import com.quadcoder.coinpet.PropertyManager;
import com.quadcoder.coinpet.network.response.Res;

import org.apache.http.Header;
import org.apache.http.HttpEntity;

/**
 * Created by Phangji on 4/5/15.
 */
public class NetworkModel {
    private static NetworkModel instance;

    public static NetworkModel getInstance() {
        if(instance == null)
            instance = new NetworkModel();
        return instance;
    }

    private NetworkModel() {
        client = new AsyncHttpClient();
        client.setCookieStore(new PersistentCookieStore(MyApplication
                .getContext()));
        client.setTimeout(30000);
    }

    AsyncHttpClient client;

    public static final String SERVER_INTERNAL_URL = "http://172.16.100.181:3300";
    public static final String SERVER_EXTERNAL_URL = "http://61.43.139.155:3300";
    public static final String SERVER_URL = SERVER_EXTERNAL_URL;

    public interface OnNetworkResultListener<T> {
        public void onResult(T res);

        public void onFail(T res);
    }

    public void cancelRequests(Context context) {
        client.cancelRequests(context, false);
    }



    public void signup(Context context, String pn, String name,  String gender, int age,
                         final OnNetworkResultListener<Res> listener) {
        String url = SERVER_URL + "/user/kids";  //api
        RequestParams params = new RequestParams();
        params.put("name", name);
        params.put("gender", gender);   //api에서 valueof로 변환 해줌.
        params.put("age", age);
        params.put("pn", pn);

        client.post(context, url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onFail(result);
            }
        });
    }

    public void confirmPn(Context context, String pn,
                       final OnNetworkResultListener<Res> listener) {
        String url = SERVER_URL + "/user/kids/login";
        RequestParams params = new RequestParams();
        params.put("pn", pn);

        client.post(context, url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = responseBody == null ? null : new String(responseBody);
                Gson gson = new Gson();
                Res result = response == null ? null : gson.fromJson(response, Res.class);
                listener.onFail(result);
            }
        });
    }

    public void setGoal(Context context, int method, String content, String goal_date, int goal_cost, int now_cost,
                          final OnNetworkResultListener<Res> listener) {
        String url = SERVER_URL + "/goal";
        RequestParams params = new RequestParams();
        params.put("method", method);
        params.put("content", content);
        params.put("goal_date", goal_date);
        params.put("goal_cost", goal_cost);
        params.put("now_cost", now_cost);
        client.addHeader("authorization", PropertyManager.getInstance().getToken());

        client.post(context, url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onFail(result);
            }
        });
    }

    public void sendCoin(Context context, int now_cost,
                        final OnNetworkResultListener<Res> listener) {
        String url = SERVER_URL + "/goal";
        RequestParams params = new RequestParams();
        params.put("now_cost", now_cost);
        client.addHeader("authorization", PropertyManager.getInstance().getToken());

        client.put(context, url, params, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = responseBody == null ? null : new String(responseBody);
                Gson gson = new Gson();
                Res result = response == null ? null : gson.fromJson(response, Res.class);
                listener.onFail(result);
            }
        });
    }

    public void getSavingList(Context context,
                         final OnNetworkResultListener<Res> listener) {
        String url = SERVER_URL + "/saving";
        client.addHeader("authorization", PropertyManager.getInstance().getToken());

        client.get(context, url, null, new AsyncHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onResult(result);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                String response = new String(responseBody);
                Gson gson = new Gson();
                Res result = gson.fromJson(response, Res.class);
                listener.onFail(result);
            }
        });
    }

}
