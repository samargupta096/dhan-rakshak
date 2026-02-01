package com.dhanrakshak.ai.voice;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Voice Command Handler for hands-free finance queries.
 * Uses on-device speech recognition and TTS.
 */
@Singleton
public class VoiceCommandHandler implements RecognitionListener {

    private static final String TAG = "VoiceCommandHandler";

    private final Context context;
    private SpeechRecognizer speechRecognizer;
    private TextToSpeech textToSpeech;
    private VoiceCommandListener listener;
    private boolean isListening = false;

    // Command patterns
    private static final Pattern NET_WORTH_PATTERN = Pattern.compile(
            "(?i)(what('s| is)? my )?net worth");

    private static final Pattern SPENDING_PATTERN = Pattern.compile(
            "(?i)how much (did I|have I) (spend|spent) (on|for) ([\\w\\s]+)");

    private static final Pattern ADD_EXPENSE_PATTERN = Pattern.compile(
            "(?i)add (₹|Rs\\.?|rupees?)?\\s?(\\d+) (expense|spending) (for|on) ([\\w\\s]+)");

    private static final Pattern BALANCE_PATTERN = Pattern.compile(
            "(?i)(what('s| is)? my )?(bank )?balance");

    private static final Pattern PORTFOLIO_PATTERN = Pattern.compile(
            "(?i)(show|what('s| is)? my )?(stock|mutual fund|investment) (portfolio|holdings)");

    private static final Pattern GOAL_PATTERN = Pattern.compile(
            "(?i)(how is|what('s| is) the progress (of|on)) my ([\\w\\s]+) goal");

    @Inject
    public VoiceCommandHandler(Context context) {
        this.context = context.getApplicationContext();
        initializeSpeechRecognizer();
        initializeTextToSpeech();
    }

    private void initializeSpeechRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
            speechRecognizer.setRecognitionListener(this);
        } else {
            Log.w(TAG, "Speech recognition not available on this device");
        }
    }

    private void initializeTextToSpeech() {
        textToSpeech = new TextToSpeech(context, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(new Locale("en", "IN"));
            }
        });
    }

    /**
     * Start listening for voice commands.
     */
    public void startListening(VoiceCommandListener listener) {
        this.listener = listener;

        if (speechRecognizer == null) {
            if (listener != null) {
                listener.onError("Speech recognition not available");
            }
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        isListening = true;
        speechRecognizer.startListening(intent);

        if (listener != null) {
            listener.onListeningStarted();
        }
    }

    /**
     * Stop listening.
     */
    public void stopListening() {
        if (speechRecognizer != null && isListening) {
            speechRecognizer.stopListening();
            isListening = false;
        }
    }

    /**
     * Parse and execute voice command.
     */
    public VoiceCommand parseCommand(String text) {
        text = text.toLowerCase().trim();

        // Net worth query
        if (NET_WORTH_PATTERN.matcher(text).find()) {
            return new VoiceCommand(CommandType.GET_NET_WORTH, null);
        }

        // Spending query
        Matcher spendingMatcher = SPENDING_PATTERN.matcher(text);
        if (spendingMatcher.find()) {
            String category = spendingMatcher.group(4);
            return new VoiceCommand(CommandType.GET_SPENDING, category);
        }

        // Add expense
        Matcher expenseMatcher = ADD_EXPENSE_PATTERN.matcher(text);
        if (expenseMatcher.find()) {
            double amount = Double.parseDouble(expenseMatcher.group(2));
            String category = expenseMatcher.group(5);
            return new VoiceCommand(CommandType.ADD_EXPENSE, amount, category);
        }

        // Balance query
        if (BALANCE_PATTERN.matcher(text).find()) {
            return new VoiceCommand(CommandType.GET_BALANCE, null);
        }

        // Portfolio query
        Matcher portfolioMatcher = PORTFOLIO_PATTERN.matcher(text);
        if (portfolioMatcher.find()) {
            String type = portfolioMatcher.group(3);
            return new VoiceCommand(CommandType.GET_PORTFOLIO, type);
        }

        // Goal progress
        Matcher goalMatcher = GOAL_PATTERN.matcher(text);
        if (goalMatcher.find()) {
            String goalName = goalMatcher.group(4);
            return new VoiceCommand(CommandType.GET_GOAL_PROGRESS, goalName);
        }

        return new VoiceCommand(CommandType.UNKNOWN, text);
    }

    /**
     * Speak response using TTS.
     */
    public void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "response");
        }
    }

    /**
     * Format currency for speech.
     */
    public String formatCurrencyForSpeech(double amount) {
        if (amount >= 10000000) {
            return String.format("%.2f crore rupees", amount / 10000000);
        } else if (amount >= 100000) {
            return String.format("%.2f lakh rupees", amount / 100000);
        } else if (amount >= 1000) {
            return String.format("%.0f thousand rupees", amount / 1000);
        } else {
            return String.format("%.0f rupees", amount);
        }
    }

    // RecognitionListener callbacks
    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty()) {
            String text = matches.get(0);
            VoiceCommand command = parseCommand(text);
            if (listener != null) {
                listener.onCommandRecognized(command, text);
            }
        }
        isListening = false;
    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> matches = partialResults.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
        if (matches != null && !matches.isEmpty() && listener != null) {
            listener.onPartialResult(matches.get(0));
        }
    }

    @Override
    public void onError(int error) {
        isListening = false;
        String errorMessage;
        switch (error) {
            case SpeechRecognizer.ERROR_NO_MATCH:
                errorMessage = "Didn't catch that. Please try again.";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                errorMessage = "Network error. Voice works offline too!";
                break;
            default:
                errorMessage = "Voice recognition error";
        }
        if (listener != null) {
            listener.onError(errorMessage);
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    @Override
    public void onRmsChanged(float rmsdB) {
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
    }

    @Override
    public void onEndOfSpeech() {
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
    }

    public void destroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (textToSpeech != null) {
            textToSpeech.shutdown();
        }
    }

    // Enums and data classes
    public enum CommandType {
        GET_NET_WORTH,
        GET_SPENDING,
        ADD_EXPENSE,
        GET_BALANCE,
        GET_PORTFOLIO,
        GET_GOAL_PROGRESS,
        UNKNOWN
    }

    public static class VoiceCommand {
        public CommandType type;
        public String parameter;
        public double amount;

        public VoiceCommand(CommandType type, String parameter) {
            this.type = type;
            this.parameter = parameter;
        }

        public VoiceCommand(CommandType type, double amount, String parameter) {
            this.type = type;
            this.amount = amount;
            this.parameter = parameter;
        }
    }

    public interface VoiceCommandListener {
        void onListeningStarted();

        void onPartialResult(String partialText);

        void onCommandRecognized(VoiceCommand command, String rawText);

        void onError(String error);
    }
}
