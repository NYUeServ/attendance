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

(function (attendance, $, undefined) {
    // Set the status class (which applies a background) if the input is selected
    function processInput (inputs, isStudentView) {
        for(var i = 0; i < inputs.length; i++) {
            if(inputs[i].checked) {
                var statusName = $(inputs[i]).siblings("label[hidden]").text();
                $(inputs[i]).parent().addClass(statusName);
            } else {
                $(inputs[i]).parent().removeClass().addClass("div-table-col");
                if(isStudentView && $(window).width() < 1205) {
                    $(inputs[i]).parent().removeClass().addClass("skip");
                }
            }
        }
    }

    // Re-setup a row after form input
    attendance.recordFormRowSetup = function(row) {
        var inputs = [];
        for(var i = 0; i < row.length; i++) {
            if(row[i].name === 'attendance-record-status-group') {
                inputs.push(row[i]);
            }
        }
        processInput(inputs);
    };

    // Set up the whole page record forms
    attendance.recordFormSetup = function() {
        var inputs = [];
        $("input[name='attendance-record-status-group']").each(function() {
            inputs.push(this);
        });

        var isStudentView = false;
        if($("#studentView").size() > 0) {
            isStudentView = true;
        }

        processInput(inputs, isStudentView);

        $('#takeAttendanceTable').on('click', '.status-group-container :radio', function(event) {
            var $radio = $(event.target);

            attendance.triggerAction({
                action: 'setStatus',
                userid: $radio.data('userid'),
                attendanceid: $('#takeAttendanceTable').data('attendanceid'),
                status: $radio.data('value'),
            })
        });
    };


    attendance.actionCallbacks = {};
    attendance.nextRequestId = 0;

    attendance.ajaxComplete = function (requestId, status, data) {
        attendance.actionCallbacks[requestId](status, data);
        delete attendance.actionCallbacks[requestId];
    };

    attendance.triggerAction = function (params, callback) {
      params['_requestId'] = attendance.nextRequestId++;
      attendance.actionCallbacks[params['_requestId']] = callback || $.noop;
      $('#takeAttendanceTable').trigger('attendance.action', params);
    };

}(window.attendance = window.attendance || {}, jQuery ));
