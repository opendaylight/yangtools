/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.generator.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.opendaylight.yangtools.binding.generator.impl.SupportTestUtil.containsMethods;

import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.AugmentationArchetype;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;

class ChoiceCaseGenTypesTest {
    @Test
    void choiceCaseResolvingTypeTest() {
        final var genTypes = DefaultBindingGenerator.generateFor(
            YangParserTestUtils.parseYangResourceDirectory("/choice-case-type-test-models"));

        assertNotNull(genTypes, "genTypes is null");
        assertEquals(41, genTypes.size());

        // test for file choice-monitoring
        final var locks = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.choice.monitoring.rev130701."
                + "netconf.state.datastores.datastore.locks";

        // choice
        assertChoice(genTypes, "LockType", locks);

        // case
        var globalLock = checkGeneratedCase(genTypes, "GlobalLock", locks + ".lock.type");
        SupportTestUtil.containsMethods(globalLock, new NameTypePattern("getGlobalLock", "GlobalLock"));
        assertEquals("LockType", globalLock.choiceName().simpleName());

        // case
        final var partialLock = checkGeneratedCase(genTypes, "PartialLock", locks + ".lock.type");
        containsMethods(partialLock, new NameTypePattern("getPartialLock", "Map<PartialLockKey,PartialLock>"));
        assertEquals("LockType", partialLock.choiceName().simpleName());

        // case
        final var fingerprint = checkGeneratedCase(genTypes, "Fingerprint", locks + ".lock.type");
        containsMethods(fingerprint, new NameTypePattern("getAlgorithmAndHash", "AlgorithmAndHash"));
        assertEquals("LockType", fingerprint.choiceName().simpleName());

        // choice
        assertChoice(genTypes, "AlgorithmAndHash", locks + ".lock.type.fingerprint");

        // case
        final var md5 = checkGeneratedCase(genTypes, "Md5", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(md5, new NameTypePattern("getMd5", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", md5.choiceName().simpleName());

        // case
        final var sha1 = checkGeneratedCase(genTypes, "Sha1", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(sha1, new NameTypePattern("getSha1", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", sha1.choiceName().simpleName());

        // case
        final var sha224 = checkGeneratedCase(genTypes, "Sha224", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(sha224, new NameTypePattern("getSha224", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", sha224.choiceName().simpleName());

        // case
        final var sha256 = checkGeneratedCase(genTypes, "Sha256", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(sha256, new NameTypePattern("getSha256", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", sha256.choiceName().simpleName());

        // case
        final var sha384 = checkGeneratedCase(genTypes, "Sha384", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(sha384, new NameTypePattern("getSha384", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", sha384.choiceName().simpleName());

        // case
        final var sha512 = checkGeneratedCase(genTypes, "Sha512", locks + ".lock.type.fingerprint.algorithm.and.hash");
        containsMethods(sha512, new NameTypePattern("getSha512", "TlsFingerprintType"));
        assertEquals("AlgorithmAndHash", sha512.choiceName().simpleName());

        // test for file augment-monitoring
        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type"
        final var augment = "org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.augment.monitoring.rev130701";

        // case
        final var autonomousLock = checkGeneratedCase(genTypes, "AutonomousLock",
            augment + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(autonomousLock, new NameTypePattern("getAutonomousDef", "AutonomousDef"));
        assertEquals("LockType", autonomousLock.choiceName().simpleName());

        // case
        final var anonymousLock = checkGeneratedCase(genTypes, "AnonymousLock",
            augment + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(anonymousLock, new NameTypePattern("getLockTime", "Uint32"));
        assertEquals("LockType", anonymousLock.choiceName().simpleName());

        // choice
        var genType = checkGeneratedCase(genTypes, "LeafAugCase",
            augment + ".netconf.state.datastores.datastore.locks.lock.type");
        containsMethods(genType, new NameTypePattern("getLeafAugCase", "String"));
        assertEquals("LockType", genType.choiceName().simpleName());

        // augment
        // "/nm:netconf-state/nm:datastores/nm:datastore/nm:locks/nm:lock-type/nm:partial-lock"
        // {
        // case
        final var partialLock1 = checkGeneratedType(AugmentationArchetype.class, genTypes, "PartialLock1", augment);
        containsMethods(partialLock1, new NameTypePattern("getAugCaseByChoice", "AugCaseByChoice"));
        assertEquals("PartialLock", partialLock1.targetName().simpleName());

        // choice
        assertChoice(genTypes, "AugCaseByChoice",
            augment + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock");

        // case
        final var foo = checkGeneratedCase(genTypes, "Foo",
            augment + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice");
        containsMethods(foo, new NameTypePattern("getFoo", "String"));
        assertEquals("AugCaseByChoice", foo.choiceName().simpleName());

        // case
        final var bar = checkGeneratedCase(genTypes, "Bar",
            augment + ".netconf.state.datastores.datastore.locks.lock.type.partial.lock.aug._case.by.choice");
        containsMethods(bar, new NameTypePattern("getBar", "Boolean"));
        assertEquals("AugCaseByChoice", bar.choiceName().simpleName());

        // augment "/nm:netconf-state/nm:datastores/nm:datastore" {
        final var datastore1 = checkGeneratedType(AugmentationArchetype.class, genTypes, "Datastore1", augment);
        containsMethods(datastore1, new NameTypePattern("getStorageFormat", "StorageFormat"));
        assertEquals("Datastore", datastore1.targetName().simpleName());

        // choice
        assertChoice(genTypes, "StorageFormat", augment + ".netconf.state.datastores.datastore");

        // case
        genType = checkGeneratedCase(genTypes, "UnknownFiles",
            augment + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getFiles", "Map<FilesKey,Files>"));
        assertEquals("StorageFormat", genType.choiceName().simpleName());

        // case
        genType = checkGeneratedCase(genTypes,
            "Xml", augment + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getXmlDef", "XmlDef"));
        assertEquals("StorageFormat", genType.choiceName().simpleName());

        // case
        genType = checkGeneratedCase(genTypes, "Yang", augment + ".netconf.state.datastores.datastore.storage.format");
        containsMethods(genType, new NameTypePattern("getYangFileName", "String"));
        assertEquals("StorageFormat", genType.choiceName().simpleName());
    }

    private static void assertChoice(final List<Archetype> genTypes, final String simpleName, final String pkgName) {
        final var choices = genTypes.stream()
            .filter(ChoiceInArchetype.class::isInstance)
            .map(ChoiceInArchetype.class::cast)
            .filter(archetype -> simpleName.equals(archetype.simpleName()) && pkgName.equals(archetype.packageName()))
            .toList();
        assertEquals(1, choices.size());
    }

    private static CaseObjectArchetype checkGeneratedCase(final List<Archetype> types, final String simpleName,
            final String pkgName) {
        return checkGeneratedType(CaseObjectArchetype.class, types, simpleName, pkgName);
    }

    private static <A extends DataContainerArchetype> A checkGeneratedType(final Class<A> clazz,
            final List<Archetype> types, final String simpleName, final String pkgName) {
        return checkGeneratedType(clazz, types, simpleName, pkgName, 1);
    }

    private static <A extends DataContainerArchetype> A checkGeneratedType(final Class<A> clazz,
            final List<Archetype> types, final String simpleName, final String pkgName, final int occurences) {
        @Nullable A found = null;
        int count = 0;
        for (var type : types) {
            if (type.simpleName().equals(simpleName) && type.packageName().equals(pkgName)) {
                found = assertInstanceOf(clazz, type);
                count++;
            }
        }

        assertNotNull(found, "Generated type " + simpleName + " wasn't found");
        assertEquals(occurences, count, simpleName + " generated type has incorrect number of occurences.");
        return found;
    }
}
