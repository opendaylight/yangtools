/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import com.google.common.annotations.Beta;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import org.opendaylight.yangtools.yang.parser.spi.meta.StatementSupport;

/**
 * Primary entry point into this implementation. Use {@link #getStatements()} to acquire a collection of
 * {@link StatementSupport} instances, which need to be registered with the parser.
 *
 * @author Robert Varga
 */
@Beta
public final class Metadata {

    private Metadata() {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a collection of statements which need to be added into parser reactor to enable support for YANG metadata.
     *
     * @return Collection of statements to be added.
     */
    public static Collection<StatementSupport<?, ?, ?>> getStatements() {
        return ImmutableList.of(Annotation.getInstance());
    }
}
