package com.example.mone_hero_contact;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import java.util.ArrayList;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class SearchFragment extends Fragment {

    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> userList;
    private EditText searchEditText;
    private TextView noResultTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        noResultTextView = view.findViewById(R.id.noResultTextView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList);
        recyclerView.setAdapter(userAdapter);

        // Écouter les changements de texte pour mettre à jour la recherche en temps réel
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchUsers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchUsers(String query) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        // Obtenir l'e-mail de l'utilisateur actuellement connecté
        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Query searchQuery = firestore.collection("users")
                .orderBy("email")
                .startAt(query)
                .endAt(query + "\uf8ff");

        searchQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userList.clear();
                for (DocumentSnapshot document : task.getResult()) {
                    User user = document.toObject(User.class);
                    user.setId(document.getId());  // Ajoutez cette ligne
                    // Vérifiez si l'e-mail de l'utilisateur ne correspond pas à l'e-mail de l'utilisateur actuellement connecté
                    if (!user.getEmail().equals(currentUserEmail)) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();

                // Afficher le message "Aucun résultat" si la liste est vide
                if(userList.isEmpty()) {
                    noResultTextView.setVisibility(View.VISIBLE);
                } else {
                    noResultTextView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(getContext(), "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
            }
        });
    }



}