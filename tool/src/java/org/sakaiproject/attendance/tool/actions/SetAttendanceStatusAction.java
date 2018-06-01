package org.sakaiproject.attendance.tool.actions;

import org.apache.wicket.ajax.AjaxRequestTarget;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.Serializable;

public class SetAttendanceStatusAction extends InjectableAction implements Serializable {

    private static final long serialVersionUID = 1L;

    public SetAttendanceStatusAction() {
    }

    @Override
    public ActionResponse handleEvent(JsonNode params, AjaxRequestTarget target) {
        // Do something
        return new EmptyOkResponse();
    }
}
