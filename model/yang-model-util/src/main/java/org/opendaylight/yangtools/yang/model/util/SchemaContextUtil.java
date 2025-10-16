/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.HashSet;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.source.SourceIdentifier;

/**
 * The Schema Context Util contains support methods for searching through Schema Context modules for specified schema
 * nodes via Schema Path or Revision Aware XPath. The Schema Context Util is designed as mixin, so it is not
 * instantiable.
 */
public final class SchemaContextUtil {
    private SchemaContextUtil() {
        // Hidden on purpose
    }

    /**
     * Extract the identifiers of all modules and submodules which were used to create a particular SchemaContext.
     *
     * @param context SchemaContext to be examined
     * @return Set of ModuleIdentifiers.
     */
    // FIXME: rehost to yang-repo-spi (or -api?)
    public static Set<SourceIdentifier> getConstituentModuleIdentifiers(final SchemaContext context) {
        final var ret = new HashSet<SourceIdentifier>();

        for (var module : context.getModules()) {
            ret.add(module.getSourceIdentifier());

            for (var submodule : module.getSubmodules()) {
                ret.add(submodule.getSourceIdentifier());
            }
        }

        return ret;
    }
}
