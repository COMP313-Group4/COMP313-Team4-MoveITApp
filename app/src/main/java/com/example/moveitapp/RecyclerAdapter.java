package com.example.moveitapp;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/*import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;*/
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.sql.Driver;
import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
    private static final String tag = "RecyclerView";
    FirebaseFirestore firebaseFirestore;



    private Context mContext;
    private ArrayList<Load> loadArrayList;

    public RecyclerAdapter(Context context, ArrayList<Load> loadArrayList) {
        this.mContext = context;
        this.loadArrayList = loadArrayList;

    }




    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item_available_load, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvVehicleType.setText(String.format("Vehicle Type: %s", loadArrayList.get(position).getVehicleWanted()));
        holder.tvLoadSize.setText(String.format("Weight: %s", loadArrayList.get(position).getWeight()));
//      holder.btnDetails.setOnClickListener(getDataFromFirebase(););
        Log.d("Tag", "Positions: " + loadArrayList.size());
    }


    @Override
    public int getItemCount() {
        return loadArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvLoadSize;
        TextView tvVehicleType;
        Button btnDetails;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLoadSize = itemView.findViewById(R.id.tvLoadSize);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);

        }
    } //  ViewHolderClass

}
