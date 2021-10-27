/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * An element of {@code error-info} container, as modeled in {@code errorInfoType} of
 * <a href="https://datatracker.ietf.org/doc/html/rfc6241#appendix-B">RFC6241, Appendix B</a>.
 */
@Beta
public interface ErrorInfo<T extends ErrorInfo<T>> extends Immutable {
    /**
     * Return this object's representation class.
     *
     * @return This object's representation class.
     */
    @NonNull Class<T> representation();
}
