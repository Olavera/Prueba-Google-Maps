package org.example.ejemplogooglemaps;

import java.util.ArrayList;
import java.util.Vector;



import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity   extends FragmentActivity implements OnMapClickListener, OnInfoWindowClickListener{

	private GoogleMap mapa;
	private Vector<Punto> points;
    Httppostaux post;
	//private LocationClient mLocationClient;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initilizeMap();
		iniciarTask();
		post = new Httppostaux();
	}

    // Inflate the menu items for use in the action bar
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main, menu);
	    return super.onCreateOptionsMenu(menu);
	}

	/** function to load map. 
	 *  If map is not created it will create it for you
	 */
	private void initilizeMap() {
		
		if (mapa == null) {
			mapa = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

			// check if map is created successfully or not
			if (mapa == null) {
				Toast.makeText(getApplicationContext(),
						"Sorry! unable to create maps", Toast.LENGTH_SHORT).show();
			}
			else{
				mapa.setMapType(GoogleMap.MAP_TYPE_NORMAL);
				mapa.setMyLocationEnabled(true);
				if (mapa.getMyLocation() != null)
					mapa.animateCamera(CameraUpdateFactory.newLatLngZoom(
							new LatLng( mapa.getMyLocation().getLatitude(), 
									mapa.getMyLocation().getLongitude()), 15));
				else
					Toast.makeText(getApplicationContext(),
							"Esperando ubicacion", Toast.LENGTH_SHORT).show();
				mapa.getUiSettings().setZoomControlsEnabled(false);
				mapa.getUiSettings().setCompassEnabled(true);
				mapa.setOnInfoWindowClickListener(this);
				mapa.setOnMapClickListener(this);
			}

		}
	}
	
	private void iniciarTask(){
		RetrieveFeed task = new RetrieveFeed();
		task.execute();
	}

	public void guardarCoche(View view) {
		ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

		parser.guardarPunto("Coche", new LatLng(mapa.getCameraPosition().target.latitude,
				mapa.getCameraPosition().target.longitude));

		iniciarTask();
	}

	@Override
	public void onMapClick(LatLng puntoPulsado) {
		ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

		parser.guardarPunto("prueba", puntoPulsado);

		iniciarTask();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());

		parser.eliminarPunto(marker.getTitle(), marker.getPosition());

		iniciarTask();
		
	}
	

	
	public void validar(View view) {
			
		new asynclogin().execute("usuarioA","contraseñaA"); 
	} 
	
	private class RetrieveFeed extends android.os.AsyncTask<String,Integer,Boolean> {


		protected Boolean doInBackground(String... params) {

			ParserXML_DOM parser = new ParserXML_DOM(getApplicationContext());
			points = parser.listaPuntos();
			
			return true;
		}

		protected void onPostExecute(Boolean result) {
			
			mapa.clear();

			for (Punto punto : points) {
				if (punto.getNombre().equals("Coche"))
					mapa.addMarker(new MarkerOptions()
					.position(punto.getCords())
					.title(punto.getNombre())
					.snippet(punto.getNombre())
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
				else
					mapa.addMarker(new MarkerOptions()
					.position(punto.getCords())
					.title(punto.getNombre())
					.snippet(punto.getNombre())
					.icon(BitmapDescriptorFactory
							.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
			}
		}

	}
    
    private class asynclogin extends AsyncTask< String, String, String > {
   	 
    	String user,pass;
 
		protected String doInBackground(String... params) {
			//obtnemos usr y pass
			user=params[0];
			pass=params[1];
			
			int id=-1;
	    	
	    	/*Creamos un ArrayList del tipo nombre valor para agregar los datos recibidos por los parametros anteriores
	    	 * y enviarlo mediante POST a nuestro sistema para relizar la validacion*/ 
	    	ArrayList<NameValuePair> postparameters2send= new ArrayList<NameValuePair>();
	     		
			    		postparameters2send.add(new BasicNameValuePair("email",user));
			    		postparameters2send.add(new BasicNameValuePair("pass",pass));

			   //realizamos una peticion y como respuesta obtenes un array JSON
	      		JSONArray jdata=post.getserverdata(postparameters2send, "http://padandroid.webcindario.com/index2.php");

			    //si lo que obtuvimos no es null
			    	if (jdata!=null && jdata.length() > 0){

			    		JSONObject json_data; //creamos un objeto JSON
						try {
							json_data = jdata.getJSONObject(0); //leemos el primer segmento en nuestro caso el unico
							 id=json_data.getInt("id");//accedemos al valor 
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}		            

						//validamos el valor obtenido
			    		 if (id==1){   		    		
			     			return "ok"; //login valido
			    		 }

				  }   		
    			return "err"; //login invalido 
        	
		}
       

        protected void onPostExecute(String result) {
           
           if (result.equals("ok")){

        	   Toast.makeText(getApplicationContext(),
						"Usuario correcto llega al servicio. ", Toast.LENGTH_SHORT)
						.show();

            }else{
            	Toast.makeText(getApplicationContext(),
						"Falla al conectar con el servicio", Toast.LENGTH_SHORT)
						.show();
            }
            
       }

    }

/*	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void validar(View v) {
		ArrayList parametros = new ArrayList();
		parametros.add("email");
		parametros.add("roberto");
		parametros.add("pass");
		parametros.add("morgan");
		// Llamada a Servidor Web PHP
		try {
			Post post = new Post();
			JSONArray datos = post.getServerData(parametros,
					"http://padandroid.webcindario.com/index2.php");
			// No se puede poner localhost, carga la consola de Windows
			// y escribe ipconfig/all para ver tu IP
			if (datos != null && datos.length() > 0) {
				JSONObject json_data = datos.getJSONObject(0);
				int numRegistrados = json_data.getInt("id");
				if (numRegistrados == 1) {
					Toast.makeText(getBaseContext(),
							"Usuario correcto. ", Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				Toast.makeText(getBaseContext(),
						"Usuario incorrecto. ", Toast.LENGTH_SHORT)
						.show();
			}
		} catch (Exception e) {
			Toast.makeText(getBaseContext(),
					"Error al conectar con el servidor. ",
					Toast.LENGTH_SHORT).show();
		}
		// FIN Llamada a Servidor Web PHP
	}

	class Post {
		private InputStream is = null;
		private String respuesta = "";

		@SuppressWarnings({ "rawtypes", "unchecked" })
		private void conectaPost(ArrayList parametros, String URL) {
			ArrayList nameValuePairs;
			try {

				HttpClient httpclient = new DefaultHttpClient();

				HttpPost httppost = new HttpPost(URL);
				nameValuePairs = new ArrayList();

				if (parametros != null) {
					for (int i = 0; i < parametros.size() - 1; i += 2) {
						nameValuePairs.add(new BasicNameValuePair((String)parametros.get(i), (String)parametros.get(i + 1)));
					}
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				}
				HttpResponse response = httpclient.execute(httppost);
				HttpEntity entity = response.getEntity();
				is = entity.getContent();
			} catch (Exception e) {

				Log.e("log_tag", "Error in http connection " + e.toString());

			} finally {

			}
		}

		private void getRespuestaPost() {
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "iso-8859-1"), 8);
				StringBuilder sb = new StringBuilder();
				String line = null;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
				is.close();
				respuesta = sb.toString();
				Log.e("log_tag", "Cadena JSon " + respuesta);
			} catch (Exception e) {

				Log.e("log_tag", "Error converting result " + e.toString());

			}
		}

		@SuppressWarnings("finally")
		private JSONArray getJsonArray() {
			JSONArray jArray = null;
			try {

				jArray = new JSONArray(respuesta);

			} catch (Exception e) {

			} finally {
				return jArray;
			}
		}

		@SuppressWarnings("rawtypes")
		public JSONArray getServerData(ArrayList parametros, String URL) {
			conectaPost(parametros, URL);
			if (is != null) {
				getRespuestaPost();
			}
			if (respuesta != null && respuesta.trim() != "") {
				return getJsonArray();
			} else {
				return null;
			}
		}
	}*/



}
