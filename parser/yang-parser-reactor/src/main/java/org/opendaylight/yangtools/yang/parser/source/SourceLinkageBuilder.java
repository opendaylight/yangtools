/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.source;

/**
 *
 */
public final class SourceLinkageBuilder {
    // RFC7950:
    //
    // - from https://www.rfc-editor.org/rfc/rfc7950#section-5.1
    //   - "A module uses the "include" statement to list all its submodules."
    //   - "For backward compatibility with YANG version 1, a submodule MAY use
    //      the "include" statement to reference other submodules within its
    //      module, but this is not necessary in YANG version 1.1."
    //   - "A submodule MUST NOT include different revisions of other submodules
    //      than the revisions that its module includes."
    //   - "A module or submodule MUST NOT include submodules from other modules,
    //      and a submodule MUST NOT import its own module."
    //   - "A module MUST include all its submodules."
    //   - "There MUST NOT be any circular chains of imports.  For example, if
    //      module "a" imports module "b", "b" cannot import "a"."
    // - from https://www.rfc-editor.org/rfc/rfc7950#section-5.1.1
    //   - "If a module is not imported with a specific revision, it is undefined
    //   -  which revision is used."
    //
    // - from https://www.rfc-editor.org/rfc/rfc7950#section-7.1.6
    //   - "Modules are
    //      only allowed to include submodules that belong to that module, as
    //      defined by the "belongs-to" statement"
    //   - "If no "revision-date" substatement is present, it is undefined which
    //      revision of the submodule is included."
    //   - "Multiple revisions of the same submodule MUST NOT be included."
    //
    // - from https://www.rfc-editor.org/rfc/rfc7950#section-7.2.2
    //   - "A submodule MUST only be included by either the module to which it
    //      belongs or another submodule that belongs to that module."
    //
    // - from https://www.rfc-editor.org/rfc/rfc7950#section-12
    //   - "A YANG version 1.1 module MUST NOT include a YANG version 1
    //      submodule, and a YANG version 1 module MUST NOT include a YANG
    //      version 1.1 submodule."
    //   - "A YANG version 1 module or submodule MUST NOT import a YANG
    //      version 1.1 module by revision."
    //   - "A YANG version 1.1 module or submodule MAY import a YANG version 1
    //      module by revision."

}
