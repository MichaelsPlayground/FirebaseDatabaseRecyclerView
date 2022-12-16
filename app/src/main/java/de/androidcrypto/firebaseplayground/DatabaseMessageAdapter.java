package de.androidcrypto.firebaseplayground;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.MessageModel;

public class DatabaseMessageAdapter extends RecyclerView.Adapter<DatabaseMessageAdapter.DatabaseMessageHolder> {

    //private Context mContext;
    private List<MessageModel> mMessageList;

    //private RelativeLayout mMessageContainer;
    //private LinearLayout mMessageLayout;
    private int mGreen300;
    private int mGray300;

    public DatabaseMessageAdapter(List<MessageModel> messageList) {
        this.mMessageList = messageList;
    }

    @NonNull
    @Override
    public DatabaseMessageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.database_chat_message,parent,false);
        return new DatabaseMessageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DatabaseMessageHolder holder, int position) {

        holder.messageMessage.setText(mMessageList.get(position).getMessage());
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
        String messageTimeString = dateFormat.format(mMessageList.get(position).getMessageTime());
        holder.messageTime.setText(messageTimeString);
        // this is for left/right and color of chat item
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isOwnChat = currentUser != null && mMessageList.get(position).getSenderId().equals(currentUser.getUid());
        setIsSender(isOwnChat, holder);
        //System.out.println("oBVH pos " + position + " is ownCht: " + isOwnChat + " senderId: " + mMessageList.get(position).getSenderId() + " message " + mMessageList.get(position).getMessage());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    private void setIsSender(boolean isSender, DatabaseMessageHolder mHolder) {
        final int color;
        if (isSender) {
            color = mGreen300;
            mHolder.mMessageContainer.setGravity(Gravity.END);
        } else {
            color = mGray300;
            mHolder.mMessageContainer.setGravity(Gravity.START);
        }
        ((GradientDrawable) mHolder.mMessageLayout.getBackground()).setColor(color);
    }

    public class DatabaseMessageHolder extends RecyclerView.ViewHolder {

        TextView messageMessage, messageTime;
        RelativeLayout mMessageContainer;
        LinearLayout mMessageLayout;

        public DatabaseMessageHolder(@NonNull View itemView) {
            super(itemView);

            messageMessage = itemView.findViewById(R.id.database_chat_message_message);
            messageTime = itemView.findViewById(R.id.database_chat_message_time);

            // this is for left/right and color of chat item
            mMessageContainer = itemView.findViewById(R.id.database_chat_message_container);
            mMessageLayout = itemView.findViewById(R.id.database_chat_message_layout);
            mGreen300 = ContextCompat.getColor(itemView.getContext(), R.color.material_green_300);
            mGray300 = ContextCompat.getColor(itemView.getContext(), R.color.material_gray_300);
        }

    }
}
