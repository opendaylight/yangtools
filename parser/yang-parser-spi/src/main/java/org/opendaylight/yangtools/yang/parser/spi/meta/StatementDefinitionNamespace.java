/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.io.Serial;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;

/**
 * Module-specific namespace for holding {@link StatementDefinition}s defined by extension statements. This namespace
 * is populated before full declaration phase.
 *
 * @author Robert Varga
 */
// FIXME: 8.0.0: Fix naming and javadoc of this namespace.
//
// We have three competing namespaces dealing with various things and the interaction is no entirely clear:
// - ExtensionNamespace, which is populated by ExtensionStatementSupport for all extensions with the corresponding
//                       effective statement and used by SubstatementValidator
// - StatementDefinitionNamespace (this), which is again populated by ExtensionStatementSupport to point to unrecognized
//                                        statement support, i.e. preventing instantiation, and is used only by
//                                        reactor's SourceSpecificContext
// - StatementSupportNamespace, which is a virtual namespace providing access to StatementSupport instances for all
//                              statements available in current processing phase of the source. It works as a union of
//                              this namespace (StatementDefinitionNamespace) and the contents of
//                              StatementSupportBundles.
//
// At the end of the day this feels like an under-utilized namespace: provided the contents of ExtensionNamespace and
// StatementSupportBundles, SourceSpecificSpecificContext should be able to work its magic even without this namespace.
@Beta
public final class StatementDefinitionNamespace extends ParserNamespace<QName, StatementSupport<?, ?, ?>> {
    @Serial
    private static final long serialVersionUID = 1L;

    public static final @NonNull NamespaceBehaviour<?, ?, ?> BEHAVIOUR =
        NamespaceBehaviour.global(StatementDefinitionNamespace.class);

    private StatementDefinitionNamespace() {
        // Hidden on purpose
    }
}
