/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import java.util.Date;
import java.util.Optional;
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
     * Returns the revision date for the module.
     *
     * @return date of the module revision which is specified as argument of
     *         YANG {@link Module <b><font color="#339900">revison</font></b>}
     *         keyword
     */
    // FIXME: BUG-4688: should return Optional<Revision>
    Optional<Date> getRevision();

    static int compareRevisions(final Optional<Date> first, final Optional<Date> second) {
        if (!first.isPresent()) {
            return second.isPresent() ? -1 : 0;
        }
        return second.isPresent() ? first.get().compareTo(second.get()) : 1;
    }
}
