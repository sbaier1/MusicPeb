package de.mordsgau.musicpeb.dao;

import android.net.Uri;

public class Artist {

	private String artistName;
	private Uri artistUri;

	public Artist(String artistName, Uri artistUri) {
		this.artistName = artistName;
		this.artistUri = artistUri;
	}

	public String getArtistName() {
		return artistName;
	}

	public Uri getArtistUri() {
		return artistUri;
	}
}
