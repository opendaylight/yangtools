/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.concepts.CheckedBuilder;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.parser.api.YangParser;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.model.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.model.parser.api.YangSyntaxErrorException;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@Beta
public final class ModuleInfoSnapshotBuilder implements CheckedBuilder<ModuleInfoSnapshot, YangParserException> {
    private final Set<YangModuleInfo> moduleInfos = new HashSet<>();
    private final YangParserFactory parserFactory;

    public ModuleInfoSnapshotBuilder(final YangParserFactory parserFactory) {
        this.parserFactory = requireNonNull(parserFactory);
    }

    public @NonNull ModuleInfoSnapshotBuilder add(final YangModuleInfo info) {
        ModuleInfoSnapshotResolver.flatDependencies(moduleInfos, info);
        return this;
    }

    public @NonNull ModuleInfoSnapshotBuilder add(final YangModuleInfo... infos) {
        for (YangModuleInfo info : infos) {
            add(info);
        }
        return this;
    }

    public @NonNull ModuleInfoSnapshotBuilder add(final Iterable<? extends YangModuleInfo> infos) {
        for (YangModuleInfo info : infos) {
            add(info);
        }
        return this;
    }

    @Override
    public ModuleInfoSnapshot build() throws YangParserException {
        final YangParser parser = parserFactory.createParser();
        final Map<SourceIdentifier, YangModuleInfo> mappedInfos = new HashMap<>();
        final Map<String, ClassLoader> classLoaders = new HashMap<>();
        for (YangModuleInfo info : moduleInfos) {
            final YangTextSchemaSource source = ModuleInfoSnapshotResolver.toYangTextSource(info);
            mappedInfos.put(source.getIdentifier(), info);

            final Class<?> infoClass = info.getClass();
            classLoaders.put(BindingReflections.getModelRootPackageName(infoClass.getPackage()),
                infoClass.getClassLoader());

            try {
                parser.addSource(source);
            } catch (YangSyntaxErrorException | IOException e) {
                throw new YangParserException("Failed to add source for " + info, e);
            }
        }

        return new DefaultModuleInfoSnapshot(parser.buildEffectiveModel(), mappedInfos, classLoaders);
    }
}
