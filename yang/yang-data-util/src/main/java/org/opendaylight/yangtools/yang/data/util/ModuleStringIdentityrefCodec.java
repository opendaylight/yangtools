/*
 * Copyright (c) 2016 Intel Corporation and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.net.URI;
import java.util.Iterator;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

public abstract class ModuleStringIdentityrefCodec extends AbstractModuleStringIdentityrefCodec {
    // FIXME: 3.0.0: hide these fields
    protected final SchemaContext context;
    @SuppressFBWarnings("URF_UNREAD_PUBLIC_OR_PROTECTED_FIELD")
    protected final QNameModule parentModuleQname;

    protected ModuleStringIdentityrefCodec(final @NonNull SchemaContext context,
            final @NonNull QNameModule parentModule) {
        this.context = requireNonNull(context);
        this.parentModuleQname = requireNonNull(parentModule);
    }

    @Override
    protected String prefixForNamespace(final URI namespace) {
        final Iterator<Module> modules = context.findModules(namespace).iterator();
        return modules.hasNext() ? modules.next().getName() : null;
    }
}
