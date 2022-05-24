package com.example.server_fast_food_da3.Remote;

import com.example.server_fast_food_da3.Model.FCMResponse;
import com.example.server_fast_food_da3.Model.FCMSendData;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAApu18PiU:APA91bGXEpcU04ogQnQiJs7uCNXMTrkGjEabZVZdK8-f3XGnkN3mnWQ9MDLF3RlBiN98o0x3-hkE49RAF_lGDgWmi-vfjOrzYknqHmS3OYEobSZzK5TweTOTJsnM-vIZBmK6LBQKJLYJ"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}

