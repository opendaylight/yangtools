package org.opendaylight.yangtools.concepts;


import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;

import java.io.Serializable;

@Beta
public class Tag implements Comparable<Tag>, Serializable {

    private String prefix;
    private String tagValue;

    public Tag(String prefix, String tagValue) {
        this.prefix = prefix;
        this.tagValue = tagValue;
    }

    public static @NonNull Tag create(String prefix, String tagValue) {
//        return new Tag("ietf:", "pantheon-tag");
        return new Tag(prefix, tagValue);
    }

    public static @NonNull Tag valueOf(final @NonNull String str) {
//        if (str.contains(":")) {
            String[] splits = str.split(":");
            return create(splits[0], splits[1]);
//        }
//        return null;
    }

    @Override
    public int compareTo(Tag o) {
        return 0;
    }

    @Override
    public String toString() {
        return  prefix + ":" + tagValue;
    }
}
