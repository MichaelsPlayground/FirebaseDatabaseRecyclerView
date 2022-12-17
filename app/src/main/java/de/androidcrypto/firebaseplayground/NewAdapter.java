package de.androidcrypto.firebaseplayground;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.Message2Model;

/**
 * This class belongs to ChatDatabaseRecyclerViewPaginated3Activity.java class
 */

public class NewAdapter extends RecyclerView.Adapter<NewAdapter.NewViewHolder>{
    List<Message2Model> messagesList;
    Context context;

    public NewAdapter( Context context)
    {
        this.messagesList = new ArrayList<>();
        this.context = context;
    }

    public void addAll(List<Message2Model> newMessages)
    {
        int initsize = messagesList.size();
        messagesList.addAll(newMessages);
        notifyItemRangeChanged(initsize,newMessages.size());
    }


    public static class NewViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;

        public NewViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name=itemView.findViewById(R.id.user_name_iv);

        }
    }

    @NonNull
    @Override
    public NewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView= LayoutInflater.from(context).inflate(R.layout.user_list_item_layout,parent,false);
        return new NewViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NewViewHolder holder, int position)
    {
        holder.name.setText(messagesList.get(position).getMessage());
    }

    @Override
    public int getItemCount()
    {
        return messagesList.size();
    }

}
