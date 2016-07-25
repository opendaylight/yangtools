/*
 * Copyright (c) 2014 Brocade Communications Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.util;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities for system properties.
 *
 * @author Thomas Pantelis
 */
public final class PropertyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PropertyUtils.class);

    private PropertyUtils() {
    }

    /**
     * Obtains the given property from the System properties and returns as an int. If the property
     * is not found the specified default value is returned. If the property value can't be parsed
     * to an int, a warning is logged and the default value is returned.
     *
     * @param propName the name of the property to get
     * @param defaultValue the default value
     * @return the System property as an int or the <code>defaultValue</code> if not found.
     */
    public static int getIntSystemProperty( String propName, int defaultValue ) {
        int propValue = defaultValue;
        String strValue = System.getProperty(propName);
        if (!Strings.isNullOrEmpty(strValue) && !strValue.trim().isEmpty() ) {
            try {
                propValue = Integer.parseInt(strValue);
            } catch (NumberFormatException e) {
                LOG.warn("Cannot parse value {} for system property {}, using default {}",
                         strValue, propName, defaultValue);
            }
        }

        return propValue;
    }
}
