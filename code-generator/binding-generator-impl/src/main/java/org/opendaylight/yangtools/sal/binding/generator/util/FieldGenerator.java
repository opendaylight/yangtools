package org.opendaylight.yangtools.sal.binding.generator.util;

import javassist.CtField;

public interface FieldGenerator {
    void process(CtField field);
}
