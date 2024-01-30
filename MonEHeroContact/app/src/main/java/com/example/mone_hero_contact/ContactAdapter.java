package com.example.mone_hero_contact;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {

    private Context context;
    private List<User> contactList;

    public ContactAdapter(Context context, List<User> contactList) {
        this.context = context;
        this.contactList = contactList;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User user = contactList.get(position);
        holder.usernameTextView.setText(user.getUsername());
        holder.emailTextView.setText(user.getEmail());

        holder.controlRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendControlRequest(user);
            }
        });
    }

    private void sendControlRequest(User user) {
        String fromUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String toUserId = user.getId();

        Map<String, Object> request = new HashMap<>();
        request.put("fromUserId", fromUserId);
        request.put("toUserId", toUserId);
        request.put("status", "pending");

        FirebaseFirestore.getInstance().collection("controlShare").add(request)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(context, "Demande de prise de contrôle envoyée à " + user.getUsername(), Toast.LENGTH_SHORT).show();

                    // Ouvrez l'activité d'attente
                    Intent intent = new Intent(context, WaitingActivity.class);
                    intent.putExtra("TARGET_USERNAME", user.getUsername());
                    intent.putExtra("DOCUMENT_ID", documentReference.getId());
                    context.startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Erreur lors de l'envoi de la demande.", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView usernameTextView, emailTextView;
        Button controlRequestButton;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            emailTextView = itemView.findViewById(R.id.emailTextView);
            controlRequestButton = itemView.findViewById(R.id.controlRequestButton);
        }
    }
}
