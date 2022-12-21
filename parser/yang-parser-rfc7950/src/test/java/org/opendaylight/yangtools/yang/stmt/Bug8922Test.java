/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

class Bug8922Test extends AbstractYangTest {
    private static final QName MY_CON = QName.create("foo", "my-con");
    private static final QName TARGET = QName.create("foo", "target");

    @Test
    void testAllFeaturesSupported() {
        final var context = assertEffectiveModel("/bugs/bug8922/foo.yang");
        final var findNode = assertInstanceOf(ContainerSchemaNode.class,
            context.findDataTreeChild(TARGET, MY_CON).orElseThrow());
        assertEquals(Optional.of("New description"), findNode.getDescription());

        assertEquals(1, context.findModuleStatements("foo").iterator().next()
            .streamEffectiveSubstatements(FeatureEffectiveStatement.class).count());
    }

    @Test
    void testNoFeatureSupported() throws Exception {
        final var context = StmtTestUtils.parseYangSource("/bugs/bug8922/foo.yang", Set.of());
        assertNotNull(context);
        assertEquals(Optional.empty(), context.findDataTreeChild(TARGET, MY_CON));
        assertTrue(context.getAvailableAugmentations().isEmpty());

        assertEquals(0, context.findModuleStatements("foo").iterator().next()
            .streamEffectiveSubstatements(FeatureEffectiveStatement.class).count());
    }
}
