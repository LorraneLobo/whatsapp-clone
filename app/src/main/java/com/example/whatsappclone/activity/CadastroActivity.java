package com.example.whatsappclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityCadastroBinding;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class CadastroActivity extends AppCompatActivity {

    private Usuario usuario;

    private FirebaseAuth auth;
    private ActivityCadastroBinding binding;

    private TextInputEditText campoNome, campoEmail, campoSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCadastrar.setOnClickListener(view -> cadastrar());
    }

    private void cadastrar(){
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.createUserWithEmailAndPassword(usuario.getEmail(), usuario.getSenha())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()){
                        Toast.makeText(CadastroActivity.this, "Usuário criado com sucesso!", Toast.LENGTH_SHORT).show();

//                        String idUsuario = Base64Custom.codificarBase64(usuario.getEmail());
//                        usuario.setIdUsuario(idUsuario);
                        usuario.salvar();
                        finish();
                    }else {

                        String execao = "";
                        try {
                            throw task.getException();
                        }catch (FirebaseAuthWeakPasswordException e){
                            execao = "A senha deve conter pelo menos 6 caracteres";
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            execao = "Por favor, digite um e-mail válido!";
                        }catch (FirebaseAuthUserCollisionException e){
                            execao = "Essa conta já foi cadastrada!";
                        }catch (Exception e){
                            execao = "Erro ao cadastrar o usuário: " + e.getMessage();
                            e.printStackTrace();
                        }

                        Toast.makeText(CadastroActivity.this, execao, Toast.LENGTH_SHORT).show();
                    }
                });
    }

}