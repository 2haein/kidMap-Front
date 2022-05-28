package com.safekid.safe_map.Login;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.safekid.safe_map.R;

import java.util.ArrayList;

public class StdRecyclerAdapter<OnItemLongClickEventListener> extends RecyclerView.Adapter<StdRecyclerAdapter.ViewHolder>{
    private ArrayList<ChildnumItem> mChildnum;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray(0);

    public StdRecyclerAdapter(Context context) {
        this.mcontext = context;
    }

    public StdRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_childnum_layout, parent, false);
        return new ViewHolder(view, mItemClickListener);
    }
    public void onBindViewHolder(@NonNull StdRecyclerAdapter.ViewHolder holder, int position){
        ChildnumItem item = mChildnum.get(position);
        holder.onBind(mChildnum.get(position));
    }
    public void setChildNum(ArrayList<ChildnumItem> childnum){
        this.mChildnum = childnum;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mChildnum.size();
    }


    //item click 처리
    public interface OnItemClickEventListener {
        void onItemClick(View a_view, int a_position);
    }
    Context mcontext;
    RecyclerView recyclerview;

    private OnItemClickEventListener mItemClickListener;
    public void setOnItemClickListener(OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    private OnItemLongClickEventListener mItemLongClickListener;
    public void setOnItemLongClickListener(OnItemLongClickEventListener a_listener) {
        mItemLongClickListener = a_listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView childNum;
        TextView UUID;

        public ViewHolder(@NonNull View itemView, final OnItemClickEventListener a_itemClickListener) {
            super(itemView);
            this.childNum = (TextView) itemView.findViewById(R.id.name);
            this.UUID = (TextView) itemView.findViewById(R.id.uuid);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View a_view) {
                    final int position = getAdapterPosition();
                    if(mSelectedItems.get(position,false)){
                        mSelectedItems.put(position,false);
                        a_view.setBackgroundColor(Color.WHITE);
                    } else {
                        mSelectedItems.put(position,true);
                        a_view.setBackgroundColor(Color.YELLOW);
                    }

                    Log.d("Recyclerview", "position = "+ getAdapterPosition());
                    mItemClickListener.onItemClick(a_view, position);
                }
            });

        }
        void onBind(ChildnumItem item){
            childNum.setText(item.getChildNum());
            UUID.setText(item.getUUID());
        }
    }

    private void toggleItemSelected(int position) {

        if (mSelectedItems.get(position, false) == true) {
            mSelectedItems.delete(position);
            notifyItemChanged(position);
        } else {
            mSelectedItems.put(position, true);
            notifyItemChanged(position);
        }
    }
    private boolean isItemSelected(int position) {
        return mSelectedItems.get(position, false);
    }

    public void clearSelectedItem() {
        int position;

        for (int i = 0; i < mSelectedItems.size(); i++) {
            position = mSelectedItems.keyAt(i);
            mSelectedItems.put(position, false);
//            notifyItemChanged(position);
        }

        mSelectedItems.clear();
    }


    public static class OnItemLongClickEventListener {
    }
}
