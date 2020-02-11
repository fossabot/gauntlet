package dev.marksman.gauntlet;

import com.jnape.palatable.lambda.adt.Maybe;
import com.jnape.palatable.lambda.functions.Fn1;
import dev.marksman.gauntlet.shrink.Shrink;
import dev.marksman.kraftwerk.Generator;
import dev.marksman.kraftwerk.Parameters;
import lombok.Value;

import static com.jnape.palatable.lambda.adt.Maybe.just;
import static com.jnape.palatable.lambda.adt.Maybe.nothing;

@Value
class OldConcreteArbitrary<A> implements Arbitrary<A> {
    private final WrappedGenerator<A> underlying;
    private final Maybe<Shrink<A>> shrink;
    private final Fn1<A, String> prettyPrinter;

    @Override
    public ValueSupplier<A> prepare(Parameters parameters) {
        return underlying.prepare(parameters);
    }

    @Override
    public Arbitrary<A> withShrink(Shrink<A> shrink) {
        return new OldConcreteArbitrary<>(underlying, just(shrink), prettyPrinter);
    }

    @Override
    public Arbitrary<A> withNoShrink() {
        return shrink.match(__ -> this,
                __ -> new OldConcreteArbitrary<>(underlying, nothing(), prettyPrinter));
    }

    @Override
    public Arbitrary<A> suchThat(Fn1<A, Boolean> predicate) {
        return new OldConcreteArbitrary<>(underlying.suchThat(predicate), shrink, prettyPrinter);
    }

    @Override
    public Arbitrary<A> withMaxDiscards(int maxDiscards) {
        WrappedGenerator<A> newUnderlying = underlying.withMaxDiscards(maxDiscards);
        return underlying != newUnderlying
                ? new OldConcreteArbitrary<>(newUnderlying, shrink, prettyPrinter)
                : this;
    }

    @Override
    public Arbitrary<A> withPrettyPrinter(Fn1<A, String> prettyPrinter) {
        return new OldConcreteArbitrary<>(underlying, shrink, prettyPrinter);
    }

    @Override
    public <B> Arbitrary<B> convert(Fn1<A, B> ab, Fn1<B, A> ba) {
        return new OldConcreteArbitrary<>(underlying.convert(ab, ba),
                shrink.fmap(s -> s.convert(ab, ba)),
                prettyPrinter.contraMap(ba));
    }

    static <A> OldConcreteArbitrary<A> concreteArbitrary(Generator<A> generator) {
        return new OldConcreteArbitrary<>(new UnfilteredGenerator<>(generator),
                nothing(), Object::toString);
    }
}
