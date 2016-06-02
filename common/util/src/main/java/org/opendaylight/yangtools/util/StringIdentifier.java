/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.concepts.Identifier;

/**
 * Utility {@link Identifier} backed by a {@link String}.
 *
 * @deprecated Treats instantiations as equal, not providing safety against mixing instances from different modules.
 *             Use a subclass of {@link AbstractStringIdentifier} instead.
 */
@Deprecated
@Beta
public final class StringIdentifier extends AbstractStringIdentifier<StringIdentifier> {
    private static final long serialVersionUID = 1L;

    public StringIdentifier(final String string) {
        super(string);
    }

    /**
     * @deprecated use {@link #getValue()} instead.
     * @return
     */
    @Deprecated
    public String getString() {
        return getValue();
    }
}
