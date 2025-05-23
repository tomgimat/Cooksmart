package fr.tomgimat.cooksmart.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GeminiUtils {
    private static final String GEMINI_API_KEY = "AIzaSyA5wNox1AL78DwLYs0Pfsa7bzOtxu46h8k";
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + GEMINI_API_KEY;

    /**
     * Appelle Gemini pour estimer la durée totale (en minutes) à partir d'instructions textuelles.
     */
    public static void extractDurationFromInstructions(String instructions, Callback callback) throws JSONException {
        OkHttpClient client = new OkHttpClient();

        String prompt = "Voici les instructions d'une recette :\n" + instructions +
                "\nAdditionne toutes les durées mentionnées (en minutes et en heures) dans chaque étape. Réponds uniquement avec un entier représentant la durée totale estimée en minutes (ex : 34). Je ne veux que la durée, si dans les étapes aucune durée explicite n'est mentionnée, réponds 0";

        JSONObject json = new JSONObject();
        JSONArray contents = new JSONArray();
        JSONObject part = new JSONObject().put("text", prompt);
        JSONObject content = new JSONObject().put("parts", new JSONArray().put(part));
        contents.put(content);
        json.put("contents", contents);

        RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(GEMINI_API_URL)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}

