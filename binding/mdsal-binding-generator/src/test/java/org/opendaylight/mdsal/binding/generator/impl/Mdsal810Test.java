/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.generator.impl;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import org.junit.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class Mdsal810Test {
    @Test
    public void testKeyConflict() {
        assertGeneratedNames("key-conflict.yang",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.KeyConflictData",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.Foo$LI",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.Foo$LIKey",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.FooKey$CO");
    }

    @Test
    public void testListenerConflict() {
        assertGeneratedNames("listener-conflict.yang",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.ListenerConflictData",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.ListenerConflictListener$CO",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.Bar",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.ListenerConflictListener");
    }

    @Test
    public void testRootConflict() {
        assertGeneratedNames("root-conflict.yang",
            "org.opendaylight.yang.gen.v1.root.conflict.norev.RootConflictData",
            "org.opendaylight.yang.gen.v1.root.conflict.norev.RootConflictData$CO");
    }

    @Test
    public void testServiceConflict() {
        assertGeneratedNames("service-conflict.yang",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.ServiceConflictData",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.ServiceConflictService$CO",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.Bar",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.BarInput",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.BarOutput",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.ServiceConflictService");
    }

    @Test
    public void testInputOutputConflict() {
        assertGeneratedNames("io-conflict.yang",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.IoConflictData",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RP",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RPInput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RPOutput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.FooInput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.FooOutput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.IoConflictService");
    }

    @Test
    public void testSchemaCollisions() {
        assertGeneratedNames("schema-conflict.yang",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.SchemaConflictData",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$AD",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$AX",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$CO",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$LI",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$NO",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RP",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RPInput",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RPOutput",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.SchemaConflictListener",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.SchemaConflictService");
    }

    private static void assertGeneratedNames(final String yangFile, final String... fqcns) {
        assertEquals(Arrays.asList(fqcns),
            DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal-810/" + yangFile))
                .stream().map(type -> type.getIdentifier().toString()).toList());
    }
}
