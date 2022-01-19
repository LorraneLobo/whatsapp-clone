package com.example.whatsappclone.activity;

import android.os.Bundle;

import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityGrupoBinding;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;

import java.util.ArrayList;

public class GrupoActivity extends AppCompatActivity {

    private ActivityGrupoBinding binding;
    private ContatosAdapter contatosAdapter;
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuaruiosRef;
    private FirebaseUser usuarioAtual;

    private ArrayList<Usuario> listaMembros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações iniciais
        usuaruiosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar toolbar
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());

        //Configurar adapter
        contatosAdapter = new ContatosAdapter(listaMembros, getApplicationContext());

        //Configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.content.recyclerMembros.setLayoutManager(layoutManager);
        binding.content.recyclerMembros.setHasFixedSize(true);
        binding.content.recyclerMembros.setAdapter(contatosAdapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContatos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuaruiosRef.removeEventListener(valueEventListenerMembros);
    }

    public void recuperarContatos(){
        valueEventListenerMembros = usuaruiosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dados: snapshot.getChildren()){
                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioAtual = usuarioAtual.getEmail();
                    if (!emailUsuarioAtual.equals(usuario.getEmail())){
                        listaMembros.add(usuario);
                    }
                }
                contatosAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}