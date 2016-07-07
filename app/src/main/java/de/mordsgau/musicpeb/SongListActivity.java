package de.mordsgau.musicpeb;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import de.mordsgau.musicpeb.player.PlayerActivity;
import de.morgsgau.musicpeb.R;

public class SongListActivity extends ListActivity {
	private ContentResolver musicResolver;
	private String artist;
	private String album;
	
	public SongListActivity() {
		artist = "Unknown Artist";
		album  = "Unknown Album";
	}

	private UIUpdateListener myListener = new UIUpdateListener() {
		@Override
		public void onUIUpdate(boolean d) {
			onResume();
		}
		@Override
		public void onCompletion(){} //unnessecary here
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((ServiceAccess)this.getApplication()).getService().setOnUIUpdateListener(myListener);
		final ServiceAccess sa = ((ServiceAccess)this.getApplication());
		artist = sa.getArtist(true);
		album = sa.getAlbum(true);
		musicResolver = getContentResolver();
		setTitle(album);
		overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_slide_out);
		
		setContentView(R.layout.song_list_view);
		
		
		
		String projection[] = {
				"DISTINCT " + MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.DURATION
		};
		
		// SELECT title, duration FROM audio WHERE artist=artist AND album=album
		Cursor cursor = musicResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
											projection, 
											MediaStore.Audio.AudioColumns.ARTIST+" = \""+artist+"\" AND "
											+ MediaStore.Audio.AudioColumns.ALBUM+" = \""+album+"\"",
											null,
											MediaStore.Audio.Media.TRACK);
		List<String> songs = new ArrayList<String>();
		List<String> durations = new ArrayList<String>();
		int duration;	// in milliseconds
		String durationStr;
		while(cursor.moveToNext()){
		    songs.add(cursor.getString(0));
		    duration = cursor.getInt(1);
		    durationStr = String.format(getResources().getConfiguration().locale, "%d:%02d",
		    							TimeUnit.MILLISECONDS.toMinutes(duration),
		    							TimeUnit.MILLISECONDS.toSeconds(duration)%60);
		    durations.add(durationStr);
		}
		cursor.close();
		
	    
		ListView songListView = (ListView) this.findViewById(android.R.id.list);
		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albums);
		SongArrayAdapter adapter = new SongArrayAdapter(this, R.id.song, R.id.duration, songs, durations);
		
		if(adapter != null)
			setListAdapter(adapter);
		
		OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
			@Override public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
				LinearLayout v = (LinearLayout) view;
				TextView tv = (TextView) v.getChildAt(0); ;
				Intent intent = new Intent(parent.getContext(), PlayerActivity.class);
				sa.setTitle(tv.getText().toString());
				startActivity(intent);
				//finish(); // We can go back to the artist view by pressing the back button
			}
		};
		OnItemLongClickListener longClickListener = new AdapterView.OnItemLongClickListener() {
			@Override public boolean onItemLongClick (AdapterView<?> parent, View view, int position, long id) {
				if (sa.isPlaying()) {
					LinearLayout v = (LinearLayout) view;
					TextView tv = (TextView) v.getChildAt(0);
					sa.addSong(tv.getText().toString());
					Toast t = Toast.makeText(getBaseContext(),
							"Adding song to playback queue.", Toast.LENGTH_SHORT);
					t.show();
					return true;
				} else {
					LinearLayout v = (LinearLayout) view;
					TextView tv = (TextView) v.getChildAt(0); ;
					Intent intent = new Intent(parent.getContext(), PlayerActivity.class);
					sa.setTitle(tv.getText().toString());
					startActivity(intent);
					return true;
				}
			}
		};
		
		
		if(itemListener != null && songListView != null) {
			songListView.setOnItemClickListener(itemListener);
			songListView.setOnItemLongClickListener(longClickListener);
		}
		
	}
	
	@Override
	public void onStop() {
		super.onStop();
		((ServiceAccess)this.getApplication()).getService().removeUIUpdateListener(myListener);
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
	
	
	
	
	
}