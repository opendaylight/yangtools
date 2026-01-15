/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter.defaultInstance;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class YangTextSnippetTest {
    @Test
    void testNotification() {
        assertFormat(YangParserTestUtils.parseYangResource("/bugs/bug2444/yang/notification.yang"));
    }

    @Test
    void testSubmoduleNamespaces() {
        assertFormat(YangParserTestUtils.parseYang("""
            module module1 {
              yang-version "1.1";
              namespace "urn:example:module1";
              prefix "module1";

              include module1submodule1;

              revision "2019-05-17" {
              }

              container cont1 {
                uses submodule-grouping;
              }
            }""", """
            submodule module1submodule1 {
              yang-version "1.1";

              belongs-to "module1" {
                prefix "module1";
              }

              import module2 {
                prefix "module2";
              }

              revision "2019-05-17" {
              }

              grouping submodule-grouping {
                uses module2:grouping1;

                leaf leaf2 {
                  type string;
                  module2:ext1 "param1";
                }
              }
            }""", """
            module module2 {
              yang-version "1.1";
              namespace "urn:example:module2";
              prefix "module2";

              revision "2019-05-17" {
              }

              grouping grouping1 {
                leaf leaf1 {
                  type string;
                }
              }

              extension ext1 {
                argument "parameter";
              }
            }"""));
    }

    private static void assertFormat(final EffectiveModelContext context) {
        for (var module : context.getModuleStatements().values()) {
            assertNotNull(formatModule(module));

            for (var substmt : module.submodules()) {
                assertNotNull(formatSubmodule(substmt));
            }
        }
    }

    private static String formatModule(final ModuleEffectiveStatement stmt) {
        return defaultInstance().toYangTextSnippet(stmt, stmt.requireDeclared()).toString();
    }

    private static String formatSubmodule(final SubmoduleEffectiveStatement stmt) {
        return defaultInstance().toYangTextSnippet(stmt, stmt.requireDeclared()).toString();
    }
}
