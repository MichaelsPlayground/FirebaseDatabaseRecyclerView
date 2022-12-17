package de.androidcrypto.firebaseplayground;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.androidcrypto.firebaseplayground.models.Message2Model;

public class UserRVAdapter  extends RecyclerView.Adapter<UserRVViewHolder> {
    private List<Message2Model> userModels;

    public UserRVAdapter() {
        this.userModels = new ArrayList<>();
    }

    @Override
    public UserRVViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserRVViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_item_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(UserRVViewHolder holder, int position) {
        holder.setData(userModels.get(position));
    }

    @Override
    public int getItemCount() {
        return userModels.size();
    }

    public void addAll(List<Message2Model> newUsers) {
        int initialSize = userModels.size();
        userModels.addAll(newUsers);
        notifyItemRangeInserted(initialSize, newUsers.size());
    }

    public String getLastItemId() {
        String ret = userModels.get(userModels.size() - 1).getKey();
        System.out.println("** getLastItemId: " + ret);
        return ret;
    }
}
