package com.async.mail.endpoint.event.consumer.model;

import com.async.mail.PojaGenerated;
import com.async.mail.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
