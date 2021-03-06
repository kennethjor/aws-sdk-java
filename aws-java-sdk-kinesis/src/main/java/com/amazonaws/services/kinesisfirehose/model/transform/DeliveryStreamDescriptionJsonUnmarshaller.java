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

package com.amazonaws.services.kinesisfirehose.model.transform;

import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.services.kinesisfirehose.model.*;
import com.amazonaws.transform.SimpleTypeJsonUnmarshallers.*;
import com.amazonaws.transform.*;

import com.fasterxml.jackson.core.JsonToken;
import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * DeliveryStreamDescription JSON Unmarshaller
 */
public class DeliveryStreamDescriptionJsonUnmarshaller implements
        Unmarshaller<DeliveryStreamDescription, JsonUnmarshallerContext> {

    public DeliveryStreamDescription unmarshall(JsonUnmarshallerContext context)
            throws Exception {
        DeliveryStreamDescription deliveryStreamDescription = new DeliveryStreamDescription();

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
                if (context.testExpression("DeliveryStreamName", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setDeliveryStreamName(StringJsonUnmarshaller
                                    .getInstance().unmarshall(context));
                }
                if (context.testExpression("DeliveryStreamARN", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setDeliveryStreamARN(StringJsonUnmarshaller
                                    .getInstance().unmarshall(context));
                }
                if (context.testExpression("DeliveryStreamStatus", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setDeliveryStreamStatus(StringJsonUnmarshaller
                                    .getInstance().unmarshall(context));
                }
                if (context.testExpression("VersionId", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setVersionId(StringJsonUnmarshaller.getInstance()
                                    .unmarshall(context));
                }
                if (context.testExpression("CreateTimestamp", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setCreateTimestamp(DateJsonUnmarshaller
                                    .getInstance().unmarshall(context));
                }
                if (context.testExpression("LastUpdateTimestamp", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setLastUpdateTimestamp(DateJsonUnmarshaller
                                    .getInstance().unmarshall(context));
                }
                if (context.testExpression("Destinations", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setDestinations(new ListUnmarshaller<DestinationDescription>(
                                    DestinationDescriptionJsonUnmarshaller
                                            .getInstance()).unmarshall(context));
                }
                if (context.testExpression("HasMoreDestinations", targetDepth)) {
                    context.nextToken();
                    deliveryStreamDescription
                            .setHasMoreDestinations(BooleanJsonUnmarshaller
                                    .getInstance().unmarshall(context));
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

        return deliveryStreamDescription;
    }

    private static DeliveryStreamDescriptionJsonUnmarshaller instance;

    public static DeliveryStreamDescriptionJsonUnmarshaller getInstance() {
        if (instance == null)
            instance = new DeliveryStreamDescriptionJsonUnmarshaller();
        return instance;
    }
}
