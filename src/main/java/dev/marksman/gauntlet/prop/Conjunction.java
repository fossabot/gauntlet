package dev.marksman.gauntlet.prop;

import dev.marksman.collectionviews.Vector;
import dev.marksman.enhancediterables.ImmutableNonEmptyFiniteIterable;
import dev.marksman.gauntlet.EvalResult;
import dev.marksman.gauntlet.Name;
import dev.marksman.gauntlet.Prop;

import static dev.marksman.gauntlet.EvalResult.pass;


class Conjunction<A> implements Prop<A> {
    private final ImmutableNonEmptyFiniteIterable<Prop<A>> operands;
    private final Name name;

    Conjunction(ImmutableNonEmptyFiniteIterable<Prop<A>> operands) {
        this.operands = operands;
        this.name = Name.name(String.join(" ∧ ",
                operands.fmap(p -> p.getName().getValue())));
    }

    @Override
    public Prop<A> and(Prop<A> other) {
        return new Conjunction<>((other instanceof Conjunction<?>)
                ? operands.concat(((Conjunction<A>) other).operands)
                : operands.append(other));
    }

    @Override
    public EvalResult test(A data) {
        return operands
                .fmap(op -> op.test(data))
                .foldLeft((acc, result) -> acc.match(__ -> passState(result),
                        fail -> failState(fail, result),
                        error -> errorState(error, result)),
                        pass());
    }

    private EvalResult passState(EvalResult acc) {
        return acc
                .match(__ -> acc,
                        fail -> EvalResult.fail(Vector.of("Conjuncts failed"),
                                Vector.of(fail)),
                        error -> error);
    }

    private EvalResult failState(EvalResult.Fail acc, EvalResult evalResult) {
        return acc
                .match(__ -> acc,
                        acc::addCause,
                        error -> error);
    }

    private EvalResult errorState(EvalResult.Error acc, EvalResult evalResult) {
        return acc
                .match(__ -> acc,
                        __ -> acc,
                        error -> acc.addErrors(error.getErrors()));
    }

    @Override
    public Name getName() {
        return name;
    }
}
