package com.games.garrett.theliquorcabinet.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.games.garrett.theliquorcabinet.GlobalStrings;
import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;
import com.games.garrett.theliquorcabinet.activities.utils.InventoryCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

/**
 * Simple activity that displays detailed information about an LCBO product. In particular,
 * it will display which local stores have how much of this item is their inventory.
 * Created by Garrett on 8/1/2017.
 */

public class DetailsActivity  extends AppCompatActivity implements InventoryCallBack{

    /* A tag for logging */
    private static final String TAG = DetailsActivity.class.getCanonicalName();

    LCBOProductInformation mProduct;

    // LCBO Product attributes :
    TextView mProductName;
    TextView mProducerName;
    ImageView mProductImage;
    TextView mOnSale;
    TextView mOnPromotion;
    TextView mSaleEndDate;
    TextView mPrice;
    TextView mDescription;
    TextView mOrigin;
    TextView mStyle;
    TextView mPrimaryCategory;
    TextView mSecondaryCategory;
    TextView mTertiaryCategory;
    TextView mTastingNote;
    TextView mAlcoholContent;
    TextView mSugarContent;
    TextView mVolume;
    TextView mUnits;

    LinearLayout mInventoryLinearLayout;
    LayoutInflater mInflater;

    ArrayList<String> mLocalStoresWithProduct;
    ArrayList<String> mStoreInventoryAddresses;
    ArrayList<String> mStoreInventoryQuantities;

    SharedPreferences mPref;
    ArrayList<String> mLocalStoreIds;
    ArrayList<String> mLocalStoreAddresses;

    @Override
    protected  void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mPref = this.getSharedPreferences("activity_settings", Context.MODE_PRIVATE);

        mProduct = (LCBOProductInformation) getIntent().getExtras().getSerializable("PRODUCT");
        mLocalStoresWithProduct = new ArrayList<>();
        mStoreInventoryAddresses = new ArrayList<>();
        mStoreInventoryQuantities = new ArrayList<>();

        mInventoryLinearLayout = (LinearLayout) findViewById(R.id.inventory_linear_layout);
        mInflater = LayoutInflater.from(this);

        setupImage();
        setupOnPromotion();
        setupOnSale();
        setupProducerName();
        setupProductAlcoholContent();
        setupProductDescription();
        setupProductName();
        setupProductOrigin();
        setupProductPrice();
        setupProductPrimaryCategory();
        setupProductSecondaryCategory();
        setupProductStyle();
        setupProductTastingNote();
        setupProductTertiaryCategory();
        setupProductUnits();
        setupProductVolume();
        setupSaleEndDate();
        setupSugarContent();

        findLocalStoresWithItem();

    }

    /**
     * Creates and returns an intent suitable for starting this activity
     * @param context  the context of the activity sending the initial intent
     * @param product  the item the activity wishes to have activity_details of
     * @return         and intent to start this activity.
     */
    public static Intent makeDetailsActivityIntent(Context context, LCBOProductInformation product){
        Bundle bundle = new Bundle();
        bundle.putSerializable("PRODUCT", product);
        return new Intent(context, DetailsActivity.class).putExtras(bundle);
    }

    private void setupProductName(){
        mProductName = (TextView) findViewById(R.id.details_product_name);
        mProductName.setText(mProduct.getName());
        mProductName.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProducerName(){
        mProducerName = (TextView) findViewById(R.id.details_producer_name);
        mProducerName.setText(mProduct.getProducerName());
        if(mProduct.getProducerName().equals("null"))  mProducerName.setVisibility(View.GONE);
        mProducerName.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupImage(){
        mProductImage = (ImageView) findViewById(R.id.details_product_image);
        if(mProduct.getImageUrl().equals("null")) mProductImage.setVisibility(View.GONE);
        else new DownloadImageTask(mProductImage).execute(mProduct.getImageUrl());
    }

    private void setupOnSale(){
        mOnSale = (TextView) findViewById(R.id.details_product_on_sale);
        String text = "Is on sale? " + mProduct.getIsOnSale();
        mOnSale.setText(text);
        mOnSale.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupOnPromotion(){
        mOnPromotion = (TextView) findViewById(R.id.details_product_on_promotion);
        String text = "Is on promotion? " + mProduct.getOnPromotion();
        mOnPromotion.setText(text);
        mOnPromotion.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupSaleEndDate(){
        mSaleEndDate = (TextView) findViewById(R.id.details_product_sale_end_date);
        String text = "Sale ends on: " + mProduct.getSaleEndsOn();
        mSaleEndDate.setText(text);
        if(mProduct.getSaleEndsOn().equals("null")) mSaleEndDate.setText("");
        mSaleEndDate.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductPrice(){
        mPrice = (TextView) findViewById(R.id.details_product_price);
        String text = convertCentsToDollars(mProduct.getPrice());
        mPrice.setText(text);
        if(mProduct.getPrice().equals("null")) mPrice.setVisibility(View.GONE);
        mPrice.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    /* Helper function that converts string cents to string dollars */
    private String convertCentsToDollars(String priceInCents){
        return "$ " + priceInCents.substring(0, priceInCents.length()-2)
                + "." + priceInCents.substring(priceInCents.length()-2, priceInCents.length());
    }

    private void setupProductDescription(){
        mDescription = (TextView) findViewById(R.id.details_product_description);
        String text= "\nDescription: " + mProduct.getDescription() +'\n';
        mDescription.setText(text);
        if(mProduct.getDescription().equals("null")) mDescription.setVisibility(View.GONE);
        mDescription.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductOrigin(){
        mOrigin = (TextView) findViewById(R.id.details_product_origin);
        String text = "Origin: " + mProduct.getOrigin();
        mOrigin.setText(text);
        if(mProduct.getOrigin().equals("null")) mOrigin.setVisibility(View.GONE);
        mOrigin.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductStyle(){
        mStyle = (TextView) findViewById(R.id.details_product_style);
        String text = "Style: " + mProduct.getStyle();
        mStyle.setText(text);
        if(mProduct.getStyle().equals("null")) mStyle.setVisibility(View.GONE);
        mStyle.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductPrimaryCategory(){
        mPrimaryCategory = (TextView) findViewById(R.id.details_product_primary_category);
        String text = "Primary Category: " + mProduct.getPrimaryCategory();
        mPrimaryCategory.setText(text);
        if(mProduct.getPrimaryCategory().equals("null")) mPrimaryCategory.setVisibility(View.GONE);
        mPrimaryCategory.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductSecondaryCategory(){
        mSecondaryCategory = (TextView) findViewById(R.id.details_product_secondary_category);
        String text = "Secondary Category: " + mProduct.getSecondaryCategory();
        mSecondaryCategory.setText(text);
        if(mProduct.getSecondaryCategory().equals("null")) mSecondaryCategory.setVisibility(View.GONE);
        mSecondaryCategory.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductTertiaryCategory(){
        mTertiaryCategory = (TextView) findViewById(R.id.details_product_tertiary_category);
        String text = "Tertiary Category: " + mProduct.getTertiaryCategory();
        mTertiaryCategory.setText(text);
        if(mProduct.getTertiaryCategory().equals("null")) mTertiaryCategory.setVisibility(View.GONE);
        mTertiaryCategory.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductTastingNote(){
        mTastingNote = (TextView) findViewById(R.id.details_product_tasting_note);
        String text = "\nTasting Note: " + mProduct.getTastingNote() + "\n";
        mTastingNote.setText(text);
        if(mProduct.getTastingNote().equals("null") || (mProduct.getTastingNote()).equals(mProduct.getDescription())){
            mTastingNote.setVisibility(View.GONE);
        }
        mTastingNote.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);

    }

    private void setupProductAlcoholContent(){
        mAlcoholContent = (TextView) findViewById(R.id.details_product_alcohol_content);
        mAlcoholContent.setText("Alcohol Content: " + Double.parseDouble(mProduct.getAlcoholContent())/100 + " %");
        if(mProduct.getAlcoholContent().equals("null")) mAlcoholContent.setVisibility(View.GONE);
        mAlcoholContent.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupSugarContent(){
        mSugarContent = (TextView) findViewById(R.id.details_product_sugar_content);
        String text = "Sugar Content: " + mProduct.getSugarContent();
        mSugarContent.setText(text);
        if(mProduct.getSugarContent().equals("null")) mSugarContent.setText("");
        mSugarContent.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductVolume(){
        mVolume = (TextView) findViewById(R.id.details_product_volume);
        mVolume.setText("Volume: " +  mProduct.getVolume() + " mL");
        if(mProduct.getVolume().equals("null")) mVolume.setVisibility(View.GONE);
        mVolume.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void setupProductUnits(){
        mUnits = (TextView) findViewById(R.id.details_product_units);
        String text = "Number of units: " + mProduct.getTotalPackageUnits();
        mUnits.setText(text);
        if(mProduct.getTotalPackageUnits().equals("null")) mUnits.setVisibility(View.GONE);
        mUnits.setShadowLayer((float) 40.0, (float) 0.0, (float) -30.0, Color.BLACK);
    }

    private void findLocalStoresWithItem(){

        //Log.d(TAG, " findLocalStoresWithItemEntered..");

        mLocalStoreIds = loadArrayFromSharedPreferences("ids");
        mLocalStoreAddresses = loadArrayFromSharedPreferences("addresses");


        //Log.d(TAG, " loaded " + mLocalStoreIds.size() + " ids and " + mLocalStoreAddresses.size() + " addresses");

        for(String storeID : mLocalStoreIds){
            String urlAddress = storeID + "/products/" + mProduct.getId() + "/inventory";
            new getLocalStoresFromProductID(this).execute(urlAddress);
        }

    }

    private ArrayList<String> loadArrayFromSharedPreferences(String item){
        ArrayList<String> arr = new ArrayList<>();
        int size = mPref.getInt(item + "_size", 0);

        for(int i=0; i < size; i++){
            arr.add(mPref.getString(item + "_" + i , null));
        }
        return arr;
    }

    /**
     * A call back method called when the async task which populates the
     * members variable list containing store address and their respective
     * inventory count (mStoreInventoryAddresses and mStoreInventoryQuantities)
     * has added a new item and that the view in this activity should be updated
     * to reflect the change.
     * @param success  true if the async task found a local store with 0 or more of this item
     */
    public void asyncInventoryCallComplete(boolean success){
        //Log.d(TAG, "entered callback with : " + success);
        if(success){
            int indexOfLatestElement = mStoreInventoryAddresses.size() -1;

            View rootView = mInflater.inflate(R.layout.row_inventory, mInventoryLinearLayout, false);
            // row_inventory just contains a a relative layout with two text views
            RelativeLayout rl = (RelativeLayout) rootView;
            TextView inventoryAddressView = (TextView) rl.findViewById(R.id.inventory_address_view);
            TextView inventoryQuantityView = (TextView) rl.findViewById(R.id.inventory_quantity_view);

            String quantityString = mStoreInventoryQuantities.get(indexOfLatestElement) + " in stock.";

            inventoryAddressView.setTextColor(ContextCompat.getColor(this, R.color.dark_yellow));
            inventoryQuantityView.setTextColor(ContextCompat.getColor(this, R.color.dark_yellow));

            inventoryAddressView.setText(mStoreInventoryAddresses.get(indexOfLatestElement));
            inventoryQuantityView.setText(quantityString);

            mInventoryLinearLayout.addView(rl);
        }
        TextView inventoryLabel = (TextView) findViewById(R.id.inventory_list_title);
        String noStores = "No local stores have this item.";
        String foundStores = "Local Stores' Inventory:";
        if(mStoreInventoryAddresses.size() == 0)  inventoryLabel.setText(noStores);
        else                                      inventoryLabel.setText(foundStores);
    }



    // async task prevents UI from freezing as image loads
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView productImage;

        private DownloadImageTask(ImageView productImage) {
            this.productImage = productImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            Bitmap bm = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                bm = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                //Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return bm;
        }

        protected void onPostExecute(Bitmap result) {
           productImage.setImageBitmap(result);
        }
    }

    /**
     * AsyncTask that access the LCBO API to check if the product ID of this item is in
     * any of the local stores' inventory.
     *
     * Requires an instance of InventoryCallBack in its constructor which is notified
     * with the results of the check.
     */
    private class getLocalStoresFromProductID extends AsyncTask<String,Void, JSONObject>{

        /* reference tot he activity to be notified when task is complete */
        private InventoryCallBack mListener;

        /* constructor */
        getLocalStoresFromProductID(InventoryCallBack listener){
             mListener = listener;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        /**
         * Opens a HttpURLConnection to the url given in urls.It then parses the JSON response,
         * updates the book keeping list in the otter class (mInventoryStoreAddresses and
         * mStoreInventoryQuantities) and notifies the InventoryCallBack of the change.
         * @param urls  urls the service should access containing inventory information
         * @return      the JSON response from the server.
         */
        @Override
        protected JSONObject doInBackground(String... urls){
           // Log.d(TAG, "Attempting to fetch from " + GlobalStrings.getLcboApiStoresUrl() + urls[0] + "?access_key=" +
            //        GlobalStrings.getLcboDevKey());
            JSONObject json = null;
            try {
                URL url = new URL(( GlobalStrings.getLcboApiStoresUrl() + urls[0] + "?access_key=" +
                        GlobalStrings.getLcboDevKey()));
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    json = readResponseStream(in);
                } finally {
                    urlConnection.disconnect();
                }
            }catch(Exception e){
               // Log.d(TAG, "Error connecting to lcbo inventory page.");
               // Log.d(TAG, "size is " + mStoreInventoryAddresses.size());
            }
            if (processLCBOProductResponse(json)) return json;
            else return null;
        }

        @Override
        protected void onPostExecute(JSONObject json){
            if( json != null) mListener.asyncInventoryCallComplete(true);
            else              mListener.asyncInventoryCallComplete((false));
        }

        /**
         * Parses the JSON returned from the LCBO API and adds the item to the list of
         * items to be displayed by updating the fragment's adapter.
         * @param page response JSONObject from API.
         *
         */
        private boolean processLCBOProductResponse (JSONObject page){
            if (page == null) return false;
            try {
                JSONObject itemOnPage = page.getJSONObject("result");
                String quantity = itemOnPage.getString("quantity");
                String storeID = itemOnPage.getString("store_id");

                if (quantity != null && !quantity.equals("0")) {
                    updateLists(storeID, quantity);
                    return true;
                }
                else return false;

            }catch (JSONException e){
                //Log.e(TAG, "Error parsing store json string : " + e.getMessage());
            }
            return false;
        }

        /** Updates the instance variable lists in the outer class */
        private void updateLists(String storeId, String quantity){
            String localStore = mLocalStoreAddresses.get(mLocalStoreIds.indexOf(storeId));
           // Log.d(TAG, "adding new entry to lists : " + localStore + " with " + quantity);
            mStoreInventoryAddresses.add(localStore);
            mStoreInventoryQuantities.add(quantity);
        }


        /**
         * Reads the response from the server
         * @param in  InputStream from the HttpUrlConnection
         * @return   response of the server in as a JSONObject
         */
        private JSONObject readResponseStream(InputStream in){
            String json = null;
            JSONObject jOBj = null;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                    sb.append("\n");
                }
                in.close();

               // Log.d(TAG, "RESPONSE : " + sb.toString());
                json = sb.toString();
            }catch (IOException e){
               // Log.d(TAG, "Buffer error while trying to read server response.");
            }

            try{
                jOBj = new JSONObject(json);
            }catch (JSONException e){
               // Log.e(TAG, "Error parsing JSON data " + e.toString());
            }

            return jOBj;
        }

    }

}
