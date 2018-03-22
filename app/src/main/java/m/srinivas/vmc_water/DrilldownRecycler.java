package m.srinivas.vmc_water;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by USER on 17-07-2017.
 */

public class DrilldownRecycler extends RecyclerView.Adapter<DrilldownRecycler.ViewHolder>{
    ArrayList<Drilldown> drilldowns;
    Context context;
    public DrilldownRecycler(ArrayList<Drilldown> drilldowns, DashboardView dashboardView) {
          this.context = dashboardView;this.drilldowns = drilldowns;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.drilldownsingle,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
         holder.applicationname.setText(drilldowns.get(position).getApplicationname());
         holder.applicationno.setText(drilldowns.get(position).getApplicationno());
         holder.edit_image.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Intent updatestatus  = new Intent(context, updatestatus.class);
                 updatestatus.putExtra("intGrivanceid",drilldowns.get(position).getIntgrievanceid().toString());
                 context.startActivity(updatestatus);
             }
         });
    }

    @Override
    public int getItemCount() {
        return drilldowns.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView applicationname,applicationno;
        ImageView edit_image;
        public ViewHolder(View itemView) {
            super(itemView);
            edit_image = (ImageView) itemView.findViewById(R.id.edit_image);
            applicationname = (TextView) itemView.findViewById(R.id.applicationname);
            applicationno = (TextView) itemView.findViewById(R.id.applicationno);

        }
    }

}
