package com.example.whatsappclone.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.example.whatsappclone.adapter.MensagensAdapter;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityChatBinding;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.helper.UsuarioFirebase;
import com.example.whatsappclone.model.Conversa;
import com.example.whatsappclone.model.Grupo;
import com.example.whatsappclone.model.Mensagem;
import com.example.whatsappclone.model.Usuario;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.whatsappclone.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;
import pl.aprilapps.easyphotopicker.MediaFile;
import pl.aprilapps.easyphotopicker.MediaSource;

public class ChatActivity extends AppCompatActivity {

    private EasyImage easyImage;

    private ActivityChatBinding binding;
    private Usuario usuarioDestinatario;
    private MensagensAdapter adapter;
    private List<Mensagem> mensagens = new ArrayList<>();
    private DatabaseReference database;
    private StorageReference storageReference;
    private DatabaseReference mensagensRef;
    private Grupo grupo;
    private ChildEventListener childEventListenerMensagens;

    //identificador usuarios remetente e destinatario
    private String idUsuarioRemetente;
    private String idUsuarioDestinatario;
    private Usuario usuarioRemetente;

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

        //recupera dados do usuario remetente
        idUsuarioRemetente = UsuarioFirebase.getIdentificadorUsuario();
        usuarioRemetente = UsuarioFirebase.getDadosUsuarioLogado();

        //Recuperar dados do usuário destinatario
        Bundle bundle = getIntent().getExtras();
        if (bundle != null){

            if (bundle.containsKey("chatGrupo")){
                grupo = (Grupo) bundle.getSerializable("chatGrupo");
                binding.textViewNomeChat.setText(grupo.getNome());
                idUsuarioDestinatario = grupo.getId();

                String foto = grupo.getFoto();
                if (foto != null){
                    Uri uri = Uri.parse(foto);
                    Glide.with(ChatActivity.this)
                            .load(uri)
                            .into(binding.circleImageFotoChat);
                }else{
                    binding.circleImageFotoChat.setImageResource(R.drawable.padrao);
                }

            }else {
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

                idUsuarioDestinatario = Base64Custom.codificarBase64(usuarioDestinatario.getEmail());
            }
        }

        //Configuração adapter
        adapter = new MensagensAdapter(mensagens, getApplicationContext());

        //Configuração recyclerview
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        binding.content.recyclerMensagens.setLayoutManager(layoutManager);
        binding.content.recyclerMensagens.setHasFixedSize(true);
        binding.content.recyclerMensagens.setAdapter(adapter);

        database = ConfiguracaoFirebase.getFirebaseDatabase();
        storageReference = ConfiguracaoFirebase.getFirebaseStorage();
        mensagensRef = database.child("mensagens")
                .child(idUsuarioRemetente)
                .child(idUsuarioDestinatario);

        easyImage = new EasyImage.Builder(this)
                // Setting to true will cause taken pictures to show up in the device gallery, DEFAULT false
                .setCopyImagesToPublicGalleryFolder(true)
                // Sets the name for images stored if setCopyImagesToPublicGalleryFolder = true
                .setFolderName("photo")
                .allowMultiple(false)
                .build();

        //Evento de clique na camera
        binding.content.imageCameraMensagem.setOnClickListener(v -> easyImage.openChooser(this));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        easyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onMediaFilesPicked(MediaFile[] imageFiles, MediaSource source) {

                //Recuperar dados da imagem para o firebase
                try {
                    byte[] bytes = FileUtils.readFileToByteArray(imageFiles[0].getFile());

                    //Criar nome da imagem
                    String nomeImagem = UUID.randomUUID().toString();

                    //Salvar imagem no firebase
                    final StorageReference imagemRef = storageReference
                            .child("imagens")
                            .child("fotos")
                            .child(idUsuarioRemetente)
                            .child(nomeImagem);

                    UploadTask uploadTask = imagemRef.putBytes(bytes);
                    uploadTask.addOnFailureListener(e -> Toast.makeText(ChatActivity.this, "Erro ao fazer upload da imagem", Toast.LENGTH_SHORT).show())
                            .addOnSuccessListener(taskSnapshot -> {

                                imagemRef.getDownloadUrl().addOnCompleteListener(task -> {
                                    String url =  task.getResult().toString();

                                    if (usuarioDestinatario != null){
                                        Mensagem mensagem = new Mensagem();
                                        mensagem.setIdUsuario(idUsuarioRemetente);
                                        mensagem.setMensagem("imagem.jpeg");
                                        mensagem.setImagem(url);

                                        //Salvar mensagem remetente
                                        salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                                        //Salvar mensagem destinatario
                                        salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);
                                    }else {
                                        for (Usuario membro: grupo.getMembros()){
                                            String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                                            String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                                            Mensagem mensagem = new Mensagem();
                                            mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                                            mensagem.setMensagem("imagem.jpeg");
                                            mensagem.setNome(usuarioRemetente.getNome());
                                            mensagem.setImagem(url);

                                            //Salvar mensagem para o membro
                                            salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                                            //Salvar conversa
                                            salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                                            //Limpar texto
                                            binding.content.editMensagem.setText("");
                                        }
                                    }
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

    public void enviarMensagem(View v){
        String textoMensagem = binding.content.editMensagem.getText().toString();

        if (!textoMensagem.isEmpty()){
            if (usuarioDestinatario != null){
                Mensagem mensagem = new Mensagem();
                mensagem.setIdUsuario(idUsuarioRemetente);
                mensagem.setMensagem(textoMensagem);

                //salvar mensagem para o remetente
                salvarMensagem(idUsuarioRemetente, idUsuarioDestinatario, mensagem);

                //salvar mensagem para o destinatario
                salvarMensagem(idUsuarioDestinatario, idUsuarioRemetente, mensagem);

                //Salvar conversa remetente
                salvarConversa(idUsuarioRemetente, idUsuarioDestinatario, usuarioDestinatario, mensagem, false);
                //Salvar conversa destinatario
                salvarConversa(idUsuarioDestinatario, idUsuarioRemetente, usuarioRemetente, mensagem, false);

                //Limpar texto
                binding.content.editMensagem.setText("");
            }else {
                for (Usuario membro: grupo.getMembros()){
                    String idRemetenteGrupo = Base64Custom.codificarBase64(membro.getEmail());
                    String idUsuarioLogadoGrupo = UsuarioFirebase.getIdentificadorUsuario();

                    Mensagem mensagem = new Mensagem();
                    mensagem.setIdUsuario(idUsuarioLogadoGrupo);
                    mensagem.setMensagem(textoMensagem);
                    mensagem.setNome(usuarioRemetente.getNome());

                    //Salvar mensagem para o membro
                    salvarMensagem(idRemetenteGrupo, idUsuarioDestinatario, mensagem);

                    //Salvar conversa
                    salvarConversa(idRemetenteGrupo, idUsuarioDestinatario, usuarioDestinatario, mensagem, true);

                    //Limpar texto
                    binding.content.editMensagem.setText("");
                }
            }

        }

    }

    private void salvarConversa(String idRemetente, String idDestinatario, Usuario usuarioExibicao, Mensagem mensagem, boolean isGroup) {
        //salvar conversa remetente
        Conversa conversaRemetente = new Conversa();
        conversaRemetente.setIdRemetente(idRemetente);
        conversaRemetente.setIdDestinatario(idDestinatario);
        conversaRemetente.setUltimaMensagem(mensagem.getMensagem());

        if (isGroup){
            //conversa de grupo
            conversaRemetente.setIsGroup("true");
            conversaRemetente.setGrupo(grupo);
        }else {
            //conversa normal
            conversaRemetente.setUsuarioExibicao(usuarioExibicao);

        }
        conversaRemetente.salvar();
    }

    public void salvarMensagem(String idRemetente, String idDestinatario, Mensagem msg){
        DatabaseReference database = ConfiguracaoFirebase.getFirebaseDatabase();
        DatabaseReference mensagemRef = database.child("mensagens");

        mensagemRef.child(idRemetente)
                .child(idDestinatario)
                .push()
                .setValue(msg);

    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarMensagens();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mensagensRef.removeEventListener(childEventListenerMensagens);
    }

    private void recuperarMensagens(){

        mensagens.clear();

        childEventListenerMensagens = mensagensRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                Mensagem mensagem = snapshot.getValue(Mensagem.class);
                mensagens.add(mensagem);
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