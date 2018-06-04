/*
 *  Copyright (c) 2017, University of Dayton
 *
 *  Licensed under the Educational Community License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *              http://opensource.org/licenses/ecl2
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.sakaiproject.attendance.tool.panels;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormSubmitBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.*;
import org.sakaiproject.attendance.model.AttendanceRecord;
import org.sakaiproject.attendance.model.AttendanceSite;
import org.sakaiproject.attendance.model.AttendanceStatus;
import org.sakaiproject.attendance.model.Status;
import org.sakaiproject.attendance.tool.dataproviders.AttendanceStatusProvider;
import org.sakaiproject.component.cover.ServerConfigurationService;

import java.util.ArrayList;
import java.util.List;

/**
 * AttendanceRecordFormDataPanel is a panel used to display the data contained within an AttendanceRecord
 *
 * @author Leonardo Canessa [lcanessa1 (at) udayton (dot) edu]
 * @author David Bauer [dbauer1 (at) udayton (dot) edu]
 */
public class AttendanceRecordFormDataPanel extends BasePanel {
    private static final    long                        serialVersionUID = 1L;
    private                 IModel<AttendanceRecord>    recordIModel;
    private                 boolean                     restricted ;
    private                 boolean                     showCommentsToStudents;
    // private                 List<Component>             ajaxTargets = new ArrayList<Component>();
    private                 String                      returnPage;
    private                 Status                      oldStatus;

    private                 WebMarkupContainer          commentContainer;
    private                 WebMarkupContainer          noComment;
    private                 WebMarkupContainer          yesComment;

    public AttendanceRecordFormDataPanel(String id, AttendanceSite attendanceSite, IModel<AttendanceRecord> aR,  String rP, FeedbackPanel fP) {
        super(id, aR);
        this.recordIModel = aR;
        this.oldStatus = aR.getObject().getStatus();
        this.showCommentsToStudents = recordIModel.getObject().getAttendanceEvent().getAttendanceSite().getShowCommentsToStudents();
        this.restricted = this.role != null && this.role.equals("Student");
        this.returnPage = rP;
        enable(fP);
        // this.ajaxTargets.add(this.pageFeedbackPanel);

        add(createRecordInputForm(attendanceSite));
    }

    private WebMarkupContainer createRecordInputForm(AttendanceSite attendanceSite) {
        WebMarkupContainer recordForm = new WebMarkupContainer("attendanceRecord");

        createStatusRadio(recordForm, attendanceSite);
        createCommentBox(recordForm);

        boolean noRecordBool = ((AttendanceRecord) this.recordIModel.getObject()).getStatus().equals(Status.UNKNOWN) && restricted;
        recordForm.setVisibilityAllowed(!noRecordBool);

        WebMarkupContainer noRecordContainer = new WebMarkupContainer("no-record");
        noRecordContainer.setVisibilityAllowed(noRecordBool);
        add(noRecordContainer);

        return recordForm;
    }

    private void createStatusRadio(final WebMarkupContainer rF, final AttendanceSite attendanceSite) {
        AttendanceStatusProvider attendanceStatusProvider = new AttendanceStatusProvider(attendanceSite, AttendanceStatusProvider.ACTIVE);
        DataView<AttendanceStatus> attendanceStatusRadios = new DataView<AttendanceStatus>("status-radios", attendanceStatusProvider) {
            @Override
            protected void populateItem(Item<AttendanceStatus> item) {
                final Status itemStatus = item.getModelObject().getStatus();
                Radio statusRadio = new Radio<Status>("record-status", new Model<Status>(itemStatus));
                item.add(statusRadio);
                statusRadio.add(new AttributeModifier("data-status", itemStatus.toString()));
                item.add(new AttributeAppender("class", " " + itemStatus.toString().toLowerCase()));
                if (itemStatus.equals(AttendanceRecordFormDataPanel.this.recordIModel.getObject().getStatus())) {
                    item.add(new AttributeAppender("class", " active"));
                }
                statusRadio.setLabel(Model.of(getStatusString(itemStatus)));
                item.add(new SimpleFormComponentLabel("record-status-name", statusRadio));
            }

            public boolean isEnabled() {
                // FIXME move isSyncing to the page to avoid extra lookups
                return !recordIModel.getObject().getAttendanceEvent().getAttendanceSite().getIsSyncing();
            }
        };

        RadioGroup group = new RadioGroup<Status>("attendance-record-status-group", new PropertyModel<Status>(this.recordIModel,"status"));
        group.setOutputMarkupPlaceholderTag(true);
        group.setRenderBodyOnly(false);
        group.add(attendanceStatusRadios);
        group.setEnabled(!this.restricted);

        rF.add(group);
    }

    private void createCommentBox(final WebMarkupContainer rF) {

        commentContainer = new WebMarkupContainer("comment-container");
        commentContainer.setOutputMarkupId(true);

        // FIXME
        commentContainer.setVisible(false);

        noComment = new WebMarkupContainer("no-comment");
        noComment.setOutputMarkupId(true);

        yesComment = new WebMarkupContainer("yes-comment");
        yesComment.setOutputMarkupId(true);

        if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
            noComment.setVisible(false);
        } else {
            yesComment.setVisible(false);
        }

        commentContainer.add(noComment);
        commentContainer.add(yesComment);

        final TextArea<String> commentBox = new TextArea<String>("comment", new PropertyModel<String>(this.recordIModel, "comment"));

        // FIXME: Handle this elsewhere
        // final AjaxSubmitLink saveComment = new AjaxSubmitLink("save-comment") {
        //     @Override
        //     protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
        //         super.onSubmit(target, form);
        //         if(recordIModel.getObject().getComment() != null && !recordIModel.getObject().getComment().equals("")) {
        //             noComment.setVisible(false);
        //             yesComment.setVisible(true);
        //         } else {
        //             noComment.setVisible(true);
        //             yesComment.setVisible(false);
        //         }
        //         commentContainer.addOrReplace(noComment);
        //         commentContainer.addOrReplace(yesComment);
        //         for (Component c : ajaxTargets) {
        //             target.add(c);
        //         }
        //     }
        // };
        //

        WebMarkupContainer saveComment = new WebMarkupContainer("save-comment");
        commentContainer.add(saveComment);
        commentContainer.add(commentBox);

        // ajaxTargets.add(commentContainer);

        if(restricted) {
            commentContainer.setVisible(showCommentsToStudents);
            saveComment.setVisible(!showCommentsToStudents);
            commentBox.setEnabled(!showCommentsToStudents);
            noComment.setVisible(!showCommentsToStudents);
            commentContainer.add(new Label("add-header", new ResourceModel("attendance.record.form.view.comment")));
        } else {
            commentContainer.add(new Label("add-header", new ResourceModel("attendance.record.form.add.comment")));
        }

        rF.add(commentContainer);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        final String version = ServerConfigurationService.getString("portal.cdn.version", "");
        response.render(JavaScriptHeaderItem.forUrl(String.format("javascript/attendanceRecordForm.js?version=%s", version)));
        response.render(OnDomReadyHeaderItem.forScript("attendance.recordFormSetup();"));
    }
}
