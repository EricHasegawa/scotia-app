package com.example.scotia_app.ui.invoices;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.scotia_app.database.DataFetcher;
import com.example.scotia_app.data.model.Filter;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Status;
import com.example.scotia_app.data.model.User;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import org.json.*;

/**
 * Handles the full list of invoices displayed to the user.
 */
public class InvoicesFragment extends Fragment {

    private static User user;
    private static Long lastTimeUserClicked = null;
    private static Long clickTime;
    private static final int UPCOMING = 1;
    private BroadcastReceiver notificationHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getView() != null)
                loadInvoices(Filter.upcoming, getView());

            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(title);
            builder.setMessage(message);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    /**
     * A List of JSONs corresponding to invoice previews to populate the invoice page's ListView.
     */
    private static JSONArray invoices = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));
        setUser();
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.requireActivity()).unregisterReceiver(notificationHandler);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_invoices, container, false);

        RecyclerView invoicesList = root.findViewById(R.id.invoices_list);
        invoicesList.setLayoutManager(new LinearLayoutManager(getContext()));

        configureTab(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));

        View view = getView();

        if (view != null) {
            final TabLayout tabLayout = view.findViewById(R.id.filter_tabs);
            int tabNumber = tabLayout.getSelectedTabPosition();

            if (tabNumber == UPCOMING) {
                loadInvoices(Filter.upcoming, view);
            } else {
                loadInvoices(Filter.completed, view);
            }
        }
    }

    private void setUser() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            user = bundle.getParcelable("user");
        }
    }

    /**
     * Fetches and saves the full list of invoices.
     */
    private void loadInvoices(Filter filter, View root) {
        final RecyclerView invoicesList = root.findViewById(R.id.invoices_list);
        TextView textView = root.findViewById(R.id.textView_placeholder);

        textView.setText(null);
        invoicesList.setAdapter(null);

        if (user != null) {
            InvoicesFetcher invoicesFetcher = new InvoicesFetcher(getActivity());
            invoicesFetcher.showProgressBar();
            invoicesFetcher.execute(user.getInvoiceURL(filter));
        }
    }

    private void configureTab(final View root) {
        final TabLayout tabLayout = root.findViewById(R.id.filter_tabs);
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        if (getContext() != null) {
            tabLayout.setBackgroundColor(getContext().getColor(R.color.white));
        } else {
            tabLayout.setBackgroundColor(Color.WHITE);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getText() == getResources().getString(R.string.tab_upcoming)) {
                    loadInvoices(Filter.upcoming, root);
                } else {
                    loadInvoices(Filter.completed, root);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    /**
     * Fetches and parses the invoice data for the logged-in user.
     */
    private static class InvoicesFetcher extends DataFetcher {

        /**
         * Initialize a new InvoicesFetcher, which runs in the given context.
         *
         * @param context The context in which this InvoicesFetcher runs.
         */
        InvoicesFetcher(Activity context) {
            super(context);
        }

        /**
         * After super.doInBackground is finished executing, store the JSONObjects corresponding to
         * the raw strings in rawJsons in invoices, and populate the ListView with formatted strings
         * with each invoice's ID and order status.
         *
         * @param rawJsons The list of raw json strings to be parsed and used in the UI.
         */
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            try {
                invoices = super.createJSONObjects(rawJsons.get(0));
                AppCompatActivity context = (AppCompatActivity) super.getActivityWeakReference().get();

                showPlaceholder(context);
                refreshInvoicesList(context);
                hideProgressBar();

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        /**
         * Reloads the list of invoices to shows user any changes
         *
         * @param context the specific activity to refresh
         */
        private void refreshInvoicesList(AppCompatActivity context) {
            RecyclerView invoicesList = context.findViewById(R.id.invoices_list);
            if (invoicesList != null) {
                InvoiceAdapter invoiceAdapter = new InvoiceAdapter(context);
                invoicesList.setAdapter(invoiceAdapter);
                invoiceAdapter.notifyDataSetChanged();
            }
        }

        /**
         * If no invoices are to be delivered, display a default text.
         *
         * @param context the context that the invoices are stored in
         */
        private void showPlaceholder(AppCompatActivity context) {
            TextView textView = context.findViewById(R.id.textView_placeholder);

            if (invoices.length() == 0) {
                textView.setVisibility(View.VISIBLE);

                TabLayout tabLayout = context.findViewById(R.id.filter_tabs);
                int tabNumber = tabLayout.getSelectedTabPosition();
                if (tabNumber == UPCOMING) {
                    textView.setText(context.getString(R.string.placeholder_completed));
                } else {
                    textView.setText(context.getString(R.string.placeholder_upcoming));
                }
            }
        }
    }

    /**
     * Allows invoices to be properly manipulated and displayed
     */
    private static class InvoiceAdapter extends RecyclerView.Adapter<InvoiceViewHolder> {

        private Activity context;

        private InvoiceAdapter(Activity context) {
            this.context = context;
        }

        @NonNull
        @Override
        public InvoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.invoice_layout, parent, false);
            return new InvoiceViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull InvoiceViewHolder holder, final int position) {
            View view = holder.itemView;
            TextView textId = view.findViewById(R.id.textView_id);
            TextView textStatus = view.findViewById(R.id.textView_status);

            if (position % 2 == 1) {
                view.setBackgroundColor(context.getColor(R.color.white));
            }

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickTime = System.currentTimeMillis();
                    if (lastTimeUserClicked == null || clickTime - lastTimeUserClicked > 1000) {
                        Intent showDetailedInvoice = new Intent(context, DetailedInvoiceActivity.class);
                        try {
                            Invoice invoice = new Invoice(invoices.getJSONObject(position));
                            showDetailedInvoice.putExtra("invoice", invoice);
                            showDetailedInvoice.putExtra("user", user);
                            context.startActivity(showDetailedInvoice);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        lastTimeUserClicked = clickTime;
                    }
                }
            });

            try {
                JSONObject jsonObject = invoices.getJSONObject(position);
                String id = jsonObject.getString("invoice_id_short");
                String status = jsonObject.getString("status");

                textId.setText(id);
                textStatus.setText(status);

                boolean paidOrDelivered = status.equals(Status.PAID.toString()) ||
                        status.equals(Status.DELIVERED.toString());

                // Sets text color to green if invoice has been paid or delivered
                if (paidOrDelivered) {
                    textStatus.setTextColor(context.getResources().getColor(R.color.lightGreen, null));
                } else {
                    textStatus.setTextColor(context.getResources().getColor(R.color.colorPrimaryDark, null));
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return invoices.length();
        }
    }

    private static class InvoiceViewHolder extends RecyclerView.ViewHolder {
        InvoiceViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}