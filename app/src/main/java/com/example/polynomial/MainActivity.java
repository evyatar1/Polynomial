package com.example.polynomial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText degree;
    private ListView cList;
    private MyAdapter adapter;
    private final String DB_NAME = "PolynomialProject", TABLE_NAME = "Polynomials";
    private SQLiteDatabase polDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        degree = findViewById(R.id.title1); // degree integration to variable
        cList = findViewById(R.id.polsList); // integrate the list of constants

        try {
            polDB = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); // open/create the DB

            //polDB.execSQL("drop table " + TABLE_NAME);
            //create the pols table
            polDB.execSQL("create table if not exists " + TABLE_NAME + "(\n" +
                    "  funcID number,\n" +
                    "  coefficient number,\n" +
                    "  power number,\n" +
                    "  PRIMARY key(funcID,power)\n" +
                    "  );");
        } catch (SQLException e) {
            Toast.makeText(this, "SQL ERROR!", Toast.LENGTH_SHORT).show();
        }
        if(adapter!=null)
            adapter.notifyDataSetChanged(); // update list
    }

    public void degreeEntered(View view) {
        try {
            if (Integer.parseInt(degree.getText().toString()) < 0) throw new Exception();
        } catch (Exception e) {//check if the degree input is wrong
            Toast.makeText(this, "Wrong input for the polynomial degree!", Toast.LENGTH_SHORT).show();
            return;
        }

        cList.setItemsCanFocus(true);
        adapter = new MyAdapter();
        cList.setAdapter(adapter);
    }

    public void addNewPol(View view) {
        if(adapter==null) // if the user didn't entered any degree for the function
        {
            Toast.makeText(this, "No Polynomial Entered!", Toast.LENGTH_SHORT).show();
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++)
            if (((ListItem) adapter.getItem(i)).getCaption().equals("")) {
                Toast.makeText(this, "The coefficient a" + i + " is empty!", Toast.LENGTH_SHORT).show();
                return;
            }
        Cursor c = polDB.rawQuery("select max(funcID) maxID from " + TABLE_NAME + ";", null); // get the last function ID
        int lastFuncID;
        if (c != null && c.moveToFirst() && c.getInt(0) > 0)
            // if the table has a last function (not empty)
            lastFuncID = c.getInt(c.getColumnIndex("maxID")); // get the ID as int
        else lastFuncID = 0;


        for (int i = 0; i < adapter.getCount(); i++)
            try {
                polDB.execSQL("insert into " + TABLE_NAME + " values (" + (lastFuncID + 1) + ", " + ((ListItem) adapter.getItem(i)).getCaption() + ", " + i + ");"); // insert the ai row for each power
            } catch (SQLException e) {
                Toast.makeText(this, "SQL ERROR!", Toast.LENGTH_SHORT).show();
            }
        Toast.makeText(this, "The polynomial added successfully!", Toast.LENGTH_SHORT).show();
    }

    public void gotoExistPols(View view) {
        Intent intent = new Intent(this, PolynomialsView.class); // create a "mover" intent
        startActivity(intent); // move to the next activity
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<ListItem> myItems = new ArrayList();

        public MyAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            for (int i = 0; i <= Integer.parseInt(degree.getText().toString()); i++) {// insert title to each item (edit text)
                ListItem listItem = new ListItem();
                listItem.caption = "a" + i;
                myItems.add(listItem);
            }
            notifyDataSetChanged(); // update list
        }

        public ArrayList<ListItem> getMyItems() {
            return myItems;
        }

        public int getCount() {
            return myItems.size();
        }

        public Object getItem(int position) {
            return myItems.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.item, null);
                holder.caption = (EditText) convertView
                        .findViewById(R.id.ItemCaption);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            //Fill EditText with the value you have in data source
            holder.caption.setHint(myItems.get(position).caption);
            holder.caption.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED); // set input type to decimal
            holder.caption.setId(position);
            holder.caption.setTypeface(null, Typeface.BOLD);
            holder.caption.setTextColor(Color.BLACK);
            holder.caption.setBackgroundColor(Color.WHITE);
            holder.caption.setGravity(Gravity.CENTER);

            //we need to update adapter once we finish with editing
            holder.caption.setOnKeyListener(new View.OnKeyListener() {
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                        final int position = v.getId();
                        final EditText Caption = (EditText) v;
                        myItems.get(position).caption = Caption.getText().toString();
                        return true;
                    }
                    return false;
                }
            });
            return convertView;
        }
    }

    class ViewHolder {
        private EditText caption;
    }

    class ListItem {
        private String caption;

        public String getCaption() {
            return caption;
        }
    }
}
