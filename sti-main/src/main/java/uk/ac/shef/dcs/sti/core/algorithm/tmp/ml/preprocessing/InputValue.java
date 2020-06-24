package uk.ac.shef.dcs.sti.core.algorithm.tmp.ml.preprocessing;

public class InputValue {

    private String inputValue;
    private String clazz;

    public InputValue(String inputValue, String clazz) {
        this.inputValue = inputValue;
        this.clazz = clazz;
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

}
