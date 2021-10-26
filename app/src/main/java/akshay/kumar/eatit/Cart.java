package akshay.kumar.eatit;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.NumberFormat;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.internal.ui.AutocompleteImplFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import akshay.kumar.eatit.Common.Common;
import akshay.kumar.eatit.Common.Config;
import akshay.kumar.eatit.Database.Database;
import akshay.kumar.eatit.Model.MyResponse;
import akshay.kumar.eatit.Model.Notification;
import akshay.kumar.eatit.Model.Order;
import akshay.kumar.eatit.Model.Request;
import akshay.kumar.eatit.Model.Sender;
import akshay.kumar.eatit.Model.Token;
import akshay.kumar.eatit.Remote.APIService;
import akshay.kumar.eatit.Remote.IGoogleService;
import akshay.kumar.eatit.ViewHolder.CartAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class Cart extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks
,GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final int PAYPAL_REQUEST_CODE =9999;
    private static final int LOCATION_REQUEST_CODE =9999;
    private static final int PLAY_SERVICE_REQUEST =9997;
    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;
    FirebaseDatabase database;
    DatabaseReference requests;
    TextView txtTotalPrice;
    Button btnPlace;
    Context mContext;
    Place shipingAddress;
    APIService mService;
    List<Order> cart = new ArrayList<>();
    CartAdapter adapter;
    private static View view;
    static PayPalConfiguration config=new PayPalConfiguration().environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId(Config.PAYPAL_CLIENT_ID);
    String address,comment;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    IGoogleService mIGoogleService;
   private static final int UPDATE_INTERVAL=5000;
    private static final int LATEST_INTERVAL=3000;
    private static final int DISPLACEMENT=10;


    private Object CompoundButton;
    private Object RadioButton;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

       mIGoogleService=Common.getGoogleMapAPI();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
        ){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            },LOCATION_REQUEST_CODE);
        }else {
            if (CheckplayServices()){
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        Intent intent=new Intent( this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
        startService(intent);

        mService=Common.getFCMService();
        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        txtTotalPrice = findViewById(R.id.total);
        btnPlace = findViewById(R.id.btnPlaceOrder);

        btnPlace.setOnClickListener(v -> {
            if (cart.size() > 0)
            {
                showAlertDialog();
            }
            else
            {
                Toast.makeText(Cart.this, "Your Cart is Empty", Toast.LENGTH_SHORT).show();
            }
        });

        loadListFood();
    }

    private void createLocationRequest() {
        mLocationRequest=new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(LATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean CheckplayServices() {
        int resultcode= GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultcode!= ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultcode)){
                GooglePlayServicesUtil.getErrorDialog(resultcode,this,PLAY_SERVICE_REQUEST).show();
            }else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                 finish();

            }
            return false;
        }
        return true;
    }

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more Step!");
        alertDialog.setMessage("Enter Delivery Address: ");

        LayoutInflater inflater=this.getLayoutInflater();
        View order_address_comment=inflater.inflate(R.layout.order_address_comment,null);
        final MaterialEditText edtAddress=(MaterialEditText)order_address_comment.findViewById(R.id.editAddress);
        final MaterialEditText edtComment=(MaterialEditText)order_address_comment.findViewById(R.id.editComment);







      /*  if (!Places.isInitialized()) {
            Places.initialize(this, getString(R.string.google_maps_key));
        }
        AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();

        // Specify the fields to return.
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG);
        AutocompleteSupportFragment edtAddress = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.place_auto_complete_fragment);
        edtAddress.getView().findViewById(R.id.places_autocomplete_search_button).setVisibility(View.GONE);


        ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).
                setHint("Enter your address");

        ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input)).
                setTextSize(20);
        edtAddress.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        edtAddress.setOnPlaceSelectedListener(new PlaceSelectionListener( ) {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
               // shipingAddress=place;
               Toast.makeText(Cart.this, "Place: " + place.getName() + ", " + place.getId(), Toast.LENGTH_SHORT).show( );

            }

            @Override
            public void onError(@NonNull Status status) {

                Toast.makeText(Cart.this, status.getStatusMessage(), Toast.LENGTH_SHORT).show( );
            }
        });


        final RadioButton rdiShipAddress=(RadioButton)findViewById(R.id.rdiShpAddress);
        final RadioButton rdiHomeAddress=(RadioButton)findViewById(R.id.homeAddress);*/
       // final RadioButton rdiPaypal=(RadioButton)findViewById(R.id.rdiPaypal);
        //final RadioGroup radioGroup=(RadioGroup) findViewById(R.id.radio_group_your_choices);
     //   final RadioButton rdiCod=(RadioButton)findViewById(R.id.rdiCOD);


       /* if(CompoundButton!=null){
            rdiHomeAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener( ) {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b){
                        if (Common.currentUser.getHomeAddress()!=null||
                                !TextUtils.isEmpty(Common.currentUser.getHomeAddress())){
                                address=Common.currentUser.getHomeAddress();

                           ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input))
                                    .setText(address);


                        }else {
                            Toast.makeText(Cart.this, "Please Update home address", Toast.LENGTH_SHORT).show( );

                        }

                    }
                }
            });
            rdiShipAddress.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked){
                        mIGoogleService.getAddressName(String.format("https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,\n" +
                                        "+Mountain+View,+CA&key=AIzaSyDEC6mdKYrElYDfZUjDcyp7dL7ntncyqVA",
                                mLastLocation.getLatitude(),mLastLocation.getLongitude())).enqueue(new Callback<String>( ) {
                            @Override
                            public void onResponse(Call<String> call, Response<String> response) {

                                try {
                                    JSONObject jsonObject=new JSONObject( response.body().toString() );
                                    JSONArray resultArray=jsonObject.getJSONArray("results");

                                    JSONObject firstObject=resultArray.getJSONObject(1);
                                    address=firstObject.getString("formatted_address");


                                    ((EditText)edtAddress.getView().findViewById(R.id.places_autocomplete_search_input))
                                            .setText(address);
                                } catch (JSONException e) {
                                    e.printStackTrace( );
                                }
                            }

                            @Override
                            public void onFailure(Call<String> call, Throwable t) {

                                Toast.makeText(Cart.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show( );
                            }
                        });
                    }
                }


            });
        }
*/



        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);


        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {


            @Override
            public void onClick(DialogInterface dialog, int which) {
                //address=Common.currentUser.getHomeAddress();
                //address= shipingAddress.getAddress().toString();
                comment=edtComment.getText().toString();
                address=edtAddress.getText().toString();

                  /*  if (!rdiShipAddress.isChecked()&& !rdiHomeAddress.isChecked()) {
                        if (rdiHomeAddress != null){
                            address=Common.currentUser.getHomeAddress();
                            // address = shipingAddress.getAddress();
                        }else {
                            Toast.makeText(Cart.this, "Please enter address or select option address", Toast.LENGTH_SHORT).show( );
                            getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.place_auto_complete_fragment)).commit();
                            return;
                        }
                    }*/
                    /*if (TextUtils.isEmpty(address)){
                        Toast.makeText(Cart.this, "Please enter address ", Toast.LENGTH_SHORT).show( );
                        getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(R.id.place_auto_complete_fragment)).commit();
                        return;
                    }*/
               /* String formatAmount=txtTotalPrice.getText().toString()
                        .replace("$","")
                        .replace(",","");

                PayPalPayment payPalPayment=new PayPalPayment(new BigDecimal(formatAmount),
                        "USD","Eat It App Order",
                        PayPalPayment.PAYMENT_INTENT_SALE);
                Intent intent=new Intent( getApplicationContext(), PaymentActivity.class);
                intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
                intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
                startActivityForResult(intent,PAYPAL_REQUEST_CODE);*/

                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        edtAddress.getText().toString(),
                        txtTotalPrice.getText().toString(),
                        "0",
                        edtComment.getText().toString()
                        ,cart
                );

                String order_number=String.valueOf(String.valueOf(System.currentTimeMillis()));
                requests.child(order_number).setValue(request);
                //deleting cart


                new Database(getBaseContext()).cleanCart();
                SendNotificationOrder(order_number);

                Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                finish();

                   /* if (!rdiCod.isChecked() &&!rdiPaypal.isChecked() ){
                        Toast.makeText(Cart.this, "Please select Payment Option", Toast.LENGTH_SHORT).show( );

                       *//* getSupportFragmentManager().beginTransaction()
                                .remove(getSupportFragmentManager().
                                        findFragmentById(R.id.place_auto_complete_fragment)).commit();*//*
                        return;
                    }*/

                   /* else if(rdiPaypal.isChecked()){
                        String formatAmount=txtTotalPrice.getText().toString()
                                .replace("$","")
                                .replace(",","");

                        PayPalPayment payPalPayment=new PayPalPayment(new BigDecimal(formatAmount),
                                "USD","Eat It App Order",
                                PayPalPayment.PAYMENT_INTENT_SALE);
                        Intent intent=new Intent( getApplicationContext(), PaymentActivity.class);
                        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION,config);
                        intent.putExtra(PaymentActivity.EXTRA_PAYMENT,payPalPayment);
                        startActivityForResult(intent,PAYPAL_REQUEST_CODE);
                    }*/

               // String order_number=String.valueOf(String.valueOf(System.currentTimeMillis()));
               /* requests.child(order_number).setValue(request);
                //deleting cart
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        address,
                        txtTotalPrice.getText().toString(),
                        "0",
                        comment
                        ,"COD",
                        "Unpaid",
                        String.format("%s,%s",mLastLocation.getLatitude(),mLastLocation.getLongitude())
                        ,cart
                );
*/

               /* new Database(getBaseContext()).cleanCart();
                Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                finish();
*/
                /*
                    else if (rdiCod.isChecked()){




                    }*/








            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                /*getSupportFragmentManager().beginTransaction().remove(getSupportFragmentManager().findFragmentById(
                        R.id.place_auto_complete_fragment
                )).commit();*/
            }
        });
        alertDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:{
                if (grantResults.length>0&& grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    if (CheckplayServices()){
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (requestCode == RESULT_OK) {

                PaymentConfirmation confirmation=data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation!=null){
                    try {
                        String paymentDetail=confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject=new JSONObject(paymentDetail);

                      /*  Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                address,
                                txtTotalPrice.getText().toString(),
                                "0",
                                comment,
                                "Paypal",
                                jsonObject.getJSONObject("response").getString("state"),
                                String.format("%s,%s",shipingAddress.getLatLng().latitude,shipingAddress.getLatLng().longitude)
                                ,cart
                        );


                        String order_number=String.valueOf(String.valueOf(System.currentTimeMillis()));
                        requests.child(order_number).setValue(request);
                        //deleting cart


                        new Database(getBaseContext()).cleanCart();
                        SendNotificationOrder(order_number);

                        Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                        finish();*/



                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
            }
            else if (resultCode== Activity.RESULT_CANCELED){
                Toast.makeText(this, "Payment Cancel", Toast.LENGTH_SHORT).show( );
            }else if (resultCode==PaymentActivity.RESULT_EXTRAS_INVALID){
                Toast.makeText(this, "Invalid Payment", Toast.LENGTH_SHORT).show( );
            }
        }
    }

    private void SendNotificationOrder(String order_number) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query data=tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapShot:snapshot.getChildren()){
                    Token serverToken=postSnapShot.getValue( Token.class );

                    Notification notification=new Notification( "EAT IT","You have new order "+ order_number );
                    Sender content=new Sender(serverToken.getToken(),notification);

                   mService.sendNotification(content).enqueue(new Callback<MyResponse>( ) {
                       @Override
                       public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                           if (response.body().success==1){
                               Toast.makeText(Cart.this, "Thank You! Order Place", Toast.LENGTH_SHORT).show();
                               finish();
                           }else {
                               Toast.makeText(Cart.this, "Failed !!", Toast.LENGTH_SHORT).show();

                           }
                       }

                       @Override
                       public void onFailure(Call<MyResponse> call, Throwable t) {
                           Toast.makeText(Cart.this, "Error"+t.getMessage(), Toast.LENGTH_SHORT).show( );
                       }
                   });

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadListFood() {
        cart = new Database(this).getCarts();
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //calculating total price
        int total = 0;
        for (Order order : cart)
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);

        txtTotalPrice.setText(fmt.format(total));


    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle() == Common.DELETE)
            deleteCart(item.getOrder());
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void deleteCart(int order) {

        cart.remove(order);

        new Database(this).cleanCart();
        // and finally , we will update  new data from List<order> to Sqlite
        for (Order item : cart)
            new Database(this).addToCart(item);

        loadListFood();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();
    }

    private void startLocationUpdate() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
        ){
            return;
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED
        ){
            return;
        }else {
            mLastLocation=LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if(mLastLocation!=null){
                Toast.makeText(this, "Your Location "+mLastLocation.getLatitude()+""+mLastLocation.getLongitude(), Toast.LENGTH_SHORT).show( );
            }else {
                Toast.makeText(this, "Couldn't get your location", Toast.LENGTH_SHORT).show( );
            }
        }


    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
       mLastLocation=location;
        displayLocation();

    }
}
