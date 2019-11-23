package com.example.scotia_app.ui.invoices;

import android.app.Activity;
import android.os.Bundle;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.data.model.Persona;
import com.example.scotia_app.data.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.scotia_app.R;

import java.util.ArrayList;

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
        assert user != null;
        configureConfirmButton(invoice, user);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        String curr_state = invoice.getStatus();
        if (curr_state.equals("ISSUED")) {
            progressBar.setProgress(33);
        } else if (curr_state.equals("PENDING")) {
            progressBar.setProgress(66);
        } else {
            progressBar.setProgress(100);
        }

        TextView invoiceId = findViewById(R.id.invoiceId);
        invoiceId.append("Invoice ID: " + invoice.getInvoice_id());

        TextView customerId = findViewById(R.id.customerId);
        customerId.append("Customer ID: " + invoice.getCustomer_id());

        TextView supplierId = findViewById(R.id.supplierId);
        supplierId.append("Supplier ID: " + invoice.getSupplier_id());

        TextView driverId = findViewById(R.id.driverId);
        driverId.append("Driver ID: " + invoice.getDriver_id());

        TextView status = findViewById(R.id.status);
        status.append("Order is currently " + curr_state);

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

    private void configureConfirmButton(final Invoice invoice, final User user) {
        FloatingActionButton fab = findViewById(R.id.fab_confirm);
        if (user.getPersona() == Persona.customer) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
                    url += "confirm-payment?id=" + invoice.getInvoice_id();
                    new ConfirmFetcher(DetailedInvoiceActivity.this).execute(url);
                    Snackbar.make(view, "This order has now been confirmed.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            });
        } else {
            fab.hide();
        }
    }

    /**
     * Sends this device's notification id to the database
     */
    private static class ConfirmFetcher extends DataFetcher {

        /**
         * Initialize a new NotificationFetcher, which runs in the given context.
         *
         * @param context The context in which this UserFetcher runs.
         */
        public ConfirmFetcher(Activity context) {
            super(context);
        }

        /**
         * After super.doInBackground is finished executing, do nothing. This method is only written
         * since it has to be overridden
         *
         * @param rawJsons The list of raw json strings whose first element is the user data
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {  }
    }
}
