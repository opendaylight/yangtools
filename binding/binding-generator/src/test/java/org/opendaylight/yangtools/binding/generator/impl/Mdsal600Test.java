/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@NonNullByDefault
class Mdsal600Test {
    private static final JavaTypeName FOO = JavaTypeName.create("org.opendaylight.yang.gen.v1.mdsal600.norev", "Foo");

    @Test
    void mdsal600Test() {
        final var models = YangParserTestUtils.parseYangResource("/mdsal600.yang");
        final var module = models.getModules().iterator().next();
        final var fooSchema = (ContainerSchemaNode) module.findDataTreeChild(QName.create("mdsal600", "foo"))
            .orElseThrow();

        // Verify leaves
        final var barSchema = assertInstanceOf(LeafSchemaNode.class,
            fooSchema.findDataTreeChild(QName.create("mdsal600", "bar")).orElseThrow());
        final var bazSchema = assertInstanceOf(LeafSchemaNode.class,
            fooSchema.findDataTreeChild(QName.create("mdsal600", "baz")).orElseThrow());
        final var barTypeSchema = barSchema.getType();
        final var bazTypeSchema = bazSchema.getType();

        // Precondition to our bug: the two types compare as equal, but are not the same
        assertEquals(barTypeSchema, bazTypeSchema);
        assertNotSame(barTypeSchema, bazTypeSchema);

        // Quick check for codegen, not really interesting
        final var generatedTypes = DefaultBindingGenerator.generateFor(models);
        assertEquals(2, generatedTypes.size());

        // The real thing, used to kaboom
        final var runtimeTypes = new DefaultBindingRuntimeGenerator().generateTypeMapping(models);

        // Verify type-to-schema lookup
        final var barType = runtimeTypes.findSchema(FOO.createEnclosed("Bar")).orElseThrow();
        final var bazType = runtimeTypes.findSchema(FOO.createEnclosed("Baz")).orElseThrow();

        // Verify underlying schema lookup
        assertSame(barSchema, barType.statement());
        assertSame(bazSchema, bazType.statement());
    }
}
