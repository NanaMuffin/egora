package app.egora.Login;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import app.egora.Communities.CommunitiesActivity;
import app.egora.ItemManagement.HomeActivity;
import app.egora.R;

public class LoginActivity extends AppCompatActivity {

    private Button buttonLogin;
    private TextView linkRegisterNow;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private DocumentReference userRef;

    // wird noch nicht verwendet ******************************
    private EditText editEmail;
    private EditText editPassword;
    private TextView linkForgotPassword;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Checking Loginstatus
        if(mAuth.getCurrentUser() != null){
            //Sprung zur Datenbankactivity
        }
        getSupportActionBar().hide(); // hide the app title bar

        // Declaration
        buttonLogin = findViewById(R.id.buttonLogin);
        linkRegisterNow = findViewById(R.id.textRegister);
        editEmail = findViewById(R.id.logEmail);
        editPassword = findViewById(R.id.logPassword);
        linkForgotPassword = findViewById(R.id.textForgotPassword);
        progressDialog = new ProgressDialog(this);


        // Login with firebase auth
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("login", "login button clicked"); // nur zum Testen
                // hier Login mit Firebase Auth ausführen
                userLogin();

            }
        });

        // Switch activity to register
        linkRegisterNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("register", "switch to registerview"); // nur zum Testen
                Intent intent = new Intent(getBaseContext(), CreateAccountActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }

    //User-Login-method
    private void userLogin(){
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        //Validating Inputs
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Please insert a valid email address", Toast.LENGTH_LONG).show();
            return;
        }
        else if (TextUtils.isEmpty(password)){
            Toast.makeText(this, "Please insert a Password", Toast.LENGTH_LONG).show();
            return;
        }

        //Showing Dialog
        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        //Sign-In
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){

                    userRef = db.collection("users").document(mAuth.getUid());
                    userRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            //Checking if Community exists
                            if(!documentSnapshot.contains("communityName")){

                                    progressDialog.dismiss();
                                    Intent intent = new Intent(getBaseContext(), CommunitiesActivity.class);
                                    startActivity(intent);
                                    finish();
                            }
                            else {
                                //Checking if the current Community was in the changing state
                                String userCommunity = documentSnapshot.get("communityName").toString();
                                Log.d("UserCommunity: ", userCommunity);
                                if(userCommunity.equals("changing") ){

                                    progressDialog.dismiss();
                                    Intent intent = new Intent(getBaseContext(), CommunitiesActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                                else {
                                    //Sending the User to the homescreen
                                    progressDialog.dismiss();
                                    Intent intent = new Intent(getBaseContext(), HomeActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        }
                    });

                }
                else {
                    progressDialog.dismiss();
                    Toast.makeText(LoginActivity.this, "Something went wrong!", Toast.LENGTH_LONG).show();
                }
            }
        });


    }


}
