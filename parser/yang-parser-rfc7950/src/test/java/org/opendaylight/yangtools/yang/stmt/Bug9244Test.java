/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.stmt.MinElementsArgument;

class Bug9244Test extends AbstractYangTest {
    @Test
    void testDeviateReplaceOfImplicitSubstatements() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug9244/");

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-10-13")).orElseThrow();
        final var barCont = assertInstanceOf(ContainerSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "bar-cont")));
        assertEquals(Optional.of(Boolean.FALSE), barCont.effectiveConfig());

        final var barLeafList = assertInstanceOf(LeafListSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "bar-leaf-list")));
        final var constraint = barLeafList.getElementCountConstraint().orElseThrow();
        assertEquals(MinElementsArgument.of(5), constraint.getMinElements());
        assertEquals(10, constraint.getMaxElements());

        final var barLeaf = assertInstanceOf(LeafSchemaNode.class,
            barModule.getDataChildByName(QName.create(barModule.getQNameModule(), "bar-leaf")));
        assertTrue(barLeaf.isMandatory());
    }
}
