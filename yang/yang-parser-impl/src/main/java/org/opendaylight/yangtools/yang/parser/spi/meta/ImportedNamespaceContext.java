/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 *
 * Special namespace which allows import of namespaces from other sources.
 *
 * <p>
 * This namespace and its subclasses are used by model processor to
 * link / import namespaces to context node from supplied {@link StmtContext}.
 * <p>
 * This abstraction allows for imports and includes be implement as derived
 * namespaces of this, but is not tied only for import and include statements.
 *
 * @param <K> Imported context identifier
 */
public interface ImportedNamespaceContext<K> extends IdentifierNamespace<K, StmtContext<?, ?, ?>> {

}
