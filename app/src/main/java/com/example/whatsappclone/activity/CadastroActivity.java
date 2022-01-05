package com.example.whatsappclone.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.whatsappclone.R;
import com.example.whatsappclone.config.ConfiguracaoFirebase;
import com.example.whatsappclone.databinding.ActivityCadastroBinding;
import com.example.whatsappclone.helper.Base64Custom;
import com.example.whatsappclone.model.Usuario;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CadastroActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private ActivityCadastroBinding binding;

    private TextInputEditText campoNome, campoEmail, campoSenha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCadastroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.buttonCadastrar.setOnClickListener(view -> {

            if (validarCampos()) {
                cadastrar();
            }
        });
    }

    private void cadastrar() {

        String email = binding.editEmail.getEditText().getText().toString();
        String senha = binding.editSenha.getEditText().getText().toString();

        auth = ConfiguracaoFirebase.getFirebaseAutenticacao();
        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(CadastroActivity.this, "Usuário criado com sucesso!", Toast.LENGTH_SHORT).show();
                        String nome = binding.editNome.getEditText().getText().toString();

                        Usuario usuario = new Usuario();
                        usuario.setNome(nome);
                        usuario.setIdUsuario(Base64Custom.codificarBase64(email));
                        usuario.setEmail(email);
                        usuario.salvar();
                        finish();
                    } else {

                        String execao = "";
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthWeakPasswordException e) {
                            execao = "A senha deve conter pelo menos 6 caracteres";
                        } catch (FirebaseAuthInvalidCredentialsException e) {
                            execao = "Por favor, digite um e-mail válido!";
                        } catch (FirebaseAuthUserCollisionException e) {
                            execao = "Essa conta já foi cadastrada!";
                        } catch (Exception e) {
                            execao = "Erro ao cadastrar o usuário: " + e.getMessage();
                            e.printStackTrace();
                        }

                        Toast.makeText(CadastroActivity.this, execao, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validarCampos() {
        List<EditText> editTextList = new ArrayList<>();
        editTextList.add(binding.editNome.getEditText());
        editTextList.add(binding.editEmail.getEditText());
        editTextList.add(binding.editSenha.getEditText());

        boolean isValid = true;

        for (EditText editText : editTextList) {
            if (editText.getText().toString().isEmpty()) {
                editText.setError("Preencha o campo");
                isValid = false;
            }
        }

        return isValid;
    }

}