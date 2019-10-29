package com.example.scotia_app.ui.invoices;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.example.scotia_app.DataFetcher;
import com.example.scotia_app.R;

import java.util.ArrayList;

import org.json.*;

public class InvoicesFragment extends Fragment {

    private InvoicesViewModel invoicesViewModel;

    /**
     * A List of Views corresponding to invoice previews to populate the invoice page's ScrollView.
     */
    private static ArrayList<JSONObject> invoices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new InvoicesFetcher(this.getActivity()).execute("");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
                ViewModelProviders.of(this).get(InvoicesViewModel.class);
        View root = inflater.inflate(R.layout.fragment_profile, container, false);
        final TextView textView = root.findViewById(R.id.text_dashboard);
        invoicesViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    /**
     * Fetches and parses the invoice data for some collection of urls.
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
         * @param rawJsons The ArrayList of raw json strings to be parsed.
         * @return A List of JSONObjects, each corresponding to a raw json string.
         */
        private ArrayList<JSONObject> createJSONObjects(ArrayList<String> rawJsons) {
            try {
                ArrayList<JSONObject> invoices = new ArrayList<>();
                for (String rawJson : rawJsons) {
                    JSONObject jsonObject = new JSONObject(rawJson);
                    String id = jsonObject.getString("invoice_id");
                    String status = jsonObject.getString("status");
                    invoices.add(jsonObject);
                }
                return invoices;
            } catch (org.json.JSONException e) {
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
            invoices = createJSONObjects(rawJsons);
            ArrayList<String> invoiceJsons = new ArrayList<>();
            for (JSONObject invoice : invoices) {
                try {
                    invoiceJsons.add("ID: " + invoice.getString("invoice_id") + "Status: " + invoice.getString("status"));
                } catch (org.json.JSONException e) {
                    e.printStackTrace();
                }
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(super.getActivityWeakReference().get(), android.R.layout.simple_list_item_1, invoiceJsons);
            //Adapt ListView with arrayAdapter
        }
    }

}