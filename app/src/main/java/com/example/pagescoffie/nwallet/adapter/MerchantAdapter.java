package com.example.pagescoffie.nwallet.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pagescoffie.nwallet.AccountDetailsActivity;
import com.example.pagescoffie.nwallet.PaymentActivity;
import com.example.pagescoffie.nwallet.R;
import com.example.pagescoffie.nwallet.SelectMerchantActivity;
import com.example.pagescoffie.nwallet.model.MerchantModel;
import com.example.pagescoffie.nwallet.model.MyAccountModel;

import java.util.ArrayList;

/**
 * Created by Pages Coffie on 5/23/2018.
 */

public class MerchantAdapter extends RecyclerView.Adapter<MerchantAdapter.ViewHolder> {

    private ArrayList<MerchantModel> merchantModelArrayList;
    private ArrayList<MerchantModel> merchantModelArrayListSearch;
    private Context context;

    /**
     * Constructor to initialize the adapter
     * @param merchantModelArrayList list of merchant accounts
     * @param context context from calling activity
     */
    public MerchantAdapter(ArrayList<MerchantModel> merchantModelArrayList, Context context){
        this.merchantModelArrayList = merchantModelArrayList;
        this.merchantModelArrayListSearch = new ArrayList<>();
        this.merchantModelArrayListSearch.addAll(merchantModelArrayList);
        this.context = context;
    }

    // create a view and inflate it.... it uses the list view created in the xml file
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.merchant_list_item, parent, false);
        return new ViewHolder(view);
    }

    // get item selected and bind data to view
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final MerchantModel merchantModel = merchantModelArrayList.get(position);

        //bind data from array list to view
        holder.merchantName.setText(merchantModel.getMerchantName());

        //set onclick listener to each cardview
        holder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), PaymentActivity.class);
                myIntent.putExtra("MERCHANT_NAME", merchantModel.getMerchantName());
                myIntent.putExtra("MERCHANT_ID", String.valueOf(merchantModel.getMerchantId()));
                v.getContext().startActivity(myIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return merchantModelArrayList.size();
    }

    public void setFilter(String keyword) {
        if (merchantModelArrayList != null) {
            keyword = keyword.toLowerCase();
            merchantModelArrayList.clear();

            if (keyword.length() == 0) {
                merchantModelArrayList.addAll(merchantModelArrayListSearch);
            } else {
                for (MerchantModel model : merchantModelArrayListSearch) {
                    if (model.getMerchantName().toLowerCase().contains(keyword)) {
                        merchantModelArrayList.add(model);
                    }
                }
            }
            notifyDataSetChanged();
        }
    }

    // an inner class that represents the user_account_list_item xml layout
    class ViewHolder extends RecyclerView.ViewHolder {

        //define view components
        private TextView merchantName;
        private LinearLayout linearLayout;
        private ImageView merchantImage;

        public ViewHolder(View itemView) {
            super(itemView);
            merchantName = (TextView)itemView.findViewById(R.id.merchantName);
            merchantImage = (ImageView)itemView.findViewById(R.id.merchantImageHolder);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.merchantLinearLayout);

        }
    }
}
