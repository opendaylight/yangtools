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

public class YangTextSnippetTest {
    @Test
    public void testNotification() {
        assertFormat(YangParserTestUtils.parseYangResource("/bugs/bug2444/yang/notification.yang"));
    }

    @Test
    public void testSubmoduleNamespaces() throws Exception {
        assertFormat(YangParserTestUtils.parseYangResourceDirectory("/bugs/yt992"));
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
        return defaultInstance().toYangTextSnippet(stmt, stmt.getDeclared()).toString();
    }

    private static String formatSubmodule(final SubmoduleEffectiveStatement stmt) {
        return defaultInstance().toYangTextSnippet(stmt, stmt.getDeclared()).toString();
    }
}
