package com.example.dogelauncher.viewModel;

import android.app.Application;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

public class ViewModeViewModel extends AndroidViewModel {

    public static final int VISIBLE = View.VISIBLE;
    public static final int GONE = View.GONE;
//    public static final int INVISIBLE = View.INVISIBLE;

    public static final int MODE_SURROUNDING = 0;
    public static final int MODE_LISTING = 1;
    public static final int MODE_EDIT = 2;

    private MutableLiveData<Integer> mode = new MutableLiveData<>(MODE_LISTING);

    public ViewModeViewModel(@NonNull Application application) {
        super(application);
    }

    public void setMode(int mode) {
        this.mode.postValue(mode);
    }

    public int getMode () {
        return mode.getValue();
    }

    public boolean isSurroundingMode (){
        return mode.getValue() == MODE_SURROUNDING;
    }

    public boolean isListingMode (){
        return mode.getValue() == MODE_LISTING;
    }


}
