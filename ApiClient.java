package com.example.cc.imagepickthroughcam;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    public static final String API_BASE_URL = "http://infosif.com/medicaASAP/index.php/api/user/";

    public static Retrofit retrofit = null;
    public static Retrofit apiclient(){
        if (retrofit == null) {
            retrofit = new Retrofit.Builder().baseUrl(API_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

}
