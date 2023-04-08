/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.stmt.AbstractYangTest;

class ElementCountConstraintsTest extends AbstractYangTest {
    @Test
    void testElementCountConstraints() {
        final var context = assertEffectiveModel("/constraint-definitions-test/foo.yang");

        final var testModule = context.findModule("foo", Revision.of("2016-09-20")).orElseThrow();
        final var constraints1 = assertInstanceOf(LeafListSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "constrained-leaf-list-1")))
            .getElementCountConstraint().orElseThrow();

        var constraints2 = assertInstanceOf(LeafListSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "constrained-leaf-list-2")))
            .getElementCountConstraint().orElseThrow();

        assertEquals(constraints1.hashCode(), constraints2.hashCode());
        assertEquals(constraints1, constraints2);

        var constraints3 = assertInstanceOf(LeafListSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "constrained-leaf-list-3")))
            .getElementCountConstraint().orElseThrow();

        assertNotEquals(constraints2.hashCode(), constraints3.hashCode());
        assertNotEquals(constraints2, constraints3);

        var constraints4 = assertInstanceOf(LeafListSchemaNode.class,
            testModule.getDataChildByName(QName.create(testModule.getQNameModule(), "constrained-leaf-list-4")))
            .getElementCountConstraint().orElseThrow();

        assertNotEquals(constraints3.hashCode(), constraints4.hashCode());
        assertNotEquals(constraints3, constraints4);

        assertEquals("ElementCountConstraint{minElements=50, maxElements=100}", constraints4.toString());
    }
}