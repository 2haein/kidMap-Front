package com.example.safe_map.Child;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_map.FHome.ErrandItemActivity;
import com.example.safe_map.FHome.errandHome;
import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.MainActivity;
import com.example.safe_map.R;

import java.util.ArrayList;
import java.util.Date;

public class CErrandRecyclerAdapter  extends RecyclerView.Adapter<CErrandRecyclerAdapter.ViewHolder>{
    private ArrayList<errandHome> mErrandHome;

    public CErrandRecyclerAdapter(Context context) {
        this.mcontext = context;
    }

    public CErrandRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_errand_layout, parent, false);
        return new CErrandRecyclerAdapter.ViewHolder(view, (StdRecyclerAdapter.OnItemClickEventListener) mItemClickListener);
    }


    public void onBindViewHolder(@NonNull CErrandRecyclerAdapter.ViewHolder holder, int position){
        errandHome item = mErrandHome.get(position);
        holder.onBind(mErrandHome.get(position));

        holder.onBind(item);
        holder.cardView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int mPosition = holder.getAdapterPosition();
                Context context = v.getContext();

                Intent detailActivity = new Intent(context, ErrandItemActivity.class);

                detailActivity.putExtra("childname", mErrandHome.get(mPosition).getChildName());
                detailActivity.putExtra("date", mErrandHome.get(mPosition).getDate());
                detailActivity.putExtra("content", mErrandHome.get(mPosition).geterrandContent());
                detailActivity.putExtra("target", mErrandHome.get(mPosition).getDestination());
                detailActivity.putExtra("start", mErrandHome.get(mPosition).getStartName());
                detailActivity.putExtra("quest", mErrandHome.get(mPosition).getQuest());
                //사진 detailActivity
                ((ChildMain)context).startActivity(detailActivity);
            }
        });
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
    private com.example.safe_map.FHome.ErrandRecyclerAdapter.OnItemClickEventListener mItemClickListener;

    public void setOnItemClickListener(com.example.safe_map.FHome.ErrandRecyclerAdapter.OnItemClickEventListener a_listener) {
        mItemClickListener = a_listener;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView childName;
        TextView date;
        TextView errandContent;
        TextView destination;
        CardView cardView;

        public ViewHolder(@NonNull View itemView, final com.example.safe_map.Login.StdRecyclerAdapter.OnItemClickEventListener a_itemClickListener) {
            super(itemView);
            this.childName = (TextView) itemView.findViewById(R.id.name);
            this.date = (TextView) itemView.findViewById(R.id.date);
            this.errandContent = (TextView) itemView.findViewById(R.id.econtent);
            this.destination = (TextView) itemView.findViewById(R.id.dest);
            this.cardView = (CardView) itemView.findViewById(R.id.layout_container);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View a_view) {
                    final int position = getAdapterPosition();
                    mItemClickListener.onItemClick(a_view, position);
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

