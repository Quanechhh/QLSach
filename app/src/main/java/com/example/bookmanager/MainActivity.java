package com.example.bookmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ListView listViewBooks;
    private DatabaseHelper dbHelper;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> bookList;
    private Button add, edit, delete;
    private static final String TAG = "MainActivity";
    private int selectedBookPosition = -1;
    private static final int REQUEST_CODE_EDIT_BOOK = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listViewBooks = findViewById(R.id.listViewBooks);
        add = findViewById(R.id.btnAdd);
        edit = findViewById(R.id.btnEdit);
        delete = findViewById(R.id.btnDelete);

        dbHelper = new DatabaseHelper(this);
        bookList = new ArrayList<>();

        loadBooks();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookList);
        listViewBooks.setAdapter(adapter);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddBookActivity.class);
                startActivity(intent);
            }
        });

        listViewBooks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedBookPosition = position;
            }
        });

        listViewBooks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedBook = bookList.get(position);
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete Book")
                        .setMessage("Are you sure you want to delete this book?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                SQLiteDatabase db = dbHelper.getWritableDatabase();
                                db.delete(DatabaseHelper.TABLE_BOOKS, DatabaseHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
                                db.close();
                                bookList.remove(position);
                                adapter.notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .show();
                return true;
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBookPosition != -1) {
                    String selectedItem = bookList.get(selectedBookPosition);
                    String[] parts = selectedItem.split(" - ");
                    String title = parts[0];
                    String author = parts[1];
                    String tags = parts[2];

                    SQLiteDatabase db = dbHelper.getReadableDatabase();
                    Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKS, new String[]{DatabaseHelper.COLUMN_ID},
                            DatabaseHelper.COLUMN_TITLE + " = ? AND " + DatabaseHelper.COLUMN_AUTHOR + " = ? AND " + DatabaseHelper.COLUMN_TAGS + " = ?",
                            new String[]{title, author, tags}, null, null, null);

                    if (cursor.moveToFirst()) {
                        int bookId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ID));
                        Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
                        intent.putExtra("BOOK_ID", bookId);
                        startActivityForResult(intent, REQUEST_CODE_EDIT_BOOK);
                    }
                    cursor.close();
                    db.close();
                } else {
                    Toast.makeText(MainActivity.this, "Vui lòng chọn một cuốn sách để chỉnh sửa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedBookPosition != -1) {
                    String selectedBook = bookList.get(selectedBookPosition);
                    String[] parts = selectedBook.split(" - ");
                    final String title = parts[0];

                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Delete Book")
                            .setMessage("Are you sure you want to delete the book: " + title + "?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                                    db.delete(DatabaseHelper.TABLE_BOOKS, DatabaseHelper.COLUMN_TITLE + " = ?", new String[]{title});
                                    db.close();
                                    bookList.remove(selectedBookPosition);
                                    adapter.notifyDataSetChanged();
                                    selectedBookPosition = -1;
                                }
                            })
                            .setNegativeButton(android.R.string.no, null)
                            .show();
                } else {
                    Toast.makeText(MainActivity.this, "Please select a book to delete", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_EDIT_BOOK && resultCode == RESULT_OK && data != null) {
            boolean bookUpdated = data.getBooleanExtra("BOOK_UPDATED", false);
            if (bookUpdated) {
                loadBooks();
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void loadBooks() {
        bookList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKS, null, null, null, null, null, null);

        try {
            if (cursor.moveToFirst()) {
                do {
                    String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TITLE));
                    String author = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_AUTHOR));
                    String tags = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TAGS));
                    bookList.add(title + " - " + author + " - " + tags);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading books", e);
        } finally {
            cursor.close();
            db.close();
        }
    }
}
