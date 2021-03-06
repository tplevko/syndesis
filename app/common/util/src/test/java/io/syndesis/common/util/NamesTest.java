/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.common.util;

import java.util.Arrays;

import io.syndesis.common.util.Names;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class NamesTest {

    @Parameter(1)
    public String integrationName;

    @Parameter(0)
    public String projectName;

    @Test
    public void testGetSanitizedName() throws Exception {
        assertEquals(projectName, Names.sanitize(integrationName));
    }

    @Parameters
    public static Iterable<Object[]> integrationToProjectNames() {
        return Arrays.asList(pair("bla", "bla"),
            pair("0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789",
                "012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890"),
            pair("how-are-you", "how are you?"), //
            pair("yet-sth-with-spaces", "yet sth  with !#ä spaceS"),
            pair("aaa-big-and-small-zzz", "AaA BIG and Small ZzZ"),
            pair("twitter-mention-salesforce-upsert-contact", "Twitter Mention -> Salesforce upsert contact"),
            pair("-twitter-mention-salesforce-upsert-contact-",
                "??? Twitter Mention <-> Salesforce upsert contact !!!"),
            pair("-s-p-a-c-e-the-final-frontier", "    s   p  a  c  e   , the final frontier"));
    }

    private static Object[] pair(final String projectName, final String integrationName) {
        return new Object[] {projectName, integrationName};
    }
}
