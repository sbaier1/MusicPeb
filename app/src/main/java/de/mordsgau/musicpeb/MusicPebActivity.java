package de.mordsgau.musicpeb;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import de.mordsgau.musicpeb.player.PlayerActivity;
import de.morgsgau.musicpeb.R;

public class MusicPebActivity extends ListActivity {

	//FIXME: no UIUpdateListener (Service starting too late)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO: WICHTIG!!! DATENBANKANFRAGEN OPTIMIEREN! (Nach Keys, AlbumArtist Tabelle nutzen)
		// TODO: OptionenMen� in Jeder Klasse
		// TODO: PlayList in ServiceAccess
		
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_peb);
		String[] projection = { "DISTINCT " + MediaStore.Audio.Media.ARTIST };
		ContentResolver musicResolver = getContentResolver();
		Cursor cursor = musicResolver.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
				MediaStore.Audio.AudioColumns.ARTIST + " not null", null,
				MediaStore.Audio.AudioColumns.ARTIST);


		cursor.moveToFirst();
		List<String> artists = new ArrayList<String>();
		while (cursor.moveToNext()) {
			artists.add(cursor.getString(0));
		}
		cursor.close();
		ListView artistView = (ListView) this.findViewById(android.R.id.list);

		final ServiceAccess sa = ((ServiceAccess)this.getApplication());
				
		OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				TextView tv = (TextView) view;
				// Log.i("test", "Current item: "+ tv.getText().toString()); // for debugging purposes
				Intent intent = new Intent(parent.getContext(), AlbumListActivity.class);
				sa.setArtist(tv.getText().toString());
				sa.setAlbum(null);
				sa.setTitle(null);
				startActivity(intent);
			}
		};
		artistView.setOnItemClickListener(itemListener);

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, artists);
		if (adapter != null)
			setListAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		ServiceAccess sa = ((ServiceAccess) this.getApplication());
		boolean display = (sa.isPlaying() || sa.canPlay());
		LinearLayout songFrame = (LinearLayout) findViewById(R.id.songFrame);
		LinearLayout.LayoutParams param;
		if(display) {
			param = new LinearLayout.LayoutParams(
		                LayoutParams.MATCH_PARENT,
		                0, 1.0f);
			TextView tv = (TextView) findViewById(R.id.text_bottom1);
			String artist = ((ServiceAccess) this.getApplication()).getArtist(false);
			tv.setText(artist);
			tv = (TextView) findViewById(R.id.text_bottom2);
			String title = ((ServiceAccess) this.getApplication()).getTitle(false);
			tv.setText(title);
			Uri albumArtUri = ((ServiceAccess) this.getApplication()).getAlbumArtUri();
			ImageView iv = (ImageView) findViewById(R.id.albumCover);
			
			iv.setImageURI(albumArtUri);
		} else {
			param = new LinearLayout.LayoutParams(
		                LayoutParams.MATCH_PARENT,
		                0, 0.0f);
		}
		songFrame.setLayoutParams(param);
		
		songFrame.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(v.getContext(), PlayerActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("fromBottomBar", true);
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music_peb, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_music_peb, container, false);
			return rootView;
		}
	}

}
