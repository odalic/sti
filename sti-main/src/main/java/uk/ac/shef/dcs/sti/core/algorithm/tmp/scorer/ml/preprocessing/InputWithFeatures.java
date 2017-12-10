package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing;

import java.util.*;

public class InputWithFeatures {

    private String inputValue;

    // LinkedHashMap keeps the keys ordered by their insertion order
    private Map<String, Integer> intFeatures = new LinkedHashMap<>();
    private Map<String, Boolean> boolFeatures = new LinkedHashMap<>();
    private String clazz;

    public InputWithFeatures() {
        super();
    }

    public void addIntFeature(String key, int value) {
        intFeatures.put(key, value);
    }

    public void addBoolFeature(String key, boolean value) {
        boolFeatures.put(key, value);
    }

    public int getIntFeature(String key) throws NoSuchElementException {
        Integer value = intFeatures.get(key);
        if (value != null) {
            return value;
        } else {
            throw new NoSuchElementException("Integral Feature '" + key + "' not found!");
        }
    }

    public boolean getBoolFeature(String key) throws NoSuchElementException {
        Boolean value = boolFeatures.get(key);
        if (value != null) {
            return value;
        } else {
            throw new NoSuchElementException("Boolean Feature '" + key + "' not found!");
        }
    }

    public String getInputValue() {
        return inputValue;
    }

    public void setInputValue(String inputValue) {
        this.inputValue = inputValue;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public List<String> getIntFeaturesKeys() {
        return getMapKeys(intFeatures);
    }

    public List<String> getBoolFeaturesKeys() {
        return getMapKeys(boolFeatures);
    }

    private <T> List<String> getMapKeys(Map<String, T> map) {
        return new ArrayList<>(map.keySet());
    }
}