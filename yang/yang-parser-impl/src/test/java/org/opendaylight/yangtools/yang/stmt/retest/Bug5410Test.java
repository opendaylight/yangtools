/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.stmt.retest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.PatternConstraint;
import org.opendaylight.yangtools.yang.model.api.type.StringTypeDefinition;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

public class Bug5410Test {

    @Test
    public void testAnchorsFix() throws ReactorException, FileNotFoundException, URISyntaxException {
        SchemaContext context = TestUtils.parseYangSources("/bugs/bug5410");
        assertNotNull(context);
        Module module = context.findModuleByName("bug5410-test", null);
        assertNotNull(module);

        LeafSchemaNode leaf = (LeafSchemaNode) module.getDataChildByName(
                QName.create(module.getQNameModule(), "test-leaf"));
        assertNotNull(leaf);

        TypeDefinition<?> type = leaf.getType();
        assertTrue(type instanceof StringTypeDefinition);

        List<PatternConstraint> patterns = ((StringTypeDefinition) type).getPatternConstraints();
        assertNotNull(patterns);
        assertEquals(1, patterns.size());

        PatternConstraint patternConstraint = patterns.iterator().next();
        String fixedPattern = patternConstraint.getRegularExpression();
        assertNotNull(Pattern.compile(fixedPattern));
        assertEquals("^\\^\\^\\^[^a-z]?\\$\\$[^A-Z]?\\$\\$\\$$", fixedPattern);

        String testString = "^^^H$$d$$$";
        assertTrue(testString.matches(fixedPattern));
    }
}
