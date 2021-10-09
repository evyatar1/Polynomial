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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class PolynomialsView extends AppCompatActivity {
    private ListView polsList;
    private ArrayAdapter adapter;
    private final String DB_NAME = "PolynomialProject", TABLE_NAME = "Polynomials";
    private SQLiteDatabase polDB;
    private ArrayList<tableRow> rows;
    private ArrayList<ArrayList<tableRow>> funcsDivided;
    private ArrayList<String> functions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_polynomials_view);

        polDB = this.openOrCreateDatabase(DB_NAME, MODE_PRIVATE, null); // open/create the DB
        polsList = findViewById(R.id.polsList);
        functions = new ArrayList<>();

        buildPols();
        adapter = new ArrayAdapter(this, R.layout.item2, functions);
        polsList.setAdapter(adapter);

        polsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(PolynomialsView.this, PolynomialActions.class);
                intent.putExtra("polFormula", ((TextView) view).getText()); // send the pol formula
                ArrayList<tableRow> function = new ArrayList<>();
                for (ArrayList<tableRow> func : funcsDivided) // find the splat function that was selected
                    if (func.get(0).getFuncID() == Integer.parseInt(functions.get(position).substring(1, functions.get(position).indexOf('(')))) { // cut the i from fi(x)
                        function = func;
                        break;
                    }
                intent.putExtra("function", function); // send the pol row items (funcID,coefficient,power)
                startActivity(intent);
            }
        });
    }

    private void buildPols() {
        rows = new ArrayList<>();
        try { // initiate rows from DB
            Cursor c = polDB.rawQuery("select * from " + TABLE_NAME + ";", null); // fetch the table rows to the cursor
            if (c != null && c.moveToFirst()) // if c has been initiated successfully and the table isn't empty
                do {
                    rows.add(new tableRow(c.getInt(c.getColumnIndex("funcID")),
                            c.getDouble(c.getColumnIndex("coefficient")),
                            c.getInt(c.getColumnIndex("power"))));
                } while (c.moveToNext());
            else {
                Toast.makeText(this, "There are no polynomials at all!", Toast.LENGTH_SHORT).show();
            }
        } catch (SQLException e) {
            Toast.makeText(this, "SQL ERROR!", Toast.LENGTH_SHORT).show();
        }

        createFunctionsList();
    }

    @Override
    public void onStart() {
        super.onStart();
        buildPols(); // if activity is opened again (after pressing return) than update the list
        for (int i = 0; i < functions.size(); i++)
            for (int j = i + 1; j < functions.size(); j++)
                if (functions.get(i).charAt(1) == functions.get(j).charAt(1)) {
                    functions.set(i, functions.get(j)); //switch with i and j to keep the order if fi(x)
                    functions.remove(j); // remove duplicates
                }

        adapter = new ArrayAdapter(this, R.layout.item2, functions);
        polsList.setAdapter(adapter);
    }

    private void createFunctionsList() {
        functions = new ArrayList<>();// initiate functions list
        divideFuncs(); // split functions (list for each function)
        for (ArrayList<tableRow> function : funcsDivided) // create string formulas
            functions.add(buildFunctionFormula(function));
    }

    private void divideFuncs() {
        funcsDivided = new ArrayList<>();

        for (int i = 0, j = 0; i < rows.size(); j++) {
            funcsDivided.add(new ArrayList<>()); // create new function j (as arrayList)
            funcsDivided.get(j).add(rows.get(i)); // add the first coefficient to function j

            for (i++; i < rows.size();i++) // insert coefficients
                if (rows.get(i).getPower()==0) // if we are on the first coefficient of the current func
                    break; // or if we checking the first coefficient
                else
                    funcsDivided.get(j).add(rows.get(i)); // add the current coefficient to function j

            if (funcsDivided.get(j).isEmpty()) // if the function is empty here it means that it's degree is 0 (1 coefficient)
                funcsDivided.get(j).add(rows.get(i)); // add that single function coefficient
        }
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
        formula = formula.replace("-  -", "-"); // fix duplicate negative signs

        if (formula.substring(formula.indexOf('=')).equals("= "))
            formula += '0';

        return formula;
    }

    private String getNumber(double num) {
        if (num % 1 == 0) // if it's an integer
            return "" + (int) num;
        return "" + num;
    }
}