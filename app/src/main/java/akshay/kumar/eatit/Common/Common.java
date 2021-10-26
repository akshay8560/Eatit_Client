package akshay.kumar.eatit.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import akshay.kumar.eatit.Model.User;
import akshay.kumar.eatit.Remote.APIService;
import akshay.kumar.eatit.Remote.GoogleRetrofitClient;
import akshay.kumar.eatit.Remote.IGeoCoordinates;
import akshay.kumar.eatit.Remote.IGoogleService;
import akshay.kumar.eatit.Remote.RetrofitClient;

public class Common {
    public static User currentUser;
    public static String PHONE_TEXT="userPhone";
    public static final String baseUrl = "https://fcm.googleapis.com/";
    public static final String GOOGLE_API_URL = "https://maps.googleapis.com/";
    public static final String DELETE = "Delete";
    public static final String USER_KEY = "User";
    public static final String PWD_KEY = "Password";

    public static String convertCodeToStatus(String status) {
        if (status.equals("0"))
            return "Placed";
        else if (status.equals("1"))
            return "On my way";
        else
            return "Shipped";

    }
    public static APIService getFCMService() {
        return RetrofitClient.getClient(baseUrl).create(APIService.class);
    }
    public static IGoogleService getGoogleMapAPI() {
        return GoogleRetrofitClient.getGoogleClient(GOOGLE_API_URL).create(IGoogleService.class);
    }
    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                        return true;
                }
            }
        }
        return false;
    }
}
