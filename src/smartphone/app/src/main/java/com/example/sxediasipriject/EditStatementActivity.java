package com.example.sxediasipriject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class EditStatementActivity extends AppCompatActivity {

    private static final String TAG = "EditStatementActivity";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseAuth mAuth;
    private String usersSelection;
    EditText nameforedit, powerforedit;
    Button save,delete;

    Spinner type;
    Map<String, Object> statement;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_statement);

        Intent intent = getIntent();
        statement = (Map<String, Object>)intent.getSerializableExtra("statement");
        nameforedit = (EditText)findViewById(R.id.nameforEdit);
        nameforedit.setText(statement.get("name").toString());
        powerforedit = (EditText)findViewById(R.id.powerforEdit);
        powerforedit.setText(statement.get("power").toString());

        save = (Button)findViewById(R.id.buttonforsave);
        delete = (Button)findViewById(R.id.buttonfordelete);

        type = (Spinner) findViewById(R.id.typeforedit);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                usersSelection = adapterView.getSelectedItem().toString();
                Log.d(TAG, "------------Choice : "+usersSelection);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                //Toast.makeText(this, "Please Choose a Type", Toast.LENGTH_LONG).show();
            }
        });
        ArrayAdapter<CharSequence> adapter=ArrayAdapter.createFromResource(this, R.array.types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        type.setAdapter(adapter);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

    }
}