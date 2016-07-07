package de.mordsgau.musicpeb.player;

import java.util.ArrayList;
import java.util.List;

import de.mordsgau.musicpeb.dao.Song;

public class PlayList {
	
	
	private List<Song> songs;
	private int current;
	

	
	public PlayList(List<Song> songs) {
		current = 0;
		this.songs = songs;
	}
	
	public PlayList(Song s)  {
		List<Song> c = new ArrayList<Song>();
		c.add(s);
		this.songs = c;
		current = 0;
	}
	
	public PlayList(List<Song> songs, int startWith) {
		current = startWith;
		this.songs = songs;
	}
	
	public int getNumberOfSongs() {
		return songs.size();
	}
	
	public Song getCurrentSong() {
		return songs.get(current);
	}
	
	public int getCurrentSongNumber() {
		return current;
	}
	
	public List<Song> getList() {
		return songs;
	}
	
	public void addSongs(List<Song> c) {
		if(songs == null) {
			songs = c;
			current = 0;
		} else {
			songs.addAll(c);
		}
	}
	
	public void addSong(Song s) {
		songs.add(s);
	}
	
	/**
	 * queue a song to play after the current Song
	 * @param s Song != null
	 */
	public void addSongAfterCurrent(Song s) {
		songs.add(current+1, s);
	}
	
	public boolean nextSong() {
		// is there a next song?
		if (current + 1 < songs.size()) {
			current++;
			return true;
		} else {
			return false;
			
		}
	}
	
	public boolean previousSong() {
		// is there a previous song?
		if (current - 1 >= 0) {
			current--;
			return true;
		} else {
			return false;
		}
	}
}
