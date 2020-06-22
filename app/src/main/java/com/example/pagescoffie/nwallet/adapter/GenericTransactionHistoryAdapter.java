package com.example.pagescoffie.nwallet.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pagescoffie.nwallet.AccountDetailsActivity;
import com.example.pagescoffie.nwallet.R;
import com.example.pagescoffie.nwallet.model.MyAccountModel;
import com.example.pagescoffie.nwallet.model.TransactionHistory;

import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Pages Coffie on 5/28/2018.
 */

public class GenericTransactionHistoryAdapter extends RecyclerView.Adapter<GenericTransactionHistoryAdapter.ViewHolder>{

    private ArrayList<TransactionHistory> transactionHistoryArrayList;
    private ArrayList<TransactionHistory> transactionHistoryArrayListSearch;
    private Context context;

    /**
     * Constructor to initialize the adapter
     * @param transactionHistoryArrayList list of accounts
     * @param context context from calling activity
     */
    public GenericTransactionHistoryAdapter(ArrayList<TransactionHistory> transactionHistoryArrayList, Context context){
        this.transactionHistoryArrayList = transactionHistoryArrayList;
        this.transactionHistoryArrayListSearch = new ArrayList<>();
        this.transactionHistoryArrayListSearch.addAll(transactionHistoryArrayList);
        this.context = context;
    }

    // create a view and inflate it.... it uses the list view created in the xml file
    @Override
    public GenericTransactionHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_history_list_item, parent, false);
        return new GenericTransactionHistoryAdapter.ViewHolder(view);
    }

    // get item selected and bind data to view
    @Override
    public void onBindViewHolder(GenericTransactionHistoryAdapter.ViewHolder holder, int position) {

        final TransactionHistory transactionHistory = transactionHistoryArrayList.get(position);

        holder.historyDetails.setText(transactionHistory.getDetails());
        holder.historyDate.setText(convertDate(transactionHistory.getDate()));
       // holder.historyAmount.append(String.valueOf(transactionHistory.getAmount())); ;
        //set color of amount to green when there is inflow of cash
        if(transactionHistory.getDebitCreditStatus().equals("Debit")) {
            holder.historyAmount.setText("GHS " + String.valueOf(transactionHistory.getAmount())); ;
            holder.historyAmount.setTextColor(Color.GREEN);
        }
        //set color of amount to red when there is outflow of cash
        else {
            holder.historyAmount.setText("GHS " + String.valueOf(transactionHistory.getAmount())); ;
            holder.historyAmount.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return transactionHistoryArrayList.size();
    }

    public void setFilter(String keyword) {
        if (transactionHistoryArrayList != null) {
            keyword = keyword.toLowerCase();
            transactionHistoryArrayList.clear();

            if (keyword.length() == 0) {
                transactionHistoryArrayList.addAll(transactionHistoryArrayListSearch);
            } else {
                for (TransactionHistory model : transactionHistoryArrayListSearch) {
                    if (model.getDetails().toLowerCase().contains(keyword) || model.getDate().toLowerCase().contains(keyword)) {
                        transactionHistoryArrayList.add(model);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    // an inner class that represents the user_account_list_item xml layout
    class ViewHolder extends RecyclerView.ViewHolder {

        //define view components
        private TextView historyDetails;
        private TextView historyDate;
        private TextView historyAmount;
        private LinearLayout linearLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            historyDetails = (TextView)itemView.findViewById(R.id.textDetails);
            historyDate = (TextView)itemView.findViewById(R.id.textDate);
            historyAmount = (TextView)itemView.findViewById(R.id.textAmount);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);

        }
    }

    private String convertDate(String sqlDate){
        Date date = null;
        String d = sqlDate.substring(0, 19).replace("T", " ");; // assigns datetime value from mysql

// parse String datetime to Date
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK ).parse(d);
        } catch (ParseException e) { e.printStackTrace(); }

// format the Date object then assigns to String
        Format formatter;
        formatter = new SimpleDateFormat("dd MMM yyyy HH:mm a", Locale.UK);
        String s = formatter.format(date);
        return s;
    }
}
