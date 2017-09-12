/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.source;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.meta.IdentifierNamespace;

/**
 * Source-specific mapping of prefixes to module identifier with specified semantic version.
 */
@Beta
public interface ImpPrefixToSemVerModuleIdentifier extends IdentifierNamespace<String, ModuleIdentifier> {

}
