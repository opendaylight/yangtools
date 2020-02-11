/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api;

import static java.util.Objects.requireNonNull;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.AbstractList;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.common.QName;

final class PathFromRoot extends AbstractList<QName> implements Immutable {
    private static final QName[] EMPTY_QNAMES = new QName[0];
    private static final VarHandle QNAMES;

    static {
        try {
            QNAMES = MethodHandles.lookup().findVarHandle(PathFromRoot.class, "qnames", QName[].class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final SchemaPath path;

    @SuppressWarnings("unused")
    private QName @Nullable [] qnames;

    PathFromRoot(final SchemaPath path) {
        this.path = requireNonNull(path);
    }

    @Override
    public Iterator<QName> iterator() {
        return Arrays.asList(qnames()).iterator();
    }

    @Override
    public Spliterator<QName> spliterator() {
        return Arrays.spliterator(qnames());
    }

    @Override
    public QName get(final int index) {
        return qnames()[index];
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int size() {
        return qnames().length;
    }

    private QName @NonNull [] qnames() {
        final QName[] local = (QName[]) QNAMES.getAcquire(this);
        return local != null ? local : loadQNames();
    }

    private QName @NonNull [] loadQNames() {
        final Deque<QName> tmp = new ArrayDeque<>();
        SchemaPath current = path;
        SchemaPath parent = path.getParent();
        do {
            tmp.addFirst(current.getLastComponent());
            current = parent;
            parent = current.getParent();
        } while (parent != null);

        final QName[] result = tmp.toArray(EMPTY_QNAMES);
        QNAMES.setRelease(this, result);
        return result;
    }
}
