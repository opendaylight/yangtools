/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;

public class Bug4079Test {

    private Set<Module> modules;

    @Test
    public void testModuleCompilation() throws URISyntaxException, IOException {
        modules = TestUtils.loadModules(getClass().getResource("/bugs/bug4079").toURI());
        assertNotNull(modules);
    }

    @Test
    public void testStringPatternFix() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = ParserListenerUtils.class.getDeclaredMethod("fixUnicodeScriptPattern", String.class);
        assertNotNull(method);
        assertEquals("fixUnicodeScriptPattern", method.getName());

        method.setAccessible(true);

        String fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsBasicLatin}|\\p{IsLatin-1Supplement})*");
        assertEquals("(\\p{InBasicLatin}|\\p{InLatin-1Supplement})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsArrows})*");
        assertEquals("(\\p{InArrows})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsDingbats})*");
        assertEquals("(\\p{InDingbats})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsSpecials})*");
        assertEquals("(\\p{InSpecials})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsBatak})*");
        assertEquals("(\\p{IsBatak})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsLatin})*");
        assertEquals("(\\p{IsLatin})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));

        fixedUnicodeScriptPattern = (String) method.invoke(null,"(\\p{IsTibetan})*");
        assertEquals("(\\p{IsTibetan})*", fixedUnicodeScriptPattern);
        assertNotNull(Pattern.compile(fixedUnicodeScriptPattern));
    }
}
