package com.games.garrett.theliquorcabinet.fragments.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.games.garrett.theliquorcabinet.R;
import com.games.garrett.theliquorcabinet.activities.DetailsActivity;
import com.games.garrett.theliquorcabinet.POJOs.LCBOProductInformation;

import java.io.InputStream;
import java.util.ArrayList;

/**
 * The adapter holding LCBOProductInformation objects to be displayed by the
 * DisplayProductsFragment.
 * Created by garrett on 21/08/17.
 */

public class DisplayProductsAdapter extends RecyclerView.Adapter<DisplayProductsAdapter.DisplayProductsViewHolder>{

    /* Tag for logging */
    private final static String TAG = DisplayProductsAdapter.class.getCanonicalName();
    /* list of products to be displayed */
    private ArrayList<LCBOProductInformation> mProductDataSet;
    /* a reference to the context */
    private Context mContext;

    /* an inner class that defines the view to be displayed by the adapter */
      static class DisplayProductsViewHolder extends RecyclerView.ViewHolder{
         CardView  mCardView;
         ImageView mThumbnail;
         TextView  mProductTitle;
         TextView  mProductCompany;
         TextView  mProductOrigin;
         TextView  mProductQuantity;
         TextView  mProductPrice;

        /* constructor assigns layout views to member variables */
        private DisplayProductsViewHolder(View v){
            super(v);
            mCardView = (CardView) v.findViewById(R.id.display_product_card_view);
            mThumbnail = (ImageView) v.findViewById(R.id.thumbnail);
            mProductTitle = (TextView) v.findViewById(R.id.product_name);
            mProductCompany = (TextView) v.findViewById(R.id.product_company);
            mProductOrigin = (TextView) v.findViewById(R.id.product_origin);
            mProductQuantity = (TextView) v.findViewById(R.id.product_quantity);
            mProductPrice = (TextView) v.findViewById(R.id.product_price);
        }
    }

    /**
     * Constructor for this adapter
     * @param productDataSet the list of LCBO products to display
     * @param context        the context of the fragment/activity using this adapter
     */
    public DisplayProductsAdapter(ArrayList<LCBOProductInformation> productDataSet, Context context){
        mContext = context;
        mProductDataSet = productDataSet;
    }

    /* hook method for creating new views */
    @Override
    public DisplayProductsAdapter.DisplayProductsViewHolder onCreateViewHolder(ViewGroup parent,
                                                                               int viewType){

        View v;
        // creates a new view
        if(mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
             v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_product, parent, false);
        }else{
             v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.card_product_horizontal, parent, false);
        }

        // set view's attributes
        return  new DisplayProductsViewHolder(v);
    }

    /* Sets the attributes for the new card view */
    @Override
    public void onBindViewHolder(DisplayProductsViewHolder holder, @SuppressLint("RecyclerView") final int position){
        /* get the right object and set attributes of the card view */
        LCBOProductInformation item = mProductDataSet.get(position);
        holder.mProductTitle.setText(item.getName());
        holder.mProductCompany.setText(item.getProducerName());
        holder.mProductOrigin.setText(item.getOrigin());

        String text = item.getTotalPackageUnits() + "  pack";
        if(item.getTotalPackageUnits().equals("1")) holder.mProductQuantity.setVisibility(View.GONE);
        else                                        holder.mProductQuantity.setText(text);

        holder.mProductPrice.setText(convertCentsToDollars(item.getPrice()));
        setupImage(holder.mThumbnail, item);

        /* Clicking on the card should launch the activity_details activity */
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               // Log.d(TAG, "CardView has been clicked. Launching activity_details. ");
                Intent detailsIntent = DetailsActivity.makeDetailsActivityIntent(mContext,
                        mProductDataSet.get(position));
                mContext.startActivity(detailsIntent);
            }
        });
    }

    @Override
    public int getItemCount(){
        return mProductDataSet.size();
    }

    /* Helper method to set thumbnail */
    private void setupImage(ImageView productImage, LCBOProductInformation item){
        if(item.getImageThumbUrl().equals("null")){
            productImage
                    .setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.not_available_img));
        }

        else new DownloadImageTask(productImage).execute(item.getImageThumbUrl());
    }

    /* Helper function that converts string cents to string dollars */
    private String convertCentsToDollars(String priceInCents){
        return "$ " + priceInCents.substring(0, priceInCents.length()-2)
                + "." + priceInCents.substring(priceInCents.length()-2, priceInCents.length());
    }


    // async task prevents UI from freezing as image loads
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView productImage;

        DownloadImageTask(ImageView productImage) {
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
}
