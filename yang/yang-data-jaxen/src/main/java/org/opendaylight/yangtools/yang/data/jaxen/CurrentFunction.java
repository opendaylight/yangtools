/*
 * Copyright (c) 2014 Robert Varga.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import com.google.common.base.Preconditions;
import java.util.List;
import javax.annotation.Nonnull;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * Implementation of current() as per http://tools.ietf.org/html/rfc6020#section-6.4.1.
 */
final class CurrentFunction implements Function {
    private final NormalizedNode<?, ?> result;

    CurrentFunction(final @Nonnull NormalizedNode<?, ?> result) {
        this.result = Preconditions.checkNotNull(result);
    }

    @Override
    public NormalizedNode<?, ?> call(final Context context, @SuppressWarnings("rawtypes") final List args) throws FunctionCallException {
        if (!args.isEmpty()) {
            throw new FunctionCallException("current() requires no arguments.");
        }
        return result;
    }
}