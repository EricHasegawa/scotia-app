package com.example.scotia_app.ui.notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.User;
import com.example.scotia_app.ui.invoices.DetailedInvoiceActivity;

import java.util.ArrayList;
import java.util.Objects;

import org.json.*;

/**
 * Handles the control of notifications sent to users.
 */
public class NotificationsFragment extends Fragment {

    private static User user;
    private static Long lastTimeUserClicked = null;
    private static Long clickTime;

    /**
     * A List of JSONs corresponding to notifications to populate the notification page's ListView.
     */
    private static JSONArray notifications = new JSONArray();
    private BroadcastReceiver notificationHandler = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadNotifications(Objects.requireNonNull(getView()));

            String title = intent.getStringExtra("title");
            String message = intent.getStringExtra("message");

            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setTitle(title);
            builder.setMessage(message);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));
        setUser();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this.requireActivity()).registerReceiver(notificationHandler,
                new IntentFilter(getString(R.string.notification_received)));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this.requireActivity()).unregisterReceiver(notificationHandler);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);

        RecyclerView notificationsList = root.findViewById(R.id.notifications_list);
        notificationsList.setLayoutManager(new LinearLayoutManager(this.getContext()));

        loadNotifications(root);
        return root;
    }

    private void setUser() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            user = bundle.getParcelable("user");
        }
    }

    private void loadNotifications(View root) {
        final RecyclerView notificationsList = root.findViewById(R.id.notifications_list);

        notificationsList.setAdapter(null);
        NotificationFetcher notificationFetcher = new NotificationFetcher(getActivity());
        notificationFetcher.showProgressBar();
        notificationFetcher.execute(user.getNotificationURL());
    }

    /**
     * Fetches and parses the invoice data for the logged-in user.
     */
    private static class NotificationFetcher extends DataFetcher {

        /**
         * Initialize a new InvoicesFetcher, which runs in the given context.
         *
         * @param context The context in which this InvoicesFetcher runs.
         */
        NotificationFetcher(Activity context) {
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
                notifications = createJSONObjects(rawJsons.get(0));
                AppCompatActivity context = (AppCompatActivity) super.getActivityWeakReference().get();

                hideProgressBar();
                showPlaceholder(context);
                refreshInvoicesList(context);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        /**
         * Reloads the given list of invoices to be shown to user
         *
         * @param context the context in which the invoices exist
         */
        private void refreshInvoicesList(AppCompatActivity context) {
            RecyclerView notificationsList = context.findViewById(R.id.notifications_list);
            if (notificationsList != null) {
                NotificationAdapter notificationAdapter = new NotificationAdapter(context);
                notificationsList.setAdapter(notificationAdapter);
                notificationAdapter.notifyDataSetChanged();
            }
        }


        /**
         * Shows no notifications if none are present
         *
         * @param context The context in which notifications exist.
         */
        private void showPlaceholder(AppCompatActivity context) {
            TextView textView = context.findViewById(R.id.textView_placeholder);

            if (notifications.length() == 0) {
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Fetches and parses the invoice data for the logged-in user.
     */
    private static class InvoiceFetcher extends DataFetcher {

        /**
         * Initialize a new InvoicesFetcher, which runs in the given context.
         *
         * @param context The context in which this InvoicesFetcher runs.
         */
        InvoiceFetcher(Activity context) {
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
            Intent showDetailedInvoice = new Intent(super.getActivityWeakReference().get().getApplicationContext(), DetailedInvoiceActivity.class);
            JSONObject invoiceJson = super.createJSONObject(rawJsons.get(0));
            Invoice invoice = new Invoice(invoiceJson);
            showDetailedInvoice.putExtra("invoice", invoice);
            showDetailedInvoice.putExtra("user", user);
            hideProgressBar();
            super.getActivityWeakReference().get().startActivity(showDetailedInvoice);
        }
    }

    /**
     * Allows notifications to be properly manipulated and displayed
     */
    private static class NotificationAdapter extends RecyclerView.Adapter<NotificationViewHolder> {

        private Activity context;

        private NotificationAdapter(Activity context) {
            this.context = context;
        }

        @NonNull
        @Override
        public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_layout, parent, false);
            return new NotificationViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull NotificationViewHolder holder, final int position) {
            View view = holder.itemView;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickTime = System.currentTimeMillis();
                    if (lastTimeUserClicked == null || clickTime - lastTimeUserClicked > 1000) {
                        try {
                            String url = "http://us-central1-scotiabank-app.cloudfunctions.net/";
                            url += "get-invoice?id=" +
                                    notifications.getJSONObject(position).getString("invoice_id");

                            InvoiceFetcher invoiceFetcher = new InvoiceFetcher(context);
                            invoiceFetcher.showProgressBar();
                            invoiceFetcher.execute(url);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        lastTimeUserClicked = clickTime;
                    }
                }
            });

            if (position % 2 == 0) {
                view.setBackgroundColor(context.getColor(R.color.white));
            } else {
                view.setBackgroundColor(context.getColor(R.color.lightGray));
            }

            TextView textId = view.findViewById(R.id.textView_message);
            TextView textStatus = view.findViewById(R.id.textView_date);

            try {
                JSONObject jsonObject = notifications.getJSONObject(position);
                String id = jsonObject.getString("message");
                String status = jsonObject.getString("date");

                textId.setText(id);
                textStatus.setText(status);

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
            return notifications.length();
        }
    }

    private static class NotificationViewHolder extends RecyclerView.ViewHolder {
        NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}