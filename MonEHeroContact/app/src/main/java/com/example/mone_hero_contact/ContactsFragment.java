package com.example.mone_hero_contact;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ContactsFragment extends Fragment {

    private RecyclerView contactsRecyclerView;
    private ContactAdapter contactAdapter;
    private List<User> contactList = new ArrayList<>();
    private List<User> filteredContactList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private EditText searchEditText;
    private TextView noResultTextView;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);

        contactsRecyclerView = view.findViewById(R.id.contactsRecyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        searchEditText = view.findViewById(R.id.searchEditText);
        noResultTextView = view.findViewById(R.id.noResultTextView);
        emptyView = view.findViewById(R.id.emptyView);

        firestore = FirebaseFirestore.getInstance();

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterContacts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        loadContacts();

        return view;
    }

    private void loadContacts() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        firestore.collection("users")
                .whereArrayContains("contacts", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    contactList.clear();
                    for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                        User user = snapshot.toObject(User.class);
                        user.setId(snapshot.getId());
                        contactList.add(user);
                    }
                    updateUI();
                    filterContacts(searchEditText.getText().toString());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors du chargement des contacts.", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterContacts(String query) {
        if (query.isEmpty()) {
            filteredContactList = new ArrayList<>(contactList);
        } else {
            // Filtrez la liste de contacts en fonction de la requête
            filteredContactList = contactList.stream()
                    .filter(user -> user.getUsername().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        }

        // Mettez à jour l'adapter avec la liste filtrée
        contactAdapter = new ContactAdapter(getContext(), filteredContactList);
        contactsRecyclerView.setAdapter(contactAdapter);

        // Mettez à jour l'interface utilisateur en fonction des résultats filtrés
        updateUI();
    }

    private void updateUI() {
        if (filteredContactList.isEmpty()) {
            contactsRecyclerView.setVisibility(View.GONE);
            if (searchEditText.getText().toString().isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                noResultTextView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                noResultTextView.setVisibility(View.VISIBLE);
            }
        } else {
            contactsRecyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            noResultTextView.setVisibility(View.GONE);
        }
    }
}
