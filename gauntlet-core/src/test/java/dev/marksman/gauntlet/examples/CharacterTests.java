package dev.marksman.gauntlet.examples;

import dev.marksman.gauntlet.GauntletApiBase;
import dev.marksman.gauntlet.Prop;
import org.junit.jupiter.api.Test;

import static dev.marksman.gauntlet.Domains.asciiCharacters;
import static dev.marksman.gauntlet.prop.Props.*;

public class CharacterTests extends GauntletApiBase {

    public static final Prop<Character> isUpperCaseLetter = Prop.predicate("is uppercase letter", Character::isUpperCase);
    public static final Prop<Character> isLowerCaseLetter = Prop.predicate("is lowercase letter", Character::isLowerCase);

    @Test
    void cannotBeBothUppercaseAndLowercase1() {
        all(asciiCharacters())
                .mustSatisfy(named("never both uppercase and lowercase",
                        (isUpperCaseLetter.and(isLowerCaseLetter)).not()));
    }

    @Test
    void cannotBeBothUppercaseAndLowercase2() {
        all(asciiCharacters())
                .mustSatisfy(named("never both uppercase and lowercase",
                        allOf(isUpperCaseLetter.implies(isLowerCaseLetter.not()),
                                isLowerCaseLetter.implies(isUpperCaseLetter.not()))));
    }

    @Test
    void someUppercaseCharactersExist() {
        some(asciiCharacters()).mustSatisfy(isUpperCaseLetter);
    }

    @Test
    void someLowercaseCharactersExist() {
        some(asciiCharacters()).mustSatisfy(isLowerCaseLetter);
    }

    @Test
    void someCharactersThatAreNeitherUppercaseOrLowercaseExist() {
        some(asciiCharacters()).mustSatisfy(noneOf(isUpperCaseLetter, isLowerCaseLetter));
    }

}