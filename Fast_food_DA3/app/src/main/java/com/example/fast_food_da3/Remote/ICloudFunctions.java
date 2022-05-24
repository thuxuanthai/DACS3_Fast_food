package com.example.fast_food_da3.Remote;

import io.reactivex.Observable;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ICloudFunctions {
    @GET("")
    Observable<Response> getCustomToken(
            @Query("access_token") String accessToken);
}