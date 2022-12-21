/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;

class Bug6972Test extends AbstractYangTest {
    @Test
    void allUnitsShouldBeTheSameInstance() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug6972");
        assertEquals(3, schemaContext.getModules().size());

        final Revision revision = Revision.of("2016-10-20");
        final Module foo = schemaContext.findModule("foo", revision).get();
        final Module bar = schemaContext.findModule("bar", revision).get();
        final Module baz = schemaContext.findModule("baz", revision).get();

        final QName barExportCont = QName.create("bar-ns", "bar-export", revision);
        final QName barFooCont = QName.create("bar-ns", "bar-foo", revision);
        final QName barFooLeaf = QName.create("bar-ns", "foo", revision);

        final UnitsEffectiveStatement unitsBar1 = getEffectiveUnits(bar, barExportCont, barFooLeaf);
        assertSame(unitsBar1, getEffectiveUnits(bar, barFooCont, barFooLeaf));

        final QName bazExportCont = QName.create("baz-ns", "baz-export", revision);
        final QName bazFooCont = QName.create("baz-ns", "baz-foo", revision);
        final QName bazFooLeaf = QName.create("baz-ns", "foo", revision);

        assertSame(unitsBar1, getEffectiveUnits(baz, bazExportCont, bazFooLeaf));
        assertSame(unitsBar1, getEffectiveUnits(baz, bazFooCont, bazFooLeaf));
    }

    private static UnitsEffectiveStatement getEffectiveUnits(final Module module, final QName containerQName,
        final QName leafQName) {
        UnitsEffectiveStatement units = null;

        final ContainerSchemaNode cont = (ContainerSchemaNode) module.getDataChildByName(containerQName);
        assertNotNull(cont);
        final LeafSchemaNode leaf = (LeafSchemaNode) cont.getDataChildByName(leafQName);
        assertNotNull(leaf);

        for (EffectiveStatement<?, ?> effStmt : ((LeafEffectiveStatement) leaf).effectiveSubstatements()) {
            if (effStmt instanceof UnitsEffectiveStatement) {
                units = (UnitsEffectiveStatement) effStmt;
                break;
            }
        }

        return units;
    }
}