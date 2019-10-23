package ca.kevinjarocki.mapseventrecorder.Adapters;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import ca.kevinjarocki.mapseventrecorder.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.MarkerOptions;

public class MarkerRecyclerAdapter extends RecyclerView.Adapter<MarkerRecyclerAdapter.ViewHolder> {

    private ArrayList<MarkerOptions> MarkerOptions;

    public View RecyclerView;
    public ViewHolder holder;
    public Context RecyclerContext;
    TextView comment;
/*    TextView lat;
    TextView longi;*/

    public MarkerRecyclerAdapter(Context context, ArrayList<MarkerOptions> markers) {
        MarkerOptions = markers;
        RecyclerContext = context;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_marker_list,parent,false);
        holder = new ViewHolder(RecyclerView);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.comment.setText("Comment: " + MarkerOptions.get(position).getSnippet()
                + String.valueOf(" Latitude: "+MarkerOptions.get(position).getPosition().latitude)
                + String.valueOf(" Longitude: " +MarkerOptions.get(position).getPosition().longitude));
    }
    @Override
    public int getItemCount() {
        return MarkerOptions.size();
    }

    public void ResetMarkers() {
        MarkerOptions.clear();

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView comment;
        public ViewHolder(View itemView) {
            super(itemView);
            comment = (TextView)itemView.findViewById(R.id.comment);

        }
    }

}
