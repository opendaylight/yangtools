/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang2sources.plugin;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang2sources.plugin.Util.moduleToIdentifier;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.hash.HashingInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Artifact serves recognition of modified modules between builds to support gradual builds.
 * The source states are persisted in project build directory.
 */
class RebuildContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebuildContext.class);

    private static final String PERSISTENCE_FILE_NAME = "rebuild-context-cache";
    private static final HashFunction HASH_FUNCTION = Hashing.crc32c();

    private final File persistenceFile;
    private final Map<SourceIdentifier, SourceState> sourceStateMap;

    RebuildContext(final File dir) {
        requireNonNull(dir, "dir should not be null");
        persistenceFile = new File(dir, PERSISTENCE_FILE_NAME);
        sourceStateMap = load(persistenceFile).getSourceStateMap();
    }

    private RebuildContextData load(final File file) {
        if (persistenceFile.exists()) {
            try (FileInputStream fis = new FileInputStream(persistenceFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                return (RebuildContextData) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.warn("Could not load rebuild context file", e);
            }
        }
        return new RebuildContextData();
    }

    /**
     * Persists resources states collected during the build.
     */
    void persist() {
        try (FileOutputStream fos = new FileOutputStream(persistenceFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            final RebuildContextData data = new RebuildContextData();
            data.setSourceStateMap(sourceStateMap);
            oos.writeObject(data);
        } catch (IOException e) {
            LOGGER.warn("Could not persist rebuild context file", e);
        }
    }

    /**
     * Marks modified resources based on hash code difference from prior build.
     *
     * @param yangSources project yang resources
     */
    void updateSourceStates(final Collection<YangTextSchemaSource> yangSources) {
        if (yangSources == null) {
            return;
        }
        yangSources.forEach(source -> {
            final SourceState sourceState = sourceStateMap
                    .computeIfAbsent(source.getIdentifier(), key -> new SourceState());
            final String hashCode = buildHashCode(source);
            // set flag if hashCode is updated
            sourceState.setModified(hashCode == null || !hashCode.equals(sourceState.getHashCode()));
            sourceState.setHashCode(hashCode);
        });
    }

    /**
     * Updates each module dependency on yang resources, including submodules and augmentations.
     *
     * @param modules project modules
     */
    void updateDependencies(final Collection<Module> modules) {
        // set submodules as dependencies for modules the ones belong to
        modules.forEach(module -> {
            final SourceIdentifier moduleId = moduleToIdentifier(module);
            final SourceState moduleState = sourceStateMap.computeIfAbsent(moduleId, key -> new SourceState());
            moduleState.setNamespace(module.getNamespace());
            moduleState.getDependencies().clear(); // clear existing
            module.getSubmodules()
                    .forEach(submodule -> moduleState.getDependencies().add(moduleToIdentifier(submodule)));
        });
        // set modules as dependencies for those being augmented
        modules.forEach(module -> {
            final SourceIdentifier moduleId = moduleToIdentifier(module);
            module.getAugmentations().forEach(
                    augmentation -> {
                        final XMLNamespace targetNamespace =
                                augmentation.getTargetPath().firstNodeIdentifier().getNamespace();
                        final Optional<SourceState> targetStateOpt = sourceStateMap.values().stream()
                                .filter(state -> Objects.equals(state.getNamespace(), targetNamespace)).findFirst();
                        if (targetStateOpt.isEmpty()) {
                            LOGGER.warn("Augmentation within module {} targets namespace {} which "
                                    + "cannot be found in project files", moduleId, targetNamespace);
                        } else {
                            targetStateOpt.get().getDependencies().add(moduleId);
                        }
                    });
        });
    }

    File getPersistenceFile() {
        return persistenceFile;
    }

    boolean isModified(final ModuleLike module) {
        final SourceIdentifier moduleId = moduleToIdentifier(module);
        return isModified(moduleId, moduleId);
    }

    private boolean isModified(final SourceIdentifier moduleId, final SourceIdentifier topModuleId) {
        if (!sourceStateMap.containsKey(moduleId)) {
            return true;
        }
        final SourceState sourceState = sourceStateMap.get(moduleId);
        if (sourceState.isModified()) {
            return true;
        }
        for (final SourceIdentifier dependencyId : sourceState.getDependencies()) {
            if (!dependencyId.equals(topModuleId) // prevent circular dependency check
                    && isModified(dependencyId, topModuleId)) {
                return true;
            }
        }
        return false;
    }

    private static @Nullable String buildHashCode(final YangTextSchemaSource source) {
        requireNonNull(source);
        try (InputStream is = source.openStream();
             HashingInputStream his = new HashingInputStream(HASH_FUNCTION, is)
        ) {
            his.readAllBytes();
            return his.hash().toString();
        } catch (final IOException e) {
            LOGGER.warn("Could not build hash for resource {}", source.getIdentifier().toYangFilename(), e);
            return null;
        }
    }

    private static class RebuildContextData implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<SourceIdentifier, SourceState> sourceStateMap = new HashMap<>();

        public Map<SourceIdentifier, SourceState> getSourceStateMap() {
            return sourceStateMap;
        }

        public void setSourceStateMap(Map<SourceIdentifier, SourceState> sourceStateMap) {
            this.sourceStateMap = sourceStateMap;
        }
    }

    private static class SourceState implements Serializable {
        private static final long serialVersionUID = 1L;

        private String hashCode;
        private XMLNamespace namespace;
        private boolean isModified = true;
        private Set<SourceIdentifier> dependencies = new HashSet<>();

        public String getHashCode() {
            return hashCode;
        }

        public void setHashCode(final @NonNull String hashCode) {
            this.hashCode = hashCode;
        }

        public XMLNamespace getNamespace() {
            return namespace;
        }

        public void setNamespace(XMLNamespace namespace) {
            this.namespace = namespace;
        }

        public boolean isModified() {
            return isModified;
        }

        public void setModified(boolean modified) {
            isModified = modified;
        }

        public Set<SourceIdentifier> getDependencies() {
            return dependencies;
        }

    }
}
