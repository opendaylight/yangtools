/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTOBuilder;

public class GeneratedTOBuilderTest {

    @Test
    public void testBuilder() {
        final GeneratedTOBuilder genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        genTypeBuilder.setSUID(genTypeBuilder.addProperty("SUID"));
        genTypeBuilder.addMethod("addCount");

        GeneratedTransferObject genTO = genTypeBuilder.build();
        genTypeBuilder.setExtendsType(genTO);

        GeneratedPropertyBuilder property = genTypeBuilder
                .addProperty("customProperty");
        genTypeBuilder.addHashIdentity(property);

        genTypeBuilder.addEqualsIdentity(property);

        genTypeBuilder.addToStringProperty(property);

        assertNotNull(genTO);
        assertNotNull(genTO.getProperties());
    }

    @Test
    public void testToString() {
        final GeneratedTOBuilder genTypeBuilder = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));
        assertThat(genTypeBuilder.toString(),
            startsWith("CodegenGeneratedTOBuilder{identifier=org.opendaylight.controller.AnnotClassCache"));
    }

    @Test
    public void testTransferBuilderToString() {
        final GeneratedTOBuilder genTypeBuilder1 = new CodegenGeneratedTOBuilder(
            JavaTypeName.create("org.opendaylight.controller", "AnnotClassCache"));

        GeneratedTransferObject genTO = genTypeBuilder1.build();
        assertThat(genTO.toString(), startsWith("GTO{identifier=org.opendaylight.controller.AnnotClassCache"));
    }
}
