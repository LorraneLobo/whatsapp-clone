package com.example.whatsappclone.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.whatsappclone.R;
import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityConfiguracoesBinding;
import com.example.whatsappclone.databinding.FragmentContatoBinding;
import com.example.whatsappclone.model.Usuario;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ContatosFragment extends Fragment {

    private FragmentContatoBinding binding;
    private ContatosAdapter adapter;
    private ArrayList<Usuario> listaContatos = new ArrayList<>();
    private DatabaseReference usuaruiosRef;
    private ValueEventListener valueEventListenerContatos;

    public ContatosFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContatoBinding.inflate(getLayoutInflater(), container, false);

        //Configurações iniciais
        usuaruiosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");

        //configurar adapter
        adapter = new ContatosAdapter(listaContatos, getActivity());

        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerViewListaContatos.setLayoutManager(layoutManager);
        binding.recyclerViewListaContatos.setHasFixedSize(true);
        binding.recyclerViewListaContatos.setAdapter(adapter);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuaruiosRef.removeEventListener(valueEventListenerContatos);
    }

    public void recuperarContatos(){
        valueEventListenerContatos = usuaruiosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dados: snapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);
                    listaContatos.add(usuario);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}