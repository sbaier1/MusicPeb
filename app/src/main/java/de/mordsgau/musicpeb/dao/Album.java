package de.mordsgau.musicpeb.dao;

import android.net.Uri;

public class Album {
	
	private String artistName;
	private String albumName;
//	private String albumCover;	TODO: Irgendsowas suchen
	private Uri albumUri;

	public Album(String artistName, Uri albumUri) {
		this.artistName = artistName;
		this.albumUri = albumUri;
	}

	public String getArtistName() {
		return artistName;
	}

	public String getAlbumName() {
		return albumName;
	}

	public Uri getAlbumUri() {
		return albumUri;
	}
}
