package pl.revanmj.smspasswordnotifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by revanmj on 13.01.2017.
 */

public class CodeExtractor {
    public static String extractCode(String message, String customRegex) {
        String regex = "([0-9]{3}( |-)[0-9]{3}( |-)[0-9]{3}|[0-9]{3,4}( |-)[0-9]{3,4}|[0-9]{4,9})";

        Pattern pattern = null;
        if (customRegex != null && !customRegex.isEmpty())
            pattern = Pattern.compile(customRegex);
        else
            pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        String result = "";

        while (matcher.find()) {
            String tmp = matcher.group();
            if (tmp.length() > result.length())
                result = tmp;
        }

        if (!result.isEmpty())
            return result;

        return null;
    }
}
