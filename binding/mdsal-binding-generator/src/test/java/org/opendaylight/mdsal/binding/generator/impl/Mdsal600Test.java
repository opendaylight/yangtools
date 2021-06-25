/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
public class Mdsal600Test {
    private static final JavaTypeName FOO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal600.norev", "Foo");

    @Test
    public void mdsal600Test() {
        final var models = YangParserTestUtils.parseYangResource("/mdsal600.yang");
        final var module = models.getModules().iterator().next();
        final var fooSchema = (ContainerSchemaNode) module.findDataTreeChild(QName.create("mdsal600", "foo"))
            .orElseThrow();

        // Verify leaves
        final var barSchema = fooSchema.findDataTreeChild(QName.create("mdsal600", "bar")).orElseThrow();
        final var bazSchema = fooSchema.findDataTreeChild(QName.create("mdsal600", "baz")).orElseThrow();
        final var barTypeSchema = ((LeafSchemaNode) barSchema).getType();
        final var bazTypeSchema = ((LeafSchemaNode) bazSchema).getType();

        // Precondition to our bug: the two types compare as equal, but are not the same
        assertEquals(barTypeSchema, bazTypeSchema);
        assertNotSame(barTypeSchema, bazTypeSchema);

        // Quick check for codegen, not really interesting
        final var generatedTypes = DefaultBindingGenerator.generateFor(models);
        assertEquals(2, generatedTypes.size());

        // The real thing, used to kaboom
        final var runtimeTypes = new DefaultBindingRuntimeGenerator().generateTypeMapping(models);

        // Verify schema-to-type lookup
        final var barType = runtimeTypes.findType(barTypeSchema).orElseThrow();
        final var bazType = runtimeTypes.findType(bazTypeSchema).orElseThrow();
        assertEquals(FOO.createEnclosed("Bar"), barType.getIdentifier());
        assertEquals(FOO.createEnclosed("Baz"), bazType.getIdentifier());

        // Verify type-to-schema lookup
        assertSame(barTypeSchema, runtimeTypes.findSchema(barType).orElseThrow());
        assertSame(bazTypeSchema, runtimeTypes.findSchema(bazType).orElseThrow());
    }
}
