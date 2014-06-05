package org.example.ejemplogooglemaps;


import java.util.Vector;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.LocationClient;
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
	//private LocationClient mLocationClient;

	@Override 
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initilizeMap();
		iniciarTask();
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



}
