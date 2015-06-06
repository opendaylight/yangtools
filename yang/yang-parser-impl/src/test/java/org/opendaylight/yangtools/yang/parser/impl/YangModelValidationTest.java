/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.google.common.collect.Sets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.junit.Before;
import org.junit.Test;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Augment_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_add_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_delete_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviation_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Import_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Include_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Prefix_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_date_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_argContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.StringContext;
import org.opendaylight.yangtools.yang.parser.util.YangValidationException;

public class YangModelValidationTest {

    private YangModelBasicValidationListener valid;

    @Before
    public void setUp() {

        valid = new YangModelBasicValidationListener();
    }

    @Test
    public void testPrefixes() {
        Prefix_stmtContext pref = mockStatement(Prefix_stmtContext.class, "unique1");
        Module_stmtContext module = mockStatement(Module_stmtContext.class, "module1");
        addChild(module, pref);

        valid.enterPrefix_stmt(pref);

        pref = mockStatement(Prefix_stmtContext.class, "unique1");
        module = mockStatement(Module_stmtContext.class, "module1");
        addChild(module, pref);

        try {
            valid.enterPrefix_stmt(pref);
        } catch (Exception e) {
            return;
        }

        fail("Validation Exception should have occured");
    }

    @Test
    public void testNamespace() {

        Namespace_stmtContext namespace = mockStatement(Namespace_stmtContext.class, "http://test.parsing.uri.com");
        Module_stmtContext module = mockStatement(Module_stmtContext.class, "module1");
        addChild(module, namespace);

        valid.enterNamespace_stmt(namespace);

        namespace = mockStatement(Namespace_stmtContext.class, "invalid uri");
        module = mockStatement(Module_stmtContext.class, "module1");
        addChild(module, namespace);

        try {
            valid.enterNamespace_stmt(namespace);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Namespace:invalid uri cannot be parsed as URI"));
            return;
        }

        fail("Validation Exception should have occured");
    }

    @Test
    public void testImports() {
        Import_stmtContext impor = mockImport("unique1", "p1");
        Module_stmtContext mod = mockStatement(Module_stmtContext.class, "module1");
        addChild(mod, impor);

        valid.enterImport_stmt(impor);

        impor = mockImport("unique1", "p2");
        mod = mockStatement(Module_stmtContext.class, "module1");
        addChild(mod, impor);

        try {
            valid.enterImport_stmt(impor);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Import:unique1 not unique"));
            return;
        }

        fail("Validation Exception should have occured");
    }

    @Test
    public void testIncludes() {
        Include_stmtContext incl = mockInclude("unique1");
        Module_stmtContext mod = mockStatement(Module_stmtContext.class, "module1");
        addChild(mod, incl);
        valid.enterInclude_stmt(incl);

        incl = mockInclude("unique1");
        mod = mockStatement(Module_stmtContext.class, "module1");
        addChild(mod, incl);

        try {
            valid.enterInclude_stmt(incl);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("Include:unique1 not unique in (sub)module"));
            return;
        }

        fail("Validation Exception should have occured");
    }

    @Test
    public void testIdentifierMatching() {
        List<String> ids = new ArrayList<String>();
        // valid
        ids.add("_ok98-.87.-.8...88-asdAD");
        ids.add("AA.bcd");
        ids.add("a");
        // invalid
        ids.add("9aa");
        ids.add("-");
        ids.add(".");

        int thrown = 0;
        for (String id : ids) {
            try {
                Module_stmtContext module = mock(Module_stmtContext.class);
                Token token = mock(Token.class);
                when(module.getStart()).thenReturn(token);
                BasicValidations.checkIdentifierInternal(module, id);
            } catch (YangValidationException e) {
                thrown++;
            }
        }

        assertEquals(3, thrown);
    }

    @Test(expected = YangValidationException.class)
    public void testAugument() {
        Augment_stmtContext augument = mockStatement(Augment_stmtContext.class, "/a:*abc/a:augument1");
        Module_stmtContext mod1 = mockStatement(Module_stmtContext.class, "mod1");
        addChild(mod1, augument);

        Token token = mock(Token.class);
        when(augument.getStart()).thenReturn(token);

        try {
            valid.enterAugment_stmt(augument);
        } catch (YangValidationException e) {
            assertThat(
                    e.getMessage(),
                    containsString("Schema node id:/a:*abc/a:augument1 not in required format, details:Prefixed id:a:*abc not in required format"));
            throw e;
        }
    }

    @Test
    public void testDeviate() {
        Deviation_stmtContext ctx = mockStatement(Deviation_stmtContext.class, "deviations");
        Deviate_add_stmtContext add = mockStatement(Deviate_add_stmtContext.class, "add");
        Deviate_delete_stmtContext del = mockStatement(Deviate_delete_stmtContext.class, "delete");

        addChild(ctx, add);
        addChild(ctx, del);

        valid.enterDeviation_stmt(ctx);

        HashSet<Class<? extends ParseTree>> types = Sets.newHashSet();
        types.add(Deviate_add_stmtContext.class);
        types.add(Deviate_delete_stmtContext.class);

        int count = ValidationUtil.countPresentChildrenOfType(ctx, types);
        assertEquals(2, count);
    }

    @Test(expected = YangValidationException.class)
    public void testStatus() throws Exception {
        Status_argContext status = mockStatement(Status_argContext.class, "unknown");
        try {
            valid.enterStatus_arg(status);
        } catch (YangValidationException e) {
            assertThat(e.getMessage(), containsString("illegal value for Status statement, only permitted:"));
            throw e;
        }
    }

    private static Import_stmtContext mockImport(final String name, final String prefixName) {
        Import_stmtContext impor = mockStatement(Import_stmtContext.class, name);

        Prefix_stmtContext prefix = mockStatement(Prefix_stmtContext.class, prefixName);
        Revision_date_stmtContext revDate = mockStatement(Revision_date_stmtContext.class, getFormattedDate());

        addChild(impor, prefix);
        addChild(impor, revDate);
        return impor;
    }

    static String getFormattedDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    private static Include_stmtContext mockInclude(final String name) {
        Include_stmtContext incl = mockStatement(Include_stmtContext.class, name);

        Revision_date_stmtContext revDate = mockStatement(Revision_date_stmtContext.class, getFormattedDate());

        addChild(incl, revDate);
        return incl;
    }

    static void mockName(final ParseTree stmt, final String name) {
        doReturn(1).when(stmt).getChildCount();

        TerminalNode terminalNode = mock(TerminalNode.class);
        doReturn(name).when(terminalNode).getText();

        StringContext nameCtx = mock(StringContext.class);
        doReturn(nameCtx).when(stmt).getChild(0);
        doReturn(terminalNode).when(nameCtx).getChild(0);
        doReturn(name).when(terminalNode).getText();

        doReturn(Collections.singletonList(terminalNode)).when(nameCtx).STRING();
    }

    static <T extends ParseTree> T mockStatement(final Class<T> stmtType, final String name) {
        T stmt = stmtType.cast(mock(stmtType));

        doReturn(0).when(stmt).getChildCount();

        if (name != null) {
            mockName(stmt, name);
        }
        return stmt;
    }

    static void addChild(final ParseTree parent, final ParseTree child) {
        int childCount = parent.getChildCount() + 1;
        doReturn(childCount).when(parent).getChildCount();
        doReturn(child).when(parent).getChild(childCount - 1);
        doReturn(parent).when(child).getParent();
    }

}
