package com.lucas.caller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PhoneAdapter extends RecyclerView.Adapter<PhoneAdapter.PhoneViewHolder> {
    private List<PhoneBean> phoneList;

    public PhoneAdapter(List<PhoneBean> phoneList) {
        this.phoneList = phoneList;
    }

    private OnPhoneItemClickListener onPhoneItemClickListener;



    public void setOnPhoneItemClickListener(OnPhoneItemClickListener listener) {
        this.onPhoneItemClickListener = listener;
    }

    @NonNull
    @Override
    public PhoneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_phone, parent, false);
        return new PhoneViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhoneViewHolder holder, int position) {
        PhoneBean phone = phoneList.get(position);
        holder.nameTextView.setText(phone.Name);
        holder.phoneTextView.setText(phone.Phone);

        holder.xiugai.setOnClickListener(v->{
            if (position != RecyclerView.NO_POSITION) {
                onPhoneItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return phoneList.size();
    }

    static class PhoneViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        TextView xiugai;

        public PhoneViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
            xiugai = itemView.findViewById(R.id.xiugai);
        }
    }

    public interface OnPhoneItemClickListener {
        void onItemClick(int position);
    }

}
