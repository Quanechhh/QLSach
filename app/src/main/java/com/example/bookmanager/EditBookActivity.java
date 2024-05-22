package com.example.bookmanager;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

public class EditBookActivity extends AppCompatActivity {
    private EditText editTextTitle, editTextAuthor, editTextTags;
    private Button buttonUpdateBook;
    private DatabaseHelper dbHelper;
    private int bookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_book);
        dbHelper = new DatabaseHelper(this);

        editTextTitle = findViewById(R.id.editTextTitle);
        editTextAuthor = findViewById(R.id.editTextAuthor);
        editTextTags = findViewById(R.id.editTextTags);
        buttonUpdateBook = findViewById(R.id.buttonUpdateBook);

        Intent intent = getIntent();
        bookId = intent.getIntExtra("BOOK_ID", -1);

        loadBookInfo();

        buttonUpdateBook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateBook();
            }
        });
    }

    private void loadBookInfo() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKS, null, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(bookId)}, null, null, null);

        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
            String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR));
            String tags = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAGS));

            editTextTitle.setText(title);
            editTextAuthor.setText(author);
            editTextTags.setText(tags);
        }

        cursor.close();
        db.close();
    }

    private void updateBook() {
        String title = editTextTitle.getText().toString();
        String author = editTextAuthor.getText().toString();
        String tags = editTextTags.getText().toString();

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TITLE, title);
        values.put(DatabaseHelper.COLUMN_AUTHOR, author);
        values.put(DatabaseHelper.COLUMN_TAGS, tags);

        db.update(DatabaseHelper.TABLE_BOOKS, values, DatabaseHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(bookId)});
        db.close();

        // Trả kết quả về MainActivity
        Intent intent = new Intent();
        intent.putExtra("BOOK_UPDATED", true);
        setResult(RESULT_OK, intent);
        finish();
    }
}
