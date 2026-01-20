/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc7952.parser;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.rfc7952.model.api.AnnotationStatement;
import org.opendaylight.yangtools.yang.common.AnnotationName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclarationReference;
import org.opendaylight.yangtools.yang.model.spi.meta.AbstractRefStatement;

final class RefAnnotationStatement extends AbstractRefStatement<AnnotationName, AnnotationStatement>
        implements AnnotationStatement {
    RefAnnotationStatement(final AnnotationStatement delegate, final DeclarationReference ref) {
        super(delegate, ref);
    }

    @Override
    public @NonNull String rawArgument() {
        return delegate().rawArgument();
    }
}
