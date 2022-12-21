/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.DeviateDefinition;
import org.opendaylight.yangtools.yang.model.api.Deviation;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

class Bug7440Test extends AbstractYangTest {
    @Test
    void testRestrictedTypeParentSchemaPathInDeviate() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug7440");

        final Revision revision = Revision.of("2016-12-23");
        final Module foo = schemaContext.findModule("foo", revision).get();
        final Module bar = schemaContext.findModule("bar", revision).get();

        final Collection<? extends Deviation> deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        final Deviation deviation = deviations.iterator().next();

        final Collection<? extends DeviateDefinition> deviates = deviation.getDeviates();
        assertEquals(1, deviates.size());
        final DeviateDefinition deviateReplace = deviates.iterator().next();

        final TypeDefinition<?> deviatedType = deviateReplace.getDeviatedType();
        assertEquals(QName.create(bar.getQNameModule(), "uint32"), deviatedType.getQName());
    }
}
