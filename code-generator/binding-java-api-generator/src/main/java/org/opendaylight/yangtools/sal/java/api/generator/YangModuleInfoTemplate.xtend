package org.opendaylight.yangtools.sal.java.api.generator

import org.opendaylight.yangtools.yang.model.api.Module
import org.opendaylight.yangtools.binding.generator.util.BindingGeneratorUtil
import org.opendaylight.yangtools.yang.binding.YangModuleInfo
import java.io.InputStream
import com.google.common.collect.ImmutableSet

/**
 * Template for generating JAVA interfaces. 
 */
class YangModuleInfoTemplate extends BaseTemplate {

    val Module module;

    new(Module module) {
        super(null)
        if (module == null) {
            throw new IllegalArgumentException("Module reference cannot be NULL!")
        }
        this.module = module;
    }


    // TODO: import statements
    def override body() '''
        package «BindingGeneratorUtil.moduleNamespaceToPackageName(module)» ;

        public class YangModuleInfoImpl implements «YangModuleInfo.importedName» {

        @Override
        public «String.importedName» getName() {
            return «module.name»;
        }

        @Override
        public «String.importedName» getRevision() {
            return «module.revision.toString»;
        } 

        @Override
        public «String.importedName» getNamespace() {
            return «module.namespace.toString»;
        } 

        @Override
        public «InputStream.importedName» getModuleSourceStream() {
            return «module.moduleSourceStream»;
        }

        @Override
        public «ImmutableSet.importedName»<«YangModuleInfo.importedName»> getImportedModules() {
            return null;
        }

        }
    '''

}

