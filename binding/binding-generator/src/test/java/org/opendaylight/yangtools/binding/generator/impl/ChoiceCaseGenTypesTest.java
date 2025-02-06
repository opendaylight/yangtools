/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsInterface;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class ChoiceCaseGenTypesTest {
    @Test
    void choiceCaseResolvingTypeTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/choice-case-type-test-models"));

        assertNotNull(genTypes, "genTypes is null");
        assertEquals(41, genTypes.size());

        // test for file choice-monitoring
        var pcgPref = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.choice.monitoring.rev130701."
                + "netconf.state.datastores.datastore.locks";
        GeneratedType genType = null;

        // choice
        checkGeneratedType(genTypes, "LockType", pcgPref);

        // case
        genType = checkGeneratedType(genTypes, "GlobalLock", pcgPref + ".lock.type");
        SupportTestUtil.containsMethods(genType, new NameTypePattern("getGlobalLock", "GlobalLock"));
        containsInterface("LockType", genType);

        // case
        genType = checkGeneratedType(genTypes, "PartialLock", pcgPref + ".lock.type");
        containsMethods(genType, new NameTypePattern("getPartialLock", "Map<PartialLockKey,PartialLock>"));
        containsInterface("LockType", genType);

        // case
        genType = checkGeneratedType(genTypes, "Fingerprint", pcgPref + ".lock.type");
        containsMethods(genType, new NameTypePattern("getAlgorithmAndHash", "AlgorithmAndHash"));
        containsInterface("LockType", genType);

        // choice
        genType = checkGeneratedType(genTypes, "AlgorithmAndHash", pcgPref + ".lock.type.fingerprint");

        // case
        genType = checkGeneratedType(genTypes, "Md5", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getMd5", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // case
        genType = checkGeneratedType(genTypes, "Sha1", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getSha1", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // case
        genType = checkGeneratedType(genTypes, "Sha224", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getSha224", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // case
        genType = checkGeneratedType(genTypes, "Sha256", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getSha256", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // case
        genType = checkGeneratedType(genTypes, "Sha384", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getSha384", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // case
        genType = checkGeneratedType(genTypes, "Sha512", pcgPref + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(genType, new NameTypePattern("getSha512", "TlsFingerprintType"));
        containsInterface("AlgorithmAndHash", genType);

        // test for file augment-monitoring
        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type"
        pcgPref = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.augment.monitoring.rev130701";

        // choice
        genType = checkGeneratedType(genTypes, "AutonomousLock", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(genType, new NameTypePattern("getAutonomousDef", "AutonomousDef"));
        containsInterface("LockType", genType);

        // choice
        genType = checkGeneratedType(genTypes, "AnonymousLock", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(genType, new NameTypePattern("getLockTime", "Uint32"));
        containsInterface("LockType", genType);

        // choice
        genType = checkGeneratedType(genTypes, "LeafAugCase", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(genType, new NameTypePattern("getLeafAugCase", "String"));
        containsInterface("LockType", genType);

        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type/nm:partial-lock"
        // {
        // case
        genType = checkGeneratedType(genTypes, "PartialLock1", pcgPref);
        containsMethods(genType, new NameTypePattern("getAugCaseByChoice", "AugCaseByChoice"));
        containsInterface("Augmentation<PartialLock>", genType);

        // choice
        genType = checkGeneratedType(genTypes, "AugCaseByChoice", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock");

        // case
        genType = checkGeneratedType(genTypes, "Foo", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice");
        containsMethods(genType, new NameTypePattern("getFoo", "String"));
        containsInterface("AugCaseByChoice", genType);

        // case
        genType = checkGeneratedType(genTypes, "Bar", pcgPref
                + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice");
        containsMethods(genType, new NameTypePattern("getBar", "Boolean"));
        containsInterface("AugCaseByChoice", genType);

        // augment "/nm:netconf-state/nm:datastores/nm:datastore" {
        genType = checkGeneratedType(genTypes, "Datastore1", pcgPref);
        containsMethods(genType, new NameTypePattern("getStorageFormat", "StorageFormat"));
        containsInterface("Augmentation<Datastore>", genType);

        // choice
        genType = checkGeneratedType(genTypes, "StorageFormat", pcgPref + ".netconf.state.datastores.datastore");

        // case
        genType = checkGeneratedType(genTypes, "UnknownFiles", pcgPref
                + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getFiles", "Map<FilesKey,Files>"));
        containsInterface("StorageFormat", genType);

        // case
        genType = checkGeneratedType(genTypes, "Xml", pcgPref + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getXmlDef", "XmlDef"));
        containsInterface("StorageFormat", genType);

        // case
        genType = checkGeneratedType(genTypes, "Yang", pcgPref + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getYangFileName", "String"));
        containsInterface("StorageFormat", genType);
    }

    private static GeneratedType checkGeneratedType(final List<GeneratedType> genTypes, final String genTypeName,
            final String packageName, final int occurences) {
        GeneratedType searchedGenType = null;
        int searchedGenTypeCounter = 0;
        for (var genType : genTypes) {
            if (!(genType instanceof GeneratedTransferObject)) {
                if (genType.getName().equals(genTypeName) && genType.getPackageName().equals(packageName)) {
                    searchedGenType = genType;
                    searchedGenTypeCounter++;
                }
            }
        }
        assertNotNull(searchedGenType, "Generated type " + genTypeName + " wasn't found");
        assertEquals(occurences, searchedGenTypeCounter,
            genTypeName + " generated type has incorrect number of occurences.");
        return searchedGenType;

    }

    private static GeneratedType checkGeneratedType(final List<GeneratedType> genTypes, final String genTypeName,
        final String packageName) {
        return checkGeneratedType(genTypes, genTypeName, packageName, 1);
    }
}
