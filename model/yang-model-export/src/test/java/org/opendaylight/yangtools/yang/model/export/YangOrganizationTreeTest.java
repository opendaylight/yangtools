/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.export;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.yang.model.export.DeclaredStatementFormatter.defaultInstance;

import java.util.Collection;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Revision;
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
    public void testSubmoduleNamespaces() {
        SchemaContext schema = YangParserTestUtils.parseYangResourceDirectory("/bugs/yt992");
        assertFormat(schema.getModules());
    }

    @Test
    @Ignore
    public void testNACMModule() {
        final SchemaContext schema = YangParserTestUtils.parseYangResourceDirectory("/yang");
        final var nacmModule = schema.findModule("ietf-netconf-acm", Revision.of("2018-02-14")).get();
        final ModuleEffectiveStatement stmt = nacmModule.asEffectiveStatement();
        final String formattedTree = defaultInstance().toYangOrganizationTree(stmt,
                stmt.getDeclared(),8).toString();

        final String expectedOrgTree =
            """
            module: ietf-netconf-acm
              +--rw nacm
                 +--rw enable-nacm?              boolean
                 +--rw read-default?             action-type
                 +--rw write-default?            action-type
                 +--rw exec-default?             action-type
                 +--rw enable-external-groups?   boolean
                 +--ro denied-operations         yang:zero-based-counter32
                 +--ro denied-data-writes        yang:zero-based-counter32
                 +--ro denied-notifications      yang:zero-based-counter32
                 +--rw groups
                 |  +--rw group* [name]
                 |     +--rw name         group-name-type
                 |     +--rw user-name*   user-name-type
                 +--rw rule-list* [name]
                    +--rw name     string
                    +--rw group*   union
                    +--rw rule* [name]
                       +--rw name                 string
                       +--rw module-name?         union
                       +--rw (rule-type)?
                       |  +--:(protocol-operation)
                       |  |  +--rw rpc-name?   union
                       |  +--:(notification)
                       |  |  +--rw notification-name?   union
                       |  +--:(data-node)
                       |     +--rw path    node-instance-identifier
                       +--rw access-operations?   union
                       +--rw action               action-type
                       +--rw comment?             string
            """;
        assertEquals(formattedTree, expectedOrgTree);
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
