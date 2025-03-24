package org.astral.findmaimaiultra.utill;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import org.astral.findmaimaiultra.been.Place;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Map<String, String>> sharedMap = new MutableLiveData<>();
    private final MutableLiveData<ArrayList<Place>> placelist = new MutableLiveData<>();


    public SharedViewModel() {
        placelist.setValue(null);
        sharedMap.setValue(new HashMap<>());
    }
    public MutableLiveData<ArrayList<Place>> getPlacelist() {
        return placelist;
    }
    public MutableLiveData<Map<String, String>> getSharedMap() {
        return sharedMap;
    }

    public void addToMap(String key, String value) {
        Map<String, String> currentMap = sharedMap.getValue();
        if (currentMap != null) {
            currentMap.put(key, value);
            sharedMap.setValue(currentMap);
        }
    }

    public void removeFromMap(String key) {
        Map<String, String> currentMap = sharedMap.getValue();
        if (currentMap != null) {
            currentMap.remove(key);
            sharedMap.setValue(currentMap);
        }
    }
    public void setPlacelist(ArrayList<Place> placelist) {
        this.placelist.setValue(placelist);
    }
    public void clearMap() {
        sharedMap.setValue(new HashMap<>());
    }
    public void clearPlacelist() {
        placelist.setValue(null);
    }
}
