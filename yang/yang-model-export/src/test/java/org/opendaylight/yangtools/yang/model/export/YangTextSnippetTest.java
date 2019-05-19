/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter.defaultInstance;

import java.util.Collection;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement.NameToEffectiveSubmoduleNamespace;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YangTextSnippetTest {


    @Test
    public void testNotification() {
        final SchemaContext schema = YangParserTestUtils.parseYangResource("/bugs/bug2444/yang/notification.yang");
        assertFormat(schema.getModules());
    }

    @Test
    public void testSubmoduleNamespaces() throws Exception {
        SchemaContext schema = YangParserTestUtils.parseYangResourceDirectory("/bugs/yt992");
        assertFormat(schema.getModules());
    }

    private static void assertFormat(final Collection<Module> modules) {
        for (Module module : modules) {
            assertTrue(module instanceof ModuleEffectiveStatement);
            final ModuleEffectiveStatement stmt = (ModuleEffectiveStatement) module;
            assertNotNull(formatModule(stmt));

            for (SubmoduleEffectiveStatement substmt : stmt.getAll(NameToEffectiveSubmoduleNamespace.class).values()) {
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
