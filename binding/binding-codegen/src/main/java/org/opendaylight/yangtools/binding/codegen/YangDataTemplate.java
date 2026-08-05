/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static org.opendaylight.yangtools.binding.codegen.TypeNames.NONNULL;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.YANGDATANAMEOF_METHOD_NAME;
import static org.opendaylight.yangtools.binding.codegen.YangModuleInfoTemplate.yangModuleInfoOf;
import static org.opendaylight.yangtools.binding.contract.Naming.NAME_STATIC_FIELD_NAME;

import com.google.common.collect.Iterators;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.YangData;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.ParameterizedType;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.api.YangDataArchetype;
import org.opendaylight.yangtools.yang.common.YangDataName;

/**
 * Template for {@link YangData} specializations.
 */
@NonNullByDefault
final class YangDataTemplate extends InterfaceTemplate<YangDataArchetype> implements BuilderTemplate.TargetTemplate {
    private static final JavaTypeName YANG_DATA_NAME = JavaTypeName.create(YangDataName.class);
    private static final ConcreteType YANG_DATA = ConcreteType.ofClass(YangData.class);

    YangDataTemplate(final DataRootArchetype root, final YangDataArchetype archetype) {
        super(root, archetype, DataContainerContract.JAVA, false);
    }

    @Override
    public YangDataTemplate self() {
        return this;
    }

    @Override
    Iterator<? extends Type> extendsTypes() {
        return Iterators.concat(
            Iterators.singletonIterator(ParameterizedType.of(YANG_DATA, archetype)),
            super.extendsTypes());
    }

    @Override
    BlockFragment constants() {
        return bb -> {
            final var yangDataName = archetype.statement().argument();
            final var yangModuleInfo = yangModuleInfoOf(yangDataName.module());
            bb
                .eol("/**")
                .eol(" * Yang Data template name of the statement represented by this class.")
                .eol(" */")
                .at().str(importedName(NONNULL)).sp().str(importedName(YANG_DATA_NAME))
                    .str(" " + NAME_STATIC_FIELD_NAME + " = ").str(importedName(yangModuleInfo))
                    .str("." + YANGDATANAMEOF_METHOD_NAME + "(").jStr(yangDataName.name()).eol(");");
        };
    }
}
