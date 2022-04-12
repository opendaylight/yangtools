package org.opendaylight.yangtools.rfc8819.model.api;

import org.junit.Test;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
public class TagTest {

    @Test
    public void testIfIsIetfTag() {
        Stream.of("ietf:first-tag", "ietf:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(ModuleTagTypes.IETF.name(), tag.getTagType()));
    }

    @Test
    public void testIfIsVendorTag() {
        Stream.of("vendor:first-tag", "vendor:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(ModuleTagTypes.VENDOR.name(), tag.getTagType()));
    }

    @Test
    public void testIfIsUserTag() {
        Stream.of("user:first-tag", "user:second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(ModuleTagTypes.USER.name(), tag.getTagType()));
    }

    @Test
    public void testIfIsReservedTag() {
        Stream.of("first-tag", "second:tag:tag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(ModuleTagTypes.RESERVED.name(), tag.getTagType()));
    }

    @Test
    public void testIfIsTagInvalid() {
        Stream.of("", "\n", "\t", "ietf:tag\ntag")
                .map(Tag::valueOf).forEach(tag -> assertEquals(ModuleTagTypes.INVALID.name(), tag.getTagType()));
    }

}
