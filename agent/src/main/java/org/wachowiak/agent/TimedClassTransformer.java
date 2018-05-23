package org.wachowiak.agent;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class TimedClassTransformer implements ClassFileTransformer {

    private ClassPool classPool = new ClassPool();


    public TimedClassTransformer() {
        classPool = new ClassPool();
        classPool.appendSystemPath();
        try {
            classPool.appendPathList(System.getProperty("java.class.path"));

            // make sure that MetricReporter is loaded
//            classPool.get("com.chimpler.example.agentmetric.MetricReporter").getClass();
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classBytes) throws IllegalClassFormatException {
        className = className.replace("/", ".");

        classPool.appendClassPath(new ByteArrayClassPath(className, classBytes));

        try {
            CtClass ctClass = classPool.get(className);
            if (ctClass.isFrozen()) {
//                logger.debug(&quot;Skip class {}: is frozen&quot;, className);
                return null;
            }

            if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
                    || ctClass.isEnum() || ctClass.isInterface()) {
//                logger.debug(&quot;Skip class {}: not a class&quot;, className);
                return null;
            }
            boolean isClassModified = false;
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                // if method is annotated, add the code to measure the time
                if (method.hasAnnotation(Measured.class)) {
                    try {
                        if (method.getMethodInfo().getCodeAttribute() == null) {
//                            logger.debug(&quot;Skip method &quot; + method.getLongName());
                            continue;
                        }
//                        logger.debug(&quot;Instrumenting method &quot; + method.getLongName());
                        method.addLocalVariable("metricStartTime", CtClass.longType);
                        method.insertBefore("metricStartTime = System.currentTimeMillis();");
                        String metricName = ctClass.getName() + "." + method.getName();
                        method.insertAfter("com.chimpler.example.agentmetric.MetricReporter.reportTime(\""
                                + metricName + "\", System.currentTimeMillis() - metricStartTime);");
                        isClassModified = true;
                    } catch (Exception e) {
//                        logger.warn(&quot;Skipping instrumentation of method {}: {}&quot;, method.getName(), e.getMessage());
                    }
                }
            }
            if (isClassModified) {
                return ctClass.toBytecode();
            }
        } catch (Exception e) {
//            logger.debug(&quot;Skip class {}: &quot;, className, e.getMessage());
        }
        return classBytes;
    }
}
