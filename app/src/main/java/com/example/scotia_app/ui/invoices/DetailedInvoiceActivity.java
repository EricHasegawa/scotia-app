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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.scotia_app.R;

import java.util.HashMap;
import java.util.Map;
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
        updateTextViews();
        configureOrders();
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
    private void updateTextViews() {
        TextView statusTextView = findViewById(R.id.status);
        statusTextView.setText(invoice.getStatus().toString());

        TextView supplierIdTextView = findViewById(R.id.supplier);
        supplierIdTextView.setText(invoice.getSupplierName());

        TextView invoiceIdTextView = findViewById(R.id.invoiceId);
        invoiceIdTextView.setText(invoice.getDisplayId());

        TextView customerIdTextView = findViewById(R.id.customer);
        customerIdTextView.setText(invoice.getCustomerName());

        TextView driverIdTextView = findViewById(R.id.driver);
        driverIdTextView.setText(invoice.getDriverName());

        TextView addressTextView = findViewById(R.id.address);
        addressTextView.setText(invoice.getAddress());
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

    private void configureOrders() {
        RecyclerView ordersList = findViewById(R.id.orders_list);
        ordersList.setLayoutManager(new LinearLayoutManager(this));
        OrderAdapter orderAdapter = new OrderAdapter();
        ordersList.setAdapter(orderAdapter);
    }

    private void setToolbarTitle() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Invoice " + invoice.getDisplayId());
    }

    private void configureBackButton() {
        FloatingActionButton backButton = findViewById(R.id.fab_back);
        {
            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }
    }

    // Allows users to confirm the status of a given delivery.
    private void configureConfirmButton() {
        final FloatingActionButton confirmationButton = findViewById(R.id.fab_confirm);
        final boolean isIssued = user.getPersona() == Persona.supplier && invoice.getStatus() ==
                Status.ISSUED;
        final boolean isPending = (user.getPersona() == Persona.customer || user.getPersona() ==
                Persona.supplier) && invoice.getStatus() == Status.PENDING;
        final boolean isPaid = user.getPersona() == Persona.driver &&
                invoice.getStatus() == Status.PAID;
        boolean shouldDisplayInvoice = (isIssued || isPending || isPaid);

        if (shouldDisplayInvoice) {
            confirmationButton.show();
            confirmationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isIssued) {
                        invoice.setStatus(Status.PENDING);
                    } else if (isPending) {
                        invoice.setStatus(Status.PAID);
                    } else {
                        invoice.setStatus(Status.DELIVERED);
                    }
                    updateStatus(view);
                }
            });
        } else {
            confirmationButton.hide();
        }
    }

    private void updateStatus(View view) {
        new OutgoingRequest(this).execute(invoice.getStatusSetterUrl());
        Snackbar.make(view, "This order has now been confirmed as " +
                invoice.getStatus().toString() + ".", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        updateProgressBar();
        updateTextViews();
    }

    // Allows orders to be properly manipulated and displayed
    private class OrderAdapter extends RecyclerView.Adapter<OrderViewHolder> {

        @NonNull
        @Override
        public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_layout, parent, false);
            return new OrderViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
            View background = holder.itemView;
            TextView nameTextView = holder.itemView.findViewById(R.id.name);
            TextView quantityTextView = holder.itemView.findViewById(R.id.quantity);
            TextView unitPriceTextView = holder.itemView.findViewById(R.id.unit_price);
            TextView totalPriceTextView = holder.itemView.findViewById(R.id.total_price);

            if (position == 0) {
                background.setBackgroundColor(getColor(R.color.black));
                nameTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                nameTextView.setTextColor(getColor(R.color.white));
                quantityTextView.setTextColor(getColor(R.color.white));
                unitPriceTextView.setTextColor(getColor(R.color.white));
                totalPriceTextView.setTextColor(getColor(R.color.white));
            } else if (position == invoice.getOrders().size() + 1) {
                background.setBackgroundColor(getColor(R.color.black));
                unitPriceTextView.setTextColor(getColor(R.color.white));
                unitPriceTextView.setText(getString(R.string.total));
                totalPriceTextView.setTextColor(getColor(R.color.white));
                totalPriceTextView.setText(invoice.getTotal());
            } else {
                if (position % 2 == 0) {
                    background.setBackgroundColor(getColor(R.color.white));
                }
                Map<String, String> order = invoice.getOrders().get(position - 1);

                nameTextView.setText(order.get("name"));
                quantityTextView.setText(order.get("quantity"));
                unitPriceTextView.setText(order.get("unit_price"));
                totalPriceTextView.setText(order.get("total_price"));
            }
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return invoice.getOrders().size() + 2;
        }
    }

    private class OrderViewHolder extends RecyclerView.ViewHolder {

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
        }

    }
}
