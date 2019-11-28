package com.example.scotia_app.ui.invoices;

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
import com.example.scotia_app.data.model.Filter;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.Status;
import com.example.scotia_app.data.model.User;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

import org.json.*;

public class InvoicesFragment extends Fragment {

    private User user;
    private Long lastTimeUserClicked = null;
    private Long clickTime;
    private static final int UPCOMING = 1;

    /**
     * A List of JSONs corresponding to invoice previews to populate the invoice page's ListView.
     */
    private static JSONArray invoices = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUser();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_invoices, container, false);
        configureShowDetailedInvoiceWhenTapped(root);
        configureTabClicked(root);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
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
            this.user = bundle.getParcelable("user");
        }
    }

    private void loadInvoices(Filter filter, View root) {
        final ListView listView = root.findViewById(R.id.invoices_list);
        TextView textView = root.findViewById(R.id.textView_placeholder);

        textView.setText(null);
        listView.setAdapter(null);

        if (this.user != null) {
            InvoicesFetcher invoicesFetcher = new InvoicesFetcher(getActivity());
            invoicesFetcher.showProgressBar();
            invoicesFetcher.execute(user.getInvoiceURL(filter));
        }
    }

    /**
     * Shows the Detailed Invoice Activity corresponding to the tapped Invoice
     */
    private void configureShowDetailedInvoiceWhenTapped(View root) {
        final ListView listView = root.findViewById(R.id.invoices_list);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickTime = System.currentTimeMillis();
                if(lastTimeUserClicked==null || clickTime - lastTimeUserClicked > 1000) {
                    Intent showDetailedInvoice = new Intent(getActivity(), DetailedInvoiceActivity.class);
                    try {
                        Invoice invoice = new Invoice(invoices.getJSONObject(position));
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

    private void configureTabClicked(final View root) {
        final TabLayout tabLayout = root.findViewById(R.id.filter_tabs);
        Objects.requireNonNull(tabLayout.getTabAt(1)).select();
        tabLayout.setBackgroundColor(getContext().getColor(R.color.white));

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
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
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

        private void refreshInvoicesList(AppCompatActivity context) {
            ListView listView = context.findViewById(R.id.invoices_list);
            if (listView != null) {
                InvoiceAdapter invoiceAdapter = new InvoiceAdapter(context);
                listView.setAdapter(invoiceAdapter);
                invoiceAdapter.notifyDataSetChanged();
            }
        }

        private void showPlaceholder(AppCompatActivity context) {
            TextView textView = context.findViewById(R.id.textView_placeholder);

            System.out.println(invoices.length());

            if (invoices.length() == 0) {
                TabLayout tabLayout = context.findViewById(R.id.filter_tabs);
                int tabNumber = tabLayout.getSelectedTabPosition();
                if (tabNumber == UPCOMING) {
                    textView.setText(context.getString(R.string.placeholder_upcoming));
                } else {
                    textView.setText(context.getString(R.string.placeholder_completed));
                }
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setVisibility(View.GONE);
            }
        }
    }

    private static class InvoiceAdapter extends BaseAdapter {

        private Context context;

        private InvoiceAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return invoices.length();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @SuppressLint({"ViewHolder"})
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = LayoutInflater.from(context).inflate(R.layout.invoice_layout, viewGroup, false);

            TextView textId = view.findViewById(R.id.textView_id);
            TextView textStatus = view.findViewById(R.id.textView_status);

            try {
                JSONObject jsonObject = invoices.getJSONObject(i);
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

            return view;
        }
    }
}