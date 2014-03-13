package com.example.microphoneaudiodispatcherexample;

import be.hogent.tarsos.dsp.AudioEvent;
import be.hogent.tarsos.dsp.MicrophoneAudioDispatcher;
import be.hogent.tarsos.dsp.onsets.OnsetHandler;
import be.hogent.tarsos.dsp.onsets.PercussionOnsetDetector;
import be.hogent.tarsos.dsp.pitch.PitchDetectionHandler;
import be.hogent.tarsos.dsp.pitch.PitchDetectionResult;
import be.hogent.tarsos.dsp.pitch.PitchProcessor;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;

public class MainActivity extends Activity {
	private TextView pitchTextView;
	private TextView clapTextView;
	private MicrophoneAudioDispatcher audioDispatcher;
	private PitchDetectionHandler pitchHandler;
	private OnsetHandler clapHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the TextViews.
        pitchTextView = (TextView)findViewById(R.id.pitch_text);
        clapTextView = (TextView)findViewById(R.id.clap_text);
        
        // Create audio handlers.
    	pitchHandler = new PitchDetectionHandler() {
    		@Override
    		public void handlePitch(PitchDetectionResult result, AudioEvent event) {
    			// Make the pitch TextView display the pitch, if any.
    			final String pitchText = result.isPitched() ?
    					result.getPitch() + " Hz" : "No pitch detected";
    			runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    					pitchTextView.setText(pitchText);
    				}
    			});
    		}
    	};
    	clapHandler = new OnsetHandler() {
    		@Override
    		public void handleOnset(double time, double salience) {
    			// Append the clap TextView with a clap message.
    			final String clapText = clapTextView.getText() +
    					"Clap heard at " + time + " seconds\n";
    			runOnUiThread(new Runnable() {
    				@Override
    				public void run() {
    					clapTextView.setText(clapText);
    				}
    			});
    		}
    	};
    }
    
    @Override
    public void onPause() {
    	// Stop the audio dispatcher so that the AudioRecord object is freed.
    	audioDispatcher.stop();
    	
    	super.onPause();
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	
    	// Create a new audio dispatcher and adding two AudioProcessors to it.
    	audioDispatcher = new MicrophoneAudioDispatcher(44100, 2048, 1024);
    	audioDispatcher.addAudioProcessor(new PitchProcessor(
    			PitchProcessor.PitchEstimationAlgorithm.YIN, 44100, 2048, pitchHandler));
    	audioDispatcher.addAudioProcessor(new PercussionOnsetDetector(
    			44100, 2048, 1024, clapHandler));
    	
    	// MicrophoneAudioProcessor is a Runnable, so start it in its own Thread.
    	(new Thread(audioDispatcher)).start();
    }
}
