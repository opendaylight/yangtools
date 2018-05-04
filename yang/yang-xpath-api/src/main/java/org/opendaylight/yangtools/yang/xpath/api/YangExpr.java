/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.xpath.api;

import com.google.common.annotations.Beta;
import java.io.Serializable;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Common base interface for all YANG Expression components.
 *
 * @author Robert Varga
 */
@Beta
public interface YangExpr extends Immutable, Serializable {

}
