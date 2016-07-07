package de.mordsgau.musicpeb.dao;

import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Contains all
 */
public class Song implements Parcelable {
	private long id;
	private String artist;
	private String album;
	private String title;

	public Song(long id, String artist, String album, String title) {
		this.id = id;
		this.artist = artist;
		this.album = album;
		this.title = title;
	}

	public Song(Parcel p) {
		readFromParcel(p);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(id);
		dest.writeString(artist);
		dest.writeString(album);
		dest.writeString(title);
	}

	private void readFromParcel(Parcel in) {
		id = in.readLong();
		artist = in.readString();
		album = in.readString();
		title = in.readString();
	}

	public long getId() {
		return id;
	}

	public String getArtist() {
		return artist;
	}

	public String getAlbum() {
		return album;
	}

	public String getTitle() {
		return title;
	}

	public Uri getUri() {
		return ContentUris.withAppendedId(
				android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				id);
	}

	@Override
	public String toString() {
		return "Artist: " + artist + ", Album: " + album + ", Title: " + title;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public Song createFromParcel(Parcel in) {
			return new Song(in);
		}

		public Song[] newArray(int size) {
			return new Song[size];
		}
	};
}
