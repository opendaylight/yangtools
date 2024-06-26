/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;
import org.opendaylight.yangtools.binding.model.api.TypeMember;

/**
 * Alphabetically type member {@link Comparator} which provides sorting by name for type members (variables and methods)
 * in a generated class.
 *
 * @param <T> TypeMember type
 */
public class AlphabeticallyTypeMemberComparator<T extends TypeMember> implements Comparator<T>, Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(final T member1, final T member2) {
        return member1.getName().compareTo(member2.getName());
    }
}
