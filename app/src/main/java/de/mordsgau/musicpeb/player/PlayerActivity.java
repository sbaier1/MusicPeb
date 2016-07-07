package de.mordsgau.musicpeb.player;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import de.mordsgau.musicpeb.ServiceAccess;
import de.mordsgau.musicpeb.UIUpdateListener;
import de.morgsgau.musicpeb.R;

public class PlayerActivity extends Activity {

	/**
	 * determine how to initialize the player: play the given artist
	 */
	public final static int PLAY_ARTIST = 0;
	/**
	 * determine how to initialize the player: play the given album
	 */
	public final static int PLAY_ALBUM = 1;
	/**
	 * determine how to initialize the player: play the given song
	 */
	public final static int PLAY_SONG = 2;
	/**
	 * determine how to initialize the player: add the given song to the
	 * playback queue
	 */
	public final static int ADD_SONG = 3;

	private ImageButton playPauseButton;
	private ImageButton nextButton;
	private ImageButton prevButton;

	private UIUpdateListener myListener = new UIUpdateListener() {
		@Override
		public void onUIUpdate(boolean d) {
			updateUI(d); // update the button's drawable when playSong finishes
		}
		@Override
		public void onCompletion(){} //unnessecary here
	};
	
	
	public PlayerActivity() {
	}

	
	
	@Override
	public void onCreate(Bundle icicle) {

		((ServiceAccess)this.getApplication()).getService().setOnUIUpdateListener(myListener);
		super.onCreate(icicle);
		setTitle("Now Playing"); // TODO
		overridePendingTransition(R.anim.anim_slide_in, R.anim.anim_slide_out);
		setContentView(R.layout.player_activity);

		Bundle b = getIntent().getExtras();
		if(b == null || !b.getBoolean("fromBottomBar")) {
			((ServiceAccess)this.getApplication()).setSongFromViewInfo();
		}
	}
	

	@Override
	protected void onStart() {
		super.onStart();
		final ServiceAccess sa = ((ServiceAccess)this.getApplication());
		
		// register button
		playPauseButton = (ImageButton) this
				.findViewById(R.id.playerPlayPauseButton);
		final Messenger messenger = ((ServiceAccess) this.getApplication())
				.getMessenger(); //FIXME: Messenger entfernen und ServiceAccess benutzen
		playPauseButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					Message pauseMessage = Message.obtain(null,
							PlayerService.MSG_PAUSE_TOGGLE, 0, 0);
					messenger.send(pauseMessage);
					Drawable d;
					d = (isPlaying() ? getResources().getDrawable(android.R.drawable.ic_media_play) : 
						getResources().getDrawable(android.R.drawable.ic_media_pause));
					
						playPauseButton.setImageDrawable(d);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		});
		
		nextButton = (ImageButton) this
				.findViewById(R.id.playerNextButton);
		nextButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(sa.playNext()) {
					setInfos();
				} else {
					Toast.makeText(getBaseContext(), "Reached playlist end"
							, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		prevButton = (ImageButton) this
				.findViewById(R.id.playerPrevButton);
		prevButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(sa.playPrevious()) {
					setInfos();
				} else {
					Toast.makeText(getBaseContext(), "Reached playlist beginning"
							, Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		Message songMessage = Message.obtain(null, PlayerService.MSG_PLAY_SONG,
				0, 0);
		songMessage.obj = ((ServiceAccess)this.getApplication()).getCurrentSong();
		try {
			messenger.send(songMessage);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		setInfos();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		((ServiceAccess)this.getApplication()).getService().removeUIUpdateListener(myListener);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateUI(isPlaying());
	}
	
	private void updateUI(boolean status) {
		Drawable d;
		d = (isPlaying() ? getResources().getDrawable(android.R.drawable.ic_media_pause) : 
			getResources().getDrawable(android.R.drawable.ic_media_play));
		ImageButton b = (ImageButton) this
				.findViewById(R.id.playerPlayPauseButton);
			b.setImageDrawable(d);
	}
	

	private void setInfos() {
		// set infos (depends on current song)
		ServiceAccess sa = ((ServiceAccess) this.getApplication());
		((TextView) this.findViewById(R.id.playerArtistName)).setText(sa.getArtist(false));
		((TextView) this.findViewById(R.id.playerAlbumName)).setText(sa.getAlbum(false));
		((TextView) this.findViewById(R.id.playerTitleName)).setText(sa.getTitle(false));

		Uri albumArtUri = sa.getAlbumArtUri();
		ImageView iv = ((ImageView) this.findViewById(R.id.playerAlbumCover));
		if (albumArtUri != null)
			iv.setImageURI(albumArtUri);
		else
			iv.setImageDrawable(getResources().getDrawable(R.drawable.player_default_album));
	}
	
	/**
	 * for onclick listeners
	 * @return isPlaying in Application class
	 */
	public boolean isPlaying() {
		return ((ServiceAccess) this.getApplication()).isPlaying();
	}
}
