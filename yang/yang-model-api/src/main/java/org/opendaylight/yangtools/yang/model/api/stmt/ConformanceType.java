/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;

/**
 * Module conformance type, as defined in <a href="https://tools.ietf.org/html/rfc7895#page-9">RFC 7895</a> and
 * <a href="https://tools.ietf.org/html/rfc7950#section-5.6">RFC 7950 section 5.6</a>.
 *
 * <p>
 * Note that OpenDaylight supports IMPLEMENT for multiple revisions of a particular module.
 *
 * @author Robert Varga
 */
@Beta
public enum ConformanceType {
    /**
     * According to RFC7895's ietf-yang-library.yang:
     * <pre>
     *     Indicates that the server implements one or more
     *     protocol-accessible objects defined in the YANG module
     *     identified in this entry.  This includes deviation
     *     statements defined in the module.
     *
     *     For YANG version 1.1 modules, there is at most one
     *     module entry with conformance type 'implement' for a
     *     particular module name, since YANG 1.1 requires that,
     *     at most, one revision of a module is implemented.
     *
     *     For YANG version 1 modules, there SHOULD NOT be more
     *     than one module entry for a particular module name.
     * </pre>
     */
    IMPLEMENT,
    /**
     * According to RFC7895's ietf-yang-library.yang:
     * <pre>
     *     Indicates that the server imports reusable definitions
     *     from the specified revision of the module but does
     *     not implement any protocol-accessible objects from
     *     this revision.
     *
     *     Multiple module entries for the same module name MAY
     *     exist.  This can occur if multiple modules import the
     *     same module but specify different revision dates in
     *     the import statements
     * </pre>
     */
    IMPORT;
}
