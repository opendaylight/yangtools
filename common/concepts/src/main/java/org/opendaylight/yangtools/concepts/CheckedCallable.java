/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import com.google.common.annotations.Beta;
import java.util.concurrent.Callable;

/**
 * A more restricted version of {@link Callable}, which is guaranteed to have more restricted upper throw bound than
 * an Exception.
 *
 * @param <V> the result type of method {@code call}
 * @param <E> the exception type of method {@code call}
 */
@Beta
@FunctionalInterface
public interface CheckedCallable<V, E extends Exception> extends Callable<V> {
    @Override
    V call() throws E;
}
