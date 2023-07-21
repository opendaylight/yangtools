/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContextProvider;

/**
 * Base class for implementing identityref codecs on based on module names.
 */
@Beta
public abstract class ModuleStringIdentityrefCodec extends AbstractModuleStringIdentityrefCodec
        implements EffectiveModelContextProvider {
    private final @NonNull EffectiveModelContext context;
    private final @NonNull QNameModule parentModule;

    protected ModuleStringIdentityrefCodec(final @NonNull EffectiveModelContext context,
            final @NonNull QNameModule parentModule) {
        this.context = requireNonNull(context);
        this.parentModule = requireNonNull(parentModule);
    }

    @Override
    public final EffectiveModelContext getEffectiveModelContext() {
        return context;
    }

    protected final QNameModule getParentModule() {
        return parentModule;
    }

    @Override
    protected String prefixForNamespace(final XMLNamespace namespace) {
        final var modules = context.findModuleStatements(namespace).iterator();
        return modules.hasNext() ? modules.next().argument().getLocalName() : null;
    }
}
