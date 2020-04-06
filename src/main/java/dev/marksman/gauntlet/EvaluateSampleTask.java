package dev.marksman.gauntlet;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

import static dev.marksman.gauntlet.TestTaskResult.error;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EvaluateSampleTask<A> implements Runnable {
    private final Context context;
    private final ResultReceiver receiver;
    private final Prop<A> property;
    private final int sampleIndex;
    private final A sample;

    @Override
    public void run() {
        if (receiver.shouldRun(sampleIndex)) {
            try {
                EvalResult evalResult = property.test(context, sample);
                receiver.reportResult(sampleIndex, TestTaskResult.testTaskResult(evalResult));
            } catch (Exception error) {
                receiver.reportResult(sampleIndex, error(error));
            }
        }
    }

    public static <A> EvaluateSampleTask<A> testSampleTask(Context context, ResultReceiver receiver, Prop<A> property, int sampleIndex, A sample) {
        return new EvaluateSampleTask<>(context, receiver, property, sampleIndex, sample);
    }
}