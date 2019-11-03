package com.example.scotia_app.ui.invoices;

import android.os.Bundle;

import com.example.scotia_app.Invoice;
import com.example.scotia_app.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.scotia_app.R;

import org.json.JSONObject;

public class DetailedInvoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        User user = getIntent().getParcelableExtra("user");
        Invoice invoice = getIntent().getParcelableExtra("invoice");
        setContentView(R.layout.activity_detailed_invoice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        configureBackButton();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Email me this invoice (DOESNT WORK YET)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        TextView invoiceId = (TextView)findViewById(R.id.invoiceId);
        invoiceId.setText("Invoice ID: " + invoice.getInvoice_id());

        TextView customerId = (TextView)findViewById(R.id.customerId);
        customerId.setText("Customer ID: " + invoice.getCustomer_id());

        TextView supplierId = (TextView)findViewById(R.id.supplierId);
        supplierId.setText("Supplier ID: " + invoice.getSupplier_id());

        TextView driverId = (TextView)findViewById(R.id.driverId);
        driverId.setText("Driver ID: " + invoice.getDriver_id());

        TextView status = (TextView)findViewById(R.id.status);
        status.setText("Order Status: " + invoice.getStatus());

        TextView total = (TextView)findViewById(R.id.total);
        total.setText("Total: " + invoice.getTotal());


    }

    private void configureBackButton() {
        Button backButton = (Button) findViewById(R.id.backButton); {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

}
