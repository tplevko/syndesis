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
package io.syndesis.integration.runtime.capture;

import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.model.integration.Step;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;

/**
 * Used to capture the out messages of processors with configured ids.  The messages are placed into
 * a map stored in the camel exchange property using the processor id as the map key.
 */
public class OutMessageCaptureProcessor implements Processor {

    public static final String CAPTURED_OUT_MESSAGES_MAP = "Syndesis.CAPTURED_OUT_MESSAGES_MAP";

    private final String id;

    public OutMessageCaptureProcessor(Step step) {
        this.id = step.getId().orElse(null);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (this.id != null) {
            Message message = exchange.hasOut() ? exchange.getOut() : exchange.getIn();

            if( message != null ) {
                Message copy = message.copy();
                Map<String, Message> outMessagesMap = getCapturedMessageMap(exchange);
                outMessagesMap.put(this.id, copy);
            }
        }
    }

    public static Map<String, Message> getCapturedMessageMap(Exchange exchange) {
        Map<String, Message> outMessagesMap = exchange.getProperty(CAPTURED_OUT_MESSAGES_MAP, Map.class);
        if( outMessagesMap == null ) {
            outMessagesMap = new HashMap<>();
            exchange.setProperty(CAPTURED_OUT_MESSAGES_MAP, outMessagesMap);
        }
        return outMessagesMap;
    }
}
