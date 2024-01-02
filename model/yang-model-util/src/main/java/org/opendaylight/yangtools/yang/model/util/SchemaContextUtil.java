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
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.Submodule;
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
        final Set<SourceIdentifier> ret = new HashSet<>();

        for (Module module : context.getModules()) {
            ret.add(moduleToIdentifier(module));

            for (Submodule submodule : module.getSubmodules()) {
                ret.add(moduleToIdentifier(submodule));
            }
        }

        return ret;
    }

    private static SourceIdentifier moduleToIdentifier(final ModuleLike module) {
        return new SourceIdentifier(Unqualified.of(module.getName()), module.getRevision().orElse(null));
    }
}
