package pl.gawryszewski.am_projekt;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHandler extends SQLiteOpenHelper {
    public static final String POST_TABLE = "POST_TABLE";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_URL = "URL";
    public static final String COLUMN_ADDRESS = "ADDRESS";
    public static final String COLUMN_LATITUDE = "LATITUDE";
    public static final String COLUMN_LONGITUDE = "LONGITUDE";

    public DataBaseHandler(@Nullable Context context) {
        super(context, "post.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE " + POST_TABLE + " (" +
                COLUMN_ID + " TEXT PRIMARY KEY, " +
                COLUMN_URL + " TEXT," +
                COLUMN_ADDRESS + " TEXT, " +
                COLUMN_LATITUDE + " REAL, " +
                COLUMN_LONGITUDE + " REAL)";
        sqLiteDatabase.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private interface PostIdCallback{
        void onPostIdReceived(String postId);
    }

    private void fetchLatestPostId(PostIdCallback callback)
    {
        GraphRequest graphRequest = GraphRequest.newGraphPathRequest(AccessToken.getCurrentAccessToken(),
                "/me/feed",
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(@NonNull GraphResponse graphResponse) {
                        if(graphResponse.getError()==null)
                        {
                            JSONObject jsonObject = graphResponse.getJSONObject();
                            try {
                                JSONArray data = jsonObject.getJSONArray("data");
                                if(data.length()>0){
                                    JSONObject latestPost = data.getJSONObject(0);
                                    String postId = latestPost.getString("id");
                                    Log.d("postId", postId);
                                    callback.onPostIdReceived(postId);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        else {
                            Log.d("postId", "Error");
                        }
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id");
        parameters.putString("limit", "1");
        graphRequest.setParameters(parameters);
        graphRequest.executeAsync();
    }
    public void addPostToDatabase(Location location, String address)
    {
        fetchLatestPostId(new PostIdCallback() {
            @Override
            public void onPostIdReceived(String postId) {
                GraphRequest request = new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/" + postId,
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(@NonNull GraphResponse response) {
                                if (response.getError() == null) {
                                    JSONObject post = response.getJSONObject();
                                    try {
                                        String postUrl = post.getString("permalink_url");
                                        Log.d("postUrl", postUrl);
                                        PostModel postModel = new PostModel(postId, postUrl,
                                                location.getLatitude(), location.getLongitude(),
                                                address);
                                        SQLiteDatabase db = DataBaseHandler.this.getWritableDatabase();
                                        ContentValues cv = new ContentValues();
                                        cv.put(COLUMN_ID, postModel.getId());
                                        cv.put(COLUMN_URL, postModel.getUrl());
                                        cv.put(COLUMN_LATITUDE, postModel.getLatitude());
                                        cv.put(COLUMN_LONGITUDE, postModel.getLongitude());
                                        cv.put(COLUMN_ADDRESS, postModel.getAddress());
                                        long insert = db.insert(POST_TABLE, null,cv);
                                        if(insert == -1)
                                        {
                                            Log.d("dbcontent", "Insert failed");
                                        }
                                        else
                                        {
                                            Log.d("dbcontent", "Insert " + cv);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.d("postUrl", "Error");
                                }
                            }
                        }
                );
                Bundle parameters = new Bundle();
                parameters.putString("fields", "permalink_url");
                request.setParameters(parameters);
                request.executeAsync();
            }
        });
    }

    public Boolean deletePost(String postId)
    {
        SQLiteDatabase db = DataBaseHandler.this.getWritableDatabase();
        try {
            db.beginTransaction();
            String queryString = "DELETE FROM "+ POST_TABLE+" WHERE "+COLUMN_ID +" = '"+postId+"'";
            db.execSQL(queryString);
            db.setTransactionSuccessful();
            Log.d("dbcontent", "Post "+postId+" deleted");
            return true;
        } catch (Exception e) {
            Log.e("dbcontent", "Error deleting post: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }

    }

    public Boolean updatePost(String postId, Bundle values)
    {
        SQLiteDatabase db = DataBaseHandler.this.getWritableDatabase();
        try {
            db.beginTransaction();
            String queryString = "UPDATE "+ POST_TABLE+
                    " SET "+COLUMN_LATITUDE+" = " + Double.parseDouble(values.getString("latitude")) +
                    ", "+COLUMN_LONGITUDE +" = "+ Double.parseDouble(values.getString("longitude")) +
                    ", "+COLUMN_ADDRESS +" = '" +values.getString("address")+
                    "' WHERE "+COLUMN_ID +" = '"+postId+"'";
            db.execSQL(queryString);
            db.setTransactionSuccessful();
            Log.d("dbcontent", "Post "+postId+" updated");
            return true;
        } catch (Exception e) {
            Log.e("dbcontent", "Error updating post: " + e.getMessage());
            return false;
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public PostModel getOne(String postId)
    {
        String queryString = "SELECT * FROM "+ POST_TABLE+" WHERE "+COLUMN_ID +" = '"+postId+"'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            String Id = cursor.getString(0);
            String postUrl = cursor.getString(1);
            String postAddress = cursor.getString(2);
            double postLat = cursor.getDouble(3);
            double postLon = cursor.getDouble(4);
            PostModel post = new PostModel(Id, postUrl, postLat, postLon, postAddress);
            Log.d("dbcontent", post.toString());
            cursor.close();
            db.close();
            return post;
        }
        else {
            Log.d("dbcontent", "no such value");
            cursor.close();
            db.close();
            return null;
        }
    }
    
    public List<PostModel> getAll()
    {
        List<PostModel> returnList = new ArrayList<>();
        String queryString = "SELECT * FROM "+ POST_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            do{
                String postId = cursor.getString(0);
                String postUrl = cursor.getString(1);
                String postAddress = cursor.getString(2);
                double postLat = cursor.getDouble(3);
                double postLon = cursor.getDouble(4);
                PostModel post = new PostModel(postId, postUrl, postLat, postLon, postAddress);
                Log.d("dbcontent", post.toString());
                returnList.add(post);

            }while(cursor.moveToNext());
        }
        else
        {
            Log.d("dbcontent", "no values");
        }
        cursor.close();
        db.close();
        return returnList;
    }
    public List<PostModel> getFilteredByAddress(String filter)
    {
        List<PostModel> returnList = new ArrayList<>();
        String queryString = "SELECT * FROM "+ POST_TABLE +" WHERE "+ COLUMN_ADDRESS +" LIKE '%" +
                filter+"%'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(queryString, null);
        if(cursor.moveToFirst())
        {
            do{
                String postId = cursor.getString(0);
                String postUrl = cursor.getString(1);
                String postAddress = cursor.getString(2);
                double postLat = cursor.getDouble(3);
                double postLon = cursor.getDouble(4);
                PostModel post = new PostModel(postId, postUrl, postLat, postLon, postAddress);
                Log.d("dbcontent", post.toString());
                returnList.add(post);

            }while(cursor.moveToNext());
        }
        else
        {
            Log.d("dbcontent", "no values");
        }
        cursor.close();
        db.close();
        return returnList;
    }
}
