package com.example.facerecognition.Adapter;

import android.content.Context;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.facerecognition.R;
import com.example.facerecognition.Object.DetectItem;

import java.util.ArrayList;

public class ObjectAdapter extends RecyclerView.Adapter<ObjectAdapter.ViewHolder>  {

    private Context context;
    private ArrayList<DetectItem> allDetects;
    private boolean first;

    public ObjectAdapter(Context context, ArrayList<DetectItem> allDetects) {

        this.context = context;
        this.allDetects = allDetects;


    }

    @Override
    public ObjectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.detectelayout, parent, false);
        return  new ObjectAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ObjectAdapter.ViewHolder holder, int position) {
        final DetectItem temp = allDetects.get(position);
        if (holder != null) {
            holder.name_LBL.setText(temp.getName());
            holder.match_LBL.setText(""+ String.format("%.3f" , temp.getMatched()) + " %" );
        }

    }

    @Override
    public int getItemCount() {

        return allDetects.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView name_LBL , match_LBL;
        CardView detected_LAY_crd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            init();
        }

        private void init() {
            detected_LAY_crd = itemView.findViewById(R.id.detected_LAY_crd);
            name_LBL = itemView.findViewById(R.id.name_LBL);
            match_LBL = itemView.findViewById(R.id.match_LBL);
        }

    }
}
