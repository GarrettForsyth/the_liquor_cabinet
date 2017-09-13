package com.games.garrett.theliquorcabinet;

/**
 * A global class to give access to strings across the project.
 * This avoids having to pass context around (as would be done if
 * the strings were kept in the strings.xml resource file) and
 * keeps the information all in one place.
 *
 * However, when paused and in low memory situations, this class might
 * be killed leading to a null pointer exception (!?!?!).
 */

public class GlobalStrings {

    private static final String REQUEST_CODE = "REQUEST_CODE";
    private static final String MESSENGER_KEY = "MESSENGER_KEY";
    private static final String SERVICE_URL = "SERVICE_URL";
    private static final String PARAMETERS = "PARAMETERS";
    private static final String ITEM_ARRAY_KEY = "ITEM_ARRAY_KEY";

    private static final String USER_RATING_ENTRY = "USER_RATING_ENTRY";
    private static final String RECOMMENDATION_DB_ADD_USER_RATING_URL = "https://immense-dawn-14307.herokuapp.com/create_user_rating.php";

    private static final String RECOMMENDATION_DB_UPDATE_USER_RATING_URL = "https://immense-dawn-14307.herokuapp.com/update_user_rating.php";
    private static final String RECOMMENDATION_DB_DELETE_USER_RATING_URL = "https://immense-dawn-14307.herokuapp.com/delete_user_product_ratings_entry.php";
    private static final String RECOMMENDATION_DB_GET_RECOMMENDATIONS_URL = "https:/immense-dawn-14307.herokuapp.com/get_user_recommendations.php";
    private static final String RECOMMENDATION_DB_DELETE_ALL_FOR_USER_URL = "https://immense-dawn-14307.herokuapp.com/delete_all_rating_entries_for_user.php";


    private static final String LCBO_API_PRODUCTS_URL = "https://lcboapi.com/products/";
    private static final String LCBO_API_STORES_URL = "https://lcboapi.com/stores/";

    private static final String USER_ID    = "userID";
    private static final String PRODUCT_ID = "productID";
    private static final String RATING     = "rating";

    @SuppressWarnings("SpellCheckingInspection")
    //private static final String LCBO_DEV_KEY = "OMITTED";
    private static final String LCBO_DEV_KEY = "OMITTED";


    public static String getLcboApiStoresUrl() {
        return LCBO_API_STORES_URL;
    }

    public static String getLcboApiProductsUrl() {
        return LCBO_API_PRODUCTS_URL;
    }

    public static String getRecommendationDbDeleteAllForUserUrl() {
        return RECOMMENDATION_DB_DELETE_ALL_FOR_USER_URL;
    }

    public static String getPARAMETERS() {
        return PARAMETERS;
    }

    public static String getUserId() {
        return USER_ID;
    }

    public static String getProductId() {
        return PRODUCT_ID;
    }

    public static String getRATING() {
        return RATING;
    }

    public static String getRecommendationDbGetRecommendationsUrl() {
        return RECOMMENDATION_DB_GET_RECOMMENDATIONS_URL;
    }

    public static String getRecommendationDbAddUserRatingUrl() {
        return RECOMMENDATION_DB_ADD_USER_RATING_URL;
    }

    @SuppressWarnings("unused")
    public static String getRecommendationDbUpdateUserRatingUrl() {
        return RECOMMENDATION_DB_UPDATE_USER_RATING_URL;
    }

    public static String getRecommendationDbDeleteUserRatingUrl() {
        return RECOMMENDATION_DB_DELETE_USER_RATING_URL;
    }

    public static String getUserRatingEntry() {return USER_RATING_ENTRY;}

    public static String getRequestCode(){
        return REQUEST_CODE;
    }

    public static String getMessengerKey(){
        return MESSENGER_KEY;
    }

    public static String getServiceUrl(){
        return SERVICE_URL;
    }

    public static String getItemArrayKey(){
        return ITEM_ARRAY_KEY;
    }

    public static String getLcboDevKey(){
        return LCBO_DEV_KEY;
    }
}
