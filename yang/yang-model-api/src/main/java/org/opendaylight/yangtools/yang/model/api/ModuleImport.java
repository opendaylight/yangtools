/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Date;
import org.opendaylight.yangtools.concepts.SemVer;

/**
 * Interface describing YANG 'import' statement.
 *
 * <p>
 * The import statement makes definitions from one module available inside
 * another module or submodule.
 */
public interface ModuleImport extends DocumentedNode {
    /**
     * Returns the name of the module to import.
     *
     * @return Name of the module to import
     */
    String getModuleName();

    /**
     * Returns the module revision to import. May be null.
     *
     * @return Revision of module to import
     */
    Date getRevision();

    /**
     * Returns the semantic version to import.
     *
     * @return Semantic version of module to import
     */
    default SemVer getSemanticVersion() {
        return Module.DEFAULT_SEMANTIC_VERSION;
    }

    /**
     * Returns the prefix associated with the imported module.
     *
     * @return Prefix used to point to imported module
     */
    String getPrefix();
}
