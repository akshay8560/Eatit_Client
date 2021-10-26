package akshay.kumar.eatit;


import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.LayoutInflater;
import android.view.View;

import com.google.firebase.database.FirebaseDatabase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;

import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;

import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;


import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import akshay.kumar.eatit.Common.Common;
import akshay.kumar.eatit.Model.Category;
import akshay.kumar.eatit.Model.Token;
import akshay.kumar.eatit.ViewHolder.FoodList;
import akshay.kumar.eatit.ViewHolder.MenuViewHolder;
import io.paperdb.Paper;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseDatabase database;
    DatabaseReference category;
    TextView TextFullName;
    RecyclerView recycler_menu;
    RecyclerView.LayoutManager linearLayoutManager;
    FirebaseRecyclerAdapter<Category, MenuViewHolder> adapter;
    SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Menu");
        setSupportActionBar(toolbar);
        swipeRefreshLayout=( SwipeRefreshLayout)findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimaryDark
        , android.R.color.holo_green_dark
        , android.R.color.holo_orange_dark
        , android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener( ) {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });

        swipeRefreshLayout.post(new Runnable( ) {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext()))
                    loadMenu();
                else {
                    Toast.makeText(Home.this, "Please Check Internet Connection", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        });
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        // Init Firebase
        database = FirebaseDatabase.getInstance();
        category = database.getReference("Category");
//        category.keepSynced(true);

        // init paper
        Paper.init(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // add to cart
                Intent cartIntent = new Intent(Home.this, Cart.class);
                startActivity(cartIntent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);


//         set name for user
         View headerView = navigationView.getHeaderView(0);
         TextFullName = headerView.findViewById(R.id.txtFullName);
         TextFullName.setText(Common.currentUser.getName());

// Load menu
        recycler_menu = findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(this);
        recycler_menu.setLayoutManager(linearLayoutManager);
        loadMenu();

        // register the service
       /* Intent service = new Intent(Home.this, ListenOrder.class);
        startService(service);*/
           getFirebaseMessagingToken();
    }

    public void getFirebaseMessagingToken () {
        FirebaseMessaging.getInstance ().getToken ()
                .addOnCompleteListener ( task -> {
                    if (!task.isSuccessful ()) {
                        return;
                    }
                    if (null != task.getResult ()) {
                        //Got FirebaseMessagingToken
                        String firebaseMessagingToken = Objects.requireNonNull ( task.getResult () );
                        //Use firebaseMessagingToken further
                        FirebaseDatabase db=FirebaseDatabase.getInstance();
                        DatabaseReference tokens=db.getReference("Tokens");
                        Token data= new Token(firebaseMessagingToken,false );
                        tokens.child(Common.currentUser.getPhone()).setValue(data);
                    }
                } );
    }

    private void loadMenu() {

        FirebaseRecyclerOptions<Category> options =
                new FirebaseRecyclerOptions.Builder<Category>()
                        .setQuery(category, Category.class)
                        .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MenuViewHolder menuViewHolder, int i,
                                            @NonNull final Category category) {


                Picasso.get().load(category.getImage()).placeholder(R.drawable.loading).fit().into(menuViewHolder.imageView);

                menuViewHolder.txtMenuName.setText(category.getName());
                      final Category clickItem = category;

                menuViewHolder.setItemClickListener((view, position, isLongClick) -> {
                    // get category id and send it to the new activity.
                    Intent foodIntent = new Intent(Home.this, FoodList.class);
                    // category id is key,we just get the key of this item
                    foodIntent.putExtra("CategoryId", adapter.getRef(position).getKey());
                    startActivity(foodIntent);
                });
            }


            @NonNull
            @Override
            public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item, parent, false);
                return new MenuViewHolder(view);
            }
        };
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);

        } else {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            super.onBackPressed();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            loadMenu();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_menu) {
            Intent menuIntent = new Intent(Home.this, Home.class);
            startActivity(menuIntent);

        } else if (id == R.id.nav_cart) {
            Intent cartIntent = new Intent(Home.this, Cart.class);
            startActivity(cartIntent);

        } else if (id == R.id.nav_orders) {
           Intent orderIntent = new Intent(Home.this, OrderStatus.class);
            startActivity(orderIntent);

        }
        else if (id == R.id.nav_home_address) {
          showHomeAddressDialog();

        }
        else if (id == R.id.nav_log_out) {

            // delete remember user and password
            Paper.book().destroy();

            Intent mainActivity = new Intent(Home.this, MainActivity.class);
            mainActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(mainActivity);

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDialog= new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("Change Home Address");
        alertDialog.setMessage("please fill all information");
        LayoutInflater  inflater=LayoutInflater.from(this);
        View layout_home=inflater.inflate(R.layout.home_address_layout,null);
        final MaterialEditText edtHomeAddress=(MaterialEditText) layout_home.findViewById(R.id.editHomeAddress);
        alertDialog.setView(layout_home);
        alertDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener( ) {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           dialog.dismiss();
           Common.currentUser.setHomeAddress(edtHomeAddress.getText().toString());

           FirebaseDatabase.getInstance().getReference("User").child(Common.currentUser.getPhone())
                   .setValue(Common.currentUser)
                   .addOnCompleteListener(new OnCompleteListener<Void>( ) {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {

                           Toast.makeText(Home.this, "Update address successful", Toast.LENGTH_SHORT).show( );
                       }
                   });
            }
        });
        alertDialog.show();
    }
}
