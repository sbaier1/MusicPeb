package de.mordsgau.musicpeb.player;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.ContentUris;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.MediaStore.Audio;
import android.util.Log;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;
import de.mordsgau.musicpeb.UIUpdateListener;
import de.mordsgau.musicpeb.dao.Song;

public class PlayerService extends Service implements OnPreparedListener,
		OnCompletionListener, OnErrorListener, MediaPlayerControl {

	/** Command to the service to display a message */
	public static final int MSG_SAY_HELLO = 1;
	public static final int MSG_PLAY_SONG = 2;
	public static final int MSG_PAUSE_TOGGLE = 3;
	public static final int MSG_STOP = 4;
	public static final int MSG_ISPLAYING = 5;


	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	private Messenger outMessenger;

	public final IBinder playerBinder = new PlayerBinder();

	private ArrayList<UIUpdateListener> listeners = new ArrayList<UIUpdateListener> ();
	
	private MediaPlayer player;
	private Song song;

	@Override
	public void onCreate() {
		super.onCreate();
		
		// init media player
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// player listener
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// init media player
		player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_MUSIC);
		// player listener
		player.setOnPreparedListener(this);
		player.setOnCompletionListener(this);
		player.setOnErrorListener(this);
	}


	/**
	 * Handler of incoming messages from clients.
	 */
	static class IncomingHandler extends Handler {

		private final WeakReference<PlayerService> mService;

		IncomingHandler(PlayerService service) {
			mService = new WeakReference<PlayerService>(service);
		}
		
		@Override
		public void handleMessage(Message msg) {
			if (mService.get().outMessenger == null)
				mService.get().outMessenger = msg.replyTo;

			switch (msg.what) {
			case MSG_PLAY_SONG:
				Song s = (Song) msg.obj;
				if (mService.get().song != null) {
					if (s.getId() != mService.get().song.getId())
						mService.get().playSong(s);
				} else {
					mService.get().playSong(s);
				}
				break;
			case MSG_PAUSE_TOGGLE:
				mService.get().togglePause();
				break;
			case MSG_STOP:
				mService.get().stop();
				break;
			case MSG_ISPLAYING:

				break;
			default:
				super.handleMessage(msg);
			}
		}
	}
	
	public void setOnUIUpdateListener (UIUpdateListener listener) 
    {
        this.listeners.add(listener);
    }
	
	public void removeUIUpdateListener(UIUpdateListener listener) {
		listeners.remove(listener);
	}

	public void setMediaController(MediaController m) {
		m.setMediaPlayer(this);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return playerBinder;
	}
 
	@Override
	public boolean onUnbind(Intent intent) {
		player.stop(); // TODO: nessecary? service persists after unbinding
		player.release();
		return super.onUnbind(intent);
	}

	/**
	 * Sets and plays the given song.
	 * 
	 * @param song
	 *            the song to play
	 */
	public void playSong(Song song) {
		if (!this.isPlaying()) {
			setSong(song);
			playSong();
		} else {
			player.stop();
			setSong(song);
			playSong();
		}
	}
	
	/**
	 * Wait for current Player to finish before playing the next song.
	 * 
	 * @param song Song to play after waiting
	 */
	public void waitAndPlaySong(Song song) {
		if (!this.isPlaying()) {
			setSong(song);
			playSong();
		} else {
			MediaPlayer next = new MediaPlayer();
			// init new media player
			next.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// player listener
			next.setOnPreparedListener(this);
			next.setOnCompletionListener(this);
			next.setOnErrorListener(this);
			player.setNextMediaPlayer(next);

			//final Song s = song;
			/*player.setOnCompletionListener(new OnCompletionListener() {

				@Override
				public void onCompletion(MediaPlayer mp) {
					setSong(s);
					playSong();
				}
			});*/
		}
	}
	

	public void togglePause() {
		if (player.isPlaying()) {
			player.pause();
		} else {
			player.start();
		}
	}

	public void setSong(Song song) {
		this.song = song;
	}

	public void stop() {
		player.stop();
		player.release();
	}

	private void playSong() {
		player.reset();
		Uri songUri = ContentUris.withAppendedId(
				Audio.Media.EXTERNAL_CONTENT_URI, song.getId());

		try {
			player.setDataSource(getApplicationContext(), songUri);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			Log.e("TESTTEST", "ILLEGALSTATE IN PLAYSONG()");
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		player.prepareAsync();
			player.setOnPreparedListener(new OnPreparedListener() { 
				@Override
				public void onPrepared(MediaPlayer mp) {
					mp.start();
					for (UIUpdateListener listener : listeners) 
					listener.onUIUpdate(mp.isPlaying());
				}
			});
			player.setOnCompletionListener(new OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					for (UIUpdateListener listener : listeners) {
					listener.onUIUpdate(mp.isPlaying());
					listener.onCompletion();}
				}
			});
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		mp.start();
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	
	  @Override public int getAudioSessionId() { return
	  player.getAudioSessionId(); }
	 

	@Override
	public int getBufferPercentage() {
		return 100;
	}

	@Override
	public int getCurrentPosition() {
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		return player.getDuration();
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void start() {
		player.start();
	}

	@Override
	public void seekTo(int pos) {
		player.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		if (player != null)
			return player.isPlaying();
		else
			return false;
	}
	
	public Messenger getMessenger() {
		return mMessenger;
	}
	
	public String getArtist() {
		return song.getArtist();
	}
	
	public String getAlbum() {
		return song.getAlbum();
	}
	
	public String getTitle() {
		return song.getTitle();
	}

	/**
	 * Necessary...
	 */
	public class PlayerBinder extends Binder {
		public PlayerService getService() {
			return PlayerService.this;
		}
	}
}
