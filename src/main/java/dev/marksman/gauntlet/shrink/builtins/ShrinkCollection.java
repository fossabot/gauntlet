package dev.marksman.gauntlet.shrink.builtins;

import dev.marksman.collectionviews.Vector;
import dev.marksman.enhancediterables.ImmutableFiniteIterable;
import dev.marksman.gauntlet.shrink.ShrinkResult;
import dev.marksman.gauntlet.shrink.ShrinkResultBuilder;
import dev.marksman.gauntlet.shrink.ShrinkStrategy;

final class ShrinkCollection {
    private ShrinkCollection() {
    }

    static <A> ShrinkStrategy<Vector<A>> shrinkCollection(int minimumSize, ShrinkStrategy<A> element) {
        if (minimumSize < 0) {
            throw new IllegalArgumentException("minimumSize must be >= 0");
        }
        return input -> {
            int size = input.size();
            if (size <= minimumSize) {
                return ShrinkResult.empty();
            }
            ShrinkResultBuilder<Vector<A>> builder = ShrinkResultBuilder.shrinkResultBuilder();
            if (minimumSize == 0) builder = builder.append(Vector.empty());
            final int single = Math.max(1, minimumSize);
            if (single < size) {
                // first element
                builder = builder.lazyAppend(() -> input.take(single));

                // last element
                builder = builder.lazyAppend(() -> input.drop(size - single));
            }
            final int quarter = Math.max(size / 4, minimumSize);
            if (quarter > single) {
                // first quarter
                builder = builder.lazyAppend(() -> input.take(quarter));

                // last quarter
                builder = builder.lazyAppend(() -> input.drop(size - quarter));
            }
            if (size >= 4 && size / 2 >= minimumSize) {
                // even elements
                builder = builder.lazyAppend(() -> evenElements(input));

                // odd elements
                builder = builder.lazyAppend(() -> oddElements(input));
            }
            final int half = Math.max(size / 2, minimumSize);
            if (half > quarter) {
                // first half
                builder = builder.lazyAppend(() -> input.take(half));

                // second half
                builder = builder.lazyAppend(() -> input.drop(size - half));
            }
            final int init = size - single;
            if (init >= minimumSize) {
                // init
                builder = builder.lazyAppend(() -> input.take(init));

                // tail
                builder = builder.lazyAppend(() -> input.drop(single));
            }
            // individual elements
            builder = builder.lazyConcat(() -> shrinkIndividualElements(0, element, input));
            return builder.build();
        };
    }

    private static <A> Vector<A> evenElements(Vector<A> vector) {
        int newSize = (1 + vector.size()) / 2;
        return Vector.lazyFill(newSize, idx -> vector.unsafeGet(idx * 2));
    }

    private static <A> Vector<A> oddElements(Vector<A> vector) {
        int newSize = vector.size() / 2;
        return Vector.lazyFill(newSize, idx -> vector.unsafeGet(1 + idx * 2));
    }

    private static <A> ImmutableFiniteIterable<Vector<A>> shrinkIndividualElements(final int n, ShrinkStrategy<A> shrink, Vector<A> input) {
        if (n >= input.size()) {
            return ShrinkResult.empty();
        } else {
            Vector<A> front = input.take(n);
            Vector<A> back = input.drop(n + 1);
            A element = input.unsafeGet(n);
            ImmutableFiniteIterable<Vector<A>> nthShrinks = shrink.apply(element).fmap(newElement -> splice(front, newElement, back));
            return nthShrinks.concat(() -> shrinkIndividualElements(n + 1, shrink, input).iterator());
        }
    }

    private static <A> Vector<A> splice(Vector<A> front, A newElement, Vector<A> back) {
        return Vector.copyFrom(front.append(newElement).concat(back));
    }

}
