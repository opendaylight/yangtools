/*
 * Copyright (c) 2017 Pantheon Technologies s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.collect.Iterables;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

class Bug9242Test extends AbstractYangTest {
    @Test
    void testDeviateReplaceWithUserDefinedTypes() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug9242/");

        final Revision revision = Revision.of("2017-10-13");
        final Module rootModule = schemaContext.findModule("root-module", revision).orElseThrow();
        final Module impModule = schemaContext.findModule("imp-module", revision).orElseThrow();

        TypeDefinition<?> deviatedMyLeafType = null;
        TypeDefinition<?> deviatedMyLeaf2Type = null;

        for (final Deviation deviation : rootModule.getDeviations()) {
            final QName last = Iterables.getLast(deviation.getTargetPath().getNodeIdentifiers());
            if (last.equals(QName.create(impModule.getQNameModule(), "my-leaf"))) {
                deviatedMyLeafType = deviation.getDeviates().iterator().next().getDeviatedType();
            }

            if (last.equals(QName.create(impModule.getQNameModule(), "my-leaf-2"))) {
                deviatedMyLeaf2Type = deviation.getDeviates().iterator().next().getDeviatedType();
            }
        }

        assertNotNull(deviatedMyLeafType);
        assertNotNull(deviatedMyLeaf2Type);

        final LeafSchemaNode myLeaf = (LeafSchemaNode) impModule.getDataChildByName(QName.create(
            impModule.getQNameModule(), "my-leaf"));
        assertSame(deviatedMyLeafType, myLeaf.getType());

        final LeafSchemaNode myLeaf2 = (LeafSchemaNode) impModule.getDataChildByName(QName.create(
            impModule.getQNameModule(), "my-leaf-2"));
        assertSame(deviatedMyLeaf2Type, myLeaf2.getType());
    }
}
