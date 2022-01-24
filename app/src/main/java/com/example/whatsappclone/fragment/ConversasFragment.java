package com.example.whatsappclone.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.example.whatsappclone.R;
import com.example.whatsappclone.activity.ChatActivity;
import com.example.whatsappclone.adapter.ContatosAdapter;
import com.example.whatsappclone.adapter.ConversasAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.FragmentContatoBinding;
import com.example.whatsappclone.databinding.FragmentConversasBinding;
import com.example.whatsappclone.helper.RecyclerItemClickListener;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Conversa;
import com.example.whatsappclone.model.Usuario;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class ConversasFragment extends Fragment {

    private FragmentConversasBinding binding;
    private ConversasAdapter adapter;
    private DatabaseReference database;
    private DatabaseReference conversasRef;
    private ChildEventListener childEventListenerConversas;

    private List<Conversa> listaConversas = new ArrayList<>();


    public ConversasFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentConversasBinding.inflate(getLayoutInflater(), container, false);

        //Configurar adapter
        adapter = new ConversasAdapter(listaConversas, getActivity());

        //configurar recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        binding.recyclerListaConversas.setLayoutManager(layoutManager);
        binding.recyclerListaConversas.setHasFixedSize(true);
        binding.recyclerListaConversas.setAdapter(adapter);

        //Configurar evento de clique
        binding.recyclerListaConversas.addOnItemTouchListener(
                new RecyclerItemClickListener(
                        getActivity(),
                        binding.recyclerListaConversas,
                        new RecyclerItemClickListener.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                List<Conversa> listaConversasAtualizada = adapter.getConversas();
                                Conversa conversaSelecionada = listaConversasAtualizada.get(position);

                                if (conversaSelecionada.getIsGroup().equals("true")){
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatGrupo", conversaSelecionada.getGrupo());
                                    startActivity(i);
                                }else {
                                    Intent i = new Intent(getActivity(), ChatActivity.class);
                                    i.putExtra("chatContato", conversaSelecionada.getUsuarioExibicao());
                                    startActivity(i);
                                }
                            }

                            @Override
                            public void onLongItemClick(View view, int position) {

                            }

                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            }
                        }
                )
        );

        //Configura conversas ref
        String identificadorUsuario = UsuarioFirebase.getIdentificadorUsuario();
        database = ConfiguracaoFirebase.getFirebaseDatabase();
        conversasRef = database.child("conversas").child(identificadorUsuario);


        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarConversas();
    }

    @Override
    public void onStop() {
        super.onStop();
        conversasRef.removeEventListener(childEventListenerConversas);
    }

    public void pesquisarConversas(String texto){

        List<Conversa> listaConversasBusca = new ArrayList<>();

        for (Conversa conversa : listaConversas){
            if (conversa.getUsuarioExibicao() != null){
                String nome = conversa.getUsuarioExibicao().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }
            }else {
                String nome = conversa.getGrupo().getNome().toLowerCase();
                String ultimaMsg = conversa.getUltimaMensagem().toLowerCase();

                if (nome.contains(texto) || ultimaMsg.contains(texto)) {
                    listaConversasBusca.add(conversa);
                }
            }

        }
        adapter = new ConversasAdapter(listaConversasBusca, getActivity());
        binding.recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recarregarConversas(){
        adapter = new ConversasAdapter(listaConversas, getActivity());
        binding.recyclerListaConversas.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void recuperarConversas(){

        childEventListenerConversas = conversasRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                //Recuperar conversas
                Conversa conversa = snapshot.getValue(Conversa.class);
                listaConversas.add(conversa);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}