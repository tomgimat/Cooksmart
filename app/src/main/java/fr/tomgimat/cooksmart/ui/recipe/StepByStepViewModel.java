package fr.tomgimat.cooksmart.ui.recipe;

import android.app.Application;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import fr.tomgimat.cooksmart.data.model.RecipeStep;
import fr.tomgimat.cooksmart.utils.GeminiUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * ViewModel pour la page des étapes de la recette
 */
public class StepByStepViewModel extends AndroidViewModel implements TextToSpeech.OnInitListener {
    private static final String TAG = "StepByStepViewModel";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final MutableLiveData<List<RecipeStep>> steps = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> currentStepIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isTimerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<Long> remainingTime = new MutableLiveData<>(0L);
    private final MutableLiveData<String> ttsStatus = new MutableLiveData<>();
    
    private TextToSpeech textToSpeech;
    private String recipeId;
    private CountDownTimer timer;

    public StepByStepViewModel(@NonNull Application application) {
        super(application);
        textToSpeech = new TextToSpeech(application, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.FRANCE);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Langue non supportée");
            }
        } else {
            Log.e(TAG, "Initialisation TTS échouée");
        }
    }

    /**
     * Charge les étapes de la recette depuis Firestore ou Gemini si elles n'existent pas
     * @param recipeId
     */
    public void loadRecipeSteps(String recipeId) {
        this.recipeId = recipeId;
        
        // Vérifier si les étapes existent déjà
        db.collection("recipes").document(recipeId)
            .get()
            .addOnSuccessListener(document -> {
                List<String> existingSteps = (List<String>) document.get("steps");
                if (existingSteps != null && !existingSteps.isEmpty()) {
                    // Les étapes existent déjà, on les charge
                    List<RecipeStep> recipeSteps = new ArrayList<>();
                    for (String step : existingSteps) {
                        recipeSteps.add(new RecipeStep(step));
                    }
                    steps.setValue(recipeSteps);
                } else {
                    // Les étapes n'existent pas, on les génère avec Gemini
                    String instructions = document.getString("instructions");
                    if (instructions != null) {
                        generateStepsWithGemini(instructions);
                    }
                }
            });
    }

    /**
     * Appelle Gemini pour générer les étapes de la recette correctement
     * Avec traduction
     * @param instructions
     */
    private void generateStepsWithGemini(String instructions) {
        try {
            GeminiUtils.extractStepsFromInstructions(instructions, new Callback() {
                @Override
                public void onFailure(Call call, java.io.IOException e) {
                    Log.e(TAG, "Erreur lors de la génération des étapes", e);
                }

                @Override
                public void onResponse(Call call, Response response) throws java.io.IOException {
                    try {
                        String responseBody = response.body().string();
                        JSONArray stepsArray = new JSONArray(responseBody);
                        List<String> stepsList = new ArrayList<>();
                        List<RecipeStep> recipeSteps = new ArrayList<>();

                        for (int i = 0; i < stepsArray.length(); i++) {
                            String step = stepsArray.getString(i);
                            stepsList.add(step);
                            recipeSteps.add(new RecipeStep(step));
                        }

                        //SAuvegarde les étapes dans Firestore
                        db.collection("recipes").document(recipeId)
                            .update("steps", stepsList)
                            .addOnSuccessListener(aVoid -> {
                                steps.postValue(recipeSteps);
                            });

                    } catch (JSONException e) {
                        Log.e(TAG, "Erreur lors du parsing des étapes", e);
                    }
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la préparation de la requête Gemini", e);
        }
    }

    /**
     * Méthode pour passer à l'étape suivante
     */
    public void nextStep() {
        List<RecipeStep> currentSteps = steps.getValue();
        if (currentSteps != null && currentStepIndex.getValue() < currentSteps.size() - 1) {
            currentStepIndex.setValue(currentStepIndex.getValue() + 1);
            speakCurrentStep();
        }
    }

    /**
     * Méthode pour passer à l'étape précédente
     */
    public void previousStep() {
        if (currentStepIndex.getValue() > 0) {
            currentStepIndex.setValue(currentStepIndex.getValue() - 1);
            speakCurrentStep();
        }
    }

    /**
     * Méthode pour dire à voix haute les instructions de l'étape courante
     */
    public void speakCurrentStep() {
        List<RecipeStep> currentSteps = steps.getValue();
        if (currentSteps != null && currentStepIndex.getValue() < currentSteps.size()) {
            String instruction = currentSteps.get(currentStepIndex.getValue()).getInstruction();
            textToSpeech.speak(instruction, TextToSpeech.QUEUE_FLUSH, null, "step_utterance");
        }
    }

    /**
     * Méthode pour démarrer le minuteur
     */
    public void startTimer() {
        List<RecipeStep> currentSteps = steps.getValue();
        if (currentSteps != null && currentStepIndex.getValue() < currentSteps.size()) {
            RecipeStep currentStep = currentSteps.get(currentStepIndex.getValue());
            if (currentStep.isTimerStep()) {
                if (timer != null) {
                    timer.cancel();
                }
                
                timer = new CountDownTimer(currentStep.getDuration() * 1000L, 1000) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        remainingTime.setValue(millisUntilFinished / 1000);
                    }

                    @Override
                    public void onFinish() {
                        isTimerRunning.setValue(false);
                        remainingTime.setValue(0L);
                        // TODO: Envoyer une notification
                    }
                };
                
                isTimerRunning.setValue(true);
                timer.start();
            }
        }
    }

    /**
     * Méthode pour arrêter le minuteur
     */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            isTimerRunning.setValue(false);
            remainingTime.setValue(0L);
        }
    }

    public LiveData<List<RecipeStep>> getSteps() {
        return steps;
    }

    public LiveData<Integer> getCurrentStepIndex() {
        return currentStepIndex;
    }

    public LiveData<Boolean> getIsTimerRunning() {
        return isTimerRunning;
    }

    public LiveData<Long> getRemainingTime() {
        return remainingTime;
    }

    public LiveData<String> getTtsStatus() {
        return ttsStatus;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (timer != null) {
            timer.cancel();
        }
    }
} 