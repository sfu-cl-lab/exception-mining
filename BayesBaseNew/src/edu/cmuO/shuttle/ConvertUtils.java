package edu.cmu.shuttle;

import edu.cmu.tetrad.data.RegexTokenizer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Some utilities for converting Choh Man's reformatted shuttle data into data
 * that Tetrad can read.
 *
 * @author Joseph Ramsey
 * @version $Revision: 5177 $ $Date: 2006-01-12 20:04:03 -0500 (Thu, 12 Jan
 *          2006) $
 */
public final class ConvertUtils {

    /**
     * Example 98:20:6:3/243.
     */
    private static final Pattern YEAR_TIME_PATTERN =
            Pattern.compile("(\\d*):(\\d*):(\\d*):(\\d*)/(\\d*)");

    /**
     * Example -8:12:0:34.
     */
    private static final Pattern LAUNCH_TIME_PATTERN =
            Pattern.compile("-?(\\d*):(\\d*):(\\d*):(\\d*)");


    /**
     * Example v234233.
     */
    private static final Pattern VAR_NUMBER_PATTERN =
            Pattern.compile("v(\\d*)");

    /**
     * Should recognize any continuous datum. Missing value is NaN.
     */
    private static final Pattern CONTINUOUS_DATA_PATTERN =
            Pattern.compile("(-?\\d*\\.\\d*([eE]-?\\d*)?)|NaN|Infinity");

    /**
     * Recognizes the discrete values that occur in these files. There's ON/OFF,
     * WET/DRY, TRUE/FALSE. Missing value is null.
     */
    private static final Pattern DISCRETE_DATA_PATTERN =
            Pattern.compile("ON|OFF|WET|DRY|TRUE|FALSE|null");

    /**
     * The delimiter for Choh Man's files.
     */
    private static final Pattern DELIMITER = Pattern.compile(" *[\t ,] *");

    /**
     * Converts a time string in form day:hour:minute:second/millis, where these
     * are all assumed to be non-negative.
     */
    public static long parseYearTime(String timeString) {
        Matcher matcher = getYearTimePattern().matcher(timeString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Malformed time string: " + timeString);
        }

        long day = Integer.parseInt(matcher.group(1));
        long hour = Integer.parseInt(matcher.group(2));
        long minute = Integer.parseInt(matcher.group(3));
        long second = Integer.parseInt(matcher.group(4));
        long millisecond = Integer.parseInt(matcher.group(5));

        if (day < 0 || hour < 0 || minute < 0 || second < 0 || millisecond < 0)
        {
            throw new IllegalArgumentException(
                    "All fields must be non-negative: " + timeString);
        }

        return ((((24 * day) + hour) * 60 + minute) * 60 + second) * 1000 +
                millisecond;
    }


    /**
     * Given -0:0:8:52, returns the number of milliseconds before launch that
     * represents, for example.
     */
    public static long parseLaunchTime(String timeString) {
        Matcher matcher = getLaunchTimePattern().matcher(timeString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Malformed time string: " + timeString);
        }

        long day = Integer.parseInt(matcher.group(1));
        long hour = Integer.parseInt(matcher.group(2));
        long minute = Integer.parseInt(matcher.group(3));
        long second = Integer.parseInt(matcher.group(4));

        if (day < 0 || hour < 0 || minute < 0 || second < 0) {
            throw new IllegalArgumentException(
                    "All fields must be non-negative: " + timeString);
        }

        return ((((24 * day) + hour) * 60 + minute) * 60 + second) * 1000;
    }

    /**
     * For string v103 returns 103, for example.
     */
    public static int parseVarNumber(String varString) {
        Matcher matcher = getVarNumberPattern().matcher(varString);

        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Malformed var number string: " + varString);
        }

        int number = Integer.parseInt(matcher.group(1));

        if (number < 0) {
            throw new IllegalArgumentException(
                    "All fields must be non-negative: " + varString);
        }

        return number;
    }

    /**
     * Given a string read in the data column of one of Choh Man's data files,
     * returns the type of that string.
     */
    public static DataType getDataType(String datum) {
        if (isContinuousDatum(datum)) {
            return DataType.CONTINUOUS;
        }
        else if (isDiscreteDatum(datum)) {
            return DataType.DISCRETE;
        }
        else if (isLaunchTimeDatum(datum)) {
            return DataType.LAUNCH_TIME;
        }

        throw new IllegalArgumentException(
                "Didn't recognize the type of that: " + datum);
    }

    public static Record parseRecord(String line) {
        RegexTokenizer tokenizer =
                new RegexTokenizer(line, ConvertUtils.getDelimiter(), '"');
        String time = tokenizer.nextToken();
        String variable = tokenizer.nextToken();
        String value = tokenizer.nextToken();
        return new Record(time, variable, value);
    }

    public static boolean isContinuousDatum(String datum) {
        return getContinuousDataPattern().matcher(datum).matches();
    }

    public static boolean isDiscreteDatum(String datum) {
        return getDiscreteDataPattern().matcher(datum).matches();
    }

    public static boolean isLaunchTimeDatum(String datum) {
        return getLaunchTimePattern().matcher(datum).matches();
    }

    private static Pattern getYearTimePattern() {
        return YEAR_TIME_PATTERN;
    }

    private static Pattern getLaunchTimePattern() {
        return LAUNCH_TIME_PATTERN;
    }

    private static Pattern getVarNumberPattern() {
        return VAR_NUMBER_PATTERN;
    }

    private static Pattern getContinuousDataPattern() {
        return CONTINUOUS_DATA_PATTERN;
    }

    private static Pattern getDiscreteDataPattern() {
        return DISCRETE_DATA_PATTERN;
    }

    private static Pattern getDelimiter() {
        return DELIMITER;
    }
}
