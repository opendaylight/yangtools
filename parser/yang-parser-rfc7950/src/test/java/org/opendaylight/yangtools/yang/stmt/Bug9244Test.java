/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.ElementCountConstraint;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;

class Bug9244Test extends AbstractYangTest {
    @Test
    void testDeviateReplaceOfImplicitSubstatements() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug9244/");

        final Module barModule = schemaContext.findModule("bar", Revision.of("2017-10-13")).get();
        final ContainerSchemaNode barCont = (ContainerSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "bar-cont"));
        assertEquals(Optional.of(Boolean.FALSE), barCont.effectiveConfig());

        final LeafListSchemaNode barLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "bar-leaf-list"));
        final ElementCountConstraint constraint = barLeafList.getElementCountConstraint().get();
        assertEquals(5, constraint.getMinElements());
        assertEquals(10, constraint.getMaxElements());

        final LeafSchemaNode barLeaf = (LeafSchemaNode) barModule.getDataChildByName(
            QName.create(barModule.getQNameModule(), "bar-leaf"));
        assertTrue(barLeaf.isMandatory());
    }
}
