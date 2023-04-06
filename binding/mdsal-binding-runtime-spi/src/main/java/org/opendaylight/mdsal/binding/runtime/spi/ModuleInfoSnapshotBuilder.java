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
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimaps;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.mdsal.binding.spec.reflect.BindingReflections;
import org.opendaylight.yangtools.yang.binding.BindingObject;
import org.opendaylight.yangtools.yang.binding.DataRoot;
import org.opendaylight.yangtools.yang.binding.YangFeature;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.repo.api.FeatureSet;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.opendaylight.yangtools.yang.parser.api.YangParser;
import org.opendaylight.yangtools.yang.parser.api.YangParserException;
import org.opendaylight.yangtools.yang.parser.api.YangParserFactory;
import org.opendaylight.yangtools.yang.parser.api.YangSyntaxErrorException;

@Beta
public final class ModuleInfoSnapshotBuilder {
    private final SetMultimap<Class<? extends DataRoot>, YangFeature<?, ?>> moduleFeatures = HashMultimap.create();
    private final Set<YangModuleInfo> moduleInfos = new HashSet<>();
    private final YangParserFactory parserFactory;

    public ModuleInfoSnapshotBuilder(final YangParserFactory parserFactory) {
        this.parserFactory = requireNonNull(parserFactory);
    }

    @SuppressWarnings("checkstyle:illegalCatch")
    public @NonNull ModuleInfoSnapshotBuilder add(final Class<? extends BindingObject> clazz) {
        final YangModuleInfo moduleInfo;
        try {
            moduleInfo = BindingReflections.getModuleInfo(clazz);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
            throw new IllegalStateException("Failed to introspect " + clazz, e);
        }

        return add(moduleInfo);
    }

    @SuppressWarnings("unchecked")
    public @NonNull ModuleInfoSnapshotBuilder add(final Class<? extends BindingObject>... classes) {
        for (Class<? extends BindingObject> clazz : classes) {
            add(clazz);
        }
        return this;
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

    public <R extends @NonNull DataRoot> @NonNull ModuleInfoSnapshotBuilder addModuleFeatures(final Class<R> module,
            final Set<? extends YangFeature<?, R>> supportedFeatures) {
        moduleFeatures.putAll(requireNonNull(module), ImmutableList.copyOf(supportedFeatures));
        return this;
    }

    /**
     * Build {@link ModuleInfoSnapshot} from all {@code moduleInfos} in this builder.
     *
     * @return Resulting {@link ModuleInfoSnapshot}
     * @throws YangParserException if parsing any of the {@link YangModuleInfo} instances fails
     */
    public @NonNull ModuleInfoSnapshot build() throws YangParserException {
        final YangParser parser = parserFactory.createParser();

        final Map<SourceIdentifier, YangModuleInfo> mappedInfos = new HashMap<>();
        final Map<String, ClassLoader> classLoaders = new HashMap<>();
        for (YangModuleInfo info : moduleInfos) {
            final YangTextSchemaSource source = ModuleInfoSnapshotResolver.toYangTextSource(info);
            mappedInfos.put(source.getIdentifier(), info);

            final Class<?> infoClass = info.getClass();
            classLoaders.put(Naming.getModelRootPackageName(infoClass.getPackage().getName()),
                infoClass.getClassLoader());

            try {
                parser.addSource(source);
            } catch (YangSyntaxErrorException | IOException e) {
                throw new YangParserException("Failed to add source for " + info, e);
            }
        }

        if (!moduleFeatures.isEmpty()) {
            final var featuresByModule =
                ImmutableMap.<QNameModule, ImmutableSet<String>>builderWithExpectedSize(moduleFeatures.size());
            for (var entry : Multimaps.asMap(moduleFeatures).entrySet()) {
                featuresByModule.put(BindingReflections.getQNameModule(entry.getKey()),
                    entry.getValue().stream().map(YangFeature::qname).map(QName::getLocalName).sorted()
                        .collect(ImmutableSet.toImmutableSet()));
            }
            parser.setSupportedFeatures(new FeatureSet(featuresByModule.build()));
        }

        return new DefaultModuleInfoSnapshot(parser.buildEffectiveModel(), mappedInfos, classLoaders);
    }
}
