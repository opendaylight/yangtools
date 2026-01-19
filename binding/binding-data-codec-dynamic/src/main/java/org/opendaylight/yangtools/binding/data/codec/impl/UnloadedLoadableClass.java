/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import net.bytebuddy.dynamic.DynamicType.Unloaded;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.opendaylight.yangtools.binding.loader.BindingClassLoader;
import org.opendaylight.yangtools.binding.loader.LoadableClass;

final class UnloadedLoadableClass<T> implements LoadableClass<T> {
    private static final ClassLoadingStrategy<BindingClassLoader> STRATEGY = (classLoader, types) ->
        types.entrySet().stream()
            .map(entry -> {
                final var key = entry.getKey();
                return Map.entry(key, classLoader.loadClass(key.getName(), entry.getValue()));
            })
            .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));

    private final Unloaded<T> unloaded;

    UnloadedLoadableClass(final Unloaded<T> unloaded) {
        this.unloaded = requireNonNull(unloaded);
    }

    @Override
    public String name() {
        return unloaded.getTypeDescription().getName();
    }

    @Override
    public Class<T> load(final BindingClassLoader classLoader) {
        return (Class<T>) unloaded.load(classLoader, STRATEGY).getLoaded();
    }

    @Override
    public void saveToDir(final Path directory) throws IOException {
        unloaded.saveIn(directory.toFile());
    }
}
