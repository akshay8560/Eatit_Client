package akshay.kumar.eatit;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;

import java.util.Arrays;

import akshay.kumar.eatit.Common.Common;
import akshay.kumar.eatit.Database.Database;
import akshay.kumar.eatit.Model.Food;
import akshay.kumar.eatit.Model.Order;
import akshay.kumar.eatit.Model.Rating;


public class FoodDetails extends AppCompatActivity implements RatingDialogListener {

    TextView food_name, food_price, food_description;
    ImageView food_image;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart,btnRating;
    ElegantNumberButton numberButton;


    String foodId = "";
    Food currentFood;


    RatingBar ratingBar;
    FirebaseDatabase database;
    DatabaseReference foods;
    DatabaseReference ratingTbl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_details);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        ratingTbl=database.getReference("Rating");
        //init view
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnRating= findViewById(R.id.btn_rating);
        ratingBar= findViewById(R.id.ratingBar);
        btnRating.setOnClickListener(v -> showRatingDialog());

        btnCart.setOnClickListener(v -> {
            new Database(getBaseContext()).addToCart(new Order(
                    foodId,
                    currentFood.getName(),
                    numberButton.getNumber(),
                    currentFood.getPrice(),
                    currentFood.getDiscount()
            ));
            Toast.makeText(FoodDetails.this, "Added to Cart", Toast.LENGTH_SHORT).show();
        });

        food_name = findViewById(R.id.food_name);
        food_image = findViewById(R.id.img_food);
        food_description = findViewById(R.id.food_description);
        food_price = findViewById(R.id.food_price);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        // get food id from intent
        if (getIntent() != null)
            foodId = getIntent().getStringExtra("FoodId");
        if (!foodId.isEmpty() && foodId != null) {
            if (Common.isConnectedToInternet(getBaseContext()))
            {
                getDetailFood(foodId);
                getRatingFood(foodId);
            }
            else {
                Toast.makeText(this, "Please Check the Internet Connection", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void getRatingFood(String foodId) {
        Query foodRating=ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating.addValueEventListener(new ValueEventListener( ) {
            int count=0,sum=0;
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
              for (DataSnapshot postSnapshot:snapshot.getChildren()){
                 Rating item=postSnapshot.getValue( Rating.class );
                  assert item != null;
                  sum += Integer.parseInt(item.getRateValue());
                  count++;
              }
            if (count !=0){
                 float average;
                average = sum/count;
                ratingBar.setRating(average);
             }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very Bad","Not Good","Quite Ok","Very Good","Excellent"))
                .setDefaultRating(1)
                .setTitle("Rate this food")
                .setDescription("Please select some stars and give your feedback")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Comment here...")
                .setHintTextColor(R.color.purple_200)
                .setCommentTextColor(android.R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.MyDialogFadeAnimation)
                .create(FoodDetails.this)
                .show();

    }

    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                // setting the image from firebase into appbar;
                Picasso.get().load(currentFood.getImage()).into(food_image);
                //set title in appbar
                collapsingToolbarLayout.setTitle(currentFood.getName());

                food_price.setText(currentFood.getPrice());
                food_description.setText(currentFood.getDescription());
                food_name.setText(currentFood.getName());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }

    @Override
    public void onPositiveButtonClicked(int value, @NonNull String comments) {

    Rating rating=new Rating(Common.currentUser.getPhone(),foodId, String.valueOf(value),comments);
        ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener( ) {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child(Common.currentUser.getPhone()).exists()){
                    ratingTbl.child(Common.currentUser.getPhone()).removeValue();
                    //update new value
                }
                ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
                Toast.makeText(FoodDetails.this, "Thank you for submit rating!", Toast.LENGTH_SHORT).show( );
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
