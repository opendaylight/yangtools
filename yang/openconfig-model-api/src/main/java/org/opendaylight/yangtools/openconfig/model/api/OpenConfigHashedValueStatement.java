/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.openconfig.model.api;

import org.opendaylight.yangtools.yang.model.api.stmt.UnknownStatement;

/**
 * Declared statement corresponding to config's "openconfig-hashed-value" (new name) or "openconfig-encrypted-value"
 * (old name).
 *
 * @author Robert Varga
 */
public interface OpenConfigHashedValueStatement extends UnknownStatement<Void> {

}
