package com.example.whatsappclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    
    private ActivityLoginBinding binding;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnLogar.setOnClickListener(v -> {
            String textoEmail = binding.inputEmailLogin.getEditText().getText().toString();
            String textoSenha = binding.inputSenhaLogin.getEditText().getText().toString();

            if (!textoEmail.isEmpty() && !textoSenha.isEmpty()){
                verificarLogin();
            }else {
                Toast.makeText(LoginActivity.this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        verificarUsuarioLogado();
    }

    private void verificarUsuarioLogado() {
        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();

        if (auth.getCurrentUser() != null){
            abrirTelaPrincipal();
        }
    }

    private void verificarLogin() {
        String email = binding.inputEmailLogin.getEditText().getText().toString();
        String senha = binding.inputSenhaLogin.getEditText().getText().toString();

        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        abrirTelaPrincipal();

                    }else {
                        String execao = "";
                        try {
                            throw task.getException();
                        }catch (FirebaseAuthInvalidCredentialsException e){
                            execao = "E-mail ou senha inválidos!";
                        }catch (FirebaseAuthInvalidUserException e){
                            execao = "Usuário não encontrado";
                        }catch (Exception e){
                            execao = "Erro ao efetuar login: " + e.getMessage();
                            e.printStackTrace();
                        }

                        Toast.makeText(LoginActivity.this, execao, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void abrirTelaPrincipal() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    public void abrirTelaCadastro(View view){
        startActivity(new Intent(this, CadastroActivity.class));
    }
}