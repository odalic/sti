package uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml;

import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputValue;
import uk.ac.shef.dcs.sti.core.algorithm.tmp.scorer.ml.preprocessing.InputWithFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultMLFeatureDetector implements MLFeatureDetector {

    private String F_INT_NUMBER_OF_WORDS = "number_of_words";
    private String F_INT_NUMBER_OF_TOTAL_CHARACTERS = "number_of_total_chars";
    private String F_INT_NUMBER_OF_ALPHABET_CHARACTERS = "number_of_alphabet_chars";
    private String F_INT_NUMBER_OF_NUMERIC_CHARACTERS = "number_of_numeric_chars";
    private String F_INT_NUMBER_OF_WHITESPACE_CHARACTERS = "number_of_whitespace_chars";
    private String F_INT_NUMBER_OF_SPECIAL_CHARACTERS = "number_of_special_chars";
    private String F_BOOL_IS_NUMERIC_VALUE = "is_numeric_value";
    private String F_BOOL_IS_INTEGRAL_NUMERIC_VALUE = "is_integral_numeric_value";
    private String F_BOOL_IS_DECIMAL_NUMERIC_VALUE = "is_decimal_numeric_value";
    private String F_BOOL_IS_PREFIXED_NUMBER_VALUE = "is_prefixed_number_value";
    private String F_BOOL_IS_POSTFIXED_NUMBER_VALUE = "is_postfixed_number_value";

    @Override
    public InputWithFeatures[] detectFeatures(InputValue[] inputValues) {
        List<InputWithFeatures> inputValuesWithFeatures = new ArrayList<>();
        for (InputValue inputValue: inputValues) {
            InputWithFeatures inputValueWithFeatures = detectFeatures(inputValue);
            inputValuesWithFeatures.add(inputValueWithFeatures);
        }
        return inputValuesWithFeatures.toArray(new InputWithFeatures[0]);
    }

    @Override
    public InputWithFeatures detectFeatures(InputValue inputValue) {
        InputWithFeatures features = new InputWithFeatures();

        String inputValueTrimmed = inputValue.getInputValue().trim();

        features.setInputValue(inputValueTrimmed);
        features.setClazz(inputValue.getClazz());

        int numberOfWords = 0;
        int numberOfTotalCharacters = inputValueTrimmed.length();
        int numberOfAlphabetCharacters = 0;
        int numberOfNumericCharacters = 0;
        int numberOfWhitespaceCharacters = 0;
        int numberOfSpecialCharacters = 0;

        boolean inWord = false;
        for (int i = 0; i < inputValueTrimmed.length(); i++) {

            char ch = inputValueTrimmed.charAt(i);

            if (Character.isLetter(ch)) {
                numberOfAlphabetCharacters++;
                if (!inWord) inWord = true;
            }
            else if (Character.isDigit(ch)) {
                if (!inWord) inWord = true;
                numberOfNumericCharacters++;
            }
            else if (Character.isWhitespace(ch)) {
                if (inWord) {
                    inWord = false;
                    numberOfWords++;
                }
                numberOfWhitespaceCharacters++;
            }
            else numberOfSpecialCharacters++;
        }
        if (inWord) {
            numberOfWords++;
        }

        features.addIntFeature(F_INT_NUMBER_OF_WORDS, numberOfWords);
        features.addIntFeature(F_INT_NUMBER_OF_TOTAL_CHARACTERS, numberOfTotalCharacters);
        features.addIntFeature(F_INT_NUMBER_OF_ALPHABET_CHARACTERS, numberOfAlphabetCharacters);
        features.addIntFeature(F_INT_NUMBER_OF_NUMERIC_CHARACTERS, numberOfNumericCharacters);
        features.addIntFeature(F_INT_NUMBER_OF_WHITESPACE_CHARACTERS, numberOfWhitespaceCharacters);
        features.addIntFeature(F_INT_NUMBER_OF_SPECIAL_CHARACTERS, numberOfSpecialCharacters);

        boolean isIntegralNumericValue = false;
        boolean isDecimalNumericValue = false;
        try {
            long longValue = Long.parseLong(inputValueTrimmed);
            isIntegralNumericValue = true;
        }catch (NumberFormatException e) {
            try {
                double doubleValue = Double.parseDouble(inputValueTrimmed);
                isDecimalNumericValue = true;
            }catch (NumberFormatException ex) {
                //ignore
            }
        }
        features.addBoolFeature(F_BOOL_IS_NUMERIC_VALUE, isIntegralNumericValue || isDecimalNumericValue);
        features.addBoolFeature(F_BOOL_IS_INTEGRAL_NUMERIC_VALUE, isIntegralNumericValue);
        features.addBoolFeature(F_BOOL_IS_DECIMAL_NUMERIC_VALUE, isDecimalNumericValue);
        features.addBoolFeature(F_BOOL_IS_PREFIXED_NUMBER_VALUE, isPrefixedNumber(inputValueTrimmed));
        features.addBoolFeature(F_BOOL_IS_POSTFIXED_NUMBER_VALUE, isPostfixedNumber(inputValueTrimmed));
        return features;
    }

    private boolean isPrefixedNumber(String inputValue) {
        return regexMatches("^[^0-9]+[0-9]+[,.]{0,1}[0-9]*$", inputValue);
    }

    private boolean isPostfixedNumber(String inputValue) {
        return regexMatches("^[0-9]+[,.]{0,1}[0-9]*[^0-9]+$", inputValue);
    }

    private boolean regexMatches(String regex, String str) {
        Pattern pat = Pattern.compile(regex);
        Matcher mat = pat.matcher(str);
        return mat.matches();
    }

}
