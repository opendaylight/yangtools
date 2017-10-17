/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public class Bug9244Test {

    @Test
    public void testDeviateReplaceOfImplicitSubstatements() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/bugs/bug9244/");
        assertNotNull(schemaContext);

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2017-10-13");

        final Module barModule = schemaContext.findModuleByName("bar", revision);
        assertNotNull(barModule);

        final ContainerSchemaNode barCont = (ContainerSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-cont"));
        assertNotNull(barCont);
        assertFalse(barCont.isConfiguration());

        final LeafListSchemaNode barLeafList = (LeafListSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-leaf-list"));
        assertNotNull(barLeafList);
        assertEquals(5, barLeafList.getConstraints().getMinElements().intValue());
        assertEquals(10, barLeafList.getConstraints().getMaxElements().intValue());

        final LeafSchemaNode barLeaf = (LeafSchemaNode) barModule.getDataChildByName(
                QName.create(barModule.getQNameModule(), "bar-leaf"));
        assertNotNull(barLeaf);
        assertTrue(barLeaf.getConstraints().isMandatory());
    }
}
