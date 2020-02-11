package dev.marksman.gauntlet;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import dev.marksman.gauntlet.shrink.Shrink;
import dev.marksman.kraftwerk.Parameters;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;
import static dev.marksman.gauntlet.FilteredArbitrary.filteredArbitrary;
import static dev.marksman.gauntlet.util.FilterChain.filterChain;

final class ConcreteArbitrary<A> implements Arbitrary<A> {
    private final Fn1<Parameters, ValueSupplier<A>> generator;
    private final Maybe<Shrink<A>> shrink;
    private final Fn1<A, String> prettyPrinter;
    private final int maxDiscards;

    private ConcreteArbitrary(Fn1<Parameters, ValueSupplier<A>> generator,
                              Maybe<Shrink<A>> shrink,
                              Fn1<A, String> prettyPrinter,
                              int maxDiscards) {
        this.generator = generator;
        this.shrink = shrink;
        this.prettyPrinter = prettyPrinter;
        this.maxDiscards = maxDiscards;
    }

    @Override
    public ValueSupplier<A> prepare(Parameters parameters) {
        return generator.apply(parameters);
    }

    @Override
    public Maybe<Shrink<A>> getShrink() {
        return shrink;
    }

    @Override
    public Fn1<A, String> getPrettyPrinter() {
        return prettyPrinter;
    }

    @Override
    public Arbitrary<A> withShrink(Shrink<A> shrink) {
        return new ConcreteArbitrary<>(generator, just(shrink), prettyPrinter, maxDiscards);
    }

    @Override
    public Arbitrary<A> withNoShrink() {
        return shrink.match(__ -> this,
                __ -> new ConcreteArbitrary<>(generator, nothing(), prettyPrinter, maxDiscards));
    }

    @Override
    public Arbitrary<A> suchThat(Fn1<A, Boolean> predicate) {
        return filteredArbitrary(this, filterChain(predicate), maxDiscards);
    }

    @Override
    public Arbitrary<A> withMaxDiscards(int maxDiscards) {
        return null;
    }

    @Override
    public Arbitrary<A> withPrettyPrinter(Fn1<A, String> prettyPrinter) {
        return new ConcreteArbitrary<>(generator, shrink, prettyPrinter, maxDiscards);
    }

    @Override
    public <B> Arbitrary<B> convert(Fn1<A, B> ab, Fn1<B, A> ba) {
        return new ConcreteArbitrary<>(generator.fmap(vs -> vs.fmap(ab)),
                shrink.fmap(s -> s.convert(ab, ba)),
                prettyPrinter.contraMap(ba),
                maxDiscards);

    }

    static <A> ConcreteArbitrary<A> concreteArbitrary(Fn1<Parameters, ValueSupplier<A>> generator,
                                                      Maybe<Shrink<A>> shrink,
                                                      Fn1<A, String> prettyPrinter) {
        return new ConcreteArbitrary<>(generator, shrink, prettyPrinter, Gauntlet.DEFAULT_MAX_DISCARDS);
    }

}
