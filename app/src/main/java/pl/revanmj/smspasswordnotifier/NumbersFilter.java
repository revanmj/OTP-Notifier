package pl.revanmj.smspasswordnotifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by revanmj on 13.01.2017.
 */

public class NumbersFilter {

    static final Set<String> NUMBERS_SET = new HashSet<String>() {{
        add("PayPal");
    }};

    public static boolean shouldExtractPassword(String address) {

        if (NUMBERS_SET.contains(address))
            return true;

        return false;
    }
}
