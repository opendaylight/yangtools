/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.mdsal.binding.model.api.JavaTypeName;
import org.opendaylight.mdsal.binding.runtime.api.ActionRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ContainerRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.InputRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.ListRuntimeType;
import org.opendaylight.mdsal.binding.runtime.api.OutputRuntimeType;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal824Test {
    private static EffectiveModelContext CONTEXT;

    @BeforeClass
    public static void beforeClass() {
        CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/mdsal824");
    }

    @Test
    public void testCompileTimeTypes() {
        assertEquals(13, DefaultBindingGenerator.generateFor(CONTEXT).size());
    }

    @Test
    public void testRunTimeTypes() {
        final var types = BindingRuntimeTypesFactory.createTypes(CONTEXT);
        final var barTop = types.schemaTreeChild(QName.create("bar", "bar-top"));
        assertThat(barTop, instanceOf(ContainerRuntimeType.class));
        final var barList = ((ContainerRuntimeType) barTop).schemaTreeChild(QName.create("bar", "bar-list"));
        assertThat(barList, instanceOf(ListRuntimeType.class));
        final var barAction = ((ListRuntimeType) barList).schemaTreeChild(QName.create("bar", "foo"));
        assertThat(barAction, instanceOf(ActionRuntimeType.class));

        final var barInput = ((ActionRuntimeType) barAction).schemaTreeChild(QName.create("bar", "input"));
        assertThat(barInput, instanceOf(InputRuntimeType.class));
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev.act.grp", "FooInput"),
            barInput.javaType().getIdentifier());

        final var barOutput = ((ActionRuntimeType) barAction).schemaTreeChild(QName.create("bar", "output"));
        assertThat(barOutput, instanceOf(OutputRuntimeType.class));
        assertEquals(JavaTypeName.create("org.opendaylight.yang.gen.v1.foo.norev.act.grp", "FooOutput"),
            barOutput.javaType().getIdentifier());
    }
}
