package com.example.scotia_app.ui.invoices;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.scotia_app.R;

import java.util.ArrayList;
import java.util.Objects;

public class DetailedInvoiceActivity extends AppCompatActivity {

    private Invoice invoice;
    private User user;
    private BroadcastReceiver notificationHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            AlertDialog.Builder builder = new AlertDialog.Builder(DetailedInvoiceActivity.this);
            builder.setTitle(title);
            builder.setMessage(message);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_intent_filter)));

        this.invoice = getIntent().getParcelableExtra("invoice");
        this.user = getIntent().getParcelableExtra("user");

        setContentView(R.layout.activity_detailed_invoice);

        setToolbarTitle();
        configureBackButton();
        configureConfirmButton();
        updateProgressBar();
        setTextViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_intent_filter)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationHandler);
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

        TextView ordersHeaderTextView = findViewById(R.id.ordersHeader);
        ordersHeaderTextView.append("Orders:");

        TextView ordersTextView = findViewById(R.id.orders);
        ArrayList<String> orders = new ArrayList<String>();
        orders =invoice.getOrders();
        for (int i = 0; i < orders.size(); i++) {
            ordersTextView.append("- " + orders.get(i) + "\n");
        }

    }

    private void updateProgressBar() {
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
        boolean isNotAlreadyDelivered = invoice.getStatus() != Status.DELIVERED;
        boolean isIssued = user.getPersona() == Persona.supplier && invoice.getStatus() ==
                Status.ISSUED;
        boolean isPending = (user.getPersona() == Persona.customer || user.getPersona() ==
                Persona.supplier) && invoice.getStatus() == Status.PENDING;
        boolean isPaid = user.getPersona() == Persona.driver &&
                invoice.getStatus() == Status.PAID;

        if (isNotAlreadyDelivered && (isPending || isIssued || isPaid)) {
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
        } else {
            confirmationButton.hide();
        }
    }

    private void confirmPendingOrPayment(View view) {
        if (invoice.getStatus() == Status.ISSUED) {
            new OutgoingRequestFetcher(DetailedInvoiceActivity.this).execute(invoice.setStatusUrl(Status.PENDING));
            Snackbar.make(view, "This order has now been confirmed as PENDING.", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            TextView status = findViewById(R.id.status);
            status.setText(Status.PENDING.toString());
            updateProgressBar();
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
        FloatingActionButton confirmButton = findViewById(R.id.fab_confirm);
        confirmButton.hide();
        updateProgressBar();
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
            FloatingActionButton confirmButton = findViewById(R.id.fab_confirm);
            confirmButton.hide();
            updateProgressBar();
        }
    }
}
