package com.example.scotia_app.ui.invoices;

import android.os.Bundle;

import com.example.scotia_app.OutgoingRequestFetcher;
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

import java.util.Objects;

public class DetailedInvoiceActivity extends AppCompatActivity {

    private Invoice invoice;
    private User user;
    private FloatingActionButton confirmButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.invoice = getIntent().getParcelableExtra("invoice");
        this.user = getIntent().getParcelableExtra("user");
        this.confirmButton = findViewById(R.id.fab_confirm);

        setContentView(R.layout.activity_detailed_invoice);

        setToolbarTitle();
        configureBackButton();
        configureConfirmButton();
        setProgressBar();
        setTextViews();
    }

    private void setTextViews() {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(invoice.getStatus().toString());

        TextView invoiceIdTextView = findViewById(R.id.invoiceId);
        invoiceIdTextView.append("Invoice ID: " + invoice.getDisplayId());

        TextView customerIdTextView = findViewById(R.id.customerId);
        customerIdTextView.append("Customer: " + invoice.getCustomerName());

        TextView supplierIdTextView = findViewById(R.id.supplierId);
        supplierIdTextView.append("Supplier: " + invoice.getSupplierName());

        TextView driverIdTextView = findViewById(R.id.driverId);
        driverIdTextView.append("Driver: " + invoice.getDriverName());

        TextView totalTextView = findViewById(R.id.total);
        totalTextView.append("Total: " + invoice.getTotal());

        TextView addressTextView = findViewById(R.id.address);
        addressTextView.append("Address: " + invoice.getAddress());
    }

    private void setProgressBar() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        Status status = invoice.getStatus();
        if (status == Status.ISSUED) {
            progressBar.setProgress(25);
        } else if (status == Status.PENDING) {
            progressBar.setProgress(50);
        } else if (status == Status.PAID) {
            progressBar.setProgress(75);
        } else {
            progressBar.setProgress(100);
        }
    }

    private void setToolbarTitle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Invoice #" + invoice.getDisplayId());
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

    private void configureConfirmButton() {
        final FloatingActionButton confirmationButton = findViewById(R.id.fab_confirm);
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
                            confirmPendingOrPayment(view);
                        case customer:
                            confirmPayment(view);
                        case driver:
                            confirmDelivery(view);
                    }
                }
            });
        }
    }

    private void confirmPendingOrPayment(View view) {
        if (invoice.getStatus() == Status.ISSUED) {
            new OutgoingRequestFetcher(DetailedInvoiceActivity.this).execute(invoice.setStatusUrl(Status.PENDING));
            Snackbar.make(view, "This order has now been confirmed as PENDING.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            TextView status = findViewById(R.id.status);
            status.setText(Status.PENDING.toString());
        } else {
            confirmPayment(view);
        }
    }

    private void confirmPayment(View view) {
        new OutgoingRequestFetcher(DetailedInvoiceActivity.this).execute(invoice.setStatusUrl(Status.PAID));
        Snackbar.make(view, "This order has now been confirmed as PAID.", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        TextView status = findViewById(R.id.status);
        status.setText(Status.PAID.toString());
        confirmButton.hide();
    }

    private void confirmDelivery(View view) {
        if (invoice.getStatus() != Status.PAID) {
            Snackbar.make(view, "This order has not been paid for yet.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            new OutgoingRequestFetcher(DetailedInvoiceActivity.this).execute(invoice.setStatusUrl(Status.DELIVERED));
            Snackbar.make(view, "This order has now been confirmed as DELIVERED.",
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            TextView status = findViewById(R.id.status);
            status.setText(Status.DELIVERED.toString());
            confirmButton.hide();
        }
    }
}
