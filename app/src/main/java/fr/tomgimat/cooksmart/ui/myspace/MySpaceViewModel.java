package fr.tomgimat.cooksmart.ui.myspace;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MySpaceViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MySpaceViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is my space fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}