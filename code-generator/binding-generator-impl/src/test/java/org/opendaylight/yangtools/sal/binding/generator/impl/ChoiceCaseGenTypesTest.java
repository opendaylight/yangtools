/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.generator.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.opendaylight.yangtools.sal.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.sal.binding.generator.impl.SupportTestUtil.containsMethods;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;
import org.opendaylight.yangtools.sal.binding.generator.api.BindingGenerator;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.parser.api.YangModelParser;
import org.opendaylight.yangtools.yang.parser.impl.YangParserImpl;

public class ChoiceCaseGenTypesTest {

    private final static List<File> yangModels = new ArrayList<>();
    private final static URL yangModelsFolder = AugmentedTypeTest.class.getResource("/choice-case-type-test-models")
            ;

    @BeforeClass
    public static void loadTestResources() throws URISyntaxException {
        final File augFolder = new File(yangModelsFolder.toURI());
        for (final File fileEntry : augFolder.listFiles()) {
            if (fileEntry.isFile()) {
                yangModels.add(fileEntry);
            }
        }
    }

    private static GeneratedType checkGeneratedType(List<Type> genTypes, String genTypeName, String packageName,
            int occurences) {
        GeneratedType searchedGenType = null;
        int searchedGenTypeCounter = 0;
        for (Type type : genTypes) {
            if (type instanceof GeneratedType && !(type instanceof GeneratedTransferObject)) {
                GeneratedType genType = (GeneratedType) type;
                if (genType.getName().equals(genTypeName) && genType.getPackageName().equals(packageName)) {
                    searchedGenType = genType;
                    searchedGenTypeCounter++;
                }
            }
        }
        assertNotNull("Generated type " + genTypeName + " wasn't found", searchedGenType);
        assertEquals(genTypeName + " generated type has incorrect number of occurences.", occurences,
                searchedGenTypeCounter);
        return searchedGenType;

    }

    private static GeneratedType checkGeneratedType(List<Type> genTypes, String genTypeName, String packageName) {
        return checkGeneratedType(genTypes, genTypeName, packageName, 1);
    }

    @Test
    public void choiceCaseResolvingTypeTest() {
        final YangModelParser parser = new YangParserImpl();
        final Set<Module> modules = parser.parseYangModels(yangModels);

        final SchemaContext context = parser.resolveSchemaContext(modules);

        assertNotNull("context is null", context);
        final BindingGenerator bindingGen = new BindingGeneratorImpl();
        final List<Type> genTypes = bindingGen.generateTypes(context);

        assertNotNull("genTypes is null", genTypes);
        assertFalse("genTypes is empty", genTypes.isEmpty());

        // test for file choice-monitoring
        String pcgPref = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.choice.monitoring.rev130701.netconf.state.datastores.datastore.locks";
        GeneratedType genType = null;

        checkGeneratedType(genTypes, "LockType", pcgPref); // choice

        genType = checkGeneratedType(genTypes, "GlobalLock", pcgPref + ".lock.type"); // case
        containsMethods(genType, new NameTypePattern("getGlobalLock", "GlobalLock"));
        containsInterface("LockType", genType);

        genType = checkGeneratedType(genTypes, "PartialLock", pcgPref + ".lock.type"); // case
        containsMethods(genType, new NameTypePattern("getPartialLock", "List<PartialLock>"));
        containsInterface("LockType", genType);

        genType = checkGeneratedType(genTypes, "Fingerprint", pcgPref + ".lock.type"); // case
        containsMethods(genType, new NameTypePattern("getAlgorithmAndHash", "AlgorithmAndHash"));
        containsInterface("LockType", genType);

        genType = checkGeneratedType(genTypes, "AlgorithmAndHash", pcgPref + ".lock.type.fingerprint"); // choice

        genType = checkGeneratedType(genTypes, "Md5", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getMd5", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        genType = checkGeneratedType(genTypes, "Sha1", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getSha1", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        genType = checkGeneratedType(genTypes, "Sha224", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getSha224", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        genType = checkGeneratedType(genTypes, "Sha256", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getSha256", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        genType = checkGeneratedType(genTypes, "Sha384", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getSha384", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        genType = checkGeneratedType(genTypes, "Sha512", pcgPref + ".lock.type.fingerprint.algorithm.and.hash"); // case
        containsMethods(genType, new NameTypePattern("getSha512", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // test for file augment-monitoring
        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type"
        pcgPref = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.augment.monitoring.rev130701";
        genType = null;

        genType = checkGeneratedType(genTypes, "AutonomousLock", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type"); // choice
        containsMethods(genType, new NameTypePattern("getAutonomousDef", "AutonomousDef"));
        containsInterface("LockType", genType);

        genType = checkGeneratedType(genTypes, "AnonymousLock", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type"); // choice
        containsMethods(genType, new NameTypePattern("getLockTime", "Long"));
        containsInterface("LockType", genType);

        genType = checkGeneratedType(genTypes, "LeafAugCase", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type"); // choice
        // FIXME
        containsMethods(genType, new NameTypePattern("getLeafAugCase", "String"));
        containsInterface("LockType", genType);

        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type/nm:partial-lock"
        // {
        genType = checkGeneratedType(genTypes, "PartialLock1", pcgPref); // case
        containsMethods(genType, new NameTypePattern("getAugCaseByChoice", "AugCaseByChoice"));
        containsInterface("Augmentation<PartialLock>", genType);

        genType = checkGeneratedType(genTypes, "AugCaseByChoice", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock"); // choice

        genType = checkGeneratedType(genTypes, "Foo", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice"); // case
        containsMethods(genType, new NameTypePattern("getFoo", "String"));
        containsInterface("AugCaseByChoice", genType);

        genType = checkGeneratedType(genTypes, "Bar", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice"); // case
        containsMethods(genType, new NameTypePattern("isBar", "Boolean"));
        containsInterface("AugCaseByChoice", genType);

        // augment "/nm:netconf-state/nm:datastores/nm:datastore" {
        genType = checkGeneratedType(genTypes, "Datastore1", pcgPref);
        containsMethods(genType, new NameTypePattern("getStorageFormat", "StorageFormat"));
        containsInterface("Augmentation<Datastore>", genType);

        genType = checkGeneratedType(genTypes, "StorageFormat", pcgPref + ".netconf.state.datastores.datastore"); // choice

        genType = checkGeneratedType(genTypes, "UnknownFiles", pcgPref
                + ".netconf.state.datastores.datastore.storage.format"); // case
        containsMethods(genType, new NameTypePattern("getFiles", "List<Files>"));
        containsInterface("StorageFormat", genType);

        genType = checkGeneratedType(genTypes, "Xml", pcgPref + ".netconf.state.datastores.datastore.storage.format"); // case
        containsMethods(genType, new NameTypePattern("getXmlDef", "XmlDef"));
        containsInterface("StorageFormat", genType);

        genType = checkGeneratedType(genTypes, "Yang", pcgPref + ".netconf.state.datastores.datastore.storage.format"); // case
        containsMethods(genType, new NameTypePattern("getYangFileName", "String"));
        containsInterface("StorageFormat", genType);

    }
}
