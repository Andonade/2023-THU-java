package com.mide.news;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class FilterActivity extends AppCompatActivity {

    private EditText wordsEditText;

    private EditText startDateText;

    private EditText endDateText;

    private Button okButton;

    private Spinner spinner;

    private String category_selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        wordsEditText = findViewById(R.id.et_keyword);
        startDateText = findViewById(R.id.et_start_date);
        endDateText = findViewById(R.id.et_end_date);
        okButton = findViewById(R.id.btn_ok);
        spinner = findViewById(R.id.spinner_category);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                category_selected = adapterView.getItemAtPosition(pos).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                category_selected = "全部";
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                filterOK();
            }
        });

        actionBarConfiguration();
    }

    private void filterOK() {
        Intent intent = new Intent();
        intent.putExtra("words", wordsEditText.getText().toString());
        intent.putExtra("startDate", startDateText.getText().toString());
        intent.putExtra("endDate", endDateText.getText().toString());
        intent.putExtra("category", category_selected);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void actionBarConfiguration() {
        ActionBar actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("搜索页");
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}