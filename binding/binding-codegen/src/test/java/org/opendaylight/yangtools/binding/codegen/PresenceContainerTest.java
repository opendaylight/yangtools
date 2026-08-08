/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.ContainerObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.EntryObjectArchetype;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ListEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class PresenceContainerTest {
    private static final QName DIRECTORY_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "directory");
    private static final QName USER_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "user");
    private static final QName SCP_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "scp");
    private static final QName DATA_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "data");

    private static Module MODULE;

    @BeforeAll
    static void beforeClass() {
        final var context = YangParserTestUtils.parseYangResource("/presence-container.yang");
        MODULE = context.findModule(XMLNamespace.of("urn:opendaylight:presence-container"), Revision.of("2022-03-17"))
                .orElseThrow();
    }

    /**
     * Test that type which is NOT container is NOT recognized as non-presence container.
     */
    @Test
    void nonContainerIsNonPresenceContainerTest() {
        final var userList = assertInstanceOf(ListEffectiveStatement.class,
            MODULE.findDataTreeChild(DIRECTORY_QNAME, USER_QNAME).orElseThrow());
        final var key = userList.keyStatement();
        assertNotNull(key);
        final var parentName = TypeName.of("foo", "parent");
        final var keyName = TypeName.of("foo", "key");
        final var listName = TypeName.of("foo", "list");
        final var archetype = EntryObjectArchetype.of(listName, userList, parentName, keyName, List.of(), List.of(),
            List.of());

        assertFalse(BuilderTemplate.isNonPresenceContainer(archetype));
    }

    /**
     * Test that presence container is NOT recognized as non-presence container.
     */
    @Test
    void presenceContainerIsNonPresenceContainerTest() {
        final var scpContainer = assertInstanceOf(ContainerEffectiveStatement.class,
            MODULE.findDataTreeChild(DIRECTORY_QNAME, SCP_QNAME).orElseThrow());
        assertFalse(BuilderTemplate.isNonPresenceContainer(ContainerObjectArchetype.of(
            TypeName.of("foo", "foo"), scpContainer, TypeName.of("foo", "parent"), List.of(), List.of(), List.of())));
    }

    /**
     * Test that non-presence container IS recognized as non-presence container.
     */
    @Test
    void nonPresenceContainerIsNonPresenceContainerTest() {
        final var dataContainer = assertInstanceOf(ContainerEffectiveStatement.class,
            MODULE.findDataTreeChild(DIRECTORY_QNAME, DATA_QNAME).orElseThrow());
        assertTrue(BuilderTemplate.isNonPresenceContainer(ContainerObjectArchetype.of(
            TypeName.of("foo", "foo"), dataContainer, TypeName.of("foo", "parent"), List.of(), List.of(), List.of())));
    }
}
