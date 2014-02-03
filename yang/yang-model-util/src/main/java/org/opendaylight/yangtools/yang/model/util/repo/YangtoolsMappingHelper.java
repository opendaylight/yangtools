/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.repo;

import java.lang.reflect.Method;
import java.util.List;
import org.eclipse.xtext.xbase.lib.Conversions;
import org.opendaylight.yangtools.yang.binding.Notification;

public class YangtoolsMappingHelper {
    public static boolean isNotificationCallback(final Method it) {
        boolean _and = false;
        boolean _and_1 = false;
        String _name = it.getName();
        boolean _startsWith = _name.startsWith("on");
        if (!_startsWith) {
            _and_1 = false;
        } else {
            Class<? extends Object>[] _parameterTypes = it.getParameterTypes();
            int _size = ((List<Class<? extends Object>>) Conversions.doWrapArray(_parameterTypes)).size();
            boolean _tripleEquals = (_size == 1);
            _and_1 = (_startsWith && _tripleEquals);
        }
        if (!_and_1) {
            _and = false;
        } else {
            Class<? extends Object>[] _parameterTypes_1 = it.getParameterTypes();
            Class<? extends Object> _get = _parameterTypes_1[0];
            boolean _isAssignableFrom = Notification.class.isAssignableFrom(_get);
            _and = (_and_1 && _isAssignableFrom);
        }
        return _and;
    }

}
