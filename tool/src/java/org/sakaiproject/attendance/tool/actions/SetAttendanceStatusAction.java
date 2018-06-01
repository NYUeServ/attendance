package org.sakaiproject.attendance.tool.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.fasterxml.jackson.databind.JsonNode;
import org.sakaiproject.attendance.model.Status;

import java.io.Serializable;

public class SetAttendanceStatusAction implements Action, Serializable {

    private static final long serialVersionUID = 1L;

    public SetAttendanceStatusAction() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        String userid = params.get("userid").textValue();
        Long eventId = params.get("eventid").asLong();
        Status status = Status.valueOf(params.get("status").textValue());

        return new EmptyOkResponse();
    }
}
