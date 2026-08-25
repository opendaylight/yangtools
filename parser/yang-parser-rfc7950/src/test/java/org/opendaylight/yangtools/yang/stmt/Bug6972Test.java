/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.stmt.UnitsEffectiveStatement;

class Bug6972Test extends AbstractYangTest {
    @Test
    void allUnitsShouldBeTheSameInstance() {
        final var modelContext = assertEffectiveModelDir("/bugs/bug6972");
        assertEquals(3, modelContext.getModuleStatements().size());

        final var revision = Revision.of("2016-10-20");
        assertThat(modelContext.findModule("foo", revision)).isPresent();
        final var bar = modelContext.findModule("bar", revision).orElseThrow();
        final var baz = modelContext.findModule("baz", revision).orElseThrow();

        final var barExportCont = QName.create("bar-ns", "bar-export", revision);
        final var barFooCont = QName.create("bar-ns", "bar-foo", revision);
        final var barFooLeaf = QName.create("bar-ns", "foo", revision);

        final var unitsBar1 = getEffectiveUnits(bar, barExportCont, barFooLeaf);
        assertSame(unitsBar1, getEffectiveUnits(bar, barFooCont, barFooLeaf));

        final var bazExportCont = QName.create("baz-ns", "baz-export", revision);
        final var bazFooCont = QName.create("baz-ns", "baz-foo", revision);
        final var bazFooLeaf = QName.create("baz-ns", "foo", revision);

        assertSame(unitsBar1, getEffectiveUnits(baz, bazExportCont, bazFooLeaf));
        assertSame(unitsBar1, getEffectiveUnits(baz, bazFooCont, bazFooLeaf));
    }

    private static UnitsEffectiveStatement getEffectiveUnits(final Module module, final QName containerQName,
            final QName leafQName) {
        final var cont = assertInstanceOf(ContainerSchemaNode.class, module.getDataChildByName(containerQName));
        return assertInstanceOf(LeafSchemaNode.class, cont.getDataChildByName(leafQName)).asEffectiveStatement()
            .unitsStatement();
    }
}
