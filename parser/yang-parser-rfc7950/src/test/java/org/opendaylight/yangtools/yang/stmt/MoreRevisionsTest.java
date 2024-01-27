/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.stmt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.UnresolvedQName.Unqualified;
import org.opendaylight.yangtools.yang.common.XMLNamespace;
import org.opendaylight.yangtools.yang.model.api.ContainerSchemaNode;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.parser.spi.meta.ReactorException;

class MoreRevisionsTest extends AbstractYangTest {
    private static final String TED_20130712 = "/semantic-statement-parser/two-revisions/ted@2013-07-12.yang";
    private static final String TED_20131021 = "/semantic-statement-parser/two-revisions/ted@2013-10-21.yang";
    private static final String IETF_TYPES = "/ietf/ietf-inet-types@2010-09-24.yang";
    private static final String NETWORK_TOPOLOGY_20130712 = "/ietf/network-topology@2013-07-12.yang";
    private static final String NETWORK_TOPOLOGY_20131021 = "/ietf/network-topology@2013-10-21.yang";
    private static final String ISIS_20130712 =
        "/semantic-statement-parser/two-revisions/isis-topology@2013-07-12.yang";
    private static final String ISIS_20131021 =
        "/semantic-statement-parser/two-revisions/isis-topology@2013-10-21.yang";
    private static final String L3_20130712 =
        "/semantic-statement-parser/two-revisions/l3-unicast-igp-topology@2013-07-12.yang";
    private static final String L3_20131021 =
        "/semantic-statement-parser/two-revisions/l3-unicast-igp-topology@2013-10-21.yang";

    @Test
    void readAndParseYangFileTest() {
        final var result = assertEffectiveModel("/semantic-statement-parser/revisions/more-revisions-test.yang");
        final var moduleByName = result.getModules().iterator().next();
        assertEquals(Revision.of("2015-06-07"), moduleByName.getQNameModule().revision());
    }

    @Test
    void twoRevisionsTest() {
        assertEffectiveModel(TED_20130712, TED_20131021, IETF_TYPES);
    }

    @Test
    void twoRevisionsTest2() {
        final var context = assertEffectiveModel(NETWORK_TOPOLOGY_20130712, NETWORK_TOPOLOGY_20131021, IETF_TYPES);

        assertEquals(3, context.getModuleStatements().size());
        assertEquals(2, context.findModules("network-topology").size());
    }

    @Test
    void moreRevisionsListKeyTest() throws ReactorException {
        assertEffectiveModel(TED_20130712, TED_20131021, ISIS_20130712, ISIS_20131021, L3_20130712, L3_20131021,
            IETF_TYPES, NETWORK_TOPOLOGY_20130712, NETWORK_TOPOLOGY_20131021);
    }

    @Test
    void multipleRevisionsTest() {
        for (int i = 0; i < 100; i++) {
            assertEffectiveModelDir("/semantic-statement-parser/multiple-revisions");
        }
    }

    @Test
    void multipleRevisionsFullTest() {
        for (int i = 0; i < 100; i++) {
            var context = assertEffectiveModelDir("/semantic-statement-parser/multiple-revisions/full");
            assertEquals(6, context.getModules().size());
            checkContentFullTest(context);
        }
    }

    private static void checkContentFullTest(final EffectiveModelContext context) {
        final XMLNamespace yangTypesNS = XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-types");

        final Revision rev20100924 = Revision.of("2010-09-24");
        final Revision rev20130516 = Revision.of("2013-05-16");
        final Revision rev20130715 = Revision.of("2013-07-15");

        final QNameModule yangTypes20100924 = QNameModule.of(yangTypesNS, rev20100924);
        final QNameModule yangTypes20130516 = QNameModule.of(yangTypesNS, rev20130516);
        final QNameModule yangTypes20130715 = QNameModule.of(yangTypesNS, rev20130715);

        final QName dateTimeTypeDef20100924 = QName.create(yangTypes20100924, "date-and-time");
        final QName dateTimeTypeDef20130516 = QName.create(yangTypes20130516, "date-and-time");
        final QName dateTimeTypeDef20130715 = QName.create(yangTypes20130715, "date-and-time");

        var yangTypesModule20100924 = context.findModule("ietf-yang-types", rev20100924).orElseThrow();
        var yangTypesModule20130516 = context.findModule("ietf-yang-types", rev20130516).orElseThrow();
        var yangTypesModule20130715 = context.findModule("ietf-yang-types", rev20130715).orElseThrow();
        assertTrue(findTypeDef(yangTypesModule20100924, dateTimeTypeDef20100924));
        assertTrue(findTypeDef(yangTypesModule20130516, dateTimeTypeDef20130516));
        assertTrue(findTypeDef(yangTypesModule20130715, dateTimeTypeDef20130715));

        checkNetconfMonitoringModuleFullTest(context, rev20130715, dateTimeTypeDef20130715);
        checkInterfacesModuleFullTest(context, rev20100924, dateTimeTypeDef20100924);
    }

    private static void checkInterfacesModuleFullTest(final EffectiveModelContext context, final Revision rev20100924,
        final QName dateTimeTypeDef20100924) {
        var rev20121115 = Revision.of("2012-11-15");

        var interfacesModule20121115 = context.findModule("ietf-interfaces", rev20121115).orElseThrow();
        var imports = interfacesModule20121115.getImports();
        assertEquals(1, imports.size());
        var interfacesImport = imports.iterator().next();
        assertEquals(Unqualified.of("ietf-yang-types"), interfacesImport.getModuleName());
        assertEquals(Optional.of(rev20100924), interfacesImport.getRevision());
    }

    private static void checkNetconfMonitoringModuleFullTest(final EffectiveModelContext context,
        final Revision rev20130715, final QName dateTimeTypeDef20130715) {
        var rev20101004 = Revision.of("2010-10-04");

        var monitoringModule20101004 = context.findModule("ietf-netconf-monitoring", rev20101004).orElseThrow();
        var imports = monitoringModule20101004.getImports();
        assertEquals(2, imports.size());
        for (var monitoringImport : imports) {
            if (monitoringImport.getModuleName().equals("ietf-yang-types")) {
                assertEquals(Optional.of(rev20130715), monitoringImport.getRevision());
            }
        }
    }

    @Test
    void multipleRevisionsSimpleTest() {
        for (int i = 0; i < 1000; i++) {
            var context = assertEffectiveModelDir("/semantic-statement-parser/multiple-revisions/simple");
            assertEquals(5, context.getModules().size());
            checkContentSimpleTest(context);
        }
    }

    private static void checkContentSimpleTest(final EffectiveModelContext context) {
        final XMLNamespace yangTypesNS = XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-yang-types");

        final Revision rev20100924 = Revision.of("2010-09-24");
        final Revision rev20130516 = Revision.of("2013-05-16");
        final Revision rev20130715 = Revision.of("2013-07-15");

        final QNameModule yangTypes20100924 = QNameModule.of(yangTypesNS, rev20100924);
        final QNameModule yangTypes20130516 = QNameModule.of(yangTypesNS, rev20130516);
        final QNameModule yangTypes20130715 = QNameModule.of(yangTypesNS, rev20130715);

        final QName dateTimeTypeDef20100924 = QName.create(yangTypes20100924, "date-and-time");
        final QName dateTimeTypeDef20130516 = QName.create(yangTypes20130516, "date-and-time");
        final QName dateTimeTypeDef20130715 = QName.create(yangTypes20130715, "date-and-time");

        var yangTypesModule20100924 = context.findModule("ietf-yang-types", rev20100924).orElseThrow();
        var yangTypesModule20130516 = context.findModule("ietf-yang-types", rev20130516).orElseThrow();
        var yangTypesModule20130715 = context.findModule("ietf-yang-types", rev20130715).orElseThrow();
        assertTrue(findTypeDef(yangTypesModule20100924, dateTimeTypeDef20100924));
        assertTrue(findTypeDef(yangTypesModule20130516, dateTimeTypeDef20130516));
        assertTrue(findTypeDef(yangTypesModule20130715, dateTimeTypeDef20130715));

        checkNetconfMonitoringModuleSimpleTest(context, rev20130715, dateTimeTypeDef20130715);
        checkInterfacesModuleSimpleTest(context, rev20100924, dateTimeTypeDef20100924);
    }

    private static void checkInterfacesModuleSimpleTest(final EffectiveModelContext context, final Revision rev20100924,
            final QName dateTimeTypeDef20100924) {
        XMLNamespace interfacesNS = XMLNamespace.of("urn:ietf:params:xml:ns:yang:ietf-interfaces");
        Revision rev20121115 = Revision.of("2012-11-15");
        final QNameModule interfacesNS20121115 = QNameModule.of(interfacesNS, rev20121115);
        QName lastChange = QName.create(interfacesNS20121115, "last-change");

        var interfacesModule20121115 = context.findModule("ietf-interfaces", rev20121115).orElseThrow();
        var leafLastChange = interfacesModule20121115.getDataChildByName(lastChange);
        QName lastChangeTypeQName = assertInstanceOf(LeafSchemaNode.class, leafLastChange).getType().getQName();
        assertEquals(dateTimeTypeDef20100924, lastChangeTypeQName);

        var imports = interfacesModule20121115.getImports();
        assertEquals(1, imports.size());
        var interfacesImport = imports.iterator().next();
        assertEquals(Unqualified.of("ietf-yang-types"), interfacesImport.getModuleName());
        assertEquals(Optional.of(rev20100924), interfacesImport.getRevision());
    }

    private static void checkNetconfMonitoringModuleSimpleTest(final EffectiveModelContext context,
        final Revision rev20130715, final QName dateTimeTypeDef20130715) {
        final QNameModule monitoring19700101 = QNameModule.of("urn:ietf:params:xml:ns:yang:ietf-netconf-monitoring");
        QName lockedTime = QName.create(monitoring19700101, "locked-time");

        var monitoringModule19700101 = context.findModule("ietf-netconf-monitoring").orElseThrow();
        var leafLockedTime = monitoringModule19700101.getDataChildByName(lockedTime);
        QName lockedTimeTypeQName = assertInstanceOf(LeafSchemaNode.class, leafLockedTime).getType().getQName();
        assertEquals(dateTimeTypeDef20130715, lockedTimeTypeQName);

        var imports = monitoringModule19700101.getImports();
        assertEquals(1, imports.size());
        var monitoringImport = imports.iterator().next();
        assertEquals(Unqualified.of("ietf-yang-types"), monitoringImport.getModuleName());
        assertEquals(Optional.of(rev20130715), monitoringImport.getRevision());
    }

    private static boolean findTypeDef(final Module module, final QName typedef) {
        for (var typeDefinition : module.getTypeDefinitions()) {
            if (typeDefinition.getQName().equals(typedef)) {
                return true;
            }
        }
        return false;
    }

    @Test
    void nodeTest() {
        final var context = assertEffectiveModelDir("/semantic-statement-parser/multiple-revisions/node-test");

        QName root = QName.create("foo", "2016-04-06", "foo-root");
        QName container20160404 = QName.create("foo", "2016-04-06", "con20160404");
        assertInstanceOf(ContainerSchemaNode.class, context.findDataTreeChild(root, container20160404).orElse(null));

        QName container20160405 = QName.create("foo", "2016-04-06", "con20160405");
        assertInstanceOf(ContainerSchemaNode.class, context.findDataTreeChild(root, container20160405).orElse(null));

        QName container20160406 = QName.create("foo", "2016-04-06", "con20160406");
        assertEquals(Optional.empty(), context.findDataTreeChild(root, container20160406));
    }
}
