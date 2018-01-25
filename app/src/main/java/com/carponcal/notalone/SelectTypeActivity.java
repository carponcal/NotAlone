package com.carponcal.notalone;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class SelectTypeActivity extends AppCompatActivity {

    static final int CREATE_ACTIVITY = 1;  // The request code
    private static final int OK_RESULT_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_type);

    }


    public void selectTypeActivity(View view){
        Intent intent = new Intent(SelectTypeActivity.this, newActivityActivity.class);
        int typeImage;
        int type;

        switch (view.getId()){
            case R.id.img_bike:
                typeImage = R.drawable.ic_bike_black_48dp;
                type = 4;
                break;
            case R.id.img_mountain:
                typeImage = R.drawable.ic_image_filter_hdr_black_48dp;
                type = 6;
                break;
            case R.id.img_walk:
                typeImage = R.drawable.ic_walk_black_48dp;
                type = 2;
                break;
            case R.id.img_running:
                typeImage = R.drawable.ic_run_black_48dp;
                type = 1;
                break;
            case R.id.img_football:
                typeImage = R.drawable.ic_soccer_black_48dp;
                type = 7;
                break;
            case R.id.img_basketball:
                typeImage = R.drawable.ic_basketball_black_48dp;
                type = 8;
                break;
            case R.id.img_tennis:
                typeImage = R.drawable.ic_tennis_black_48dp;
                type = 5;
                break;
            case R.id.img_rollerblade:
                typeImage = R.drawable.rollerblade;
                type = 3;
                break;
            default:
                typeImage = 0;
                type = 0;
                break;

        }
        intent.putExtra("typeImage", typeImage);
        intent.putExtra("type", type);
        startActivityForResult(intent, CREATE_ACTIVITY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ((requestCode == CREATE_ACTIVITY)){

            setResult(OK_RESULT_CODE,data);
            finish();
        }
    }



}
