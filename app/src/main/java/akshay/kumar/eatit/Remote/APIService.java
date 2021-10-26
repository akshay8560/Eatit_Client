package akshay.kumar.eatit.Remote;


import akshay.kumar.eatit.Model.MyResponse;
import akshay.kumar.eatit.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public  interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAVnD4LCw:APA91bEdPJOWohEk_TqKZDXONxUzdUZ3jJOLU-Ed7uEbH69dYgKX6WnXEQSPpwR5Tj0HLv5OwkeFcNhs-mkL-Nr18kYmmXigQ2BgutL2_Af7Jrp5caoW7yRCD5-UNKENzDxtItyZcm0n"
            })
    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
