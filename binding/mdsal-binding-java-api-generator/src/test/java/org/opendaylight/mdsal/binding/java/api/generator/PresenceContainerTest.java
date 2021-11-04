/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opendaylight.mdsal.binding.model.api.GeneratedType;
import org.opendaylight.mdsal.binding.model.api.YangSourceDefinition;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class PresenceContainerTest {
    private static final QName DIRECTORY_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "directory");
    private static final QName USER_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "user");
    private static final QName SCP_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "scp");
    private static final QName DATA_QNAME = QName.create("urn:opendaylight:presence-container",
            "2022-03-17", "data");

    private static Module module;

    @Mock
    GeneratedType type;

    @BeforeClass
    public static void beforeClass() {
        final var context = YangParserTestUtils.parseYangResource("/presence-container.yang");
        module = context.findModule(XMLNamespace.of("urn:opendaylight:presence-container"), Revision.of("2022-03-17"))
                .orElseThrow();
    }

    /**
     * Test that type which is NOT container is NOT recognized as non-presence container.
     */
    @Test
    public void nonContainerIsNonPresenceContainerTest() {
        final var userList = module.findDataTreeChild(DIRECTORY_QNAME, USER_QNAME).orElseThrow();
        final var definition = YangSourceDefinition.of(module, userList);
        doReturn(definition).when(type).getYangSourceDefinition();
        assertFalse(GeneratorUtil.isNonPresenceContainer(type));
    }

    /**
     * Test that presence container is NOT recognized as non-presence container.
     */
    @Test
    public void presenceContainerIsNonPresenceContainerTest() {
        final var scpContainer = module.findDataTreeChild(DIRECTORY_QNAME, SCP_QNAME).orElseThrow();
        final var definition = YangSourceDefinition.of(module, scpContainer);
        doReturn(definition).when(type).getYangSourceDefinition();
        assertFalse(GeneratorUtil.isNonPresenceContainer(type));
    }

    /**
     * Test that non-presence container IS recognized as non-presence container.
     */
    @Test
    public void nonPresenceContainerIsNonPresenceContainerTest() {
        final var dataContainer = module.findDataTreeChild(DIRECTORY_QNAME, DATA_QNAME).orElseThrow();
        final var definition = YangSourceDefinition.of(module, dataContainer);
        doReturn(definition).when(type).getYangSourceDefinition();
        assertTrue(GeneratorUtil.isNonPresenceContainer(type));
    }
}
