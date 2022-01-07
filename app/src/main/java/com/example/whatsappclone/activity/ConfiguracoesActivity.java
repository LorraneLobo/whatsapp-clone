package com.example.whatsappclone.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityConfiguracoesBinding;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.Permissao;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class ConfiguracoesActivity extends AppCompatActivity {

    private EasyImage easyImage;
    private ActivityConfiguracoesBinding binding;
    private StorageReference storageReference;
    private String idUsuario;

    private String[] permissoesNecessarias = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConfiguracoesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Configurações iniciais
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        idUsuario = UsuarioFirebase.getIdentificadorUsuario();

        //Validar permissões
        Permissao.validarPermissoes(permissoesNecessarias, this, 1);

        Toolbar toolbar = findViewById(R.id.toolbarPrincipal);
        toolbar.setTitle("Configurações");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Recuperar dados do usuário
        FirebaseUser usuario = UsuarioFirebase.getUsuarioAtual();
        Uri url = usuario.getPhotoUrl();

        if (url != null){
            Glide.with(ConfiguracoesActivity.this)
                    .load(url)
                    .into(binding.circleImageViewFotoPerfil);
        }else {
            binding.circleImageViewFotoPerfil.setImageResource(R.drawable.padrao);
        }

        binding.editPerfilNome.setText(usuario.getDisplayName());

        easyImage = new EasyImage.Builder(this)
                // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
                .setCopyImagesToPublicGalleryFolder(true)
                // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
                .setFolderName("photo")
                .allowMultiple(false)
                .build();

        binding.imageButtonCamera.setOnClickListener(v -> {
            easyImage.openChooser(this);
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {
                binding.circleImageViewFotoPerfil.setImageURI(Uri.fromFile(imageFiles[0].getFile()));

                //Recuperar dados da imagem para o firebase
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(imageFiles[0].getFile());
                    //Salvar imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("perfil")
                            .child(idUsuario + ".jpeg");

                    UploadTask uploadTask = imagemRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(e -> Toast.makeText(ConfiguracoesActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(taskSnapshot -> {
                                Toast.makeText(ConfiguracoesActivity.this, "Sucesso ao fazer upload da imagem", Toast.LENGTH_SHORT).show();

                                imagemRef.getDownloadUrl().addOnCompleteListener(task -> {
                                   Uri url =  task.getResult();
                                    atualizaFotoUsuario(url);
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

    public void atualizaFotoUsuario(Uri url){
        UsuarioFirebase.atualizarFotoUsuario(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int permissaoResultado : grantResults) {
            if (permissaoResultado == PackageManager.PERMISSION_DENIED) {
                alertaValidacaoPermissao();
            }
        }
    }

    private void alertaValidacaoPermissao() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Permissões Negadas");
        builder.setCancelable(false);
        builder.setMessage("Para utilizar o app é necessário aceitar as permissões solicitadas");
        builder.setPositiveButton("Confirmar", (dialog, which) -> finish());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}