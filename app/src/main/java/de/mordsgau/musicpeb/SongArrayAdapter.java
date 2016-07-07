package de.mordsgau.musicpeb;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import de.morgsgau.musicpeb.R;

public class SongArrayAdapter extends ArrayAdapter<String> {
  private final Context context;
  private final List<String> songs;
  private final List<String> durations;
  int mFieldId;
  int mFieldIdDurations;

  public SongArrayAdapter(Context context, int idSong, int idDurations, List<String> songs, List<String> durations) {
    super(context, R.layout.song_list_element, songs);
    this.context = context;
    this.songs = songs;
    this.durations = durations;
    this.mFieldId = idSong;
    this.mFieldIdDurations = idDurations;
  }

  @Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	        return createViewFromResource(position, convertView, parent, R.layout.song_list_element);
	    }

	    private View createViewFromResource(int position, View convertView, ViewGroup parent,
	            int resource) {
	    	
	    	LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        View view;
	        TextView text;
	        TextView textDuration;

	        if (convertView == null) {
	            view = mInflater.inflate(R.layout.song_list_element, parent, false);
	        } else {
	            view = convertView;
	        }

	        try {
	            if (mFieldId == 0) {
	                //  If no custom field is assigned, assume the whole resource is a TextView
	                text = (TextView) view;
	                textDuration = (TextView) view;
	            } else {
	                //  Otherwise, find the TextView field within the layout
	                text = (TextView) view.findViewById(mFieldId);
	                textDuration = (TextView) view.findViewById(mFieldIdDurations);
	            }
	        } catch (ClassCastException e) {
	            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
	            throw new IllegalStateException(
	                    "ArrayAdapter requires the resource ID to be a TextView", e);
	        }

	        String item = songs.get(position);
	        String itemDuration = durations.get(position);
	        if (item instanceof CharSequence && itemDuration instanceof CharSequence) {
	            text.setText((CharSequence)item);
	            textDuration.setText((CharSequence)itemDuration);
	        } else {
	            text.setText(item.toString());
	        }

	        return view;
	    }
  }