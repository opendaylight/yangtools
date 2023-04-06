/*
 * Copyright (c) 2023 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.Set;
import org.junit.Test;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yang.gen.v1.mdsal767.norev.$YangModuleInfoImpl;
import org.opendaylight.yang.gen.v1.mdsal767.norev.Mdsal767Data;
import org.opendaylight.yang.gen.v1.mdsal767.norev.One$F;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.impl.DefaultYangParserFactory;

public class ModuleInfoSnapshotBuilderTest {
    private static final YangParserFactory PARSER_FACTORY = new DefaultYangParserFactory();

    @Test
    public void testModuleRegistration() throws YangParserException {
        final ModuleInfoSnapshotBuilder snapshotBuilder = new ModuleInfoSnapshotBuilder(PARSER_FACTORY);
        snapshotBuilder.add($YangModuleInfoImpl.getInstance());
        snapshotBuilder.addModuleFeatures(Mdsal767Data.class, Set.of(One$F.VALUE));

        final ModuleInfoSnapshot snapshot = snapshotBuilder.build();
        final EffectiveModelContext modelContext = snapshot.getEffectiveModelContext();
        final Map<QNameModule, ModuleEffectiveStatement> modules = modelContext.getModuleStatements();
        final ModuleEffectiveStatement module = modules.get(QNameModule.create(XMLNamespace.of("mdsal767")));
        assertEquals(1, module.features().size());
        final FeatureEffectiveStatement feature = module.features().stream().findAny().orElseThrow();
        assertEquals(QName.create("mdsal767", "one"), feature.argument());
    }
}

