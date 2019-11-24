package com.example.scotia_app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Invoice implements Parcelable  {

    private String id;
    private String displayId;
    private String customerName;
    private String supplierName;
    private String driverName;
    private Status status;
    private String total;
    private ArrayList<String> orders;

    /**
     * Create a new Invoice with the data in the given parcel.
     *
     * @param in The data of this Invoice.
     */
    private Invoice(Parcel in) {
        id = in.readString();
        displayId = in.readString();
        customerName = in.readString();
        supplierName = in.readString();
        driverName = in.readString();
        status      = Status.valueOf(in.readString());
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
            this.id = invoiceData.getString("invoice_id");
            this.displayId = invoiceData.getString("invoice_id_short");
            this.customerName = invoiceData.getString("customer_name");
            this.supplierName = invoiceData.getString("supplier_name");
            this.driverName = invoiceData.getString("driver_name");
            this.status = Status.valueOf(invoiceData.getString("status"));
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
        dest.writeString(id);
        dest.writeString(displayId);
        dest.writeString(customerName);
        dest.writeString(supplierName);
        dest.writeString(driverName);
        dest.writeString(status.toString());
        dest.writeString(total);
        dest.writeStringList(orders);
    }

    public String getId() {
        return id;
    }

    public String getDisplayId() {
        return displayId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public String getDriverName() {
        return driverName;
    }

    public Status getStatus() {
        return status;
    }

    public String getTotal() {
        return total;
    }

    public ArrayList<String> getOrders() {
        return orders;
    }

}
