/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yang.gen.v1.mdsal767.norev.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.mdsal767.norev.Mdsal767Data;
import org.opendaylight.yang.gen.v1.mdsal767.norev.One$F;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;

class ModuleInfoSnapshotBuilderTest {
    @Test
    void testModuleRegistration() throws Exception {
        final var snapshotBuilder = new ModuleInfoSnapshotBuilder(new DefaultYangParserFactory());
        snapshotBuilder.add($YangModuleInfoImpl.getInstance());
        snapshotBuilder.addModuleFeatures(Mdsal767Data.class, Set.of(One$F.VALUE));

        final var snapshot = snapshotBuilder.build();
        final var modelContext = snapshot.modelContext();
        final var modules = modelContext.getModuleStatements();
        final var module = modules.get(QNameModule.create(XMLNamespace.of("mdsal767")));
        assertEquals(1, module.features().size());
        final var feature = module.features().stream().findAny().orElseThrow();
        assertEquals(QName.create("mdsal767", "one"), feature.argument());
    }
}

