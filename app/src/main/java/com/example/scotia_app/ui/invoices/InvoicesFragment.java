package com.example.scotia_app.ui.invoices;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.data.model.Invoice;
import com.example.scotia_app.R;
import com.example.scotia_app.data.model.User;

import java.util.ArrayList;

import org.json.*;

public class InvoicesFragment extends Fragment {

    private InvoicesViewModel invoicesViewModel = new InvoicesViewModel();
    private User user;

    /**
     * A List of Views corresponding to invoice previews to populate the invoice page's ScrollView.
     */
    private static JSONArray invoices = new JSONArray();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets the selected user
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.user = bundle.getParcelable("user");
            assert this.user != null;
            new InvoicesFetcher(this.getActivity()).execute(user.getInvoiceURL());
        }
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        ViewModelProviders.of(this).get(InvoicesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_invoices, container, false);

        // Displays a message if user is null. Temporary solution for phase 1.
        Bundle bundle = getArguments();
        if (bundle != null) {
            root.findViewById(R.id.text_invoices).setVisibility(View.GONE);
        } else {
            root.findViewById(R.id.text_invoices).setVisibility(View.VISIBLE);
        }

        configureShowDetailedInvoiceWhenTapped(root);
        return root;
    }

    /**
     * Shows the Detailed Invoice Activity corresponding to the tapped Invoice
     */
    private Long lastTimeUserClicked = null;
    private Long clickTime;

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

    /**
     * Fetches and parses the invoice data for the logged-in user.
     */
    static private class InvoicesFetcher extends DataFetcher {

        /**
         * Initialize a new InvoicesFetcher, which runs in the given context.
         *
         * @param context The context in which this InvoicesFetcher runs.
         */
        InvoicesFetcher(Activity context) {
            super(context);
        }

        /**
         * Return a list of JSONObjects with which to populate invoices.
         *
         * @param rawJson The raw json string to be parsed
         * @return A List of JSONObjects, each corresponding to a raw json string.
         */
        private JSONArray createJSONObjects(String rawJson) {
            try {
                return new JSONArray(rawJson);
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return null;
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
                invoices = createJSONObjects(rawJsons.get(0));
                ArrayList<String> invoiceJsons = new ArrayList<>();
                for (int i = 0; i < invoices.length(); i++) {
                    try {
                        invoiceJsons.add(
                                "ID: " +
                                        invoices.getJSONObject(i).getString("invoice_id") +
                                        " Status: " +
                                        invoices.getJSONObject(i).getString("status"));
                    } catch (org.json.JSONException e) {
                        e.printStackTrace();
                    }
                }

                AppCompatActivity context = (AppCompatActivity) super.getActivityWeakReference().get();

                ListView listView = context.findViewById(R.id.invoices_list);
                if (listView != null) {
                    ArrayAdapter<String> invoicesAdapter = new ArrayAdapter<>
                            (super.getActivityWeakReference().get(),
                                    android.R.layout.simple_list_item_1, invoiceJsons);
                    listView.setAdapter(invoicesAdapter);
                    invoicesAdapter.notifyDataSetChanged();
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}