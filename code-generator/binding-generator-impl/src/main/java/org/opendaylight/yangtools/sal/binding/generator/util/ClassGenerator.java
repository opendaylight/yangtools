package org.opendaylight.yangtools.sal.binding.generator.util;

import javassist.CtClass;

public interface ClassGenerator {
    void process(CtClass cls);
}
