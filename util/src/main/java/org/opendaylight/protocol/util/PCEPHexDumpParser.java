/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.protocol.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CharStreams;

/**
 * Parses PCEP messages from a text file. Messages need to follow this formatting:
 * 
 * Received PCEP Open message. Length:28.
 * 
 * 20 01 00 1c 01 10 00 18 20 1e 78 03 00 10 00 04 00 00 00 05 00 1a 00 04 00 00 00 b4
 */
public final class PCEPHexDumpParser {
	private static final int MINIMAL_LENGTH = 4;
	private static final Logger LOG = LoggerFactory.getLogger(PCEPHexDumpParser.class);
	private static final String LENGTH = "LENGTH:";

	private PCEPHexDumpParser() {

	}

	public static List<byte[]> parseMessages(final File file) throws IOException {
		Preconditions.checkArgument(file != null, "Filename cannot be null");
		return parseMessages(new FileInputStream(file));
	}

	public static List<byte[]> parseMessages(final InputStream is) throws IOException {
		Preconditions.checkNotNull(is);
		try (InputStreamReader isr = new InputStreamReader(is)) {
			return parseMessages(CharStreams.toString(isr));
		} finally {
			is.close();
		}
	}

	private static List<byte[]> parseMessages(final String c) {
		final String content = clearWhiteSpaceToUpper(c);

		final List<byte[]> messages = Lists.newLinkedList();
		int idx = content.indexOf(LENGTH, 0);
		while (idx > -1) {
			// next chars are final length, ending with '.'
			final int lengthIdx = idx + LENGTH.length();
			final int messageIdx = content.indexOf('.', lengthIdx);

			final int length = Integer.valueOf(content.substring(lengthIdx, messageIdx));
			final int messageEndIdx = idx + length * 2;

			// Assert that message is longer than minimum 4(header.length == 4)
			// If length in PCEP message would be 0, loop would never end
			Preconditions.checkArgument(length >= MINIMAL_LENGTH, "Invalid message at index " + idx + ", length atribute is lower than "
					+ MINIMAL_LENGTH);

			final String hexMessage = content.substring(idx, messageEndIdx);
			byte[] message = null;
			try {
				message = Hex.decodeHex(hexMessage.toCharArray());
			} catch (final DecoderException e) {
				new RuntimeException(e);
			}
			messages.add(message);
			idx = messageEndIdx;
			idx = content.indexOf(LENGTH, idx);
		}
		LOG.info("Succesfully extracted {} messages", messages.size());
		return messages;
	}

	private static String clearWhiteSpaceToUpper(final String line) {
		return line.replaceAll("\\s", "").toUpperCase();
	}
}
