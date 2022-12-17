package de.androidcrypto.firebaseplayground;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import de.androidcrypto.firebaseplayground.models.Message2Model;

public class UserRVViewHolder extends RecyclerView.ViewHolder {

    private ImageView mUserIV;
    private TextView mUserNameTV;
    private TextView mEmailTV;
    private TextView mProviderTV;

    public UserRVViewHolder(View itemView) {
        super(itemView);
        findViews(itemView);
    }

    private void findViews(View view) {
        mUserIV = view.findViewById(R.id.user_iv);
        mUserNameTV = view.findViewById(R.id.user_name_iv);
        mEmailTV = view.findViewById(R.id.email);
        mProviderTV = view.findViewById(R.id.provider);
    }

    public void setData(Message2Model userModel) {
        mUserNameTV.setText(userModel.getMessage());
        mEmailTV.setText(String.valueOf(userModel.getMessageTime()));
        mProviderTV.setText(userModel.getSenderId());

        /*
        Glide.with(mUserIV.getContext())
                .load(userModel.getProfileImageLink())
                .thumbnail(0.1f)
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(mUserIV);
        */
    }

}