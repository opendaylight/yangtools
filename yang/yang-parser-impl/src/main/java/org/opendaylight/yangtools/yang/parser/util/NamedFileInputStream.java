package org.opendaylight.yangtools.yang.parser.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public final class NamedFileInputStream extends FileInputStream {
    private final File file;

    public NamedFileInputStream(File file) throws FileNotFoundException {
        super(file);
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + file + "}";
    }

}
