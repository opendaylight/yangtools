package org.opendaylight.yangtools.sal.binding.generator.util;

import javassist.CtMethod;

public interface MethodGenerator {
    void process(CtMethod method);
}
