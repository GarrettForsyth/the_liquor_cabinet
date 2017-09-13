package com.games.garrett.theliquorcabinet.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.activities.utils.GenerateRandomString;

import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.gms.security.ProviderInstaller.ProviderInstallListener;

/**
 * This activity helps the user navigate to the to other activities in the application.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    /* array holding the res id for the buttons */
    private static final int[] BUTTON_IDS = {
            R.id.quick_rate_button,
            R.id.browse_button,
            R.id.recommend_button,
            R.id.liquor_logs_button,
            R.id.settings_button
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        upgradeSecurityProvider();

        setUpButtons();
        assignUniqueUserId();

        TextView title = (TextView) findViewById(R.id.title);
        title.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

    }

    /**
     * Assigns a unique userID that is used to create a use profile
     * to make recommendations.
     */
    private void assignUniqueUserId(){
        SharedPreferences mPref = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);
        String userID = mPref.getString("userID", "");
        if  (userID.equals("")){
            SharedPreferences.Editor editor = mPref.edit();
            editor.putString("userID", GenerateRandomString.randomString(30));
            editor.apply();
        }
    }

    private void setUpButtons(){
        for(int id : BUTTON_IDS){
            Button button = (Button) findViewById(id);
            button.setOnClickListener(this);
            button.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
        }
    }

    /* Handles click events for each button */
    public void onClick(View view){

        switch(view.getId()){
            case R.id.quick_rate_button:
                Intent quickRateIntent = QuickRateActivity.makeQuickRateActivityIntent(this);
                startActivity(quickRateIntent);
                break;
            case R.id.browse_button:
                Intent browseIntent = BrowseActivity.makeBrowseActivityIntent(this);
                startActivity(browseIntent);
                break;
            case R.id.recommend_button:
                Intent recommendIntent = RecommendationActivity.makeRecommendationActivityIntent(this);
                startActivity(recommendIntent);
                break;
            case R.id.liquor_logs_button:
                Intent liquorLogsIntent = LiquorLogActivity.makeLiquorLogsActivityIntent(this);
                startActivity(liquorLogsIntent);
                break;
            case R.id.settings_button:
                Intent settingsIntent = SettingsActivity.makeSettingsActivityIntent(this);
                startActivity(settingsIntent);
                break;
            default:
                break;
        }
    }

    /**
     *  Updates provider if needed to protect against SSL exploits.
     */
    private void upgradeSecurityProvider() {
        ProviderInstaller.installIfNeededAsync(this, new ProviderInstallListener() {
            @Override
            public void onProviderInstalled() {

            }

            @Override
            public void onProviderInstallFailed(int errorCode, Intent recoveryIntent) {
                GooglePlayServicesUtil.showErrorNotification(errorCode, getApplication());
            }
        });
    }
}
