package dev.marksman.gauntlet.shrink.builtins;

import com.jnape.palatable.lambda.adt.hlist.Tuple2;
import dev.marksman.gauntlet.GauntletApiBase;
import dev.marksman.kraftwerk.Generator;
import dev.marksman.kraftwerk.constraints.IntRange;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import testsupport.shrink.ShrinkTestCase;

import static dev.marksman.gauntlet.prop.Props.allOf;
import static dev.marksman.gauntlet.shrink.builtins.ShrinkNumerics.shrinkByte;
import static dev.marksman.gauntlet.shrink.builtins.ShrinkNumerics.shrinkInt;
import static dev.marksman.gauntlet.shrink.builtins.ShrinkNumerics.shrinkLong;
import static dev.marksman.gauntlet.shrink.builtins.ShrinkNumerics.shrinkShort;
import static dev.marksman.kraftwerk.Generators.generateByte;
import static dev.marksman.kraftwerk.Generators.generateInt;
import static dev.marksman.kraftwerk.Generators.generateIntRange;
import static dev.marksman.kraftwerk.Generators.generateLong;
import static dev.marksman.kraftwerk.Generators.generateOrderedPair;
import static dev.marksman.kraftwerk.Generators.generateShort;
import static dev.marksman.kraftwerk.frequency.FrequencyMap.frequencyMap;
import static testsupport.shrink.ShrinkTestCase.allElementsWithinDomain;
import static testsupport.shrink.ShrinkTestCase.constrainedShrinkTestCase;
import static testsupport.shrink.ShrinkTestCase.neverRepeatsAnElement;
import static testsupport.shrink.ShrinkTestCase.shrinkOutputEmptyWhenInputOutsideOfDomain;
import static testsupport.shrink.ShrinkTestCase.shrinkTestCases;

class ShrinkNumericsTest extends GauntletApiBase {

    private static final Generator<Tuple2<Integer, Integer>> generateIntSpan =
            generateOrderedPair(generateInt());

    // For the shrink input, we want most values to be in the domain, but occasionally exercise it outside of the domain
    private static Generator<Integer> generateMostlyInDomain(IntRange range) {
        return frequencyMap(1, generateInt())
                .add(3, generateInt(range))
                .toGenerator();
    }

    @Nested
    @DisplayName("ints")
    class Ints {

        @Test
        void unclamped() {
            all(shrinkTestCases(generateInt(), shrinkInt()))
                    .mustSatisfy(neverRepeatsAnElement());
        }

        @Test
        @Disabled
        void clamped() {
            // TODO:
            // java.lang.AssertionError: Failed property 'never repeats an element ∧ all elements within domain ∧ when input is outside of shrink domain, shrink output is empty' with value 'ShrinkTestCase(input=-2147483648, output=dev.marksman.gauntlet.shrink.LazyCons$1@30272916, min=-2147483648, max=1413962530)'. reasons: FailureReasons(items=Vector(Conjuncts failed.))

            all(constrainedShrinkTestCase(generateIntRange(),
                    ShrinkNumericsTest::generateMostlyInDomain,
                    ShrinkNumerics::shrinkInt))
                    .mustSatisfy(allOf(
                            ShrinkTestCase.<Integer>neverRepeatsAnElement(),
                            allElementsWithinDomain(),
                            shrinkOutputEmptyWhenInputOutsideOfDomain()
                    ));
        }

    }

    @Nested
    @DisplayName("longs")
    class Longs {

        @Test
        void unclamped() {
            all(shrinkTestCases(generateLong(), shrinkLong()))
                    .mustSatisfy(neverRepeatsAnElement());
        }

    }

    @Nested
    @DisplayName("shorts")
    class Shorts {

        @Test
        void unclamped() {
            all(shrinkTestCases(generateShort(), shrinkShort()))
                    .mustSatisfy(neverRepeatsAnElement());
        }

    }

    @Nested
    @DisplayName("bytes")
    class Bytes {

        @Test
        void unclamped() {
            all(shrinkTestCases(generateByte(), shrinkByte()))
                    .mustSatisfy(neverRepeatsAnElement());
        }

    }

}