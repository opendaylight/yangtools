/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;

class Bug7440Test extends AbstractYangTest {
    @Test
    void testRestrictedTypeParentSchemaPathInDeviate() {
        final var schemaContext = assertEffectiveModelDir("/bugs/bug7440");

        final var revision = Revision.of("2016-12-23");
        final var foo = schemaContext.findModule("foo", revision).get();
        final var bar = schemaContext.findModule("bar", revision).get();

        final var deviations = foo.getDeviations();
        assertEquals(1, deviations.size());
        final var deviation = deviations.iterator().next();

        final var deviates = deviation.getDeviates();
        assertEquals(1, deviates.size());
        final var deviateReplace = deviates.iterator().next();

        final var deviatedType = deviateReplace.getDeviatedType();
        assertEquals(QName.create(bar.getQNameModule(), "uint32"), deviatedType.getQName());
    }
}
