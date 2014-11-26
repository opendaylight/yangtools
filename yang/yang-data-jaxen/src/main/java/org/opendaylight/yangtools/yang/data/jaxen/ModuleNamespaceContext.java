/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import org.jaxen.NamespaceContext;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A {@link NamespaceContext} implementation which is backed by a {@link Module} as
 * resolved within the scope of a {@link SchemaContext}.
 */
final class ModuleNamespaceContext extends JaxenNamespaceContext implements NamespaceContext {
    private final BiMap<String, QNameModule> prefixes;

    private ModuleNamespaceContext(final BiMap<String, QNameModule> prefixes) {
        this.prefixes = Preconditions.checkNotNull(prefixes);
    }

    /**
     * Create a new instance of {@link ModuleNamespaceContext}. Instantiation requires
     * establishing how a module's imports are mapped to actual modules and their
     * namespaces. This information is cached and used for improved lookups.
     *
     * @param ctx
     * @param module
     * @return
     * @throws IllegalArgumentException when module mappings cannot be
     */
    static ModuleNamespaceContext create(final SchemaContext ctx, final Module module) {
        // Always check for null ctx
        Preconditions.checkNotNull(ctx, "Schema context may not be null");

        // Use immutable map builder for detection of duplicates (which should never occur)
        final Builder<String, QNameModule> b = ImmutableBiMap.builder();
        b.put(module.getPrefix(), module.getQNameModule());

        for (ModuleImport i : module.getImports()) {
            final Module mod = ctx.findModuleByName(i.getModuleName(), i.getRevision());
            Preconditions.checkArgument(mod != null, "Unsatisfied import of %s by module %s", i, module);

            b.put(i.getPrefix(), mod.getQNameModule());
        }

        return new ModuleNamespaceContext(b.build());
    }

    @Override
    public String translateNamespacePrefixToUri(final String prefix) {
        final QNameModule mod = getModule(prefix);
        return mod == null ? null : mod.getNamespace().toString();
    }

    @Override
    QNameModule getModule(final String prefix) {
        return prefixes.get(prefix);
    }

    private String getPrefix(final QNameModule module) {
        return prefixes.inverse().get(module);
    }

    @Override
    String jaxenQName(final QName qname) {
        // TODO: We should probably cache these
        return getPrefix(qname.getModule()) + ':' + qname.getLocalName();
    }
}
