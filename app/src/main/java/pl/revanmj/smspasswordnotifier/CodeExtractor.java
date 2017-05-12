package pl.revanmj.smspasswordnotifier;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by revanmj on 13.01.2017.
 */

public class CodeExtractor {
    public static String extractCode(String message) {
        String regex = "([0-9]{3}( |-)[0-9]{3}( |-)[0-9]{3}|[0-9]{3,4}( |-)[0-9]{3,4}|[0-9]{4,9})";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            String code = matcher.group(0);
            return code;
        }

        return null;
    }
}
