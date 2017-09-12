/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import java.util.NavigableMap;
import org.opendaylight.yangtools.concepts.SemVer;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Namespace class for storing Maps of all modules with the same name. This namespace is
 * used only in case the semantic versioning is enabled, otherwise it is empty.
 */
@Beta
public interface SemanticVersionModuleNamespace
    extends IdentifierNamespace<String, NavigableMap<SemVer, StmtContext<?, ?, ?>>> {

}
