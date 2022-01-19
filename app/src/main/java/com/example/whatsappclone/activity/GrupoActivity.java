package com.example.whatsappclone.activity;

import android.os.Bundle;

import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.adapter.GrupoSelecionadoAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityGrupoBinding;
import com.example.whatsappclone.helper.RecyclerItemClickListener;
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

import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;

import java.util.ArrayList;

public class GrupoActivity extends AppCompatActivity {

    private ActivityGrupoBinding binding;
    private ContatosAdapter contatosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuaruiosRef;
    private FirebaseUser usuarioAtual;

    private ArrayList<Usuario> listaMembros = new ArrayList<>();
    private ArrayList<Usuario> listaMembrosSelecionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações iniciais
        usuaruiosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioAtual = UsuarioFirebase.getUsuarioAtual();

        //Configurar toolbar
        binding.toolbar.setTitle("Novo grupo");
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

        binding.content.recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                binding.content.recyclerMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembros.get(position);

                        //Remover usuario selecionado da lista
                        listaMembros.remove(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        //Adiciona usuario na nova lista de selecionados
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        atualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));

        //Configurar recyclerview para os membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        binding.content.recyclerMembrosSelecionados.setLayoutManager(layoutManagerHorizontal);
        binding.content.recyclerMembrosSelecionados.setHasFixedSize(true);
        binding.content.recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        binding.content.recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                binding.content.recyclerMembrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembrosSelecionados.get(position);

                        //Remover usuario da listagem de membros selecionados
                        listaMembrosSelecionados.remove(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        //Adicionar a listagem de membros
                        listaMembros.add(usuarioSelecionado);
                        contatosAdapter.notifyDataSetChanged();

                        atualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }
        ));
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
                atualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void atualizarMembrosToolbar(){
        int totalSelecionados = listaMembrosSelecionados.size();
        int totalContatos = listaMembros.size() + totalSelecionados;

        binding.toolbar.setSubtitle(totalSelecionados + " de " + totalContatos + " selecionados");
    }
}