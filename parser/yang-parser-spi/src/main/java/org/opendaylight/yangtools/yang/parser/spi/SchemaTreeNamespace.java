/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi;

import com.google.common.annotations.Beta;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.DeclaredStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeAwareEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaTreeEffectiveStatement;
import org.opendaylight.yangtools.yang.parser.spi.meta.ParserNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

/**
 * Statement local namespace, which holds direct schema node descendants. This corresponds to the contents of the schema
 * tree as exposed through {@link SchemaTreeAwareEffectiveStatement}.
 */
// FIXME: 7.0.0: this contract seems to fall on the reactor side of things rather than parser-spi. Consider moving this
//               into yang-(parser-)reactor-api.
@Beta
public final class SchemaTreeNamespace<D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
        extends ParserNamespace<QName, StmtContext<?, D, E>> {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final @NonNull SchemaTreeNamespace<?, ?> INSTANCE = new SchemaTreeNamespace<>();

    private SchemaTreeNamespace() {
        super("schemaTree");
    }

    @SuppressWarnings("unchecked")
    public static <D extends DeclaredStatement<QName>, E extends SchemaTreeEffectiveStatement<D>>
            @NonNull SchemaTreeNamespace<D, E> instance() {
        return (SchemaTreeNamespace<D, E>) INSTANCE;
    }
}
