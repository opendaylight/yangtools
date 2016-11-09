/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Date;
import javax.annotation.Nullable;
import org.opendaylight.yangtools.concepts.SemVer;

/**
 * Interface describing YANG 'import' statement.
 * <p>
 * The import statement makes definitions from one module available inside
 * another module or submodule.
 * </p>
 */
public interface ModuleImport {

    /**
     * @return Name of the module to import
     */
    String getModuleName();

    /**
     * @return Revision of module to import
     */
    Date getRevision();

    /**
     * @return Semantic version of module to import
     */
    default SemVer getSemanticVersion() {
        return Module.DEFAULT_SEMANTIC_VERSION;
    }

    /**
     * @return Prefix used to point to imported module
     */
    String getPrefix();

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * ModuleImport which do not allow a description statement.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain a description statement.
     *
     * @return string that represents the argument of description statement
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default String getDescription() {
        return null;
    }

    /**
     * All implementations should override this method.
     * The default definition of this method is used only in YANG 1.0 (RFC6020) implementations of
     * ModuleImport which do not allow a reference statement.
     * These YANG statements have been changed in YANG 1.1 (RFC7950) and can now contain a reference statement.
     *
     * @return string that represents the argument of reference statement
     */
    // FIXME: version 2.0.0: make this method non-default
    @Nullable default String getReference() {
        return null;
    }
}
