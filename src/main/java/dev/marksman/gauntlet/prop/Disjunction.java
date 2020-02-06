package dev.marksman.gauntlet.prop;

import dev.marksman.enhancediterables.ImmutableNonEmptyFiniteIterable;
import dev.marksman.gauntlet.Context;
import dev.marksman.gauntlet.EvalResult;
import dev.marksman.gauntlet.Name;
import dev.marksman.gauntlet.Prop;

import static dev.marksman.gauntlet.EvalResult.evalResult;
import static dev.marksman.gauntlet.Failure.failure;
import static dev.marksman.gauntlet.Name.name;


final class Disjunction<A> implements Prop<A> {
    private final ImmutableNonEmptyFiniteIterable<Prop<A>> operands;
    private final Name name;

    Disjunction(ImmutableNonEmptyFiniteIterable<Prop<A>> operands) {
        this.operands = operands;
        this.name = name(String.join(" ∨ ",
                operands.fmap(p -> p.getName().getValue())));
    }

    @Override
    public Prop<A> or(Prop<A> other) {
        return new Disjunction<>((other instanceof Disjunction<?>)
                ? operands.concat(((Disjunction<A>) other).operands)
                : operands.append(other));
    }

    @Override
    public EvalResult test(Context context, A data) {
        EvalResult result = evalResult(failure(this, "All disjuncts failed."));
        for (Prop<A> prop : operands) {
            EvalResult test = prop.test(context, data);
            if (test.isSuccess()) {
                return test;
            } else {
                result = combine(result, test);
            }
        }
        return result;
    }

    private EvalResult combine(EvalResult acc, EvalResult item) {
        // success + _ -> success
        // failure + success -> success
        // failure + failure -> failure

        return acc
                .match(EvalResult::evalResult,
                        f1 -> item.match(EvalResult::evalResult,
                                f2 -> evalResult(f1.addCause(f2))));
    }

    @Override
    public Name getName() {
        return name;
    }
}
