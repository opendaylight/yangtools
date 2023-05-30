/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;
import org.opendaylight.yangtools.binding.model.ri.Types;

class GeneratedTOBuilderTest {
    @Test
    void testBuilder() {
        final var genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        genTypeBuilder.setSUID(genTypeBuilder.addProperty("SUID").setReturnType(BindingTypes.SCALAR_TYPE_OBJECT));
        genTypeBuilder.addMethod("addCount").setReturnType(Types.VOID);

        var genTO = genTypeBuilder.build();
        genTypeBuilder.setExtendsType(genTO);

        var property = genTypeBuilder.addProperty("customProperty");
        genTypeBuilder.addHashIdentity(property);

        genTypeBuilder.addEqualsIdentity(property);

        genTypeBuilder.addToStringProperty(property);

        assertNotNull(genTO);
        assertNotNull(genTO.getProperties());
    }

    @Test
    void testToString() {
        final var genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));
        assertThat(genTypeBuilder.toString())
            .startsWith("CodegenGeneratedTOBuilder{identifier=org.opendaylight.controller.AnnotClassCache");
    }

    @Test
    void testTransferBuilderToString() {
        final var genTypeBuilder1 = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        var genTO = genTypeBuilder1.build();
        assertThat(genTO.toString()).startsWith("GTO{identifier=org.opendaylight.controller.AnnotClassCache");
    }
}
