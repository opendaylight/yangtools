/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.stmt;

import com.google.common.annotations.Beta;

/**
 * Common interface for unrecognized unknown statements. UnrecognizedStatement
 * is an instance of a statement defined via an 'extension' statement, for which
 * the parser did not have semantic support (in which case the statement would
 * result in a corresponding semantic subclass of UnknownStatement).
 */
@Beta
public interface UnrecognizedStatement extends UnknownStatement<String> {
}
