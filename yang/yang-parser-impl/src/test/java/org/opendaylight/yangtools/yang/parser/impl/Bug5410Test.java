/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;

public class Bug5410Test {

    @Test
    public void testCaretAndDollarEscaping() throws IOException, URISyntaxException {

        Set<Module> modules = TestUtils.loadModules(getClass().getResource("/bugs/bug5410").toURI());
        assertNotNull(modules);
        Module module = modules.iterator().next();

        // case 1
        LeafSchemaNode leaf = (LeafSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf"));
        assertNotNull(leaf);

        TypeDefinition<?> type = leaf.getType();
        assertTrue(type instanceof ExtendedType);

        List<PatternConstraint> patterns = ((ExtendedType) type).getPatternConstraints();
        assertNotNull(patterns);
        assertEquals(1, patterns.size());

        PatternConstraint patternConstraint = patterns.iterator().next();
        String fixedPattern = patternConstraint.getRegularExpression();
        assertNotNull(Pattern.compile(fixedPattern));
        assertEquals("^\\^\\^\\^[^a-z]?\\$\\$[^A-Z]?\\$\\$\\$$", fixedPattern);

        String testString = "^^^H$$d$$$";
        assertTrue(testString.matches(fixedPattern));

        // case 2
        leaf = (LeafSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf2"));
        assertNotNull(leaf);

        type = leaf.getType();
        assertTrue(type instanceof ExtendedType);

        patterns = ((ExtendedType) type).getPatternConstraints();
        assertNotNull(patterns);
        assertEquals(1, patterns.size());

        patternConstraint = patterns.iterator().next();
        fixedPattern = patternConstraint.getRegularExpression();
        assertNotNull(Pattern.compile(fixedPattern));
        assertEquals("^\\^\\^\\^[^\\$]?\\d{3}[^\\$]?\\$\\$\\$$", fixedPattern);

        testString = "^^^H123d$$$";
        assertTrue(testString.matches(fixedPattern));
        testString = "^^^$123d$$$";
        assertFalse(testString.matches(fixedPattern));
        testString = "^^^H123$$$$";
        assertFalse(testString.matches(fixedPattern));

        // case 3
        leaf = (LeafSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf3"));
        assertNotNull(leaf);

        type = leaf.getType();
        assertTrue(type instanceof ExtendedType);

        patterns = ((ExtendedType) type).getPatternConstraints();
        assertNotNull(patterns);
        assertEquals(1, patterns.size());

        patternConstraint = patterns.iterator().next();
        fixedPattern = patternConstraint.getRegularExpression();
        assertNotNull(Pattern.compile(fixedPattern));
        assertEquals("^\\^\\^\\^[^\\^]?\\d{3}[^\\^]?$", fixedPattern);

        testString = "^^^H123d";
        assertTrue(testString.matches(fixedPattern));
        testString = "^^^^123d";
        assertFalse(testString.matches(fixedPattern));
        testString = "^^^H123^";
        assertFalse(testString.matches(fixedPattern));
    }
}
