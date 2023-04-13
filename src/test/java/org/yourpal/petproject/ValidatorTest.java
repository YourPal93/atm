package org.yourpal.petproject;

import org.junit.jupiter.api.Test;
import org.yourpal.petproject.enums.AtmMods;

import static org.junit.jupiter.api.Assertions.*;

class ValidatorTest {

    @Test
    public void luhnAlgoValidator() {
        var validOne = "2280370800400594";
        var invalidOne = "2280250800400594";
        var atmMod = AtmMods.NORMAL;


        assertTrue(Validator.validateCard(validOne, atmMod));
        assertFalse(Validator.validateCard(invalidOne, atmMod));
    }

}