/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import java.util.UUID;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Utility {@link Identifier} backed by a {@link UUID}.
 *
 * @author Robert Varga
 */
@Beta
public abstract class AbstractStringIdentifier<T extends AbstractStringIdentifier<T>>
        extends AbstractIdentifier<String> implements Comparable<T> {
    private static final long serialVersionUID = 1L;

    protected AbstractStringIdentifier(final String string) {
        super(string);
    }

    @Override
    public final int compareTo(@Nonnull final T o) {
        return getValue().compareTo(o.getValue());
    }
}
