package com.example.pagescoffie.nwallet.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pagescoffie.nwallet.R;

/**
 * Created by Pages Coffie on 5/26/2018.
 */

public class EmptyRecyclerViewAdapter extends RecyclerView.Adapter<EmptyRecyclerViewAdapter.ViewHolder> {

    private String mMessage;

    public EmptyRecyclerViewAdapter(){}

    public EmptyRecyclerViewAdapter(String message){
        mMessage = message;
    }

    @Override
    public EmptyRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.empty_view_layout, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);

        if(mMessage != null){
            viewHolder.mMessageView.setText(mMessage);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(EmptyRecyclerViewAdapter.ViewHolder holder, int position) {}

    @Override
    public int getItemCount() {
        return 1;//must return one otherwise none item is shown
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mMessageView;
        public LinearLayout emptyView;
        public ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mMessageView = (TextView) view.findViewById(R.id.empty_text);
            emptyView = (LinearLayout) itemView.findViewById(R.id.empty_view_holder);
            imageView = (ImageView)itemView.findViewById(R.id.empty_image);
        }
    }
}
