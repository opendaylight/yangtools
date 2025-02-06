/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.InputRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.ListRuntimeType;
import org.opendaylight.yangtools.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal824Test {
    private static EffectiveModelContext CONTEXT;

    @BeforeAll
    static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/mdsal824");
    }

    @Test
    void testCompileTimeTypes() {
        assertEquals(13, DefaultBindingGenerator.generateFor(CONTEXT).size());
    }

    @Test
    void testRunTimeTypes() {
        final var types = BindingRuntimeTypesFactory.createTypes(CONTEXT);
        final var barTop = assertInstanceOf(ContainerRuntimeType.class,
            types.schemaTreeChild(QName.create("bar", "bar-top")));
        final var barList = assertInstanceOf(ListRuntimeType.class,
            barTop.schemaTreeChild(QName.create("bar", "bar-list")));
        final var barAction = assertInstanceOf(ActionRuntimeType.class,
            barList.schemaTreeChild(QName.create("bar", "foo")));

        final var barInput = assertInstanceOf(InputRuntimeType.class,
            barAction.schemaTreeChild(QName.create("bar", "input")));
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev.act.grp", "FooInput"),
            barInput.javaType().getIdentifier());

        final var barOutput = assertInstanceOf(OutputRuntimeType.class,
            barAction.schemaTreeChild(QName.create("bar", "output")));
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev.act.grp", "FooOutput"),
            barOutput.javaType().getIdentifier());
    }
}
