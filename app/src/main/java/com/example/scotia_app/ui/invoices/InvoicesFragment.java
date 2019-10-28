package com.example.scotia_app.ui.invoices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private ArrayList<View> invoices = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new InvoicesFetcher().execute("");
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
     * Parse raw JSON invoice data and populate invoices with the created invoices.
     *
     * @param rawJson The JSON array of invoices to parse and populate scrollView with,
     */
    private void loadInvoices(String rawJson) {
        try {
            JSONObject jsonObject = new JSONObject(rawJson);
            JSONArray jsonArray = jsonObject.getJSONArray("invoices");
            for (int i = 0; i < jsonArray.length(); i++) {
                invoices.add(createView(jsonArray.getJSONObject(i)));
            }
        }
        catch (JSONException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Given jsonObjectInvoice, a JSONObject containing data for a single invoice, constructs an
     * appropriate View to be displayed within scrollView.
     *
     * @param jsonObjectInvoice A single invoice, in form of a JSONObject.
     * @return A TextView containing some of the invoice's data to be displayed on this invoice
     * preview.
     */
    private TextView createView(JSONObject jsonObjectInvoice) {
        return new TextView(getContext());
    }

    /**
     * Extend DataFetcher class and override onPostExecute to update this fragment once the invoices
     * are retrieved from the database.
     *
     */
    static private class InvoicesFetcher extends DataFetcher {
        @Override
        protected void onPostExecute(ArrayList<String> rawJsons) {
            //populate the scollview with info obtained from rawJsons
        }
    }

}