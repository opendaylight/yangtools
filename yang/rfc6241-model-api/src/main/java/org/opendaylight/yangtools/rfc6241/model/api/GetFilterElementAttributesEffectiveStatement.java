/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc6241.model.api;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

/**
 * Effective statement representation of 'get-filter-element-attributes' extension defined in
 * <a href="https://tools.ietf.org/html/rfc6241">RFC6241</a>.
 */
@Beta
public interface GetFilterElementAttributesEffectiveStatement
        extends EffectiveStatement<Void, GetFilterElementAttributesStatement> {

}
