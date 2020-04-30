package dev.marksman.gauntlet;

import com.jnape.palatable.lambda.functions.Fn2;
import dev.marksman.kraftwerk.Seed;

import static dev.marksman.gauntlet.SupplyTree.composite;

final class CompositeSupply2<A, B, Out> implements Supply<Out> {
    private final Supply<A> vsA;
    private final Supply<B> vsB;
    private final Fn2<A, B, Out> fn;

    CompositeSupply2(Supply<A> vsA, Supply<B> vsB, Fn2<A, B, Out> fn) {
        this.vsA = vsA;
        this.vsB = vsB;
        this.fn = fn;
    }

    static <A, B> GeneratorOutput<B> threadSeed(int posIndex,
                                                GeneratorOutput<A> ra,
                                                Fn2<A, Seed, GeneratorOutput<B>> f) {
        return ra.getValue()
                .match(gf -> GeneratorOutput.failure(ra.getNextState(), gf),
                        a -> f.apply(a, ra.getNextState()));
    }

    private static String positionName(int posIndex) {
        return "position " + (posIndex + 1);
    }

    // TODO: find another way to build SupplyTree because this is going to be way too ugly

//    private GeneratorOutput<Out> getNext2(Seed input) {
//        GeneratorOutput<A> ra = vsA.getNext(input);
//        return ra.getValue()
//                .match(sf -> supplyFailure(SupplyTree.composite(sf.getTree(),
//                        vsB.getSupplyTree())),
//                        a -> {
//                            GeneratorOutput<B> rb = vsB.getNext(ra.getNextState());
//                            rb.getValue()
//                                    .match(sf -> GeneratorOutput.failure(rb.getNextState(),
//                                            supplyFailure(SupplyTree.composite(vsA.getSupplyTree(),
//                                            sf.getTree()))),
//                                            b -> GeneratorOutput.success(rb.getNextState(),
//                                                    fn.apply(a, b)));
//                            return match;
//                        });
//    }

    @Override
    public SupplyTree getSupplyTree() {
        return composite(vsA.getSupplyTree(), vsB.getSupplyTree());
    }

    @Override
    public GeneratorOutput<Out> getNext(Seed input) {
        return threadSeed(0,
                vsA.getNext(input), (a, s1) -> threadSeed(1, vsB.getNext(s1),
                        (b, s2) -> GeneratorOutput.success(s2, fn.apply(a, b))));

    }
}
