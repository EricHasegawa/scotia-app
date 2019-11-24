package com.example.scotia_app.ui.invoices;

import android.app.Activity;
import android.os.Bundle;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.data.model.Persona;
import com.example.scotia_app.data.model.Status;
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

        assert invoice != null;
        getSupportActionBar().setTitle("Invoice #" + invoice.getDisplayId());

        configureBackButton();

        assert user != null;
        configureConfirmButton(invoice, user);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        switch (invoice.getStatus()) {
            case ISSUED:
                progressBar.setProgress(25);
            case PENDING:
                progressBar.setProgress(50);
            case PAID:
                progressBar.setProgress(75);
            case DELIVERED:
                progressBar.setProgress(100);
        }

        TextView invoiceId = findViewById(R.id.invoiceId);
        invoiceId.append("Invoice ID: " + invoice.getId());

        TextView customerId = findViewById(R.id.customerId);
        customerId.append("Customer ID: " + invoice.getCustomerName());

        TextView supplierId = findViewById(R.id.supplierId);
        supplierId.append("Supplier ID: " + invoice.getSupplierName());

        TextView driverId = findViewById(R.id.driverId);
        driverId.append("Driver ID: " + invoice.getDriverName());

        TextView status = findViewById(R.id.status);
        status.append("Order has been " + invoice.getStatus());

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
        FloatingActionButton confirmationButton = findViewById(R.id.fab_confirm);
        boolean isAlreadyPaid = (user.getPersona() == Persona.customer || user.getPersona() ==
                Persona.supplier) && invoice.getStatus() == Status.PAID;
        boolean isAlreadyDelivered = user.getPersona() == Persona.driver &&
                invoice.getStatus() == Status.DELIVERED;
        boolean cannotBeDelivered = user.getPersona() == Persona.driver &&
                invoice.getStatus() != Status.PAID;

        if (isAlreadyDelivered || isAlreadyPaid || cannotBeDelivered) {
            confirmationButton.hide();
        } else {
            confirmationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (user.getPersona()) {
                        case supplier:
                            confirmPayment(invoice, view);
                        case customer:
                            confirmPayment(invoice, view);
                        case driver:
                            confirmDelivery(invoice, view);
                    }
                }
            });
        }
    }

    private void confirmPayment(Invoice invoice, View view) {
        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
        url += "confirm-payment?id=" + invoice.getId();
        new ConfirmFetcher(DetailedInvoiceActivity.this).execute(url);
        Snackbar.make(view, "This order has now been confirmed as PAID.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        TextView status = findViewById(R.id.status);
        status.clearComposingText();
        status.setText("Order has been PAID");
    }

    private void confirmDelivery(Invoice invoice, View view) {
        String url = "https://us-central1-scotiabank-app.cloudfunctions.net/";
        if (invoice.getStatus() != Status.PAID) {
            Snackbar.make(view, "This order has not been paid for yet.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            url += "confirm-delivery?id=" + invoice.getId();
            new ConfirmFetcher(DetailedInvoiceActivity.this).execute(url);
            Snackbar.make(view, "This order has now been confirmed as DELIVERED.",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            TextView status = findViewById(R.id.status);
            status.setText("Order has been DELIVERED");
        }
    }

    /**
     * Sends this device's notification id to the database
     */
    private static class ConfirmFetcher extends DataFetcher {

        /**
         * Initialize a new NotificationTokenFetcher, which runs in the given context.
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
