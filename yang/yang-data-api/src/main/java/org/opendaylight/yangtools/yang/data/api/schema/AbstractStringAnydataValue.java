/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.schema;

import com.google.common.annotations.Beta;

/**
 * Base class for String-based anydata values. The contents of the string is dependent on the type of this value.
 *
 * @author Robert Varga
 *
 * @param <T> AnydataValue type
 */
@Beta
public abstract class AbstractStringAnydataValue<T extends AbstractStringAnydataValue<T>> extends AnydataValue<T> {
    /**
     * Return the value as a string. The contents of this string is defined by the class returned by
     * {@link #getValueType()}.
     *
     * @return String representation of this anydata value, in type-specific format.
     */
    public abstract String getStringValue();
}
