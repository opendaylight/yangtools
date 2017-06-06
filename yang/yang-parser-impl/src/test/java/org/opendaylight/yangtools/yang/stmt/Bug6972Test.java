/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import org.junit.Ignore;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.LeafEffectiveStatementImpl;
import org.opendaylight.yangtools.yang.parser.stmt.rfc6020.effective.UnitsEffectiveStatementImpl;

public class Bug6972Test {

    @Ignore
    @Test
    public void allUnitsShouldBeTheSameInstance() throws Exception {
        final SchemaContext schemaContext = StmtTestUtils.parseYangSources("/bugs/bug6972");
        assertNotNull(schemaContext);
        assertEquals(3, schemaContext.getModules().size());

        final Date revision = SimpleDateFormatUtil.getRevisionFormat().parse("2016-10-20");

        final Module foo = schemaContext.findModuleByName("foo", revision);
        assertNotNull(foo);
        final Module bar = schemaContext.findModuleByName("bar", revision);
        assertNotNull(bar);
        final Module baz = schemaContext.findModuleByName("baz", revision);
        assertNotNull(baz);

        final QName barExportCont = QName.create("bar-ns", "bar-export", revision);
        final QName barFooCont = QName.create("bar-ns", "bar-foo", revision);
        final QName barFooLeaf= QName.create("bar-ns", "foo", revision);

        final UnitsEffectiveStatementImpl unitsBar1 = getEffectiveUnits(bar, barExportCont, barFooLeaf);
        final UnitsEffectiveStatementImpl unitsBar2 = getEffectiveUnits(bar, barFooCont, barFooLeaf);

        final QName bazExportCont = QName.create("baz-ns", "baz-export", revision);
        final QName bazFooCont = QName.create("baz-ns", "baz-foo", revision);
        final QName bazFooLeaf= QName.create("baz-ns", "foo", revision);

        final UnitsEffectiveStatementImpl unitsBaz1 = getEffectiveUnits(baz, bazExportCont, bazFooLeaf);
        final UnitsEffectiveStatementImpl unitsBaz2 = getEffectiveUnits(baz, bazFooCont, bazFooLeaf);

        assertTrue(unitsBar1 == unitsBar2 && unitsBar1 == unitsBaz1 && unitsBar1 == unitsBaz2);
    }

    private static UnitsEffectiveStatementImpl getEffectiveUnits(final Module module, final QName containerQName,
            final QName leafQName) {
        UnitsEffectiveStatementImpl units = null;

        final ContainerSchemaNode cont = (ContainerSchemaNode) module.getDataChildByName(containerQName);
        assertNotNull(cont);
        final LeafSchemaNode leaf = (LeafSchemaNode) cont.getDataChildByName(leafQName);
        assertNotNull(leaf);

        for (EffectiveStatement<?, ?> effStmt : ((LeafEffectiveStatementImpl) leaf).effectiveSubstatements()) {
            if (effStmt instanceof UnitsEffectiveStatementImpl) {
                units = (UnitsEffectiveStatementImpl) effStmt;
                break;
            }
        }

        return units;
    }
}