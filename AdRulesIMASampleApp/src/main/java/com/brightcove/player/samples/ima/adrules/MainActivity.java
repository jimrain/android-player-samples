package com.brightcove.player.samples.ima.adrules;

import android.os.Bundle;
import android.util.Log;
import com.brightcove.ima.GoogleIMAComponent;
import com.brightcove.ima.GoogleIMAEventType;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.event.EventType;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcovePlayer;
import com.brightcove.player.view.BrightcoveVideoView;
import com.google.ads.interactivemedia.v3.api.AdDisplayContainer;
import com.google.ads.interactivemedia.v3.api.AdsRequest;
import com.google.ads.interactivemedia.v3.api.ImaSdkFactory;
import java.util.ArrayList;

/**
 * This app illustrates how to use "Ad Rules" the Google IMA plugin
 * and the Brightcove Player for Android.  Note: cue points are not
 * used with Ad Rules.
 *
 * @author Paul Matthew Reilly (original code)
 * @author Paul Michael Reilly (added explanatory comments)
 */
public class MainActivity extends BrightcovePlayer {

    private final String TAG = this.getClass().getSimpleName();

    private EventEmitter eventEmitter;
    private GoogleIMAComponent googleIMAComponent;
    private String adRulesURL = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=%2F15018773%2Feverything2&ciu_szs=300x250%2C468x60%2C728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=dummy&correlator=[timestamp]&cmsid=133&vid=10XWSh7W4so&ad_rule=1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // When extending the BrightcovePlayer, we must assign the BrightcoveVideoView before
        // entering the superclass. This allows for some stock video player lifecycle
        // management.
        setContentView(R.layout.ima_activity_main);
        brightcoveVideoView = (BrightcoveVideoView) findViewById(R.id.brightcove_video_view);
        super.onCreate(savedInstanceState);
        eventEmitter = brightcoveVideoView.getEventEmitter();

        // Use a procedural abstraction to setup the Google IMA SDK via the plugin.
        setupGoogleIMA();

        // Establish the video object.
        brightcoveVideoView.add(Video.createVideo("http://rmcdn.2mdn.net/MotifFiles/html/1248596/android_1330378998288.mp4"));

        // Log whether or not instance state in non-null.
        if (savedInstanceState != null) {
            Log.v(TAG, "Restoring saved position");
        } else {
            Log.v(TAG, "No saved state");
        }
    }

    /**
     * Setup the Brightcove IMA Plugin.
     */
    private void setupGoogleIMA() {
        // After the video has been prepared, setup Ad Rules.
        eventEmitter.on(EventType.DID_SET_VIDEO, new EventListener() {
            @Override
            public void processEvent(Event event) {
                googleIMAComponent.initializeAdsRequests();
            }
        });

        // Establish the Google IMA SDK factory instance.
        final ImaSdkFactory sdkFactory = ImaSdkFactory.getInstance();

        // Enable logging up ad start.
        eventEmitter.on(GoogleIMAEventType.DID_START_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Enable logging any failed attempts to play an ad.
        eventEmitter.on(GoogleIMAEventType.DID_FAIL_TO_PLAY_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Enable Logging upon ad completion.
        eventEmitter.on(GoogleIMAEventType.DID_COMPLETE_AD, new EventListener() {
            @Override
            public void processEvent(Event event) {
                Log.v(TAG, event.getType());
            }
        });

        // Set up a listener for initializing AdsRequests. The Google
        // IMA plugin emits an ad request event as a result of
        // initializeAdsRequests() being called.
        eventEmitter.on(GoogleIMAEventType.ADS_REQUEST_FOR_VIDEO, new EventListener() {
            @Override
            public void processEvent(Event event) {
                // Create a container object for the ads to be presented.
                AdDisplayContainer container = sdkFactory.createAdDisplayContainer();
                container.setPlayer(googleIMAComponent.getVideoAdPlayer());
                container.setAdContainer(brightcoveVideoView);

                // Build an ads request object and point it to the ad
                // display container created above.
                AdsRequest adsRequest = sdkFactory.createAdsRequest();
                adsRequest.setAdTagUrl(adRulesURL);
                adsRequest.setAdDisplayContainer(container);

                ArrayList<AdsRequest> adsRequests = new ArrayList<AdsRequest>(1);
                adsRequests.add(adsRequest);

                // Respond to the event with the new ad requests.
                event.properties.put(GoogleIMAComponent.ADS_REQUESTS, adsRequests);
                eventEmitter.respond(event);
            }
        });

        // Create the Brightcove IMA Plugin and pass in the event
        // emitter so that the plugin can integrate with the SDK.
        googleIMAComponent = new GoogleIMAComponent(brightcoveVideoView, eventEmitter, true);
    }
}