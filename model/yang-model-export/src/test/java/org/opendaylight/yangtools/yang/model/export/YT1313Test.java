/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1313Test {
    @Test
    public void testSubmoduleImportPrefixes() {
        final var bar = YangParserTestUtils.parseYang("""
            module bar {
              namespace bar;
              prefix bar;

              include bar-one;
              include bar-two;
            }""", """
            submodule bar-one {
              belongs-to bar {
                prefix bar;
              }

              import foo {
                prefix foo1;
              }

              leaf one {
                type foo1:foo;
              }
            }""", """
            submodule bar-two {
              belongs-to bar {
                prefix bar;
              }

              import foo {
                prefix foo2;
              }

              leaf two {
                type foo2:foo;
              }
            }""", """
            module foo {
              namespace foo;
              prefix foo;

              typedef foo {
                type string;
              }
            }""")
            .getModuleStatement(QNameModule.of("bar"));

        final StatementPrefixResolver resolver = StatementPrefixResolver.forModule(bar);
        assertNotNull(resolver);
    }
}
