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
        void onResponseTechData(String raw);
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
//        "https://www.investing.com/instruments/Service/GetTechincalData"
//                , data={'pairID':10,
//                'period':300,
//                'viewType':'normal'}
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
        @POST("instruments/Service/GetTechincalData")
        Call<ResponseBody> getPairData(@Field("pairID") String pairId,
                                             @Field("period") String period,
                                             @Field("viewType") String viewType); // = normal
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
    void doPairRequest(String pairId) {
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
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        Server server = retrofit.create(Server.class);

        Call<ResponseBody> call = server.getPairData(pairId, "300", "normal");
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!response.isSuccessful()){ Log.d(TAG, "onResponse: "+response.message());}
                else {
                    Log.d(TAG, "onResponseTechData: ok "+ response.headers().values("Set-Cookie"));
                    try {
                        mOnResponseListener.onResponseTechData(response.body().string());
//                        Log.d(TAG, "onResponse: ok "+ response.body().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

//                    for (String s : response.headers().values("Set-Cookie")) {
//                        if (s.toUpperCase().contains("PHPSESSID")) {
//                            phpSessId = s.split("; ", 0)[0];
//                            Log.d(TAG, "onResponse: " + phpSessId);
//                            preferences.edit().putString("phpSessID", phpSessId).apply();
//                        }
//                        if (s.contains("StickySession")) {
//                            stickySess = s.split("; ", 0)[0];
//                            Log.d(TAG, "onResponse: " + stickySess);
//                            preferences.edit().putString("stickySess", stickySess).apply();
//                        }
//                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

//        Call<MyResponseResult> call = server.searchRequest(text, text, "0", "All");
//        Log.d(TAG, "doRequest: request = " + call.request().headers().toMultimap());
//
//        call.enqueue(new Callback<MyResponseResult>() {
//            @Override
//            public void onResponse(@NonNull Call<MyResponseResult> call, @NonNull Response<MyResponseResult> response) {
//                if (response.isSuccessful()) {
////                    Log.d(TAG, "onResponse: ok, response : " + response.headers().toMultimap().toString());
////                    Log.d(TAG, "onResponse: body : " + response.body().toString());
//                    mOnResponseListener.onResponse(response.body());
//                } else {
//                    Log.d(TAG, "onResponse: " + response.message());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<MyResponseResult> call, @NonNull Throwable t) {
//                t.printStackTrace();
//            }
//        });
    }

    private class ResponseInterceptorTechData implements Interceptor {
//        POST /instruments/Service/GetTechincalData HTTP/1.1
//        Host: www.investing.com
//        Connection: keep-alive
//        Content-Length: 37
//        Accept: */*
//Origin: https://www.investing.com
//X-Requested-With: XMLHttpRequest
//User-Agent: Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36 OPR/48.0.2685.52
//Content-Type: application/x-www-form-urlencoded
//Referer: https://www.investing.com/currencies/eur-usd-technical
//Accept-Encoding: gzip, deflate, br
//Accept-Language: ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4
//Cookie: adBlockerNewUserDomains=1523214001; __qca=P0-287614633-1523214006760; G_ENABLED_IDPS=google; PHPSESSID=d2e629n1niepichm5mdfktoos5; StickySession=id.93839400626.486www.investing.com; __gads=ID=6c4e311f175a08b6:T=1526918740:S=ALNI_MazRBkGYd13p5sNh9eRfWFB2Jdftw; editionPostpone=1526918746362; r_p_s_n=1; geoC=RU; gtmFired=OK; _gat=1; _gat_allSitesTracker=1; _hjIncludedInSample=1; SideBlockUser=a%3A2%3A%7Bs%3A10%3A%22stack_size%22%3Ba%3A1%3A%7Bs%3A11%3A%22last_quotes%22%3Bi%3A8%3B%7Ds%3A6%3A%22stacks%22%3Ba%3A1%3A%7Bs%3A11%3A%22last_quotes%22%3Ba%3A2%3A%7Bi%3A0%3Ba%3A3%3A%7Bs%3A7%3A%22pair_ID%22%3Bs%3A5%3A%2240423%22%3Bs%3A10%3A%22pair_title%22%3Bs%3A0%3A%22%22%3Bs%3A9%3A%22pair_link%22%3Bs%3A19%3A%22%2Fequities%2Falrosa-ao%22%3B%7Di%3A1%3Ba%3A3%3A%7Bs%3A7%3A%22pair_ID%22%3Bs%3A1%3A%221%22%3Bs%3A10%3A%22pair_title%22%3Bs%3A14%3A%22Euro+US+Dollar%22%3Bs%3A9%3A%22pair_link%22%3Bs%3A19%3A%22%2Fcurrencies%2Feur-usd%22%3B%7D%7D%7D%7D; optimizelySegments=%7B%224225444387%22%3A%22opera%22%2C%224226973206%22%3A%22referral%22%2C%224232593061%22%3A%22false%22%2C%225010352657%22%3A%22none%22%7D; optimizelyBuckets=%7B%7D; nyxDorf=YGA3ejNmNm9lLWFsYjM0MTN8Nmw3OGdt; billboardCounter_1=1; _ga=GA1.2.747517432.1523214006; _gid=GA1.2.1215073502.1527070208; optimizelyEndUserId=oeu1523214005118r0.9853507195635554

        @Override
        public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
            okhttp3.Response response = chain.proceed(chain.request());
            okhttp3.Response modified = response.newBuilder()
                    .addHeader("Cookie", phpSessId +"; " + stickySess)
                    .build();
            return modified;
        }
    }
}
