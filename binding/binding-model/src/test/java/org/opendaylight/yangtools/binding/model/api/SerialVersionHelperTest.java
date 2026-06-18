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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilderBase;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.RuntimeGeneratedTypeBuilder;

class SerialVersionHelperTest {
    @Test
    void computeDefaultSUIDTest() {
        final var generatedTypeBuilder = newBuilder("my.package", "MyName");
        generatedTypeBuilder.addMethod("myMethodName").setAccessModifier(AccessModifier.PUBLIC)
            .setReturnType(Types.PRIMITIVE_INT);
        generatedTypeBuilder.addProperty("myProperty").setReadOnly(true).setReturnType(Types.PRIMITIVE_LONG);
        generatedTypeBuilder.addImplementsType(Types.typeForClass(Serializable.class));

        assertSerialVersion(6788238694991761868L, generatedTypeBuilder);
    }

    @Test
    void computeDefaultSUIDStabilityTest() {
        // test method computeDefaultSUID
        final var genTypeBuilder = newBuilder("org.opendaylight.yangtools.test", "TestType");
        assertSerialVersion(3315273139240025558L, genTypeBuilder);

        genTypeBuilder.addMethod("testMethod").setReturnType(Types.STRING);
        genTypeBuilder.addAnnotation("org.opendaylight.yangtools.test.annotation", "AnnotationTest");
        genTypeBuilder.addProperty("newProp").setReturnType(Types.BOOLEAN);
        genTypeBuilder.addImplementsType(newBuilder("org.opendaylight.yangtools.test", "Type2"));
        assertSerialVersion(2532542948215379779L, genTypeBuilder);

        assertSerialVersion(6063820951740169208L, newBuilder("org.opendaylight.yangtools.test2", "TestType2"));
    }

    @NonNullByDefault
    private static RuntimeGeneratedTypeBuilder newBuilder(final String packageName, final String simpleName) {
        return new RuntimeGeneratedTypeBuilder(JavaTypeName.create(packageName, simpleName));
    }

    @NonNullByDefault
    private static void assertSerialVersion(final long expected, final GeneratedTypeBuilderBase<?> to) {
        assertEquals(expected, SerialVersionHelper.computeSerialVersion(to.build()));
    }
}
