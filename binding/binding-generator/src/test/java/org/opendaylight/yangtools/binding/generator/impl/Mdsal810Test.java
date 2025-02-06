/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class Mdsal810Test {
    @Test
    void testKeyConflict() {
        assertGeneratedNames("key-conflict.yang",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.KeyConflictData",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.Foo$LI",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.Foo$LIKey",
            "org.opendaylight.yang.gen.v1.key.conflict.norev.FooKey$CO");
    }

    @Test
    void testListenerConflict() {
        assertGeneratedNames("listener-conflict.yang",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.ListenerConflictData",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.ListenerConflictListener",
            "org.opendaylight.yang.gen.v1.listener.conflict.norev.Bar");
    }

    @Test
    void testRootConflict() {
        assertGeneratedNames("root-conflict.yang",
            "org.opendaylight.yang.gen.v1.root.conflict.norev.RootConflictData",
            "org.opendaylight.yang.gen.v1.root.conflict.norev.RootConflictData$CO");
    }

    @Test
    void testServiceConflict() {
        assertGeneratedNames("service-conflict.yang",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.ServiceConflictData",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.ServiceConflictService",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.Bar",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.BarInput",
            "org.opendaylight.yang.gen.v1.service.conflict.norev.BarOutput");
    }

    @Test
    void testInputOutputConflict() {
        assertGeneratedNames("io-conflict.yang",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.IoConflictData",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RP",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RPInput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.Foo$RPOutput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.FooInput",
            "org.opendaylight.yang.gen.v1.io.conflict.norev.FooOutput");
    }

    @Test
    void testSchemaCollisions() {
        assertGeneratedNames("schema-conflict.yang",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.SchemaConflictData",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$AD",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$AX",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$CO",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$LI",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$NO",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RP",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RPInput",
            "org.opendaylight.yang.gen.v1.schema.conflict.norev.FooBar$RPOutput");
    }

    private static void assertGeneratedNames(final String yangFile, final String... fqcns) {
        assertEquals(List.of(fqcns),
            DefaultBindingGenerator.generateFor(YangParserTestUtils.parseYangResource("/mdsal-810/" + yangFile))
                .stream().map(type -> type.getIdentifier().toString()).toList());
    }
}
