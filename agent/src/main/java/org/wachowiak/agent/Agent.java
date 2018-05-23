package org.wachowiak.agent;

import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String argument, Instrumentation instrumentation) {
        instrumentation.addTransformer(new TimedClassTransformer());
    }
}
