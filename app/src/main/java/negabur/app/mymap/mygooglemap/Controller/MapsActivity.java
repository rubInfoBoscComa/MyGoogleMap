package negabur.app.mymap.mygooglemap.Controller;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import negabur.app.mymap.mygooglemap.Model.Ubicacio;
import negabur.app.mymap.mygooglemap.R;

/**
 * Created by Ruben on 6/3/15.s
 */
public class MapsActivity extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    //Definició de variables
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    private ImageButton btnCerca;
    private EditText txtCerca;
    private Spinner spinMapa;


    private static final String URL_DATA = "http://www.infobosccoma.net/pmdm/pois.php";

    private ArrayList<Ubicacio> dades;

    /**
     * Mètode On Create que s'executa a l'iniciar l'aplicació
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
        //Defincio dels buttons
        btnCerca = (ImageButton) findViewById(R.id.btnCerca);

        txtCerca = (EditText) findViewById(R.id.txtCercador);

        spinMapa = (Spinner) findViewById(R.id.spinMapa);
        spinMapa.setOnItemSelectedListener(this);

        //Ens situa en un pnt exacte del mapa, en aquest cas en la localitat de Vic
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(41.9432686, 1.8890318), 7));


        btnCerca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (comprovarConnexio()) {
                    try {
                        new DescarregarDades().execute(URL_DATA);
                    } catch (IllegalStateException ex) {

                    }

                }
            }
        });

    }

    /**
     * Mètode que detecta el canvi d'orientació per poder tornar a evaluar la proximitat del zoom
     * @param newConfig
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);

        //Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
          if(dades != null){
              centerMap();
          }
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
        {
            if(dades != null){
                centerMap();
            }
        }
    }

    /**
     * Mètode que comprova la connexió a internet i demana si vols activar-la
     * En cas d'afirmació, obre el menu de configuracions
     * @return true o false
     */
    public boolean comprovarConnexio() {

        //Si no hi ha cap connexio, s'obre el dialeg
        if (!CheckInternet.connexioMovil(this) && !CheckInternet.connexioWifi(this)) {
            //Dialeg
            AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setTitle("Sense Connexió");
            //Assignant missatge
            builder1.setMessage("Sembla que no tens connexió a  Internet! i per tant no pots cercar cap ubicació");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Activar Internet",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent i = new Intent(Settings.ACTION_SETTINGS);
                            startActivity(i);

                        }
                    });
            builder1.setNegativeButton("No Activar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                        }
                    });

            AlertDialog confirmMessage = builder1.create();
            confirmMessage.show();
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }



    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * Mètode que ens estableix el primer punt en el mapa
     */
    private void setUpMap() {
        mMap.addMarker(new MarkerOptions().position(new LatLng(41.9306266,2.2547965)).title("Vic"));
    }

    /**
     * Mètode que centra el mapa per a  poder visualitzar tots els punts al mapa
     */
    private void centerMap() {
        LatLngBounds.Builder bounds = new LatLngBounds.Builder();

        for (Ubicacio u : dades) {
            bounds.include(new LatLng(u.getLatitude(), u.getLongitude()));

        }
        LatLngBounds boundsOk = bounds.build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsOk, 150));
    }

    /**
     * Mètode que ens afegeix  les posicions al mapa
     */
    private void addPoints(ArrayList<Ubicacio> llista) {
        mMap.clear();
        for (Ubicacio l : llista) {
            mMap.addMarker(new MarkerOptions().position(new LatLng(l.getLatitude(), l.getLongitude())).title(l.getName()));
            centerMap();
        }

    }


    /**
     * Mètode per escollir el tipus de mapa
     * Normal/Híbrid/Topogràfic/Satèl·lit
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        String tipus = (String) parent.getItemAtPosition(position);

        switch (position) {

            case 0:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case 1:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            case 2:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;

            case 3:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    class DescarregarDades extends AsyncTask<String, Void, ArrayList<Ubicacio>> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Mètode de la classe AsincTask
         * doInBackground que s'executa en segon pla
         * @param params
         * @return
         */
        @Override
        protected ArrayList<Ubicacio> doInBackground(String... params) {
            ArrayList<Ubicacio> llistaTitulars = null;
            DefaultHttpClient httpclient = new DefaultHttpClient();
            HttpPost httppostreq = new HttpPost(URL_DATA);
            HttpResponse httpresponse = null;
            try {
                // Afegir els paràmetres
                List<NameValuePair> parametres = new ArrayList<NameValuePair>(1);
                if (txtCerca.getText().toString() != null) {
                    //Afegir paràmetre
                    parametres.add(new BasicNameValuePair("city", txtCerca.getText().toString()));
                }
                httppostreq.setEntity(new UrlEncodedFormEntity(parametres));
                httpresponse = httpclient.execute(httppostreq);
                String responseText = EntityUtils.toString(httpresponse.getEntity());
                llistaTitulars = tractarJSON(responseText);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return llistaTitulars;
        }

        /**
         * Mètode que s'executa al finalitzar l'execució realitzatda en segon pla (do in Background)
         * @param llista
         */
        @Override
        protected void onPostExecute(ArrayList<Ubicacio> llista) {
            dades = llista;
            addPoints(dades);
        }

        /**
         * Metode que tracta el JSOn
         * @param json
         * @return
         */
        private ArrayList<Ubicacio> tractarJSON(String json) {
            Gson converter = new Gson();
            return converter.fromJson(json, new TypeToken<ArrayList<Ubicacio>>() {
            }.getType());
        }
    }
}
