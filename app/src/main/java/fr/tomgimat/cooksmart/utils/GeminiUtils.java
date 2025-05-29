package fr.tomgimat.cooksmart.utils;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class GeminiUtils {
    private static final String TAG = "GeminiUtils";
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

    /**
     * Envoie une image à Gemini pour la reconnaissance d'ingrédient
     * @param bitmap L'image à analyser
     * @param ingredientsList La liste des ingrédients disponibles dans l'application
     * @param callback Le callback pour gérer la réponse
     */
    public static void recognizeIngredient(Bitmap bitmap, List<String> ingredientsList, Callback callback) {
        try {
            OkHttpClient client = new OkHttpClient();

            // Convertir le bitmap en base64
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            String base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);

            // Construire le prompt pour Gemini
            StringBuilder prompt = new StringBuilder();
            prompt.append("Voici une photo d'un ingrédient alimentaire. Identifie l'ingrédient et réponds uniquement avec son nom en français, sans article, sans ponctuation et en minuscules. ");
            prompt.append("Voici la liste des ingrédients valides dans l'application : ");
            prompt.append(String.join(", ", ingredientsList));
            prompt.append(". Si l'ingrédient n'est pas dans cette liste, réponds 'non_trouve'.");

            // Construire le JSON pour l'API
            JSONObject json = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            
            JSONArray parts = new JSONArray();
            
            // Ajouter le texte
            JSONObject textPart = new JSONObject();
            textPart.put("text", prompt.toString());
            parts.put(textPart);
            
            // Ajouter l'image
            JSONObject imagePart = new JSONObject();
            JSONObject inlineData = new JSONObject();
            inlineData.put("mimeType", "image/jpeg");
            inlineData.put("data", base64Image);
            imagePart.put("inlineData", inlineData);
            parts.put(imagePart);
            
            content.put("parts", parts);
            contents.put(content);
            json.put("contents", contents);

            // Envoyer la requête
            RequestBody body = RequestBody.create(json.toString(), MediaType.parse("application/json"));
            Request request = new Request.Builder()
                    .url(GEMINI_API_URL)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(callback);

        } catch (JSONException e) {
            Log.e(TAG, "Erreur lors de la préparation de la requête Gemini", e);
        }
    }
}

