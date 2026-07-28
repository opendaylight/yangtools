/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opendaylight.yangtools.binding.model.api.ContainerArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@ExtendWith(MockitoExtension.class)
class PresenceContainerTest {
    private static final QName DIRECTORY_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "directory");
    private static final QName SCP_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "scp");
    private static final QName DATA_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "data");
    private static final JavaTypeName FOO = JavaTypeName.create("foo", "foo");

    private static Module module;

    @BeforeAll
    static void beforeClass() {
        final var context = YangParserTestUtils.parseYangResource("/presence-container.yang");
        module = context.findModule(XMLNamespace.of("urn:opendaylight:presence-container"), Revision.of("2022-03-17"))
                .orElseThrow();
    }

    /**
     * Test that presence container is NOT recognized as non-presence container.
     */
    @Test
    void presenceContainerIsNonPresenceContainerTest() {
        final var scpContainer = assertInstanceOf(ContainerEffectiveStatement.class,
            module.findDataTreeChild(DIRECTORY_QNAME, SCP_QNAME).orElseThrow());
        assertInstanceOf(ContainerArchetype.Presence.class, ContainerArchetype.builder(FOO, scpContainer).build());
    }

    /**
     * Test that non-presence container IS recognized as non-presence container.
     */
    @Test
    void nonPresenceContainerIsNonPresenceContainerTest() {
        final var dataContainer = assertInstanceOf(ContainerEffectiveStatement.class,
            module.findDataTreeChild(DIRECTORY_QNAME, DATA_QNAME).orElseThrow());
        assertInstanceOf(ContainerArchetype.Structural.class, ContainerArchetype.builder(FOO, dataContainer).build());
    }
}
