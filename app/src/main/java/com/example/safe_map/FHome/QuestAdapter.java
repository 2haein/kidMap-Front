package com.example.safe_map.FHome;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.safe_map.R;

import java.util.ArrayList;

public class QuestAdapter extends RecyclerView.Adapter<QuestAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<QuestData> mArrayList;

    public QuestAdapter(Context context, ArrayList<QuestData> arrayList) {
        this.mArrayList = arrayList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public QuestAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_quest, parent, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull QuestAdapter.ViewHolder holder, int position) {
        QuestData data = mArrayList.get(position);
        holder.quest_name.setText(data.getQuest());
    }

    @Override
    public int getItemCount() {
        return mArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView quest_name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.quest_name = itemView.findViewById(R.id.quest_name);
        }
    }
}
