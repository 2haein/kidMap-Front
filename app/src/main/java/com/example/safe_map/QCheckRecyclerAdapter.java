package com.example.safe_map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_map.FHome.QuestAdapter;
import com.example.safe_map.FHome.QuestData;

import java.util.ArrayList;

public class QCheckRecyclerAdapter extends RecyclerView.Adapter<QCheckRecyclerAdapter.ItemViewHolder> {
    private ArrayList<QuestData> QuestData = new ArrayList<>();
    private Context mContext;

    public QCheckRecyclerAdapter(Context mContext, ArrayList<com.example.safe_map.FHome.QuestData> mArrayList) {
        this.QuestData = mArrayList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public QCheckRecyclerAdapter.ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quest_check_, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QCheckRecyclerAdapter.ItemViewHolder holder, int position) {
        holder.onBind(QuestData.get(position));
    }

    @Override
    public int getItemCount() {
        return QuestData.size();
    }

    void addItem(QuestData data){
        QuestData.add(data);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView quest_name;

        ItemViewHolder(View itemView) {
            super(itemView);
            quest_name = itemView.findViewById(R.id.quest_cnt);
        }

        void onBind(com.example.safe_map.FHome.QuestData data) {
            quest_name.setText(data.getQuest());
        }
    }
}
