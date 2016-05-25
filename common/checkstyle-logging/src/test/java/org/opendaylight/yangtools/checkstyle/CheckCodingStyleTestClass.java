/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.checkstyle;

import java.util.Comparator;
import java.util.Map;

import static java.lang.String.CASE_INSENSITIVE_ORDER;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;

public class CheckCodingStyleTestClass {
        // NOTE:  This test class contains intentional checkstyle issues, for CheckstyleTest
	public CheckCodingStyleTestClass() {
		Comparator<String> string = CASE_INSENSITIVE_ORDER; 
		
	   Map<String, String> map = Maps.newHashMap();
	}
}
