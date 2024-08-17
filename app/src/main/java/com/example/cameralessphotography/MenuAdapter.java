package com.example.cameralessphotography;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;

public class MenuAdapter extends RecyclerView.Adapter<MenuAdapter.ViewHolder>  {
    private Context context;
    private List<String> menuItemList;
    public MenuAdapter(Context context, List<String> menuItemList)
    {
        this.context = context;
        this.menuItemList = menuItemList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_menu, parent, false);
        return new MenuAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView menuItemTV = holder.itemView.findViewById(R.id.menuTextView);
        Spinner menuItemSP = (Spinner)holder.itemView.findViewById(R.id.menuSpinner);
        menuItemTV.setText(menuItemList.get(position));

        ArrayAdapter<CharSequence> adapter;
        switch (position)
        {
            case 0:
                adapter = ArrayAdapter.createFromResource(context, R.array.orientationSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 1:
                adapter = ArrayAdapter.createFromResource(context, R.array.aspect_ratioSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 2:
                adapter = ArrayAdapter.createFromResource(context, R.array.flashSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 3:
                adapter = ArrayAdapter.createFromResource(context, R.array.exposureSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 4:
                adapter = ArrayAdapter.createFromResource(context, R.array.apertureArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 5:
                adapter = ArrayAdapter.createFromResource(context, R.array.ISOSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 6:
                adapter = ArrayAdapter.createFromResource(context, R.array.white_balanceSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            case 7:
                adapter = ArrayAdapter.createFromResource(context, R.array.focalLenSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
                break;
            default:
                adapter = ArrayAdapter.createFromResource(context, R.array.defaltSpinnerArray, android.R.layout.simple_spinner_dropdown_item);
        }

        menuItemSP.setAdapter(adapter);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return menuItemList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout relativeLayout;
        public ViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.menuRelativeLayout);
        }
    }


}