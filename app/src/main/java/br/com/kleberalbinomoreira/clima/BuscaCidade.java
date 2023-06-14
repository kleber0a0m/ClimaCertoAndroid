package br.com.kleberalbinomoreira.clima;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.content.Context;


import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class BuscaCidade extends AppCompatActivity {

    private List<Cidade> cidadeList = new ArrayList<>();

    private Button btnBuscar;
    private TextView textView;

    private Cidade cidadeSelecionada = new Cidade();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busca_cidade);
        sharedPreferences();

    }

    public void sharedPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (sharedPreferences.contains("cidadeId") && sharedPreferences.contains("cidadeNome") && sharedPreferences.contains("cidadeUf")) {
            cidadeSelecionada.setUsarLocalizacao(false);
            cidadeSelecionada.setId(sharedPreferences.getString("cidadeId", "0"));
            cidadeSelecionada.setNome(sharedPreferences.getString("cidadeNome", "0"));
            cidadeSelecionada.setUf(sharedPreferences.getString("cidadeUf", "0"));

            Log.i("cidadeId", sharedPreferences.getString("cidadeId", "0"));
            Log.i("cidadeNome", sharedPreferences.getString("cidadeNome", "0"));
            Log.i("cidadeUf", sharedPreferences.getString("cidadeUf", "0"));

            Intent intent = new Intent(BuscaCidade.this, PrevisaoTempo.class);
            intent.putExtra("cidade", cidadeSelecionada);
            startActivity(intent);
        }

    }

    public void buscarCidade(View view) {
        EditText cidade = findViewById(R.id.editCidade);
        try {
            String nomeCidade = cidade.getText().toString();
            String url = "http://servicos.cptec.inpe.br/XML/listaCidades?city=" + removerAcentos(nomeCidade);
            new Tarefa().execute(url);
        }catch (Exception e){
            Log.e("Erro", e.getMessage());
        }
    }

    public void buscarLocalizacao(View view) {

        Location location = obterLocalizacaoAtual();
        String url = null;
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            url = "http://servicos.cptec.inpe.br/XML/cidade/7dias/" + latitude + "/" + longitude + "/previsaoLatLon.xml";

        } else {
            Toast.makeText(this, "Não foi possível obter a localização atual", Toast.LENGTH_LONG).show();
        }
        Intent intent = new Intent(BuscaCidade.this, PrevisaoTempo.class);
        Cidade cidade = new Cidade();
        cidade.setLatitude(String.valueOf(location.getLatitude()));
        cidade.setLongitude(String.valueOf(location.getLongitude()));
        cidade.setUsarLocalizacao(true);
        cidade.setUrlLocalizacao(url);
        intent.putExtra("cidade", cidade);
        startActivity(intent);
    }

    private class Tarefa  extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... strings) {
            String dados = Conexao.getDados(strings[0]);
            System.out.println("Dados: "+dados);
            return dados;
        }

        @Override
        protected void onPostExecute(String s) {
            cidadeList = ConsumirXML.getCidade(s);

            if (cidadeList.size() == 1) {
                cidadeSelecionada = cidadeList.get(0);
                cidadeSelecionada.setUsarLocalizacao(false);
                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cidadeId", cidadeSelecionada.getId());
                editor.putString("cidadeNome", cidadeSelecionada.getNome());
                editor.putString("cidadeUf", cidadeSelecionada.getUf());
                editor.commit();

                Intent intent = new Intent(BuscaCidade.this, PrevisaoTempo.class);
                intent.putExtra("cidade", cidadeSelecionada);
                startActivity(intent);
            }

            if (cidadeList.size() > 1) {
                exibirPopupSelecaoCidade(cidadeList);
            }

            if(cidadeList.isEmpty()){
                EditText cidade = findViewById(R.id.editCidade);
                exibirToast("Cidade não encontrada");
                cidade.setText("");
            }

        }
    }

    private void exibirPopupSelecaoCidade(final List<Cidade> cidades) {
        String[] nomesCidades = new String[cidades.size()];
        for (int i = 0; i < cidades.size(); i++) {
            nomesCidades[i] = cidades.get(i).getNome()+" - "+cidades.get(i).getUf();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Selecione a cidade");
        builder.setItems(nomesCidades, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                cidadeSelecionada = cidades.get(which);

                cidadeSelecionada.setUsarLocalizacao(false);
                SharedPreferences sharedPreferences = getSharedPreferences("sharedPreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("cidadeId", cidadeSelecionada.getId());
                editor.putString("cidadeNome", cidadeSelecionada.getNome());
                editor.putString("cidadeUf", cidadeSelecionada.getUf());
                editor.commit();

                Intent intent = new Intent(BuscaCidade.this, PrevisaoTempo.class);
                intent.putExtra("cidade", cidadeSelecionada);
                startActivity(intent);

            }
        });
        builder.show();
    }

    public Location obterLocalizacaoAtual() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Verifica permissão de acesso à localização
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permissão não concedida, retorne null ou solicite permissão ao usuário aqui
            return null;
        }

        // Obtenha a última localização conhecida do provedor de localização
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // Se a última localização não estiver disponível, tente o provedor de rede
        if (location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }

        return location;
    }

    public void exibirToast(String mensagem) {
        Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
    }

    public static String removerAcentos(String texto) {
        // Normaliza o texto para remover os acentos
        String textoNormalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);

        // Substitui os caracteres acentuados por versões sem acentos
        textoNormalizado = textoNormalizado.replaceAll("[^\\p{ASCII}]", "");

        return textoNormalizado;
    }
}