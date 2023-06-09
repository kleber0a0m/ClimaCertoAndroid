package br.com.kleberalbinomoreira.clima;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class BoasVindas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boas_vindas);
    }

    public void continuarBuscaCidade(View view) {
        Intent intent = new Intent(this, BuscaCidade.class);
        startActivity(intent);
    }
}