/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;
import org.eclipse.jdt.annotation.NonNull;

/**
 * Marker interface for statements which may contain a 'must' statement, as defined in RFC7950. There is a significant
 * difference RFC6020 (YANG 1) and RFC7590 (YANG 1.1) in which statements sport this feature.
 *
 * @deprecated Use {@link MustStatementAwareDeclaredStatement} instead.
 */
@Deprecated
public interface MustStatementContainer {
    /**
     * Return collection of {@link MustStatement}. For RFC6020, this method returns an empty collection for statements
     * which do not allow for must statement children.
     *
     * @return collection of must statements
     */
    @NonNull Collection<? extends MustStatement> getMusts();
}
