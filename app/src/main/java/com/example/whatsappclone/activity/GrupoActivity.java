package com.example.whatsappclone.activity;

import android.os.Bundle;

import com.example.whatsappclone.databinding.ActivityGrupoBinding;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

public class GrupoActivity extends AppCompatActivity {

    private ActivityGrupoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurar toolbar
        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        binding.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show());
    }
}