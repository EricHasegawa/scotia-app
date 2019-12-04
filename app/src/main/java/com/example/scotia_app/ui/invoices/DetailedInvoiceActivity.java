package com.example.scotia_app.ui.invoices;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.example.scotia_app.database.OutgoingRequest;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.data.model.Persona;
import com.example.scotia_app.data.model.Status;
import com.example.scotia_app.data.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.view.View;
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
    private BroadcastReceiver notificationTokenUpdatedHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String url = intent.getStringExtra("url");
            new OutgoingRequest(DetailedInvoiceActivity.this).execute(url);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                new IntentFilter(getString(R.string.notification_received)));
        LocalBroadcastManager.getInstance(this).registerReceiver(notificationTokenUpdatedHandler,
                new IntentFilter(getString(R.string.notification_token_updated)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationHandler);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationTokenUpdatedHandler);
    }

    // Displays all of the detailed invoice information via textViews.
    private void setTextViews() {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(invoice.getStatus().toString());

        TextView invoiceIdTextView = findViewById(R.id.invoiceId);
        invoiceIdTextView.append(invoice.getDisplayId());

        TextView customerIdTextView = findViewById(R.id.customerId);
        customerIdTextView.append("Customer: " + invoice.getCustomerName());

        TextView supplierIdTextView = findViewById(R.id.supplierId);
        supplierIdTextView.append(invoice.getSupplierName());

        TextView driverIdTextView = findViewById(R.id.driverId);
        driverIdTextView.append("Driver: " + invoice.getDriverName());

        TextView totalTextView = findViewById(R.id.total);
        totalTextView.append("Total: " + invoice.getTotal());

        TextView addressTextView = findViewById(R.id.address);
        addressTextView.append("Address: " + invoice.getAddress());

        TextView ordersHeaderTextView = findViewById(R.id.ordersHeader);
        ordersHeaderTextView.append("Orders:");

        TextView ordersTextView = findViewById(R.id.orders);
        ArrayList<String> orders = invoice.getOrders();
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
        Objects.requireNonNull(getSupportActionBar()).setTitle("Invoice " + invoice.getDisplayId());
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

    // Allows users to confirm the states/status of a given delivery.
    private void configureConfirmButton() {
        final FloatingActionButton confirmationButton = findViewById(R.id.fab_confirm);
        boolean isIssued = user.getPersona() == Persona.supplier && invoice.getStatus() ==
                Status.ISSUED;
        boolean isPending = (user.getPersona() == Persona.customer || user.getPersona() ==
                Persona.supplier) && invoice.getStatus() == Status.PENDING;
        boolean isPaid = user.getPersona() == Persona.driver &&
                invoice.getStatus() == Status.PAID;

        if (isIssued) {
            confirmationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatus(Status.PENDING, view);
                }
            });
        } else if (isPending) {
            confirmationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatus(Status.PAID, view);
                }
            });
        } else if (isPaid) {
            confirmationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setStatus(Status.DELIVERED, view);
                }
            });
        } else {
            confirmationButton.hide();
        }
    }

    private void setStatus(Status status, View view) {
        new OutgoingRequest(this).execute(invoice.setStatusUrl(status));
        Snackbar.make(view, "This order has now been confirmed as PENDING.",
                Snackbar.LENGTH_LONG).setAction("Action", null).show();
        TextView statusText = findViewById(R.id.status);
        statusText.setText(status.toString());
        updateProgressBar();
    }
}
