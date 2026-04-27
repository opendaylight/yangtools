/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.Types;
import org.opendaylight.yangtools.binding.model.ri.generated.type.builder.CodegenGeneratedTOBuilder;

class ClassCodeGeneratorTest {
    /**
     * Test for testing of false scenario. Test tests value types. Value types are not allowed to have default
     * constructor.
     */
    @Test
    void defaultConstructorNotPresentInValueTypeTest() {
        final var toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack", "DefCtor"));
        toBuilder.setTypedef(true);

        var propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);

        final var genTO = toBuilder.build();

        final String outputStr = new TOGenerator(genTO).generate();

        assertNotNull(outputStr);
        assertFalse(outputStr.contains("public DefCtor()"));
    }

    @Test
    void toStringTest() {
        final var toBuilder = new CodegenGeneratedTOBuilder(JavaTypeName.create("simple.pack", "DefCtor"));
        toBuilder.setTypedef(true);

        var propBuilder = toBuilder.addProperty("foo");
        propBuilder.setReturnType(Types.typeForClass(String.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);

        propBuilder = toBuilder.addProperty("bar");
        propBuilder.setReturnType(Types.typeForClass(Integer.class));
        propBuilder.setReadOnly(false);
        toBuilder.addToStringProperty(propBuilder);
        assertNotNull(new TOGenerator(toBuilder.build()).generate());
    }
}
