package de.mordsgau.musicpeb;


import android.net.Uri;

public class Artist {
private String artistName;
private Uri artistUri;
	public Artist() {
		artistName = "";
		artistUri = null;
	}
	
	public Artist(String artistName, Uri artistLink) {
		this.artistName = artistName;
		this.artistUri = artistLink;
	}
	
	public String getArtistName() {
		return artistName;
	}
	
	public Uri getArtistUri() {
		return artistUri;
	}
}
