/*
 * Copyright 2010-2016 Amazon.com, Inc. or its affiliates. All Rights
 * Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.services.devicefarm.model.transform;

import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.devicefarm.model.*;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.*;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * Radios JSON Unmarshaller
 */
public class RadiosJsonUnmarshaller implements
        Unmarshaller<Radios, JsonUnmarshallerContext> {

    public Radios unmarshall(JsonUnmarshallerContext context) throws Exception {
        Radios radios = new Radios();

        int originalDepth = context.getCurrentDepth();
        String currentParentElement = context.getCurrentParentElement();
        int targetDepth = originalDepth + 1;

        JsonToken token = context.getCurrentToken();
        if (token == null)
            token = context.nextToken();
        if (token == VALUE_NULL)
            return null;

        while (true) {
            if (token == null)
                break;

            if (token == FIELD_NAME || token == START_OBJECT) {
                if (context.testExpression("wifi", targetDepth)) {
                    context.nextToken();
                    radios.setWifi(BooleanJsonUnmarshaller.getInstance()
                            .unmarshall(context));
                }
                if (context.testExpression("bluetooth", targetDepth)) {
                    context.nextToken();
                    radios.setBluetooth(BooleanJsonUnmarshaller.getInstance()
                            .unmarshall(context));
                }
                if (context.testExpression("nfc", targetDepth)) {
                    context.nextToken();
                    radios.setNfc(BooleanJsonUnmarshaller.getInstance()
                            .unmarshall(context));
                }
                if (context.testExpression("gps", targetDepth)) {
                    context.nextToken();
                    radios.setGps(BooleanJsonUnmarshaller.getInstance()
                            .unmarshall(context));
                }
            } else if (token == END_ARRAY || token == END_OBJECT) {
                if (context.getLastParsedParentElement() == null
                        || context.getLastParsedParentElement().equals(
                                currentParentElement)) {
                    if (context.getCurrentDepth() <= originalDepth)
                        break;
                }
            }
            token = context.nextToken();
        }

        return radios;
    }

    private static RadiosJsonUnmarshaller instance;

    public static RadiosJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new RadiosJsonUnmarshaller();
        return instance;
    }
}
