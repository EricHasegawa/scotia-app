package com.example.scotia_app.ui.notifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Status;
import com.example.scotia_app.data.model.User;
import com.example.scotia_app.ui.invoices.DetailedInvoiceActivity;

import java.util.ArrayList;

import org.json.*;

public class NotificationsFragment extends Fragment {

    private User user;
    private Long lastTimeUserClicked = null;
    private Long clickTime;

    /**
     * A List of JSONs corresponding to notifications to populate the notification page's ListView.
     */
    private static JSONArray notifications = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUser();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_notifications, container, false);
        configureShowDetailedInvoiceWhenTapped(root);
        loadNotifications(root);
        return root;
    }

    private void setUser() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.user = bundle.getParcelable("user");
        }
    }

    /**
     * Shows the Detailed Invoice Activity corresponding to the tapped Invoice
     */
    private void configureShowDetailedInvoiceWhenTapped(View root) {
        final ListView listView = root.findViewById(R.id.notifications_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickTime = System.currentTimeMillis();
                if(lastTimeUserClicked==null || clickTime - lastTimeUserClicked > 1000) {
                    Intent showDetailedInvoice = new Intent(getActivity(), DetailedInvoiceActivity.class);
                    try {
                        Invoice invoice = new Invoice(notifications.getJSONObject(position));
                        showDetailedInvoice.putExtra("invoice", invoice);
                        showDetailedInvoice.putExtra("user", user);
                        startActivity(showDetailedInvoice);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    lastTimeUserClicked = clickTime;
                }
            }
        });
    }

    private void loadNotifications(View root) {
        final ListView listView = root.findViewById(R.id.notifications_list);
        TextView textView = root.findViewById(R.id.textView_placeholder);

        textView.setText(null);
        listView.setAdapter(null);

        if (this.user != null) {
            new NotificationFetcher(this.getActivity()).execute(user.getNotificationURL());
        }
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

                showPlaceholder(context);
                refreshInvoicesList(context);

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        private void refreshInvoicesList(AppCompatActivity context) {
            ListView listView = context.findViewById(R.id.notifications_list);
            if (listView != null) {
                NotificationAdapter notificationAdapter = new NotificationAdapter(context, notifications);
                listView.setAdapter(notificationAdapter);
                notificationAdapter.notifyDataSetChanged();
            }
        }

        private void showPlaceholder(AppCompatActivity context) {
            TextView textView = context.findViewById(R.id.textView_placeholder);
            textView.setVisibility(View.VISIBLE);

//            if (notifications.length() == 0) {
//                textView.setVisibility(View.VISIBLE);
//            } else {
//                textView.setVisibility(View.GONE);
//            }
        }
    }

    private static class NotificationAdapter extends BaseAdapter {

        private Context context;
        private JSONArray notificationJson;

        private NotificationAdapter(Context context, JSONArray notificationJson) {
            this.context = context;
            this.notificationJson = notificationJson;
        }

        @Override
        public int getCount() {
            return notificationJson.length();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint("ViewHolder")
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(context).inflate(R.layout.notification_layout, null);

            if (i % 2 == 0) {
                view.setBackgroundColor(context.getColor(R.color.white));
            } else {
                view.setBackgroundColor(context.getColor(R.color.lightGray));
            }

            TextView textId = view.findViewById(R.id.textView_message);
            TextView textStatus = view.findViewById(R.id.textView_date);

            try {
                JSONObject jsonObject = notificationJson.getJSONObject(i);
                String id = jsonObject.getString("message");
                String status = jsonObject.getString("date");

                textId.setText(id);
                textStatus.setText(status);

            } catch (JSONException e) {
                e.printStackTrace();
            }

            return view;
        }
    }
}