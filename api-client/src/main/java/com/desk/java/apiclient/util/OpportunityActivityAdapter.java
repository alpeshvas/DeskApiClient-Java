/*
 * Copyright (c) 2016, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of Salesforce.com, Inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package com.desk.java.apiclient.util;

import com.desk.java.apiclient.model.*;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Type;
import java.util.Date;

/**
 * <p>
 *     Deserializes {@link IOpportunityActivity} marker objects to either {@link OpportunitySystemEvent}
 *     objects or {@link com.desk.java.apiclient.model.OpportunityActivity} subclasses.
 * </p>
 *
 * Created by Matt Kranzler on 12/29/15.
 * Copyright (c) 2016 Desk.com. All rights reserved.
 */
public class OpportunityActivityAdapter implements JsonDeserializer<IOpportunityActivity> {

    static final String LINKS = "_links";
    static final String SELF = "self";
    static final String CLASS = "class";
    static final String HISTORY = "history";
    static final String OPPORTUNITY_ATTACHMENT = "opportunity_attachment";
    static final String TYPE = "type";

    private final Gson gson;

    public OpportunityActivityAdapter() {
        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, ISO8601DateAdapter.TYPE_ADAPTER)
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();
    }

    @Override
    public IOpportunityActivity deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement instanceof JsonObject) {
            JsonObject element = (JsonObject) jsonElement;
            JsonObject links = element.getAsJsonObject(LINKS);
            if (links != null) {
                JsonObject self = links.getAsJsonObject(SELF);
                if (self != null) {
                    JsonPrimitive clazz = self.getAsJsonPrimitive(CLASS);

                    // if the class is of type 'history' then it's a system event
                    if (HISTORY.equalsIgnoreCase(clazz.getAsString())) {
                        return gson.fromJson(jsonElement, OpportunitySystemEvent.class);

                    // if the class is of type 'opportunity_activity' or 'attachment'
                    } else {
                        JsonPrimitive activityType = element.getAsJsonPrimitive(TYPE);

                        if (OpportunityActivityType.CALL.name().equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityCall.class);
                        } else if (OpportunityActivityType.EMAIL.name().equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityEmail.class);
                        } else if (OpportunityActivityType.EVENT.name().equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityEvent.class);
                        } else if (OpportunityActivityType.NOTE.name().equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityNote.class);
                        } else if (OpportunityActivityType.TASK.name().equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityTask.class);
                        } else if (OPPORTUNITY_ATTACHMENT.equalsIgnoreCase(activityType.getAsString())) {
                            return gson.fromJson(jsonElement, OpportunityAttachment.class);
                        }
                    }
                }
            }
        }

        // if we get here we don't know what this is
        return null;
    }
}
