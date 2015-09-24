/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.jaxen;

import static org.junit.Assert.*;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathResult;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathDocument;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.text.ParseException;
import java.net.URI;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.base.Converter;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.xpath.XPathSchemaContext;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.Test;

public class JaxenSchemaContextTest2 {
    private QNameModule moduleQName;
    private QName rootQName;
    private QName listAQName;
    private QName listBQName;
    private QName leafAQName;
    private QName leafCQName;
    private QName leafBQName;

    private QName foo;
    private QName bar;
    private QName baz;
    @Test
    public void test() throws IOException, URISyntaxException, XPathExpressionException, ParseException {
        SchemaContext schemaContext = createSchemaContext();
        assertNotNull(schemaContext);

        initQNames();

        XPathSchemaContext xpathSchemaContext = new JaxenSchemaContextFactory().createContext(schemaContext);

        XPathExpression xpathExpression = xpathSchemaContext.compileExpression(createSchemaPath(), createPrefixes(), createXPath());
        XPathDocument xpathDocument = xpathSchemaContext.createDocument(createNormalizedNodes());

        Optional<? extends XPathResult<?>> result = xpathExpression.evaluate(xpathDocument, createYangInstanceIdentifier());

        assertTrue(result.isPresent());
        XPathResult<?> xPathResult = result.get();
        System.out.println(xPathResult.getValue());

    }


    /**
     * @return root -> leaf-c
     */
    private YangInstanceIdentifier createYangInstanceIdentifier() {
        return YangInstanceIdentifier.of(foo).node(bar).node(baz);
    }


    /**
     * @return root
     */
    private String createXPath() {
        // TODO Auto-generated method stub
        return "/foo/bar/baz";
    }


    /**
     * @return
     */
    private Converter<String, QNameModule> createPrefixes() {
        BiMap<String, QNameModule> stateCapitals =
                HashBiMap.create();
        stateCapitals.put("test2", moduleQName);

        return Maps.asConverter(stateCapitals);
    }

    /**
     * rootQName -> listAQName -> leafAQName
     * @return
     */

    private SchemaPath createSchemaPath() {
        return SchemaPath.create(true, rootQName, listAQName, leafAQName);
    }

    /**
     *container root {
        list list-a {
            key "leaf-a";

            leaf leaf-a {
                type string;
            }

            choice choice-a {
                case one {
                    leaf one {
                        type string;
                    }
                }
                case two-three {
                    leaf two {
                        type string;
                    }
                    leaf three {
                        type string;
                    }
                }
            }

            list list-b {
                key "leaf-b";
                leaf leaf-b {
                    type string;
                }
            }
        }
    }
     * @throws URISyntaxException
     * @throws IOException
     */
    private SchemaContext createSchemaContext() throws IOException, URISyntaxException {
        return TestUtils.loadSchemaContext(getClass().getResource("/test/documentTest").toURI());
    }

    /**
     * Returns a test document
     *
     * <pre>
     * root
     *     leaf-c "value c"
     *     list-a
     *          leaf-a "foo"
     *     list-a
     *          leaf-a "bar"
     *          list-b
     *                  leaf-b "one"
     *          list-b
     *                  leaf-b "two"
     *
     * </pre>
     *
     * @return
     */
    private NormalizedNode<?, ?> createNormalizedNodes() {
        return TestUtils.createNormalizedNodes();
    }

    /**
     * @throws ParseException
     * @throws URISyntaxException
     *
     */
    private void initQNames() throws URISyntaxException, ParseException {
        this.moduleQName = QNameModule.create(new URI("urn:opendaylight.test2"), SimpleDateFormatUtil.getRevisionFormat().parse("2015-08-08"));
        this.rootQName = QName.create(moduleQName, "root");
        this.listAQName = QName.create(moduleQName, "list-a");
        this.listBQName = QName.create(moduleQName, "list-b");
        this.leafAQName = QName.create(moduleQName, "leaf-a");
        this.leafBQName = QName.create(moduleQName, "leaf-b");
        this.leafCQName = QName.create(moduleQName, "leaf-c");

        this.foo = QName.create(moduleQName, "foo");
        this.bar = QName.create(moduleQName, "bar");
        this.baz = QName.create(moduleQName, "baz");
    }

}
