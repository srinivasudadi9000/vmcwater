package m.srinivas.vmc_water;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class Home extends Activity implements View.OnClickListener{
   TextView mytitle;
   ImageView logo_img;
   Button register_btn,verification_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        mytitle = (TextView) findViewById(R.id.mytitle);
        logo_img = (ImageView) findViewById(R.id.logo_img);
        verification_btn = (Button) findViewById(R.id.verification_btn);
        register_btn = (Button) findViewById(R.id.register_btn);
        Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.zoomin);
        mytitle.startAnimation(slideUp);
        logo_img.startAnimation(
                AnimationUtils.loadAnimation(Home.this, R.anim.rotation) );
        register_btn.setOnClickListener(this);
        verification_btn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_btn:
                Intent reg = new Intent(Home.this,Registeration.class);
                startActivity(reg);
                break;
            case R.id.verification_btn:
                Intent dashboard = new Intent(Home.this,Dashboard.class);
                startActivity(dashboard);
                break;
        }
    }
}
