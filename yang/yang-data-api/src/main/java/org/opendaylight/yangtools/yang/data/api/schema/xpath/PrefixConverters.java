/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema.xpath;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Converter;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;
import com.google.common.collect.Maps;
import javax.annotation.Nonnull;
import javax.xml.xpath.XPathExpressionException;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * A set of utility functions for dealing with common types of namespace mappings.
 */
@Beta
public final class PrefixConverters {
    private PrefixConverters() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a prefix {@link Converter} for {@link XPathExpressionException} defined in a particular YANG
     * {@link Module} .Instantiation requires establishing how a module's imports are mapped to actual modules
     * and their namespaces. This information is cached and used for improved lookups.
     *
     * @param ctx A SchemaContext
     * @param module Module in which the XPath is defined
     * @return A new Converter
     */
    public static @Nonnull Converter<String, QNameModule> create(final SchemaContext ctx, final Module module) {
        // Always check for null ctx
        requireNonNull(ctx, "Schema context may not be null");

        // Use immutable map builder for detection of duplicates (which should never occur)
        final Builder<String, QNameModule> b = ImmutableBiMap.builder();
        b.put(module.getPrefix(), module.getQNameModule());

        for (ModuleImport i : module.getImports()) {
            final Module mod = ctx.findModuleByName(i.getModuleName(), i.getRevision());
            checkArgument(mod != null, "Unsatisfied import of %s by module %s", i, module);

            b.put(i.getPrefix(), mod.getQNameModule());
        }

        return Maps.asConverter(b.build());
    }
}
