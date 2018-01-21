package me.shatilov.symlab.sarathi.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kirill on 21-Jan-18.
 *
 * Data model description.
 */

public class SettingsModel {
    private List<MiddleBoxModel> middleBoxes;

    public SettingsModel() {
        middleBoxes = new ArrayList<>();
    }

    public SettingsModel(List<MiddleBoxModel> middleBoxes) {
        this.middleBoxes = middleBoxes;
    }

    public List<MiddleBoxModel> getMiddleBoxes() {
        return middleBoxes;
    }

    public void setMiddleBoxes(List<MiddleBoxModel> middleBoxes) {
        this.middleBoxes.clear();
        this.middleBoxes.addAll(middleBoxes);
    }
}
