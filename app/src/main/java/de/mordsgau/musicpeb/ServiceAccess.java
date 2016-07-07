package de.mordsgau.musicpeb;

import java.util.List;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Messenger;
import android.provider.MediaStore;
import android.util.Log;
import de.mordsgau.musicpeb.dao.Song;
import de.mordsgau.musicpeb.player.PlayList;
import de.mordsgau.musicpeb.player.PlayerService;

//TODO: improve IPC
public class ServiceAccess extends Application {
	private Messenger outMessenger;
	private Intent playIntent;
	private PlayerConnection connection = new PlayerConnection();
	private PlayerService serviceConnection;
	private PlayList playlist;
	private String viewArtist;
	private String viewAlbum;
	private String viewTitle;
	
	protected class PlayerConnection implements ServiceConnection {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
			serviceConnection = binder.getService();
			outMessenger = serviceConnection.getMessenger();
			serviceConnection.setOnUIUpdateListener(new UIUpdateListener() {
				@Override
				public void onUIUpdate(boolean d) {	}
				@Override
				public void onCompletion() {
					Log.i("INFO", "onCompletion() in PlayerConnection called. Playing next song.");
					if(!isPlaying())
						playNext();
				}
			});
			
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			outMessenger = null;
			serviceConnection = null;
		}
	};
    
	@Override
	public void onCreate() {
		super.onCreate();
		playIntent = new Intent(this.getApplicationContext(), PlayerService.class);
		this.startService(playIntent);
		bindService(playIntent, connection, Context.BIND_AUTO_CREATE);
	}
	
	public boolean isPlaying() {
		if(serviceConnection != null) {
			return serviceConnection.isPlaying();
		} else {
			return false;
		}
	}
	
	public boolean canPlay() {
		if(serviceConnection != null && playlist != null)
			return true;
		return false;
			
	}
	
	public void setArtist(String a) {
		viewArtist = a;
	}
	
	public void setAlbum(String a) {
		viewAlbum = a;
	}
	
	public void setTitle(String t) {
		viewTitle = t;
	}
	
	public void setSongFromViewInfo() {
		if(viewArtist == null && viewAlbum == null && viewTitle == null) {
			return;
		}
		if(serviceConnection.isPlaying()) {
			if(viewArtist == serviceConnection.getArtist() 
					&& viewAlbum == serviceConnection.getAlbum()
					&& viewTitle == serviceConnection.getTitle()) {
				return;
			}
			
		}
		Cursor c = getInfo(new String[] {MediaStore.Audio.Media._ID}, null);
		long songId = -1;
		c.moveToFirst();
		if(c.getCount() > 0)
			songId = c.getLong(0);
		c.close();
		setSong(new Song(songId, getArtist(true), getAlbum(true), getTitle(true)));
	}
	
	public Cursor getInfo(String[] projection, String sortBy) {
		return getInfoCustom(projection, sortBy, this.getArtist(true), this.getAlbum(true), this.getTitle(true));
	}
	
	
	public Cursor getInfoCustom(String[] projection, String sortBy, String artist, String album, String title) {
		Cursor c;
		String selectStr = (MediaStore.Audio.AudioColumns.ARTIST + " = \""
				+ artist + "\"");

		if (album != null)
			selectStr = (selectStr + " AND "
					+ MediaStore.Audio.AudioColumns.ALBUM + " = \"" + album + "\"");

		if (title != null)
			selectStr = (selectStr + " AND "
					+ MediaStore.Audio.AudioColumns.TITLE + " = \"" + title + "\"");

		c = this.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				projection, selectStr, null, sortBy);
		
		return c;
	}
	
	/**
	 * add a List of songs to the playlist.
	 * @param c List<Song>. if playlist is null, new playlist is created
	 */
	public void addSongs(List<Song> c) {
		if(playlist != null) {
		playlist.addSongs(c);
		} else {
			playlist = new PlayList(c);
			serviceConnection.playSong(playlist.getCurrentSong());
		}
	}
	
	public void setSongs(List<Song> c) {
		playlist = new PlayList(c);
		serviceConnection.playSong(playlist.getCurrentSong());
	}
	
	/**
	 * Set the Playlist to only a given Song and start playing it.
	 * @param s Song
	 */
	public void setSong(Song s) {
		playlist = new PlayList(s);
		serviceConnection.playSong(playlist.getCurrentSong());
	}
	
	/**
	 * Add a single song to the end of the playlist.
	 * @param s Song object != null
	 */
	public void addSong(Song s) { 
		if(playlist != null) {
		playlist.addSong(s);
		} else {
			playlist = new PlayList(s);
			serviceConnection.playSong(playlist.getCurrentSong());
		}
	}
	
	public void addSongAfterCurrent(String title) {
		Cursor c = getInfoCustom(new String[] { MediaStore.Audio.AudioColumns._ID}, null, getArtist(true), getAlbum(true), title);
		long songId = -1;
		c.moveToFirst();
		if(c.getCount() > 0)
			songId = c.getLong(0);
		c.close();
		playlist.addSongAfterCurrent(new Song(songId, viewArtist, viewAlbum, title));
	}
	
	public void addSong(String title) {
		Cursor c = getInfoCustom(new String[] { MediaStore.Audio.AudioColumns._ID}, null, getArtist(true), getAlbum(true), title);
		long songId = -1;
		c.moveToFirst();
		if(c.getCount() > 0)
			songId = c.getLong(0);
		c.close();
		playlist.addSong(new Song(songId, viewArtist, viewAlbum, title));
	}
	
	/**
	 * Add a single song to be played after the current song.
	 * @param s
	 */
	public void addSongAfterCurrent(Song s) {
		playlist.addSongAfterCurrent(s);
	}
	
	public Song getCurrentSong() {
		return (playlist != null) ? playlist.getCurrentSong() : null;
	}
	
	/**
	 * 
	 * @param viewMode return viewVariable (1) or currently playing one (0) (if existent)
	 * @return
	 */
	public String getArtist(boolean viewMode) {
		if(serviceConnection == null && viewMode == false) {
			return viewArtist;
		} else if(viewMode == true) {
			return viewArtist;
		} else {
			return serviceConnection.getArtist();
		}
	}
	
	public String getAlbum(boolean viewMode) {
		if(serviceConnection == null && viewMode == false) {
			return viewAlbum;
		} else if(viewMode == true) {
			return viewAlbum;
		} else {
			return serviceConnection.getAlbum();
		}
	}
	
	public String getTitle(boolean viewMode) {
		if(serviceConnection == null && viewMode == false) {
			return viewTitle;
		} else if(viewMode == true) {
			return viewTitle;
		} else {
			return serviceConnection.getTitle();
		}
	}
	
	
	public boolean playNext() {
		if(playlist.nextSong()) {
			serviceConnection.waitAndPlaySong(playlist.getCurrentSong());
			return true;
		}
		return false;
	}
	
	public boolean playPrevious() {
		if(playlist.previousSong()) {
			serviceConnection.playSong(playlist.getCurrentSong());
			return true;
		}
		return false;
	}
	
	/**
	 * self explanatory
	 * @return a reference to the player service
	 */
	public PlayerService getService() {
		return this.serviceConnection;
	}
	
	/**
	 * get the URI of a given Song
	 * @param artist String
	 * @param album String
	 * @param title String
	 * @return Uri of albumart
	 */
	public Uri getAlbumArtUri(String artist, String album, String title) {
		Cursor c;
		// get albumId by meta data
		String selectStr = (MediaStore.Audio.AudioColumns.ARTIST + " = \""
							+ artist + "\"");

			selectStr = (selectStr + " AND "
						+ MediaStore.Audio.AudioColumns.ALBUM + " = \"" + album + "\"");

			selectStr = (selectStr + " AND "
						+ MediaStore.Audio.AudioColumns.TITLE + " = \"" + title + "\"");

		c = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.ALBUM_ID }, selectStr, null, null);
		
		if(c.getCount() == 0) {
			return null;
		}
		// if there is an album cover, get its URI
		c.moveToFirst();
		long albumId = c.getLong(0);
		c.close();
		c = null;
		
		c = getContentResolver().query(ContentUris.withAppendedId(
					                  MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId),
						              new String[] { MediaStore.Audio.AlbumColumns.ALBUM_ART },
						              null,
						              null,
						              null);
		c.moveToFirst();
		
		String uri = c.getString(0);

		c.close();
		if(uri == null) 
			return null;
		
		return Uri.parse(uri);
	}
	
	/**
	 * Returns the uri of the song cover that is currently played.
	 * @return if the song has a cover return that's URI, otherwise return <code>null</code>
	 */
	public Uri getAlbumArtUri() {
		if(getArtist(false) == "" && getTitle(false) == "") {
			return null;
		}
		return getAlbumArtUri(getArtist(false), getAlbum(false), getTitle(false));
	}
	
	public Uri getViewAlbumArtUri() {
		return (playlist != null) ? getAlbumArtUri(this.getArtist(true), this.getAlbum(true), this.getTitle(true))
								  : getAlbumArtUri();
	}
	
	public Messenger getMessenger() {
		return outMessenger;
	}
}
