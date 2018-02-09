package ca.ualberta.cs.lonelytwitter;

import android.os.AsyncTask;
import android.util.Log;

import com.searchly.jestdroid.DroidClientConfig;
import com.searchly.jestdroid.JestClientFactory;
import com.searchly.jestdroid.JestDroidClient;

import java.util.ArrayList;
import java.util.List;

import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * Created by romansky on 10/20/16.
 */
public class ElasticsearchTweetController {
    private static JestDroidClient client;

    // TODO we need a function which adds tweets to elastic search
    public static class AddTweetsTask extends AsyncTask<NormalTweet, Void, Void> {

        @Override
        protected Void doInBackground(NormalTweet... tweets) {
            verifySettings();

            for (NormalTweet tweet : tweets) {
                Index index = new Index.Builder(tweet).index("testing").type("tweet").build(); //CMPUT301W18T26

                try {
                    // where is the client?
                    DocumentResult result = client.execute(index);
                    if (result.isSucceeded()) {
                        tweet.setId(result.getId());
                    } else {
                        Log.i("Error","Some error");
                    }
                }
                catch (Exception e) {
                    Log.i("Error", "The application failed to build and send the tweets");
                }

            }
            return null;
        }
    }

    // TODO we need a function which gets tweets from elastic search
    public static class GetTweetsTask extends AsyncTask<String, Void, ArrayList<NormalTweet>> {

        @Override
        protected ArrayList<NormalTweet> doInBackground(String... search_parameters) {
            verifySettings();

            ArrayList<NormalTweet> tweets = new ArrayList<NormalTweet>();

                // TODO Build the query
            Search search  = new Search.Builder(search_parameters[0]).addIndex("testing").addType("tweet").build();
            try {
               // TODO get the results of the query
                SearchResult result = client.execute(search);
                if (result.isSucceeded()) {
                    List<NormalTweet> foundTweets = result.getSourceAsObjectList(NormalTweet.class);
                    tweets.addAll(foundTweets);
                    Log.i("Success", "Successfully found queries");
                    Log.d("Parameter",search_parameters[0]);

                } else {
                    Log.i("Error", "Search query failed to find any thing");

                }
            }
            catch (Exception e) {
                Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server!");
            }

            return tweets;
        }
    }

    /**
     * Searches the server for any strings that contain a query, and returns a list of tweets
     * that contain the query
     *
     * @param searchparam
     * @return ArrayList<NormalTweet></NormalTweet>
     */
    public static ArrayList<NormalTweet> searchForString(String searchparam){
        verifySettings();
        ArrayList<NormalTweet> tweets = new ArrayList<NormalTweet>();
        Search search = new Search.Builder(searchparam).addIndex("testing").addType("tweet").build();
        Log.d("Parameter",searchparam);

        try {
            // TODO get the results of the query
            SearchResult result = client.execute(search);
            if (result.isSucceeded()) {
                List<NormalTweet> foundTweets = result.getSourceAsObjectList(NormalTweet.class);
                tweets.addAll(foundTweets);
                Log.i("Success", "Successfully found queries");

            } else {
                Log.i("Error", "Search query failed to find any thing");

            }
        }
        catch (Exception e) {
            Log.i("Error", "Something went wrong when we tried to communicate with the elasticsearch server");
        }
        return tweets;

    }


    public static void verifySettings() {
        if (client == null) {
            DroidClientConfig.Builder builder = new DroidClientConfig.Builder("http://cmput301.softwareprocess.es:8080");
            DroidClientConfig config = builder.build();

            JestClientFactory factory = new JestClientFactory();
            factory.setDroidClientConfig(config);
            client = (JestDroidClient) factory.getObject();
        }
    }
}