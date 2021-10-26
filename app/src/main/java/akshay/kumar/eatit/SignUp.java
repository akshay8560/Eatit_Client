package akshay.kumar.eatit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import akshay.kumar.eatit.Model.User;

public class SignUp extends AppCompatActivity {
    EditText edtPhone, edtName, edtPassword;
    Button SignUp;
    String phoneNumber;
    ImageButton showHideBtn;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        edtPhone = findViewById(R.id.editPhoneNumber);
        edtName = findViewById(R.id.editName);
        edtPassword = findViewById(R.id.editPassword);
        SignUp = findViewById(R.id.btnSignUp);
        showHideBtn = findViewById(R.id.showHideBtn);

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference table_user = database.getReference("User");

        SignUp.setOnClickListener(new View.OnClickListener( ) {
            @Override
            public void onClick(View v) {
                final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                mDialog.setMessage("Please Wait....");
                mDialog.show();
                table_user.addValueEventListener(new ValueEventListener( ) {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.child(edtPhone.getText().toString()).exists()){

                            mDialog.dismiss();
                            Toast.makeText(akshay.kumar.eatit.SignUp.this, "mobile number already exists", Toast.LENGTH_SHORT).show( );

                        }else {
                            mDialog.dismiss();
                            User user=new User( edtName.getText().toString(),edtPassword.getText().toString() );
                            table_user.child(edtPhone.getText().toString()).setValue(user);
                            Toast.makeText(SignUp.this, "Sign Up Successfull", Toast.LENGTH_SHORT).show( );
                            finish();
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