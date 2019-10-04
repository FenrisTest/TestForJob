package com.petersburg_studio.testforjob;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private StringBuilder stringBuilder = new StringBuilder();
    private PaginationController mController;
    private int currentPage = 1;
    private int lastPage = 1;
    private ActionBar actionBar;

    private static final String PAGE_SYMBOL = "symbol";

    private static final String APP_PREFERENCES = "mysettings";
    private SharedPreferences settings;

    private NestedScrollView nestedScrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        final TextView textView = findViewById(R.id.text_view);
        nestedScrollView = findViewById(R.id.nested_scroll_view);
        mController = new PaginationController(textView);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl("gs://myapplicationtest-b1047.appspot.com").child("text.txt");

        try {
            final File localFile = File.createTempFile("text", "txt");
            storageRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
                File file = new File(localFile.getAbsolutePath());

                BufferedReader reader;

                FileInputStream fileInputStream = null;
                try {
                    fileInputStream = new FileInputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                assert fileInputStream != null;
                reader = new BufferedReader(new InputStreamReader(fileInputStream));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    onTextLoaded(stringBuilder.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(exception -> Toast.makeText(this, R.string.fail_download, Toast.LENGTH_SHORT).show());
        } catch (IOException e) {
            Toast.makeText(this, R.string.download_error, Toast.LENGTH_SHORT).show();
            textView.setText(R.string.download_error);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle(getString(R.string.title_main_activity));

        nestedScrollView.setOnTouchListener(new OnSwipeTouchListener(this) {
            public void onSwipeRight() {
                mController.previous();
                if (currentPage > 1) {
                    currentPage--;
                    actionBar.setTitle("(" + currentPage + "/" + lastPage + ") " + getString(R.string.title_main_activity));
                    scrollTop();
                }
            }

            public void onSwipeLeft() {
                mController.next();
                if (currentPage < lastPage) {
                    currentPage++;
                    actionBar.setTitle("(" + currentPage + "/" + lastPage + ") " + getString(R.string.title_main_activity));
                    scrollTop();
                }
            }
        });

    }

    void scrollTop() {
        nestedScrollView.smoothScrollTo(0, 0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(PAGE_SYMBOL, currentPage);
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (settings.contains(PAGE_SYMBOL)) {
            currentPage = settings.getInt(PAGE_SYMBOL, 0);
        }
    }

    void onTextLoaded(String text) {
        mController.onTextLoaded(text, currentPage, () -> {
            int lengthText = stringBuilder.toString().length();
            lastPage = lengthText / 500 + 1;
            actionBar.setTitle("(" + currentPage + "/" + lastPage + ") " + getString(R.string.title_main_activity));
        });
    }
}
