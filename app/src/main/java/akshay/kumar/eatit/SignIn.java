package akshay.kumar.eatit;

import static com.google.android.gms.common.internal.service.Common.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.common.internal.service.Common;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import akshay.kumar.eatit.Model.User;
import io.paperdb.Paper;

public class SignIn extends AppCompatActivity {
    EditText edtPhone, edtPassword;
    Button btnSignIn;
    CheckBox ckbRemember;
    ImageButton showHideBtn;
    boolean flag = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        edtPassword = findViewById(R.id.editPassword);
        edtPhone = findViewById(R.id.editPhoneNumber);
        btnSignIn = findViewById(R.id.btnSignIn);

        showHideBtn = findViewById(R.id.showHideBtn);

        showHideBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag == true) {
                    flag = false;
                    edtPassword.setTransformationMethod(null);
                    if (edtPassword.getText().length() > 0)
                        edtPassword.setSelection(edtPassword.getText().length());

                } else {
                    flag = true;
                    edtPassword.setTransformationMethod(new PasswordTransformationMethod());
                    if (edtPassword.getText().length() > 0)
                        edtPassword.setSelection(edtPassword.getText().length());

                }
            }
        });


        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                mDialog.setMessage("Please Wait....");
                mDialog.show();
                table_user.addValueEventListener(new ValueEventListener( ) {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            mDialog.dismiss();
                            if (snapshot.child(edtPhone.getText().toString()).exists()){

                                User user=snapshot.child(edtPhone.getText().toString()).getValue(User.class);
                                user.setPhone(edtPhone.getText().toString());
                                if (user.getPassword().equals(edtPassword.getText().toString())){
                                    Intent homeIntent = new Intent(SignIn.this, Home.class);
                                    akshay.kumar.eatit.Common.Common.currentUser=user;
                                    startActivity(homeIntent);
                                    finish();
                                }else{
                                    Toast.makeText(SignIn.this, "Sign in Failed", Toast.LENGTH_SHORT).show( );
                                }
                            }else {
                                Toast.makeText(SignIn.this, "User not exist", Toast.LENGTH_SHORT).show( );
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



            }
        });

    }
}