package com.example.sxediasipriject;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ReviewStatements extends Fragment implements View.OnClickListener{

    private FirebaseAuth mAuth;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ConstraintLayout layoutForCards;
    TableLayout table;
    List<Map<String,Object>> statements;
    private static final String TAG = "HomeActivity";
    String currentID;
    public ReviewStatements() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_review_statements, container, false);
        mAuth = FirebaseAuth.getInstance();
        statements = new ArrayList<>();
        layoutForCards = (ConstraintLayout) rootView.findViewById(R.id.constraintLayoutForCards);
        table  = (TableLayout) rootView.findViewById(R.id.tableForStatements);

        getData();

        return rootView;
    }

    private void createRows(){

        if(statements.size()== 0){
            Log.d(TAG, "ZERO STATEMENTS");
        }else{
            for(int i=0; i<statements.size(); i++){
                currentID = (String) statements.get(i).get("id");
                TableRow row = new TableRow(getContext());
                row.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.MATCH_PARENT,
                        TableRow.LayoutParams.WRAP_CONTENT));
                TextView text1 = new TextView(getContext());
                TextView text2 = new TextView(getContext());
                TextView text3 = new TextView(getContext());
                Button button = new Button(getContext());

                text1.setText((String)statements.get(i).get("name"));
                text1.setWidth(70);
                text1.setGravity(Gravity.CENTER);
                text1.setPadding(0, 20,20,20);

                GeoPoint location = (GeoPoint)statements.get(i).get("location");
                text2.setWidth(70);
                text2.setText(location.toString());
                text2.setGravity(Gravity.CENTER);
                text2.setPadding(0, 20,20,20);

                Timestamp time = (Timestamp)statements.get(i).get("date");
                text3.setWidth(70);
                text3.setText(time.toDate().toString());
                text3.setGravity(Gravity.CENTER);
                text3.setPadding(0, 20,20,20);

                button.setText("#");
                button.setWidth(30);
                button.setGravity(Gravity.CENTER);
                button.setPadding(0, 20,20,20);
                button.setOnClickListener(this);

                row.addView(text1);
                row.addView(text2);
                row.addView(text3);
                row.addView(button);
                table.addView(row);
            }

        }

    }


    synchronized private void getData(){

db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("statementFolders").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
    @Override
    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
        List<DocumentSnapshot> snapshotList;
        snapshotList = queryDocumentSnapshots.getDocuments();

        if(snapshotList.isEmpty()){

        }else{
            Log.d(TAG, "SIZE IS : "+snapshotList.size());
            for(DocumentSnapshot snapshot : snapshotList) {
                Map<String, Object> document = snapshot.getData();
                document.put("id", snapshot.getId());
                statements.add(document);
                Log.d(TAG, "ADDEDD"+"  --------------------------------------------------------");
            }
            createRows();
        }
    }
});


    }

    @Override
    public void onClick(View view) {
        db.collection("Users").document(mAuth.getCurrentUser().getUid()).collection("statementFolders").document(currentID).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                Map<String, Object> document = documentSnapshot.getData();
                Map<String, Object> document2 = new HashMap<>();
                document2.put("name" , document.get("name"));
                document2.put("power" , document.get("power"));
                document2.put("type" , document.get("type"));
                Intent intent = new Intent(getActivity(), EditStatementActivity.class);
                intent.putExtra("statement", (Serializable) document2);
                startActivity(intent);
            }
        });
    }
}