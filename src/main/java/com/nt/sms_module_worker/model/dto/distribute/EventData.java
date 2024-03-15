package com.nt.sms_module_worker.model.dto.distribute;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventData {
    @JsonProperty("channel")
    private String channel;

    @JsonProperty("eventItem")
    private EventItem eventItem;

}
