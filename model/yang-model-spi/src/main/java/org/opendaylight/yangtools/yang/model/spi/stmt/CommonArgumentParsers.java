/*
 * Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.spi.stmt;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.spi.meta.ArgumentParser;

/**
 * {@link ArgumentParser}s for binding YANG ABNF constructs to their {@code yang.common} and {@code yang.model.api}
 * equivalents.
 */
// FIXME: also DoNotMock
// FIXME: ArgumentParser<Uin8>, etc. if need be
@Beta
@NonNullByDefault
public final class CommonArgumentParsers {
    // keep alpha-sorted, please
    private final AbsoluteSchemaNodeIdParser absoluteSchemaNodeId;
    private final DescendantSchemaNodeIdParser descendantSchemaNodeId;
    private final IdentifierParser identifier;
    private final NodeIdentifierParser nodeIdentifier;

    public CommonArgumentParsers(final NamespaceBinding namespaceBinding) {
        identifier = new IdentifierParser(namespaceBinding);
        nodeIdentifier = new NodeIdentifierParser(identifier);
        absoluteSchemaNodeId = new AbsoluteSchemaNodeIdParser(nodeIdentifier);
        descendantSchemaNodeId = new DescendantSchemaNodeIdParser(nodeIdentifier);
    }

    /**
     * An {@link ArgumentParser} interpreting an {@code identifier} ABNF string as a {@link QName} bound to
     * {@link NamespaceBinding#currentModule()}.
     */
    public ArgumentParser<QName> identifier() {
        return identifier;
    }

    /**
     * An {@link ArgumentParser} interpreting an {@code identifier} ABNF string as a {@link QName} bound to the
     * {@link QNameModule} looked up through the optional {@code prefix ":"} resolution process mechanism.
     */
    public ArgumentParser<QName> nodeIdentifier() {
        return nodeIdentifier;
    }

    /**
     * An {@link ArgumentParser} interpreting an {@code absolute-schema-nodeid} ABNF string as
     * a {@link SchemaNodeIdentifier.Absolute} bound by a {@link NodeIdentifierParser}.
     */
    public ArgumentParser<SchemaNodeIdentifier.Absolute> absoluteSchemaNodeId() {
        return absoluteSchemaNodeId;
    }

    /**
     * An {@link ArgumentParser} interpreting an {@code descendant-schema-nodeid} ABNF string as
     * a {@link SchemaNodeIdentifier.Descendant} bound by a {@link NodeIdentifierParser}.
     */
    public ArgumentParser<SchemaNodeIdentifier.Descendant> descendantSchemaNodeId() {
        return descendantSchemaNodeId;
    }
}
