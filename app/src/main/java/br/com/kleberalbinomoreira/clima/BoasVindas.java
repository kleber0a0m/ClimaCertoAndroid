package br.com.kleberalbinomoreira.clima;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class BoasVindas extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boas_vindas);
        sharedPreferences();
    }

    public void continuarBuscaCidade(View view) {
        Intent intent = new Intent(this, BuscaCidade.class);
        startActivity(intent);
    }

    public void sharedPreferences(){
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if(sharedPreferences.contains("primeiroAcesso")){
            editor.putBoolean("primeiroAcesso", false);
            Intent intent = new Intent(this, BuscaCidade.class);
            startActivity(intent);
        }else{
            editor.putBoolean("primeiroAcesso", true);
        }

        boolean valorBooleano = sharedPreferences.getBoolean("primeiroAcesso", false);
        Log.i("valorBooleano", String.valueOf(valorBooleano));
        editor.apply();
    }
}