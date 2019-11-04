package com.example.scotia_app;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Invoice implements Parcelable  {

    private String invoice_id;
    private String customer_id;
    private String supplier_id;
    private String driver_id;
    private String status;
    private String total;
    private ArrayList<String> orders;

    /**
     * Create a new Invoice with the data in the given parcel.
     *
     * @param in The data of this Invoice.
     */
    private Invoice(Parcel in) {
        invoice_id  = in.readString();
        customer_id = in.readString();
        supplier_id = in.readString();
        driver_id   = in.readString();
        status      = in.readString();
        total       = in.readString();
        orders = new ArrayList<>();
        in.readStringList(orders);
    }

    /** Create a new Invoice with the data in the given JSONObject.
     *
     * @param invoiceData The data of this Invoice.
     */
    public Invoice (JSONObject invoiceData) {
        try {
            this.invoice_id = invoiceData.getString("invoice_id");
            this.customer_id = invoiceData.getString("customer_id");
            this.supplier_id = invoiceData.getString("supplier_id");
            this.driver_id = invoiceData.getString("driver_id");
            this.status = invoiceData.getString("status");
            this.total = invoiceData.getString("total");
            JSONArray array = invoiceData.getJSONArray("orders");
            orders = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                this.orders.add(array.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * IDE-generated Parcelable methods
     */
    public static final Creator<Invoice> CREATOR = new Creator<Invoice>() {
        @Override
        public Invoice createFromParcel(Parcel in) {
            return new Invoice(in);
        }

        @Override
        public Invoice[] newArray(int size) {
            return new Invoice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Passes the attributes of this Invoice into a parcel
     *
     * @param dest The parcel to pass the attributes into
     * @param flags Optional details about how the attributes should be passed
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(invoice_id);
        dest.writeString(customer_id);
        dest.writeString(supplier_id);
        dest.writeString(driver_id);
        dest.writeString(status);
        dest.writeString(total);
        dest.writeStringList(orders);
    }

    public String getInvoice_id() {
        return invoice_id;
    }

    public String getCustomer_id() {
        return customer_id;
    }

    public String getSupplier_id() {
        return supplier_id;
    }

    public String getDriver_id() {
        return driver_id;
    }

    public String getStatus() {
        return status;
    }

    public String getTotal() {
        return total;
    }

    public ArrayList<String> getOrders() {
        return orders;
    }

}
