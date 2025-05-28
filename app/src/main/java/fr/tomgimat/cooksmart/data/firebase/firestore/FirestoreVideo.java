package fr.tomgimat.cooksmart.data.firebase.firestore;

import com.google.firebase.firestore.DocumentSnapshot;

public class FirestoreVideo {
    public String id;
    public String title;
    public String videoLink;
    public String thumbnailUrl;
    public String description;
    public String category;

    public FirestoreVideo() {
    }

    public FirestoreVideo(String id, String title, String videoLink, String thumbnailUrl, String description, String category) {
        this.id = id;
        this.title = title;
        this.videoLink = videoLink;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
        this.category = category;
    }

    public static FirestoreVideo fromFirestoreDoc(DocumentSnapshot doc) {
        FirestoreVideo video = new FirestoreVideo();
        video.id = doc.getId();
        video.title = doc.getString("title");
        video.videoLink = doc.getString("video_link");
        video.thumbnailUrl = doc.getString("thumbnail_url");
        video.description = doc.getString("description");
        video.category = doc.getString("category");
        return video;
    }
} 