/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.model.api.stmt.ImportEffectiveStatement;

/**
 * Interface describing YANG 'import' statement. The import statement makes definitions from one module available inside
 * another module or submodule.
 */
// FIXME: 7.0.0: this class is a leak of the declared world into the effective one. In effective world, all nodes form
//               a tree, which consists of multiple (mostly) QName-navigated namespaces. As such module imports
//               contribute only a prefix/QNameModule mapping to the effective world and hence should be mapped that
//               way:
//               - Module exposes String->QNameModule mapping
public interface ModuleImport extends DocumentedNode, EffectiveStatementEquivalent {
    @Override
    ImportEffectiveStatement asEffectiveStatement();

    /**
     * Returns the name of the module to import.
     *
     * @return Name of the module to import
     */
    default @NonNull Unqualified getModuleName() {
        return asEffectiveStatement().argument();
    }

    /**
     * Returns the module revision to import. May be null.
     *
     * @return Revision of module to import
     */
    Optional<Revision> getRevision();

    /**
     * Returns the prefix associated with the imported module.
     *
     * @return Prefix used to point to imported module
     */
    @NonNull String getPrefix();
}
