package app.egora.Utils;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.shashank.sony.fancytoastlib.FancyToast;

import app.egora.ItemManagement.ItemActivity;
import app.egora.Model.Chat;
import app.egora.Model.Message;

public class FirestoreUtil {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private String newID;
    private String returnString;

    public FirestoreUtil() {
         newID = "none";
         returnString = "null";
    }

    private static boolean exists;

    public static Query getUserChatsQuery(String userID) {
        return db.collection("chats").whereEqualTo("userID", userID).orderBy("lastActivity", Query.Direction.DESCENDING);
    }

    public static Query getMessagesQuery(String chatID) {
        return db.collection("chats").document(chatID).collection("messages").orderBy("date", Query.Direction.ASCENDING);
    }

    public static String getCurrentUserID() {
        return mAuth.getCurrentUser().getUid();
    }

    public static String getCurrentUserName() {
        return mAuth.getCurrentUser().getDisplayName();
    }

    public static void deleteChat(String chatID, String otherChatID) {
        db.collection("chats").document(otherChatID).update("otherChatID", "none");
        db.collection("chats").document(chatID)
                .delete()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error FUtil 3: ", e.toString());
                    }
                });
    }


    public static void createAndSendMessage(String chatID, String otherChatID, String textMessage) {
        Message message = new Message(getCurrentUserID(), textMessage);
        //chat A
        db.collection("chats").document(chatID).collection("messages").document().set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error FUtil 1a: ", e.toString());
                    }
                });

        db.collection("chats").document(chatID).update("lastActivity", message.getDate(), "lastMessageText", message.getText())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error FUtil 2a: ", e.toString());
                    }
                });

        //chat B
        db.collection("chats").document(otherChatID).collection("messages").document().set(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error FUtil 1b: ", e.toString());
                    }
                });

        db.collection("chats").document(otherChatID).update("lastActivity", message.getDate(), "lastMessageText", message.getText())
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("error FUtil 2b: ", e.toString());
                    }
                });
    }

    public static void signOut() {
        mAuth.signOut();
    }

}
