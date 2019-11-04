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
        configureEmailButton();

        TextView invoiceId = findViewById(R.id.invoiceId);
        invoiceId.append("Invoice ID: " + invoice.getInvoice_id());

        TextView customerId = findViewById(R.id.customerId);
        customerId.append("Customer ID: " + invoice.getCustomer_id());

        TextView supplierId = findViewById(R.id.supplierId);
        supplierId.append("Supplier ID: " + invoice.getSupplier_id());

        TextView driverId = findViewById(R.id.driverId);
        driverId.append("Driver ID: " + invoice.getDriver_id());

        TextView status = findViewById(R.id.status);
        status.append("Order Status: " + invoice.getStatus());

        TextView total = findViewById(R.id.total);
        total.append("Total: " + invoice.getTotal());

    }

    private void configureBackButton() {
        FloatingActionButton backButton = findViewById(R.id.fab_back); {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    private void configureEmailButton() {
        FloatingActionButton fab = findViewById(R.id.fab_email);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Email me this invoice (DOESNT WORK YET)", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

}
