package com.games.garrett.theliquorcabinet.services.utils;

import android.util.Log;
import android.util.Pair;

import com.games.garrett.theliquorcabinet.GlobalStrings;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the request sent to the RecommendationDBService
 * Created by Garrett on 8/15/2017.
 */

public class HandleRecommendationDBRequest {

    /* Tag used for logging */
    private static final String TAG =  HandleRecommendationDBRequest.class.getCanonicalName();

    /* The different request codes that be be requested of this service */
    private static final int REQUEST_ADD_RATING_RECORD    = 100;
    private static final int REQUEST_DELETE_RATING_RECORD = 102;
    private static final int REQUEST_RECOMMENDATIONS      = 103;
    private static final int REQUEST_DELETE_ALL_RATING_ENTRIES_FOR_USER = 104;

    // JSON Node names
    private static final String TAG_SUCCESS = "success";

    private static final String DOWNLOAD_FAILURE = "DOWNLOAD_FAILURE";

    /**
     * RecommendationDBService calls this to handle user's requests
     * @param userRatingEntry   identifies the record to be operated on (contains: userID and productID)
     * @return     Formatted data from the LCBO API. Either store,
     *             inventory or product information.
     */
    public static String handleRequest(ArrayList<String> userRatingEntry, int requestCode){
        Log.d(TAG, "Processing request in  HandleRecommendationDBRequest.class ");
        String response = null;

        switch(requestCode){
            case REQUEST_ADD_RATING_RECORD:
                response = addRatingRecord(userRatingEntry);
                break;
            case REQUEST_DELETE_RATING_RECORD:
                response = deleteRatingRecord(userRatingEntry);
                break;
            case REQUEST_RECOMMENDATIONS:
                response = getUserDrinkRecommendations(userRatingEntry);
                break;
            case REQUEST_DELETE_ALL_RATING_ENTRIES_FOR_USER:
                response = deleteAllUsersRatings(userRatingEntry);
                break;
            default:
                break;
        }
        return response;
    }

    /**
     * Sends request to add a rating record to the recommendation database hosted on a
     * webservice
     * @param ratingEntry  the rating entry to add
     * @return             the response format he webservice
     */
    private static String addRatingRecord(ArrayList<String> ratingEntry){
        // make sure all three required attributes are present
        if(ratingEntry.size() != 3){
            Log.d(TAG, "RatingEntry should have 3 elements.");
            return null;
        }

        Log.d(TAG, "Adding rating with parameters : userID = "
                + ratingEntry.get(0) + " productID = "
                + ratingEntry.get(1) + " rating = "
                + ratingEntry.get(2));

        JSONParser jsonParser = new JSONParser();
        List<Pair<String,String>> params = createParamList(ratingEntry);
        JSONObject json = jsonParser.makeHttpsRequest(GlobalStrings.getRecommendationDbAddUserRatingUrl(), "POST", params);
        if (json == null) return DOWNLOAD_FAILURE;

        // log response to logcat
        String response = json.toString();
        Log.d(TAG, "ADD RATING SERVER RESPONSE: " + response);

        //check for success
        try{
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1){
                // successfully created rating record
                Log.d(TAG, "Successfully created rating record");
            }else{
                Log.d(TAG, "Failed to create rating record");
            }
        }catch (JSONException e){
            Log.e(TAG, "Failed to read json during success check.");
        }

        return response;
    }

    /**
     * Deletes a user's rating record in the recommendation datatbase.
     * @param ratingEntry list with user's id and product id
     * @return            the server's response to the deletion request
     */
    private static String deleteRatingRecord(ArrayList<String> ratingEntry){

        if(ratingEntry.size() != 2){
            Log.d(TAG, "RatingEntry should have 2 elements for deletion.");
            return null;
        }


        Log.d(TAG, "Deleting rating with parameters : userID = "
                + ratingEntry.get(0) + " productID = "
                + ratingEntry.get(1));

        JSONParser jsonParser = new JSONParser();
        List<Pair<String,String>> params = createParamList(ratingEntry);
        JSONObject json = jsonParser.makeHttpsRequest(GlobalStrings.getRecommendationDbDeleteUserRatingUrl(), "POST", params);
        if (json == null) return DOWNLOAD_FAILURE;

        // log response to logcat
        String response = json.toString();
        Log.d(TAG, "DELETE RATING SERVER RESPONSE: " + response);

        //check for success
        try{
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1){
                // successfully created rating record
                Log.d(TAG, "Successfully deleted rating record");
            }else{
                Log.d(TAG, "Failed to delete rating record");
            }
        }catch (JSONException e){
            Log.e(TAG, "Failed to read json during success check.");
        }

        return response;

    }

    /**
     * Deletes all of a user's rating records in the recommendation datatbase.
     * @param ratingEntry list with user's id and product id
     * @return            the server's response to the deletion request
     */
    private static String deleteAllUsersRatings(ArrayList<String> ratingEntry){

        if(ratingEntry.size() != 1){
            Log.d(TAG, "RatingEntry should have 1 elements for deleting all of user's records.");
            return null;
        }

        Log.d(TAG, "Deleting all user's rating with parameters : userID = "
                + ratingEntry.get(0));

        JSONParser jsonParser = new JSONParser();
        List<Pair<String,String>> params = createParamList(ratingEntry);
        JSONObject json = jsonParser.makeHttpsRequest(GlobalStrings.getRecommendationDbDeleteAllForUserUrl(), "POST", params);
        if (json == null) return DOWNLOAD_FAILURE;

        // log response to logcat
        String response = json.toString();
        Log.d(TAG, "DELETE ALL RATING SERVER RESPONSE: " + response);

        //check for success
        try{
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1){
                // successfully created rating record
               // Log.d(TAG, "Successfully deleted rating record");
            }else{
              //  Log.d(TAG, "Failed to delete rating record");
            }
        }catch (JSONException e){
          //  Log.e(TAG, "Failed to read json during success check.");
        }

        return response;

    }

    /**
     * Helper function that translates the list of passed attributes that defines a user's
     * rating record into a list of Pair objects that the JSONParser understands.
     * @param ratingEntry  list of attributes defining a user's rating entry
     * @return             list of Pair objects that JSONParser can read
     */
    private static List<Pair<String,String>> createParamList(ArrayList<String> ratingEntry){
        String newUserID    = ratingEntry.get(0);
        String newProductID = "";
        if(ratingEntry.size() > 1) newProductID = ratingEntry.get(1);
        String newRating = "";
        if(ratingEntry.size() == 3)  newRating = ratingEntry.get(2);

        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>(GlobalStrings.getUserId(), newUserID));
        if(ratingEntry.size() > 1) params.add(new Pair<>(GlobalStrings.getProductId(), newProductID));
        if(ratingEntry.size() == 3) params.add(new Pair<>(GlobalStrings.getRATING(), newRating));

        return params;
    }

    /**
     * Sends rating user entry's to recommendation data base and returns the servers
     * response.
     * @param ratingEntry  list containing user's rating record attributes.
     * @return             a json string response from the server.
     */
    private static String getUserDrinkRecommendations(ArrayList<String> ratingEntry){
        JSONParser jsonParser = new JSONParser();
        List<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("userID", ratingEntry.get(0)));

       // Log.d(TAG, "Trying to access : " + GlobalStrings.getRecommendationDbGetRecommendationsUrl());

        JSONObject json = jsonParser.makeHttpsRequest(
                GlobalStrings.getRecommendationDbGetRecommendationsUrl(),
                "POST", params);

        // todo jsonparser returns null if no rec OR if it can't establish a connection
        // need to differentiate these.
        if ( json == null ) return "NO_RECOMMENDATIONS";

        String response = json.toString();
       // Log.d(TAG, "ADD RATING SERVER RESPONSE: " + response);

        //check for success
        try{
            int success = json.getInt(TAG_SUCCESS);

            if (success == 1){
                // successfully created rating record
               // Log.d(TAG, "Successfully created rating record");
            }else{
               // Log.d(TAG, "Failed to create rating record");
            }
        }catch (JSONException e){
           // Log.e(TAG, "Failed to read json during success check.");
        }

        return response;

    }
}
