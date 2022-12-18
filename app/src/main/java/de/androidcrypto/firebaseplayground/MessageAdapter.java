package de.androidcrypto.firebaseplayground;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.androidcrypto.firebaseplayground.models.Message2Model;

/**
 * This class belongs to ChatDatabaseRecyclerViewPaginated4Activity.java class
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Message2Model> mMessageList;

    public MessageAdapter(List<Message2Model> mMessageList)
    {
        this.mMessageList = mMessageList;
    }

    /*
    public void addAll(List<Message2Model> newMessages)
    {
        int initsize = mMessageList.size();
        mMessageList.addAll(newMessages);
        notifyItemRangeChanged(initsize,newMessages.size());
    }
*/

    public static class MessageViewHolder extends RecyclerView.ViewHolder
    {
        TextView name;
        TextView email;
        TextView messageTime;

        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            name = itemView.findViewById(R.id.user_name_iv);
            email = itemView.findViewById(R.id.email);
            messageTime = itemView.findViewById(R.id.provider);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View itemView= LayoutInflater.from(parent.getContext()).inflate(R.layout.user_list_item_layout,parent,false);
        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position)
    {
        holder.name.setText(mMessageList.get(position).getMessage());
        holder.email.setText(mMessageList.get(position).getSenderId());
        holder.messageTime.setText(mMessageList.get(position).getMessageTimeString());
    }

    @Override
    public int getItemCount()
    {
        return mMessageList.size();
    }

}
