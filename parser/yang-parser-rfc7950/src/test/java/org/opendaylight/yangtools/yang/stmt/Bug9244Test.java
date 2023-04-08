/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

public class Bug9244Test extends AbstractYangTest {
    @Test
    public void testDeviateReplaceOfImplicitSubstatements() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug9244/");

        final var barModule = schemaContext.findModule("bar", Revision.of("2017-10-13")).orElseThrow();
        final var barCont = (ContainerSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-cont"));
        assertNotNull(barCont);
        assertEquals(Optional.of(Boolean.FALSE), barCont.effectiveConfig());

        final var barLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-leaf-list"));
        assertNotNull(barLeafList);
        final var constraint = barLeafList.getElementCountConstraint().orElseThrow();
        assertEquals((Object) 5, constraint.getMinElements());
        assertEquals((Object) 10, constraint.getMaxElements());

        final var barLeaf = (LeafSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-leaf"));
        assertNotNull(barLeaf);
        assertTrue(barLeaf.isMandatory());
    }
}
