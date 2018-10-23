/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.collect.Range;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * A {@link StatementSupport} instance, which supports multiple revisions of a particular statement.
 *
 * @param <A> Argument type
 * @param <D> Declared Statement representation
 * @param <E> Effective Statement representation
 */
@Beta
public interface RevisionRangeStatementSupport<A, D extends DeclaredStatement<A>, E extends EffectiveStatement<A, D>>
        extends StatementSupport<A, D, E> {
    /**
     * Return the range of revisions supported by this statement support.
     *
     * @return Set of supported ranges.
     */
    @NonNull Range<Revision> getSupportedRevisions();
}
