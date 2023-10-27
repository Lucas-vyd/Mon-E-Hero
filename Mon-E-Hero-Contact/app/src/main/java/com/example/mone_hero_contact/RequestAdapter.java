package com.example.mone_hero_contact;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestViewHolder> {

    private List<DocumentSnapshot> requestList;
    private Context context;

    public RequestAdapter(List<DocumentSnapshot> requestList, Context context) {
        this.requestList = requestList;
        this.context = context;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        DocumentSnapshot document = requestList.get(position);
        String fromUserId = document.getString("fromUserId");
        String requestId = document.getId();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        holder.setSenderNameFromId(fromUserId);

        holder.acceptButton.setOnClickListener(v -> {


            // Mise à jour de la requête à "accepted"
            firestore.collection("contactRequests").document(requestId)
                    .update("status", "accepted")
                    .addOnSuccessListener(aVoid -> {
                        // Ajouter les utilisateurs à la liste de contacts de chaque compte
                        firestore.collection("users").document(fromUserId)
                                .update("contacts", FieldValue.arrayUnion(document.getString("toUserId")));

                        firestore.collection("users").document(document.getString("toUserId"))
                                .update("contacts", FieldValue.arrayUnion(fromUserId));

                        Toast.makeText(context, "Demande acceptée", Toast.LENGTH_SHORT).show();
                        requestList.remove(position);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erreur lors de l'acceptation", Toast.LENGTH_SHORT).show();
                    });
        });

        holder.declineButton.setOnClickListener(v -> {
            firestore.collection("contactRequests").document(requestId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(context, "Demande refusée", Toast.LENGTH_SHORT).show();
                        requestList.remove(position);
                        notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(context, "Erreur lors du refus", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        TextView senderTextView;
        Button acceptButton, declineButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            senderTextView = itemView.findViewById(R.id.senderTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            declineButton = itemView.findViewById(R.id.declineButton);
        }

        void setSenderNameFromId(String userId) {
            FirebaseFirestore.getInstance().collection("users").document(userId)
                    .get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            User user = documentSnapshot.toObject(User.class);
                            senderTextView.setText(user.getUsername());
                        }
                    }).addOnFailureListener(e -> {
                        // Gérer les erreurs ici, par exemple mettre l'ID comme nom par défaut
                        senderTextView.setText(userId);
                    });
        }
    }
}
