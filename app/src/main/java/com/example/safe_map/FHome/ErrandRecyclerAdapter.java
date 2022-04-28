package com.example.safe_map.FHome;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.R;

import java.util.ArrayList;
import java.util.Date;

public class ErrandRecyclerAdapter  extends RecyclerView.Adapter<ErrandRecyclerAdapter.ViewHolder>{
    private ArrayList<errandHome> mErrandHome;

    public ErrandRecyclerAdapter(Context context) {
        this.mcontext = context;
    }

    public ErrandRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_errand_layout, parent, false);
        return new ViewHolder(view, (StdRecyclerAdapter.OnItemClickEventListener) mItemClickListener);
    }
    public void onBindViewHolder(@NonNull ErrandRecyclerAdapter.ViewHolder holder, int position){
        errandHome item = mErrandHome.get(position);
        holder.onBind(mErrandHome.get(position));
    }
    public void setErrandHome(ArrayList<errandHome> mErrandHome){
        this.mErrandHome = mErrandHome;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mErrandHome.size();
    }

    //item click 처리
    public interface OnItemClickEventListener {
        void onItemClick(View a_view, int a_position);
    }
    Context mcontext;
    RecyclerView recyclerview;
    private ErrandRecyclerAdapter.OnItemClickEventListener mItemClickListener;

    public void setOnItemClickListener(ErrandRecyclerAdapter.OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView childName;
        TextView date;
        TextView errandContent;
        TextView destination;

        public ViewHolder(@NonNull View itemView, final com.example.safe_map.Login.StdRecyclerAdapter.OnItemClickEventListener a_itemClickListener) {
            super(itemView);
            this.childName = (TextView) itemView.findViewById(R.id.name);
            this.date = (TextView) itemView.findViewById(R.id.date);
            this.errandContent = (TextView) itemView.findViewById(R.id.econtent);
            this.destination = (TextView) itemView.findViewById(R.id.dest);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View a_view) {
                    final int position = getAdapterPosition();
                    //a_view.setBackgroundColor(Color.YELLOW);
                    //Log.d("Recyclerview", "position = "+ getAdapterPosition());
                    //mItemClickListener.onItemClick(a_view, position);
                }
            });
        }
        void onBind(errandHome item){
            childName.setText(item.getChildName());
            date.setText(item.getDate());
            errandContent.setText(item.geterrandContent());
            destination.setText(item.getDestination());
        }
    }
}
