/*
 * Copyright (c) 2022 PANTHEON.tech and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang2sources.plugin;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.buildHashCode;
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.toWritableObject;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.concepts.WritableObject;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Artifact serves recognition of modified resources and output files to support incremental builds.
 * The states are persisted in a requested directory.
 */
final class RebuildContext {
    private static final Logger LOG = LoggerFactory.getLogger(RebuildContext.class);

    static final String PERSISTENCE_FILE_NAME = "rebuild-context-cache";

    private final Map<String, ResourceState> configStateMap = new HashMap<>();
    private final Map<QNameModule, ResourceState> moduleStateMap = new HashMap<>();
    private final Path persistenceFile;

    RebuildContext(final File dir) {
        persistenceFile = dir.toPath().resolve(PERSISTENCE_FILE_NAME);
        loadData();
    }

    private void loadData() {
        if (Files.isRegularFile(persistenceFile)) {
            try (var ois = new ObjectInputStream(Files.newInputStream(persistenceFile))) {
                final RebuildContextData data = (RebuildContextData) ois.readObject();
                configStateMap.putAll(data.getConfigStateMap());
                moduleStateMap.putAll(data.getModuleStateMap());
            } catch (IOException | ClassNotFoundException e) {
                LOG.warn("Could not load from rebuild context file", e);
            }
        }
    }

    /**
     * Persists collected state data.
     */
    void persist() {
        try (var oos = new ObjectOutputStream(Files.newOutputStream(persistenceFile))) {
            final RebuildContextData data = new RebuildContextData();
            data.setModuleStateMap(moduleStateMap);
            data.setConfigStateMap(configStateMap);
            oos.writeObject(data);
        } catch (IOException e) {
            LOG.warn("Could not persist rebuild context file", e);
        }
    }

    /**
     * Updates configuration states.
     *
     * @param configMap configuration id to object map
     */
    void setConfigurations(final Map<String, ? extends WritableObject> configMap) {
        requireNonNull(configMap, "configMap should not be null");
        checkArgument(!configMap.isEmpty(), "configmap should not be empty");
        final var newStateMap = configMap.entrySet().stream()
            .collect(Collectors.toMap(
                Entry::getKey,
                entry -> buildResourceState(entry.getValue(), configStateMap.get(entry.getKey()))));
        configStateMap.clear();
        configStateMap.putAll(newStateMap);
    }

    /**
     * Updates module states.
     *
     * @param modules modules
     */
    void setModules(final Collection<Module> modules) {
        requireNonNull(modules, "modules should not be null");
        checkArgument(!modules.isEmpty(), "modules should not be empty");
        final var newStateMap = modules.stream()
            .collect(Collectors.toMap(
                Module::getQNameModule,
                module -> buildResourceState(toWritableObject(module), moduleStateMap.get(module.getQNameModule()))));
        moduleStateMap.clear();
        moduleStateMap.putAll(newStateMap);
    }

    private static ResourceState buildResourceState(final WritableObject object, final ResourceState priorState) {
        final int hashCode = buildHashCode(object);
        return new ResourceState(hashCode, priorState == null || hashCode != priorState.hash());
    }

    /**
     * Indicates the updates for requested configuration.
     *
     * @param configId configuration id
     * @return true if there is any update found in configuration; false otherwise
     */
    boolean isConfigurationContextChanged(final String configId) {
        final var state = configStateMap.get(requireNonNull(configId));
        return state == null || state.modified();
    }

    /**
     * Indicates the updates for effective model context.
     *
     * @return true if there is any update found in any module; false otherwise
     */
    boolean isModuleContextChanged() {
        return moduleStateMap.values().stream().anyMatch(ResourceState::modified);
    }

    private static class RebuildContextData implements Serializable {
        private static final long serialVersionUID = 1L;

        private Map<String, ResourceState> configStateMap = new HashMap<>();
        private Map<QNameModule, ResourceState> moduleStateMap = new HashMap<>();

        public Map<String, ResourceState> getConfigStateMap() {
            return configStateMap;
        }

        public void setConfigStateMap(Map<String, ResourceState> configStateMap) {
            this.configStateMap = configStateMap;
        }

        public Map<QNameModule, ResourceState> getModuleStateMap() {
            return moduleStateMap;
        }

        public void setModuleStateMap(Map<QNameModule, ResourceState> moduleStateMap) {
            this.moduleStateMap = moduleStateMap;
        }
    }

    private record ResourceState(int hash, boolean modified) implements Serializable {
        // Nothing else
    }
}
