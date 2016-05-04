/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import java.net.URI;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 *
 * Pre-linkage global mapping of module names to namespaces
 *
 */
public interface ModuleNameToNamespace extends IdentifierNamespace<String, URI> {

}
