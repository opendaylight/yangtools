/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.spi.SimpleSchemaContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YT691Test {
    @Test
    void testGetAllModuleIdentifiers() {
        final var foo = new SourceIdentifier("foo", "2016-01-01");
        final var sub1Foo = new SourceIdentifier("sub1-foo", "2016-01-01");
        final var sub2Foo = new SourceIdentifier("sub2-foo", "2016-01-01");
        final var bar = new SourceIdentifier("bar", "2016-01-01");
        final var sub1Bar = new SourceIdentifier("sub1-bar", "2016-01-01");
        final var baz = new SourceIdentifier("baz", "2016-01-01");
        final var context = YangParserTestUtils.parseYang("""
            module bar {
              namespace "bar";
              prefix bar;
              revision 2016-01-01;

              include sub1-bar;

              leaf bar-leaf {
                type string;
              }
            }""", """
            submodule sub1-bar {
              belongs-to bar {
                prefix bar;
              }

              revision 2016-01-01;

              leaf sub1-bar-leaf {
                type string;
              }
            }""", """
            module baz {
              namespace "baz";
              prefix baz;
              revision 2016-01-01;

              leaf baz-leaf {
                type string;
              }
            }""", """
            module foo {
              namespace "foo";
              prefix foo;
              revision 2016-01-01;

              include sub1-foo;
              include sub2-foo;

              leaf foo-leaf {
                type string;
              }
            }""", """
            submodule sub1-foo {
              belongs-to foo {
                prefix foo;
              }

              revision 2016-01-01;

              leaf sub1-foo-leaf {
                type string;
              }
            }""", """
            submodule sub2-foo {
              belongs-to foo {
                prefix foo;
              }

              revision 2016-01-01;

              leaf sub2-foo-leaf {
                type string;
              }
            }""");
        final var allModuleIdentifiers = SchemaContextUtil.getConstituentModuleIdentifiers(context);
        assertEquals(6, allModuleIdentifiers.size());
        final var allModuleIdentifiersResolved = SchemaContextUtil.getConstituentModuleIdentifiers(
                SimpleSchemaContext.forModules(context.getModules()));
        assertEquals(6, allModuleIdentifiersResolved.size());
        assertEquals(allModuleIdentifiersResolved, allModuleIdentifiers);
        assertEquals(Set.of(foo, sub1Foo, sub2Foo, bar, sub1Bar, baz), allModuleIdentifiers);
        assertTrue(allModuleIdentifiers.contains(foo));
    }
}