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
import com.google.common.util.concurrent.ListenableFuture;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.runtime.api.ModuleInfoSnapshot;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.YangTextSchemaSource;

@Beta
public abstract class ForwardingModuleInfoSnapshot extends ForwardingObject implements ModuleInfoSnapshot {
    @Override
    protected abstract ModuleInfoSnapshot delegate();

    @Override
    public Class<?> loadClass(final String fullyQualifiedName) throws ClassNotFoundException {
        return delegate().loadClass(fullyQualifiedName);
    }

    @Override
    public @NonNull EffectiveModelContext getEffectiveModelContext() {
        return delegate().getEffectiveModelContext();
    }

    @Override
    public ListenableFuture<? extends YangTextSchemaSource> getSource(final SourceIdentifier sourceIdentifier) {
        return delegate().getSource(sourceIdentifier);
    }
}
