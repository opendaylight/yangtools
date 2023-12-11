/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.api;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.binding.YangModuleInfo;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.spi.SchemaSourceProvider;

/**
 * A snapshot of a set of {@link YangModuleInfo}s, assembled to form an {@link EffectiveModelContext}.
 */
@Beta
public interface ModuleInfoSnapshot extends Immutable, SchemaSourceProvider<YangTextSource> {
    /**
     * The {@link EffectiveModelContext} resulting from all models exposed from constituent module infos.
     *
     * @return the resulting model context
     */
    @NonNull EffectiveModelContext modelContext();

    <T> Class<T> loadClass(String fullyQualifiedName) throws ClassNotFoundException;
}
