package com.example.whatsappclone.activity;

import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.whatsappclone.R;

public class ChatActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityChatBinding binding;
    private Usuario usuarioDestinatario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurar toolbar
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configuracoes iniciais

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            usuarioDestinatario = (Usuario) bundle.getSerializable("chatContato");
            binding.textViewNomeChat.setText(usuarioDestinatario.getNome());

            String foto = usuarioDestinatario.getFoto();
            if (foto != null){
                Uri uri = Uri.parse(usuarioDestinatario.getFoto());
                Glide.with(ChatActivity.this)
                        .load(uri)
                        .into(binding.circleImageFotoChat);
            }else{
                binding.circleImageFotoChat.setImageResource(R.drawable.padrao);
            }
        }

    }

}