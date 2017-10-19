/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.net.URI;
import java.util.Date;
import java.util.Optional;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;

/**
 * Module identifier. This "identifier" is deprecated and is to be removed in 2.0.0.
 * @author Robert Varga
 *
 * @deprecated Use {@link SourceIdentifier} instead.
 */
@Deprecated
public interface ModuleIdentifier {
    /**
     * Returns the name of the module which is specified as argument of YANG
     * {@link Module <b><font color="#FF0000">module</font></b>} keyword.
     *
     * @return string with the name of the module
     */
    String getName();

    /**
     * Returns the namespace of the module which is specified as argument of
     * YANG {@link Module <b><font color="#00FF00">namespace</font></b>}
     * keyword.
     *
     * @return URI format of the namespace of the module
     */
    // FIXME: this should not be here
    URI getNamespace();

    /**
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of
     *         YANG {@link Module <b><font color="#339900">revison</font></b>}
     *         keyword
     */
    // FIXME: BUG-4688: should return Optional<Revision>
    // FIXME: this should not be here
    Date getRevision();

    /**
     * Returns the semantic version of YANG module.
     *
     * <p>
     * If the semantic version is not specified, default semantic version of
     * module is returned.
     *
     * @return SemVer semantic version of yang module which is specified as
     *         argument of
     *         (urn:opendaylight:yang:extension:semantic-version?revision
     *         =2016-02-02)semantic-version statement
     */
    // FIXME: this should not be here
    Optional<SemVer> getSemanticVersion();
}
