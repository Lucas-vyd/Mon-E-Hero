package com.example.mone_hero_contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Toast;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());

        // Vérifiez le statut entre l'utilisateur actuel et l'utilisateur listé
        checkFriendshipStatus(user, holder);
    }

    private void checkFriendshipStatus(User user, UserViewHolder holder) {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String listedUserId = user.getId();

        // Vérifier s'ils sont déjà amis
        if (user.getContacts().contains(currentUserId)) {
            holder.addContactButton.setText("Vous êtes déjà ami");
            holder.addContactButton.setEnabled(false);
            return;
        }

        // Vérifier les demandes d'ami
        FirebaseFirestore.getInstance().collection("contactRequests")
                .whereEqualTo("fromUserId", currentUserId)
                .whereEqualTo("toUserId", listedUserId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        holder.addContactButton.setText("Demande en attente");
                        holder.addContactButton.setEnabled(false);
                    } else {
                        holder.addContactButton.setText("Ajouter à la liste de contact");
                        holder.addContactButton.setEnabled(true);
                        // Ajout de la logique pour envoyer une demande d'ami
                        holder.addContactButton.setOnClickListener(v -> {
                            sendFriendRequest(currentUserId, listedUserId, holder);
                        });
                    }
                });
    }

    private void sendFriendRequest(String fromUserId, String toUserId, UserViewHolder holder) {
        Map<String, Object> request = new HashMap<>();
        request.put("fromUserId", fromUserId);
        request.put("toUserId", toUserId);
        request.put("status", "pending");

        FirebaseFirestore.getInstance().collection("contactRequests").add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(holder.itemView.getContext(), "Requête envoyée!", Toast.LENGTH_SHORT).show();
                    holder.addContactButton.setText("Demande en attente");
                    holder.addContactButton.setEnabled(false);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(holder.itemView.getContext(), "Erreur lors de l'envoi de la requête.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView usernameTextView;
        TextView emailTextView;
        Button addContactButton;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            addContactButton = itemView.findViewById(R.id.addContactButton);
        }
    }
}