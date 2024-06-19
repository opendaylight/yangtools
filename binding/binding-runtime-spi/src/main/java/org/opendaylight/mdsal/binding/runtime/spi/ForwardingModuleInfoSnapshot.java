/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.runtime.spi;

import com.google.common.annotations.Beta;
import com.google.common.collect.ForwardingObject;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.api.source.YangTextSource;
import org.opendaylight.yangtools.yang.model.repo.api.MissingSchemaSourceException;

@Beta
@NonNullByDefault
public abstract class ForwardingModuleInfoSnapshot extends ForwardingObject implements ModuleInfoSnapshot {
    @Override
    protected abstract ModuleInfoSnapshot delegate();

    @Override
    public <T> Class<T> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        return delegate().loadClass(fullyQualifiedName);
    }

    @Override
    public EffectiveModelContext modelContext() {
        return delegate().modelContext();
    }

    @Override
    public @Nullable YangTextSource yangTextSource(final SourceIdentifier sourceId) {
        return delegate().yangTextSource(sourceId);
    }

    @Override
    public YangTextSource getYangTextSource(final SourceIdentifier sourceId) throws MissingSchemaSourceException {
        return delegate().getYangTextSource(sourceId);
    }
}
