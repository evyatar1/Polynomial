package com.example.polynomial;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

public class PolynomialActions extends AppCompatActivity {
    private String polFormula;
    private final String DB_NAME = "PolynomialProject", TABLE_NAME = "Polynomials";
    private SQLiteDatabase polDB;
    private TextView funcTitle;
    private ListView cList;
    private MyAdapter adapter;
    private ArrayList<tableRow> function;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polynomial_actions);

        polDB = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); // open/create the DB
        funcTitle = findViewById(R.id.funcTitle); // title integration
        cList = findViewById(R.id.polsList); // integrate the list of constants

        Intent intent = getIntent();
        polFormula = intent.getStringExtra("polFormula"); // get the selected formula from the previous activity
        function =  (ArrayList<tableRow>)intent.getSerializableExtra("function"); // get the selected function from the previous activity

        funcTitle.setText(polFormula);
        funcTitle.setTypeface(null, Typeface.BOLD);

        buildList();
    }

    private void buildList() {
        adapter = new MyAdapter();
        cList.setAdapter(adapter);
    }

    public void updateThePol(View view) {
        String c;
        for (int i = 0; i < adapter.myItems.size(); i++) { // update DB
            c = ((ListItem) adapter.getItem(i)).getCaption();
            if(!c.equals("") && !c.equals("-") && c.charAt(0) != 'a') {
                polDB.execSQL("update " + TABLE_NAME + " set funcID=" + function.get(0).getFuncID() +
                        ", coefficient=" + c +
                        ", power=" + i +
                        " where funcID=" + function.get(0).getFuncID() + " and power=" + i + ";");
                function.get(i).setCoefficient(Double.parseDouble(c)); // update the splat function
            }
        }

        polFormula=buildFunctionFormula(function); // update the formula
        updateTitle(); // update the function title

        buildList(); //update the list

        Toast.makeText(this, "The polynomial updated successfully!", Toast.LENGTH_SHORT).show();
    }

    public void deleteThePol(View view) { // delete the whole function from DB (by it's ID)
        polDB.execSQL("delete from " + TABLE_NAME + " where funcID=" + polFormula.charAt(1));

        Intent intent = new Intent(this, PolynomialsView.class); // create a "mover" intent
        startActivity(intent); // move to the previous activity
        Toast.makeText(this,"The polynomial has been deleted successfully!",Toast.LENGTH_SHORT).show();
    }

    public class MyAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        private ArrayList<ListItem> myItems = new ArrayList();

        public MyAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Cursor c = polDB.rawQuery("select count(*)-1 degree from " + TABLE_NAME + " where funcID=" + polFormula.charAt(1) + ";", null);
            c.moveToFirst(); // get the degree of the function
            int degree = c.getInt(c.getColumnIndex("degree"));
            c = polDB.rawQuery("select coefficient c from " + TABLE_NAME + " where funcID=" + polFormula.charAt(1), null);
            c.moveToFirst(); // get the coefficients of the function
            for (int i = 0; i <= degree; i++, c.moveToNext()) {// insert title to each item (edit text)
                ListItem listItem = new ListItem();
                listItem.caption = "a" + i + " = " + c.getDouble(c.getColumnIndex("c"));
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

    private void updateTitle() {
        funcTitle.setText(polFormula); //update the function in the title
    }

    public void gotoGraph(View view)
    {
        Intent intent=new Intent(this,Graph.class); //move to graph view
        intent.putExtra("function",function); // send the splat function
        intent.putExtra("title",polFormula); // send the splat function
        startActivity(intent);
    }

    private String buildFunctionFormula(ArrayList<tableRow> function) {
        String formula = "f" + function.get(0).getFuncID() + "(x) = ";
        for (int i = 0; i < function.size(); i++)
            if (function.get(i).getCoefficient() == 0) // skip zeros
                continue;
            else if (function.get(i).getCoefficient() > 0)
                if (function.get(i).getCoefficient() == 1) // if c=1
                    if (function.get(i).getPower() == 0) // constant
                        formula += " + " + getNumber(function.get(i).getCoefficient());
                    else if (function.get(i).getPower() == 1) // 1*x = x
                        formula += " + x";
                    else // 1*x^n = x^n
                        formula += " + x^" + function.get(i).getPower();
                else //if c>0 ^ c!=1
                    if (function.get(i).getPower() == 0) // c
                        formula += " + " + getNumber(function.get(i).getCoefficient());
                    else if (function.get(i).getPower() == 1) // c*x
                        formula += " + " + getNumber(function.get(i).getCoefficient()) + "x";
                    else // c*x^n
                        formula += " + " + getNumber(function.get(i).getCoefficient()) + "x^" + function.get(i).getPower();
            else // c<0
                if (function.get(i).getCoefficient() == -1) // if c=-1
                    if (function.get(i).getPower() == 0) // constant
                        formula += " - " + getNumber(function.get(i).getCoefficient());
                    else if (function.get(i).getPower() == 1) // -1*x = -x
                        formula += " - x";
                    else // 1*x^n = x^n
                        formula += " - x^" + function.get(i).getPower();
                else //if c<0 ^ c!=-1
                    if (function.get(i).getPower() == 0) // c
                        formula += " " + getNumber(function.get(i).getCoefficient());
                    else if (function.get(i).getPower() == 1) // c*x
                        formula += " " + getNumber(function.get(i).getCoefficient()) + "x";
                    else // c*x^n
                        formula += " " + getNumber(function.get(i).getCoefficient()) + "x^" + function.get(i).getPower();
        formula = formula.replace("  ", " "); // fix spaces
        formula = formula.replace("= +", "= "); // fix first positive coefficient
        formula = formula.replace("-", "- "); // fix first negative coefficient space

        if(formula.substring(formula.indexOf('=')).equals("= "))
            formula+='0';
        return formula;
    }

    private String getNumber(double num) {
        if (num % 1 == 0) // if it's an integer
            return "" + (int) num;
        return "" + num;
    }
}
