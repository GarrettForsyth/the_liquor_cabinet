package com.games.garrett.theliquorcabinet.POJOs;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Plain Old Java Object containing a LCBO product's attributes.
 * Created by Garrett on 7/31/2017.
 */

public class LCBOProductInformation implements Parcelable, Serializable{

    /* Used to serialize and allow passing intents*/
    private static final long serialVersionUID = 1L;

    /* String used to tag log messages */
    private static final String TAG = LCBOProductInformation.class.getCanonicalName();

    private String id;
    private String name;
    private String producerName;
    private String alcoholContent;
    private String description;
    private String isOnSale;
    private String onPromotion;
    private String saleEndsOn;
    private String price;
    private String primaryCategory;
    private String secondaryCategory;
    private String tertiaryCategory;
    private String style;
    private String imageUrl;
    private String imageThumbUrl;
    private String sugarContent;
    private String tags;
    private String tastingNote;
    private String totalPackageUnits;
    private String volume;
    private String origin;

    /**
     * Constructor to create from a JSONObject.
     * @param item JSONObject containing item's attributes
     */
    public LCBOProductInformation(JSONObject item){
        try{
            id = item.getString("id");
            name = item.getString("name");
            producerName = item.getString("producer_name");
            alcoholContent = item.getString("alcohol_content");
            description = item.getString("description");
            isOnSale = item.getString("has_limited_time_offer");
            onPromotion = item.getString("has_value_added_promotion");
            saleEndsOn = item.getString("limited_time_offer_ends_on");
            price = item.getString("price_in_cents");
            primaryCategory = item.getString("primary_category");
            secondaryCategory = item.getString("secondary_category");
            tertiaryCategory = item.getString("tertiary_category");
            style = item.getString("style");
            imageUrl = item.getString("image_url");
            imageThumbUrl = item.getString("image_thumb_url");
            sugarContent  = item.getString("sugar_content") ;
            tags = item.getString("tags");
            tastingNote  = item.getString("tasting_note");
            totalPackageUnits = item.getString("total_package_units");
            volume  = item.getString("volume_in_milliliters");
            origin = item.getString("origin");
        }catch (JSONException e){
           // Log.e(TAG, "Error parsing JSON String while creating LCBOProductInformation object.");
        }
    }

    public String getName() {
        return name;
    }

    public String getProducerName() {
        return producerName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getId() {
        return id;
    }

    public String getAlcoholContent() {
        return alcoholContent;
    }

    public String getDescription() {
        return description;
    }

    public String getIsOnSale() {
        return isOnSale;
    }

    public String getOnPromotion() {
        return onPromotion;
    }

    public String getSaleEndsOn() {
        return saleEndsOn;
    }

    public String getPrice() {
        return price;
    }

    public String getPrimaryCategory() {
        return primaryCategory;
    }

    public String getSecondaryCategory() {
        return secondaryCategory;
    }

    public String getTertiaryCategory() {
        return tertiaryCategory;
    }

    public String getStyle() {
        return style;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getImageThumbUrl() {
        return imageThumbUrl;
    }

    public String getSugarContent() {
        return sugarContent;
    }

    @SuppressWarnings("unused")
    public String getTags() {
        return tags;
    }

    public String getTastingNote() {
        return tastingNote;
    }

    public String getTotalPackageUnits() {
        return totalPackageUnits;
    }

    public String getVolume() {
        return volume;
    }


    /* everything below here is for implementing Parcelable */

    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(id);
        out.writeString(name);
        out.writeString(producerName);
        out.writeString(alcoholContent);
        out.writeString(description);
        out.writeString(isOnSale);
        out.writeString(onPromotion);
        out.writeString(saleEndsOn);
        out.writeString(price);
        out.writeString(primaryCategory);
        out.writeString(secondaryCategory);
        out.writeString(tertiaryCategory);
        out.writeString(style);
        out.writeString(imageUrl);
        out.writeString(imageThumbUrl);
        out.writeString(sugarContent);
        out.writeString(tags);
        out.writeString(tastingNote);
        out.writeString(totalPackageUnits);
        out.writeString(volume);
        out.writeString(origin);

    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<LCBOProductInformation> CREATOR = new Parcelable.Creator<LCBOProductInformation>() {
        public LCBOProductInformation createFromParcel(Parcel in) {
            return new LCBOProductInformation(in);
        }

        public LCBOProductInformation[] newArray(int size) {
            return new LCBOProductInformation[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    private LCBOProductInformation(Parcel in) {
        id = in.readString();
        name =in.readString();
        producerName  =in.readString();
        alcoholContent =in.readString();
        description =in.readString();
        isOnSale =in.readString();
        onPromotion =in.readString();
        saleEndsOn =in.readString();
        price =in.readString();
        primaryCategory =in.readString();
        secondaryCategory =in.readString();
        tertiaryCategory =in.readString();
        style =in.readString();
        imageUrl =in.readString();
        imageThumbUrl =in.readString();
        sugarContent =in.readString();
        tags =in.readString();
        tastingNote =in.readString();
        totalPackageUnits =in.readString();
        volume =in.readString();
        origin =in.readString();
    }
}
