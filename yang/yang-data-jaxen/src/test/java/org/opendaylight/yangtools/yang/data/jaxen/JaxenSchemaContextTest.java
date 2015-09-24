/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import com.google.common.base.Converter;
import com.google.common.base.VerifyException;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.xml.xpath.XPathExpressionException;
import org.jaxen.UnresolvableException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;

public class JaxenSchemaContextTest {
    private JaxenSchemaContextFactory testFactory = new JaxenSchemaContextFactory();
    private QNameModule qmodule;
    private ConverterNamespaceContext convertNctx;
    private QName name;
    private JaxenSchemaContext context;
    private Converter<String, QNameModule> converter;
    private SchemaPath testingSchemaPath;
    private XPathDocument document;
    private XPathExpression jaxenXpath;
    private NormalizedNodeNavigator navigator;

    @Before
    public void setup() throws URISyntaxException, IOException, ParseException, XPathExpressionException {
        final SchemaContext ctx = TestUtils.loadSchemaContext(getClass().getResource("/test/documentTest").toURI());
        final ContainerNode node = (ContainerNode) TestUtils.createNormalizedNodes();

        context = (JaxenSchemaContext) testFactory.createContext(ctx);
        assertNotNull(context);

        document = context.createDocument(node);
        assertNotNull(document);

        final NormalizedNode<?, ?> rootNode = document.getRootNode();
        assertNotNull(rootNode);

        qmodule = ctx.getModules().iterator().next().getQNameModule();
        assertNotNull(qmodule);

        final BiMap<String, QNameModule> mapConverter = HashBiMap.create();
        mapConverter.put("test2", qmodule);
        converter = Maps.asConverter(mapConverter);
        convertNctx = new ConverterNamespaceContext(converter);

        name = ctx.getModules().iterator().next().getChildNodes().iterator().next().getQName();

        final DateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        final URI namespace = URI.create("urn:opendaylight.test2");
        final Date revision = simpleDateFormat.parse("2015-08-08");
        final String prefix = "test2";

        testingSchemaPath = TestUtils.createPath(true, namespace, revision, prefix, "root", "list-a");
        assertNotNull(testingSchemaPath);

        jaxenXpath = context.compileExpression(testingSchemaPath, converter, "/test2");
        assertNotNull(jaxenXpath);

        navigator = new NormalizedNodeNavigator(convertNctx, (JaxenDocument) document);
        assertNotNull(navigator);
    }

    @Test
    public void testConverterNamespaceContextBackFront() {
        assertEquals("test2", convertNctx.doBackward(qmodule));
        assertEquals(qmodule, convertNctx.doForward("test2"));
    }

    @Test
    public void testConverterNamespaceContextPrefixJaxenName() {
        assertNotNull(name);
        assertEquals("test2:root", convertNctx.jaxenQName(name));
        String prefix = convertNctx.translateNamespacePrefixToUri("test2");
        assertNotNull(prefix);
        assertEquals("urn:opendaylight.test2", prefix);
    }

    @Test
    public void testCompileExpression() {
        assertNotNull(jaxenXpath.getApexPath());
        assertEquals(testingSchemaPath, jaxenXpath.getEvaluationPath());
    }

    @Test
    public void testJaxenXpath() throws XPathExpressionException {
        YangInstanceIdentifier mockYangInstanceIdentifier = Mockito.mock(YangInstanceIdentifier.class);
        assertNotNull(jaxenXpath.evaluate(document, mockYangInstanceIdentifier));
        assertNotNull(jaxenXpath.evaluate(document, TestUtils.getYangInstanceIdentifier(2)));
        assertNotNull(jaxenXpath.evaluate(document, TestUtils.getYangInstanceIdentifier(1)));
    }


    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedOperationException() {
        assertNotNull(navigator.getCommentStringValue("Test"));
    }

    @Test(expected = VerifyException.class)
    public void testIsMethodsInNodeNavigator() {
        assertNotNull(navigator.isAttribute("test"));
        assertNotNull(navigator.isComment("test"));
        assertNotNull(navigator.isElement("test"));
        assertNotNull(navigator.isNamespace("test"));
        assertNotNull(navigator.isText("test"));
        assertNotNull(navigator.isProcessingInstruction("test"));
        assertNotNull(navigator.isDocument("test"));
    }

    @Test(expected = XPathExpressionException.class)
    public void testCompileExpressionException() throws XPathExpressionException {
        assertNotNull(context.compileExpression(testingSchemaPath, converter, "/broken-path*"));
    }

    @Test(expected = UnresolvableException.class)
    public void testYangFunctionContext() throws UnresolvableException {
        YangFunctionContext yangFun = YangFunctionContext.getInstance();
        assertNotNull(yangFun);
        assertNotNull(yangFun.getFunction("urn:opendaylight.test2", null, "current"));
        yangFun.getFunction("urn:opendaylight.test2", "test2", "root");
    }
}