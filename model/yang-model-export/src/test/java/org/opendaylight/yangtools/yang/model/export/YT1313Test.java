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
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YT1313Test {
    private static final String BAR_YANG = """
        module bar {
          namespace bar;
          prefix bar;
          include bar-one;
          include bar-two;
        }""";
    private static final String BAR_ONE_YANG = """
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
        }""";
    private static final String BAR_TWO_YANG = """
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
        }""";
    private static final String FOO_YANG = """
        module foo {
          namespace foo;
          prefix foo;
          typedef foo {
            type string;
          }
        }""";

    @Test
    public void testSubmoduleImportPrefixes() {
        final var bar = YangParserTestUtils.parseYang(BAR_YANG, BAR_ONE_YANG, BAR_TWO_YANG, FOO_YANG)
            .getModuleStatement(QNameModule.create(XMLNamespace.of("bar")));

        final StatementPrefixResolver resolver = StatementPrefixResolver.forModule(bar);
        assertNotNull(resolver);
    }
}
