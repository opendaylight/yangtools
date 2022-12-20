/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter.defaultInstance;

import java.util.Collection;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

public class YangOrganizationTreeTest {
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

    @Test
    @Ignore
    public void testNACMModule() {
        SchemaContext schema = YangParserTestUtils.parseYangResource("/yang/ietf-netconf-acm@2018-02-14.yang");
        assertFormat(schema.getModules());
    }

    private static void assertFormat(final Collection<? extends Module> modules) {
        for (Module module : modules) {
            final ModuleEffectiveStatement stmt = module.asEffectiveStatement();
            assertNotNull(formatModule(stmt));

            for (SubmoduleEffectiveStatement substmt : stmt.submodules()) {
                assertNotNull(formatSubmodule(substmt));
            }
        }
    }

    private static String formatModule(final ModuleEffectiveStatement stmt) {
        final String formattedTree = defaultInstance().toYangOrganizationTree(stmt, stmt.getDeclared(),2).toString();
        return formattedTree;
    }

    private static String formatSubmodule(final SubmoduleEffectiveStatement stmt) {
        final String formattedTree = defaultInstance().toYangOrganizationTree(stmt, stmt.getDeclared(),2).toString();
        return formattedTree;
    }
}
