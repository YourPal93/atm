package org.yourpal.petproject;

import org.yourpal.petproject.enums.AtmMods;

public class Validator {

    public static boolean validateCard(String cardNumber, AtmMods atmMod) {
        if(!cardNumber.matches("^\\d+$")) return false;

        if(atmMod == AtmMods.TEST) return true;

        int summ = 0;
        boolean isEven = true;
        for(int i = 0, len = cardNumber.length(); i < len; i++) {
            int currNum = cardNumber.charAt(i) - '0';

            if(isEven) {
                currNum *= 2;
            }

            summ += currNum / 10;
            summ += currNum % 10;
            isEven = !isEven;
        }

        return summ % 10 == 0;
    }
}
