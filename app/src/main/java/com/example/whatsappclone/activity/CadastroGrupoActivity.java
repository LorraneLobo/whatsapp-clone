package com.example.whatsappclone.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.whatsappclone.adapter.GrupoSelecionadoAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityCadastroGrupoBinding;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Grupo;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class CadastroGrupoActivity extends AppCompatActivity {

    private ActivityCadastroGrupoBinding binding;
    private EasyImage easyImage;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private StorageReference storageReference;
    private Grupo grupo;

    private ArrayList<Usuario> listaMembrosSelecionados = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCadastroGrupoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setTitle("Novo grupo");
        binding.toolbar.setSubtitle("Defina o nome");
        setSupportActionBar(binding.toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        grupo = new Grupo();
//        idUsuario = UsuarioFirebase.getIdentificadorUsuario();
//        usuarioLogado = UsuarioFirebase.getDadosUsuarioLogado();

        easyImage = new EasyImage.Builder(this)
                // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
                .setCopyImagesToPublicGalleryFolder(true)
                // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
                .setFolderName("photo")
                .allowMultiple(false)
                .build();

        //Configurar evento de clique
        binding.content.imageGrupo.setOnClickListener(v -> easyImage.openChooser(CadastroGrupoActivity.this));

        //Recuperar lista de membros passada
        if (getIntent().getExtras() != null){
            List<Usuario> membros = (List<Usuario>) getIntent().getExtras().getSerializable("membros");
            listaMembrosSelecionados.addAll(membros);

            binding.content.textTotalParticipantes.setText("Participantes: " + listaMembrosSelecionados.size());
        }

        //Configurar recyclerview
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());

        RecyclerView.LayoutManager layoutManagerHorizontal = new LinearLayoutManager(
                getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        binding.content.recyclerMembrosGrupo.setLayoutManager(layoutManagerHorizontal);
        binding.content.recyclerMembrosGrupo.setHasFixedSize(true);
        binding.content.recyclerMembrosGrupo.setAdapter(grupoSelecionadoAdapter);

        //Configurar fab
        binding.fabSalvarGrupo.setOnClickListener(v -> {
            String nomeGrupo = binding.content.editNomeGrupo.getText().toString();

            //Adiciona a lista de membros o usuário que está logado
            listaMembrosSelecionados.add(UsuarioFirebase.getDadosUsuarioLogado());
            grupo.setMembros(listaMembrosSelecionados);
            grupo.setNome(nomeGrupo);
            grupo.salvar();

        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                binding.content.imageGrupo.setImageURI(Uri.fromFile(imageFiles[0].getFile()));

                //Recuperar dados da imagem para o firebase
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(imageFiles[0].getFile());
                    //Salvar imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("grupos")
                            .child(grupo.getId() + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(e -> Toast.makeText(CadastroGrupoActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(taskSnapshot -> {

                                imagemRef.getDownloadUrl().addOnCompleteListener(task -> {
                                    String url =  task.getResult().toString();
                                    grupo.setFoto(url);
                                });

                            });

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onImagePickerError(@NonNull Throwable error, @NonNull MediaSource source) {
                //Some error handling
                error.printStackTrace();
            }

            @Override
            public void onCanceled(@NonNull MediaSource source) {
                //Not necessary to remove any files manually anymore
            }
        });
    }
}