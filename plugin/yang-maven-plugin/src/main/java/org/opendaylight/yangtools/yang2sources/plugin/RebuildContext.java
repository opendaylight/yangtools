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
import static org.opendaylight.yangtools.yang2sources.plugin.RebuildContextUtils.toSerializable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Artifact serves recognition of modified resources and output files to support incremental builds.
 * The states are persisted in a requested directory.
 */
class RebuildContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(RebuildContext.class);

    static final String PERSISTENCE_FILE_NAME = "rebuild-context-cache";

    private final File persistenceFile;
    private final Map<String, ResourceState> configStateMap = new HashMap<>();
    private final Map<QNameModule, ResourceState> moduleStateMap = new HashMap<>();

    RebuildContext(final File dir) {
        requireNonNull(dir, "dir should not be null");
        persistenceFile = new File(dir, PERSISTENCE_FILE_NAME);
        loadData();
    }

    private void loadData() {
        if (persistenceFile.exists()) {
            try (FileInputStream fis = new FileInputStream(persistenceFile);
                 ObjectInputStream ois = new ObjectInputStream(fis)
            ) {
                final RebuildContextData data = (RebuildContextData) ois.readObject();
                configStateMap.putAll(data.getConfigStateMap());
                moduleStateMap.putAll(data.getModuleStateMap());
            } catch (IOException | ClassNotFoundException e) {
                LOGGER.warn("Could not load from rebuild context file", e);
            }
        }
    }

    /**
     * Persists collected state data.
     */
    void persist() {
        try (FileOutputStream fos = new FileOutputStream(persistenceFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)
        ) {
            final RebuildContextData data = new RebuildContextData();
            data.setModuleStateMap(moduleStateMap);
            data.setConfigStateMap(configStateMap);
            oos.writeObject(data);
        } catch (IOException e) {
            LOGGER.warn("Could not persist rebuild context file", e);
        }
    }

    /**
     * Updates configuration states.
     *
     * @param configMap configuration id to object map
     */
    void setConfigurations(final Map<String, ? extends Serializable> configMap) {
        requireNonNull(configMap, "configMap should not be null");
        checkArgument(!configMap.isEmpty(), "configmap should not be empty");
        final Map<String, ResourceState> newStateMap = configMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> buildResourceState(entry.getValue(), configStateMap.get(entry.getKey()))
                ));
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
        final Map<QNameModule, ResourceState> newStateMap = modules.stream()
                .collect(Collectors.toMap(
                        Module::getQNameModule,
                        module -> buildResourceState(
                                toSerializable(module), moduleStateMap.get(module.getQNameModule()))
                ));
        moduleStateMap.clear();
        moduleStateMap.putAll(newStateMap);
    }

    private static ResourceState buildResourceState(final Serializable object, final ResourceState priorState) {
        final String hashCode = buildHashCode(object);
        final boolean isModified = priorState == null || !Objects.equals(hashCode, priorState.getHashCode());
        final ResourceState curState = new ResourceState();
        curState.setHashCode(hashCode);
        curState.setModified(isModified);
        return curState;
    }

    /**
     * Indicates the updates for requested configuration.
     *
     * @param configId configuration id
     * @return true if there is any update found either in modules or in configuration; false otherwise
     */
    boolean isContextChanged(final String configId) {
        requireNonNull(configId, "configId cannot be null");
        for (ResourceState state : moduleStateMap.values()) {
            if (state.isModified()) {
                return true;
            }
        }
        if (!configStateMap.containsKey(configId) || configStateMap.get(configId).isModified()) {
            return true;
        }
        return false;
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

    private static class ResourceState implements Serializable {
        private static final long serialVersionUID = 1L;

        private String hashCode;
        private boolean isModified;

        public String getHashCode() {
            return hashCode;
        }

        public void setHashCode(final @NonNull String hashCode) {
            this.hashCode = hashCode;
        }

        public boolean isModified() {
            return isModified;
        }

        public void setModified(boolean modified) {
            isModified = modified;
        }
    }

}
