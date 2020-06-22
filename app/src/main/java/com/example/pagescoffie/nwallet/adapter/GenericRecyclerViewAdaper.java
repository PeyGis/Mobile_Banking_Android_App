package com.example.pagescoffie.nwallet.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.pagescoffie.nwallet.AccountDetailsActivity;
import com.example.pagescoffie.nwallet.R;
import com.example.pagescoffie.nwallet.model.MyAccountModel;
import com.example.pagescoffie.nwallet.model.MyWalletModel;
import com.example.pagescoffie.nwallet.model.SingletonAPI;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Pages Coffie on 5/26/2018.
 */

public class GenericRecyclerViewAdaper extends RecyclerView.Adapter {

    //instance variables
    private ArrayList<MyAccountModel> myAccountModelArrayList;
    private ArrayList<MyWalletModel> userWalletArrayList;
    private Context context;
    private int type;

    public GenericRecyclerViewAdaper(ArrayList<MyAccountModel> userAccountModelArrayList, ArrayList<MyWalletModel> userWalletArrayList, Context context, int type) {
        this.myAccountModelArrayList = userAccountModelArrayList;
        this.userWalletArrayList = userWalletArrayList;
        this.context = context;
        this.type = type;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(viewType ==1){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.accounts_list_item, parent, false);
            return new UserAccountViewHolder(view);
        } else if(viewType ==2){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallets_list_item, parent, false);
            return new UserWalletViewHolder(view);
        } else {
            return null;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof UserAccountViewHolder){
            UserAccountViewHolder accountViewHolder = (UserAccountViewHolder) holder;
            final MyAccountModel myAccountModel = myAccountModelArrayList.get(position);

            //bind data from array list to view
            accountViewHolder.accountTYpe.setText(myAccountModel.getAccountType());
            String act = myAccountModel.getAccountNumber().substring(0, 4) + "XXXXXXXX" + myAccountModel.getAccountNumber().substring(myAccountModel.getAccountNumber().length()-4, 16);
            accountViewHolder.accountNumber.setText(act);
            accountViewHolder.accountBalance.setText( "GHS " + String.valueOf(myAccountModel.getAccountBalance()));

            //set onclick listener to each cardview
            accountViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myIntent = new Intent(v.getContext(), AccountDetailsActivity.class);
                    myIntent.putExtra("ACCOUNT_BALANCE", String.valueOf(myAccountModel.getAccountBalance()));
                    myIntent.putExtra("ACCOUNT_NUMBER", myAccountModel.getAccountNumber());
                    myIntent.putExtra("ACCOUNT_TYPE", myAccountModel.getAccountType());
                    myIntent.putExtra("TYPE", "ACCOUNT");
                    v.getContext().startActivity(myIntent);
                }
            });


        } else if (holder instanceof UserWalletViewHolder){
            final UserWalletViewHolder walletViewHolder = (UserWalletViewHolder)holder;
            final MyWalletModel userWalletModel = userWalletArrayList.get(position);

            walletViewHolder.networkType.setText(userWalletModel.getNetworkOperator());
            walletViewHolder.walletNumber.setText(userWalletModel.getWalletId());
            walletViewHolder.walletBalance.setText("GHS " + String.valueOf(userWalletModel.getWalletBalance()));

            //set click listener to each wallet item
            walletViewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent myIntent = new Intent(v.getContext(), AccountDetailsActivity.class);
                    myIntent.putExtra("ACCOUNT_BALANCE", String.valueOf(userWalletModel.getWalletBalance()));
                    myIntent.putExtra("ACCOUNT_NUMBER", userWalletModel.getWalletId());
                    myIntent.putExtra("ACCOUNT_TYPE", userWalletModel.getNetworkOperator());
                    myIntent.putExtra("TYPE", "WALLET");
                    v.getContext().startActivity(myIntent);
                }
            });

        }else{
            //do nothing for now
        }

    }

    @Override
    public int getItemCount() {
        int listSize = 0;
        if(this.myAccountModelArrayList !=null) {
            listSize = myAccountModelArrayList.size();
        }
        else if(this.userWalletArrayList !=null) {
            listSize = userWalletArrayList.size();
        }
        return listSize;
    }

    @Override
    public int getItemViewType(int position) {
        return this.type;
    }

    //User Devices View Layout inner class
    public class UserWalletViewHolder extends RecyclerView.ViewHolder{

        //define view components
        private TextView networkType;
        private TextView walletNumber;
        private TextView walletBalance;
        private LinearLayout linearLayout;
        //private ImageView walletImage;

        public UserWalletViewHolder(View itemView) {
            super(itemView);
            networkType = (TextView)itemView.findViewById(R.id.networkType);
            walletNumber = (TextView)itemView.findViewById(R.id.textWalletNumber);
            walletBalance = (TextView)itemView.findViewById(R.id.textWalletBalance);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);

        }
    }

    // an inner class that represents the user_account_list_item xml layout
    public class UserAccountViewHolder extends RecyclerView.ViewHolder{

        //define view components
        private TextView accountTYpe;
        private TextView accountNumber;
        private TextView accountBalance;
        private LinearLayout linearLayout;
        private ImageView accountImage;

        public UserAccountViewHolder(View itemView) {
            super(itemView);
            accountTYpe = (TextView)itemView.findViewById(R.id.textAccountType);
            accountNumber = (TextView)itemView.findViewById(R.id.textAccountNumber);
            accountBalance = (TextView)itemView.findViewById(R.id.textAccountBalance);
            accountImage = (ImageView)itemView.findViewById(R.id.accoutImageHolder);
            linearLayout = (LinearLayout)itemView.findViewById(R.id.linearLayout);

        }
    }

    public void showToast(String message){
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

}
