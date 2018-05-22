package com.greyogproducts.greyog.searchproto;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

import static com.greyogproducts.greyog.searchproto.MainActivity.TAG;

/**
 * Created by mac on 04/03/2018.
 */

class RetrofitHelper {
    private final String baseURL = "https://www.investing.com";
    private String phpSessId;
    private String stickySess;
    private SharedPreferences preferences;
    private OnResponseListener mOnResponseListener;

    interface OnResponseListener {
        void onResponse(MyResponseResult responseResult);
    }

    void setOnResponseListener(OnResponseListener listener) {
        mOnResponseListener = listener;
    }

    static RetrofitHelper getInstance() {
        return mInstance;
    }
    private static final RetrofitHelper mInstance = new RetrofitHelper();

    private RetrofitHelper() {

    }

    void setPrefs(SharedPreferences prefs) {
        this.preferences = prefs;
        phpSessId = prefs.getString("phpSessID", phpSessId);
        stickySess = prefs.getString("stickySess", stickySess);
    }

    private interface Server {
        @GET("technical/technical-summary")
        Call<ResponseBody> techRequest();
        @Headers({ "Host: www.investing.com"
                ,"Connection: keep-alive"
                ,"Content-Length: 44"
                ,"Cache-Control: max-age=0"
                ,"User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36 OPR/50.0.2762.67"
                ,"Upgrade-Insecure-Requests: 1"
                ,"Accept: application/json, text/javascript, */*; q=0.01"
                ,"Origin: https://www.investing.com"
                ,"X-Requested-With: XMLHttpRequest"
                ,"Content-Type: application/x-www-form-urlencoded"
                ,"Referer: https://www.investing.com/technical/technical-summary"
//                ,"Accept-Encoding: gzip, deflate, br"
                ,"Accept-Language: en-GB,en-US;q=0.9,en;q=0.8"
        })
        @FormUrlEncoded
        @POST("search/service/search")
        Call<MyResponseResult> searchRequest(@Field("search_text") String searchText,
                                         @Field("term") String term,
                                         @Field("country_id") String countryId, // =0
                                         @Field("tab_id") String tabId); // =All
    }

    private class ResponseInterceptor implements Interceptor {

        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            okhttp3.Response response = chain.proceed(chain.request());
            okhttp3.Response modified = response.newBuilder()
                    .addHeader("Cookie", phpSessId +"; " + stickySess)
                    .build();
            return modified;
        }
    }

    void doTechRequest() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        Server server = retrofit.create(Server.class);
        Call<ResponseBody> call = server.techRequest();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()){ Log.d(TAG, "onResponse: "+response.message());}
                else {
//                    Log.d(TAG, "onResponse: ok "+ response.headers().values("Set-Cookie"));
                    for (String s : response.headers().values("Set-Cookie")) {
                        if (s.toUpperCase().contains("PHPSESSID")) {
                            phpSessId = s.split("; ", 0)[0];
                            Log.d(TAG, "onResponse: " + phpSessId);
                            preferences.edit().putString("phpSessID", phpSessId).apply();
                        }
                        if (s.contains("StickySession")) {
                            stickySess = s.split("; ", 0)[0];
                            Log.d(TAG, "onResponse: " + stickySess);
                            preferences.edit().putString("stickySess", stickySess).apply();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    void doRequest(String text) {
        if (phpSessId == null) {
            Log.d(TAG, "doRequest: no phpSessID found");
            return;
        }
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new ResponseInterceptor())
                .build();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        Server server = retrofit.create(Server.class);
        Call<MyResponseResult> call = server.searchRequest(text, text, "0", "All");
        Log.d(TAG, "doRequest: request = " + call.request().headers().toMultimap());

        call.enqueue(new Callback<MyResponseResult>() {
            @Override
            public void onResponse(@NonNull Call<MyResponseResult> call, @NonNull Response<MyResponseResult> response) {
                if (response.isSuccessful()) {
//                    Log.d(TAG, "onResponse: ok, response : " + response.headers().toMultimap().toString());
//                    Log.d(TAG, "onResponse: body : " + response.body().toString());
                    mOnResponseListener.onResponse(response.body());
                } else {
                    Log.d(TAG, "onResponse: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponseResult> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
