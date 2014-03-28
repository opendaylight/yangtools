package org.opendaylight.yangtools.yang.parser.impl;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.opendaylight.yangtools.yang.model.api.Module;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

public class YangParserIncludedSubmoduleTest {

	@Test
	public void testIncludedSubmodules() throws URISyntaxException {
		final String EXPECTED_NAMESPACE = "http://cisco.com/ns/yang/Cisco-IOS-XR-ipv4-acl-cfg";
		URI directoryPath = getClass().getResource("/submodule-include")
				.toURI();
		File directory = new File(directoryPath);

		Set<Module> modules = new YangParserImpl().parseYangModels(Arrays
				.asList(directory.listFiles()));
		assertEquals(4, modules.size());

		Module[] allModules = new Module[modules.size()];
		modules.toArray(allModules);

		Module moduleAclCfg = TestUtils.findModule(
				new HashSet<Module>(modules), "Cisco-IOS-XR-ipv4-acl-cfg");

		Set<TypeDefinition<?>> nextHopType = moduleAclCfg.getTypeDefinitions();
		TypeDefinition<?> typeDef = nextHopType.iterator().next();
		String typeDefNamespace = typeDef.getQName().getNamespace().toString();
		assertEquals(EXPECTED_NAMESPACE, typeDefNamespace);
	}
}
