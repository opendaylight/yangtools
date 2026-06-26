/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.Serializable;
import org.junit.jupiter.api.Test;

class SerialVersionHelperTest {
    @Test
    void computeDefaultSUIDTest() {
        assertEquals(6788238694991761868L, new SerialVersionHelper(JavaTypeName.create("my.package", "MyName"))
            .setAbstract(true)
            .addInterface(JavaTypeName.create(Serializable.class))
            .addField("myProperty")
            .addMethod("myMethodName", AccessModifier.PUBLIC)
            .computeSerialVersion());
    }

    @Test
    void computeDefaultSUIDStabilityTest() {
        final var svh = new SerialVersionHelper(JavaTypeName.create("org.opendaylight.yangtools.test", "TestType"))
            .setAbstract(true);
        assertEquals(3315273139240025558L, svh.computeSerialVersion());

        assertEquals(2532542948215379779L, svh
            .addMethod("testMethod", AccessModifier.PUBLIC)
            .addField("newProp")
            .addInterface(JavaTypeName.create("org.opendaylight.yangtools.test", "Type2"))
            .computeSerialVersion());

        assertEquals(6063820951740169208L,
            new SerialVersionHelper(JavaTypeName.create("org.opendaylight.yangtools.test2", "TestType2"))
                .setAbstract(true)
                .computeSerialVersion());
    }
}
