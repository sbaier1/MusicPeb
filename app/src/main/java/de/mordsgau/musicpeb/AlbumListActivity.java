package de.mordsgau.musicpeb;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
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

public class AlbumListActivity extends ListActivity {
	private String artist;
	
	public AlbumListActivity() {
		artist = "Unknown Artist";
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
		setTitle(artist);
		overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_slide_out);
		setContentView(R.layout.album_list_view);
		
		Log.e("E", "artist: "+artist);
		Cursor cursor = sa.getInfo(new String[] { "DISTINCT " + MediaStore.Audio.AudioColumns.ALBUM }
								, MediaStore.Audio.AudioColumns.ALBUM);
		
		List<String> albums = new ArrayList<String>();
		while(cursor.moveToNext()){
		    albums.add(cursor.getString(0));
		}
		cursor.close();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, albums);
		
		ListView albumView = (ListView) this.findViewById(android.R.id.list);
		
		
		if(adapter != null)
			setListAdapter(adapter);
		OnItemClickListener itemListener = new AdapterView.OnItemClickListener() {
			@Override public void onItemClick (AdapterView<?> parent, View view, int position, long id) {
				TextView tv = (TextView) view;
				Intent intent = new Intent(parent.getContext(), SongListActivity.class);
				sa.setAlbum(tv.getText().toString());
				sa.setTitle(null);
				startActivity(intent);
				//finish(); // We can go back to the artist view by pressing the back button
			}
		};
		if(itemListener != null && albumView != null)
			albumView.setOnItemClickListener(itemListener);
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
			param = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0,
					1.0f);
			updateBottomBar(param);
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
	
	public void updateBottomBar(LinearLayout.LayoutParams param) {
		TextView tv = (TextView) findViewById(R.id.text_bottom1);
		String artist = ((ServiceAccess) this.getApplication())
				.getArtist(false);
		tv.setText(artist);
		tv = (TextView) findViewById(R.id.text_bottom2);
		String title = ((ServiceAccess) this.getApplication()).getTitle(false);
		tv.setText(title);
		Uri albumArtUri = ((ServiceAccess) this.getApplication())
				.getAlbumArtUri();
		ImageView iv = (ImageView) findViewById(R.id.albumCover);

		iv.setImageURI(albumArtUri);
	}
	

	
	
}
