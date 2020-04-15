$(document).ready(function() {
    dashboard.init();
  });

  var dashboard = (function() {
    var dataTypes = [];
    var bulkColumns = {};
    var pgNo = 0;
    var totalPages;

    var init = function() {
      $("[data-js=create]").click(showChannelFormHandler);
      $("[data-js=bulk]").click(showBulkFormHandler);
      $("[data-js=addMore]").click(addBulkFormHandler);
      $(document).on("click", "[data-js=remove]", removeColumnRow)
      $("[data-js=sendChannelData]").click(sendChannelData);
      $("[data-js=sendBulkData]").click(sendBulkData);
      $(document).on("click", "[data-js=edit-bulk-row]", function(e){openEditFormBulk(e)});
      $("[data-js=editBulkData]").click(editBulkData);
      $("[data-js=cancelEditBulkData]").click(cancelEditBulkData);
      $("[data-js=bulk-switch-config]").click(switchToConfig);
      $("[data-js=bulk-switch-list]").click(switchToList);

      $("[data-js=channel-switch-create]").click(switchToChannelCreate);
      $("[data-js=channel-switch-list]").click(switchToChannelList);

      $(document).on("click", "[data-js=showMore]", showColumnDetails);
//      $("[data-js=createProcessBtn]").click(createProcessBtn);
      $(document).on("click", "[data-js=edit-row]", editTheRow);
      $(document).on("click", "[data-js=update-row]", udpateEditedData);
      $(document).on("click", "[data-js=cancel-row]", cancelEditedData);

      getChannelDetails();

      //pagination
      $("[data-js=prev]").click(backToPrevPage);
      $("[data-js=next]").click(forwardToNextPage);
      // search in bulk data
        $("[data-by]").click(function(e){searchBulkData(e)});

        document.addEventListener("click", function(e){
            if(e.target && e.target.attributes['data-js'] && e.target.attributes['data-js'].value == "clickMeToCopy"){
                var children = e.target.parentElement.children;
                if(children && children.length > 0){
                    for(var i=0; i<children.length; i++){
                        if(children[i].type === "textarea"){
                            children[i].select();
                            document.execCommand('copy');
                            alert("Button copied!");
                        }
                    };
                }
             }
          });
    };

    var backToPrevPage = function(){
        if(pgNo > 1){
            pgNo--;
            $("[data-js=next]").attr("disabled", false);
            getBulkData();
        } else{
            $(this).attr("disabled", true);
        }
    };

    var forwardToNextPage = function(){
        if(pgNo < totalPages){
            pgNo++;
            $("[data-js=prev]").attr("disabled", false);
            getBulkData();
        } else{
            $(this).attr("disabled", true);
        }
    };

    var showChannelFormHandler = function() {
      if(!$(this).hasClass("active")){
        $(".main-menu").toggleClass("active");
      }
      document.querySelector("[data-js=bulk-form]").classList.add("hide");
      $("[data-js=channel-form]").removeClass("hide");
    };
    var showBulkFormHandler = function() {
      if(!$(this).hasClass("active")){
        $(".main-menu").toggleClass("active");
      }
      $("[data-js=channel-form]").addClass("hide");
      $("[data-js=bulk-form]").removeClass("hide");
    };
    var addBulkFormHandler = function() {
      $(this).before(htmlContext());
      validateRemoveColumnRow.call(this);
    };

    var removeColumnRow = function(){
        validateRemoveColumnRow.call(this, true);
        $(this).parent(".inputs").remove();
    };

    var validateRemoveColumnRow = function(remove){
        var inputs = $(this).closest("[data-js=bulk-data-form]").find(".inputs");
//        if(inputs.length === 0){
//            $("[data-js=bulk-data-form]").find(".inputs [data-js=remove]").hide();
//        }else
        if(inputs.length === 1 || (remove && inputs.length === 2)){
            inputs.find("[data-js=remove]").hide();
        } else{
            inputs.find("[data-js=remove]").show();
        }
    };

    var htmlContext = function(data) {
        var bulkColumns = {
            jsonKeyName: "",
            excelDisplayName: "",
            dataType: ""
        };
        if(data){
            bulkColumns["jsonKeyName"] = data.jsonKeyName;
            bulkColumns["excelDisplayName"] = data.excelDisplayName;
            bulkColumns["dataType"] = data.dataType;
        }
      return `<div class="flex inputs">
                <label>Key name</label>
                <input name="jsonKeyName" class="custom" placeholder="Enter key name" value="`+ bulkColumns["jsonKeyName"] +`" />

              <label>Display name</label>
              <input
                  name="excelDisplayName"
                  class="custom"
                  placeholder="Enter display Name"
                  value="`+ bulkColumns["excelDisplayName"] +`"
              />

              <label>Data type list</label>
              <select class="custom" name="dataType">
                  `+ createSelect(dataTypes, true, bulkColumns["dataType"] ) +`
              </select>
              <span class="remove-column" data-js="remove">X</span>
          </div>`;
    };

    var validateForm = function(form) {
      var valid = true;
      $(form)
        .find("input, select")
        .each(function() {
          if(
            $(this).val() === ""
            && ($(this).attr("data-js") !== "edit-id" || $(this).attr("data-js") !== "info-tag" || $(this).attr("data-js") !== "toEmail")
          ) {
            valid = false;
          }
        });
      return valid;
      // $("[data-js=create-channel-form]").validate({
      //   // initialize the plugin
      //   rules: {
      //     name: {
      //       required: true
      //     },
      //     channelEndPoint: {
      //       required: true
      //     }
      //   },
      //   submitHandler: function(form) {
      //     sendChannelData();
      //     return false;
      //   }
      //   // $(this).rules("add",
      //   //         {
      //   //             required: true
      //   //         })
      // });
    };

    var sendChannelData = function(e, edit, data) {
        var requestType = "POST";
        if(edit){
            var url = "/hkc/bulk/process/edit/channel/" + data.id;
            var name = data.name;
            var channelEndPoint = data.channelEndPoint;
            var id = data.id;
            if(!name || !channelEndPoint || !id){
                alert("All fields required!");
                return false;
            }
            var data = {
                name: name,
                channelEndPoint: channelEndPoint,
                cid: id
            };
            requestType = "PUT";
        } else{
            var url = "/hkc/bulk/process/create/channel";
            var channelForm = $("[data-js=create-channel-form]");
            var name = $(channelForm)
                .find("input[name=name]")
                .val();
            var channelEndPoint = $(channelForm)
                .find("input[name=channelEndPoint]")
                .val();
            var id = $(channelForm)
                .find("[data-js=edit-id]")
                .val();
            if (!validateForm(channelForm)) {
                alert("All fields required!");
                return false;
            }

            var data = {
                name: name,
                channelEndPoint: channelEndPoint
            };
        }

      $.ajax({
        type: requestType,
        url: url,
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(res) {
            errorResponse(res);
            getChannelDetails();
            $(channelForm).find("input[name=name]").val("");
            $(channelForm).find("input[name=channelEndPoint]").val("");
            $(channelForm).find("[data-js=edit-id]").val("");
              alert("Channel added");
            }
      });
    };

     var udpateEditedData = function(){
        var parentRow = $(this).closest("tr");
        var objectData = {
            name: parentRow.find("[data-js=name]").text(),
            channelEndPoint: parentRow.find("[data-js=channelEndPoint]").text(),
            id: parentRow.find("[data-js=id]").val()
        }
        sendChannelData(false, true, objectData);
     }

    var cancelEditedData = function(){
        var tr = $(this).closest("tr");
        var channelNameEle = tr.find("[data-js=name]");
        var channelEndPointEle = tr.find("[data-js=channelEndPoint]");
        var name = channelNameEle.attr("value");
        var channelEndPoint = channelEndPointEle.attr("value");
        var parentRow = tr.find("td").attr("contenteditable", false);
        channelNameEle.text(name);
        channelEndPointEle.text(channelEndPoint);
        $(this).closest("td").addClass("hide");
        $(this).closest("tr").find("[data-js=edit-row]").closest("td").removeClass("hide");
    };

    var resetCreatedBulkButtons = function(){
        // remove buttons made after submission
        $("[data-js=buttonCode]").empty();
        $("[data-js=createProcess]").addClass("hide");
    }

    var cancelEditBulkData = function(){
        $("[data-js=bulk-switch-list]").click();
        var parentForm = $("[data-js=edit-bulk-configuration]");
        var processForm = parentForm.find("[data-js=bulk-process-form]");
        var dataForm = parentForm.find("[data-js=bulk-data-form]");
        processForm[0].reset();
        dataForm[0].reset();
        parentForm.find("[data-js=toEmail]").val("");
        resetCreatedBulkButtons();

        parentForm.find(".inputs").slice( 2 ).remove();
    };

    var editBulkData = function(){
        sendBulkData("", true);
    }

    var sendBulkData = function(e, edit) {
        var url = "/hkc/bulk/process/create/bulkConfig";
        var requestType = "POST";
        var editIdValue = "";
        if(edit){
            var parentForm = $("[data-js=edit-bulk-configuration]");
            var processForm = parentForm.find("[data-js=bulk-process-form]");
            editIdValue = processForm[0].editId.value;
            url = ("/hkc/bulk/process/edit/bulkConfig/" + editIdValue);
            requestType = "PUT";
        } else{
            var parentForm = $("[data-js=bulk-configuration]");
            var processForm = parentForm.find("[data-js=bulk-process-form]");
        }

      var dataForm = parentForm.find("[data-js=bulk-data-form]");
      if (!(validateForm(processForm) && validateForm(dataForm))) {
        alert("All fields are required!");
        return false;
      }
      var columnDetails = function(){
        var data = dataForm.serializeArray();
        var newArray= [];
        var newObj = {};
        for(var i=0; i<data.length; i++){
            var values = Object.values(data[i]);
            newObj[values[0]] = values[1];
            if((i+1) % 3 === 0){
                newArray.push(newObj);
                newObj = {};
            }
        }
        return newArray;
      }
      var toEmail = parentForm.find("[data-js=toEmail]");
      var data = {
        channelId: processForm[0].channelId.value,
        buttonName: processForm[0].buttonName.value,
        targetProcessingUri: processForm[0].targetProcessingUri.value,
        sheetName: processForm[0].sheetName.value,
        stateParam: processForm[0].stateParam.value,
        infoTag: processForm[0].infoTag.value,
        id: editIdValue,
        toEmail: toEmail.val(),
        columnDetails: columnDetails(),
      };

      $.ajax({
        type: requestType,
        url: url,
        contentType: "application/json",
        data: JSON.stringify(data),
        success: function(res) {
            errorResponse(res);
            if(res && res.result){
                parentForm.find("[data-js=createProcess]").removeClass("hide");
                createBtnCode(parentForm.find("[data-js=buttonCode]")[0], res.result.bulkEndPoint, "Bulk button", res.result.buttonName + " sheet", "", res.result.infoTag);
                var textAreaValueHtml = `<div class="flex-1 division-right" data-js="createProcess" style=" width: 80%; display: inline-block; ">
                    <input type="text" class="custom" placeholder="Enter google sheet url" data-js="processEndPoint" style="padding: 4px; border-radius: 5px; border: 2px solid #64798f1f; width: 50%;">
                    <span data-js="err" style="color: red; margin: 4px; display: none;">Please enter valid url!</span>
                    <input type="hidden" data-js="processEndPointValue" value="`+ res.result.processEndPoint +`" />
                    <input type="hidden" data-js="buttonNameValue" value="`+ res.result.buttonName +` process" />
                    <span data-js="code-area" style="display: inline-block;"></span>
                 </div>`;

                var textAreaValueJs = `<script>
                   document.querySelectorAll("[data-js=processEndPoint]").forEach(function(elem) {
                       elem.addEventListener("keyup", function(e){createProcessBtn(e)});
                   });
                   var createProcessBtn = `+ createProcessBtn.toString() +`;
                   var createBtnCode = `+ createBtnCode.toString() +`
                   document.addEventListener("click", function(e){
                           if(e.target && e.target.attributes['data-js'] && e.target.attributes['data-js'].value == "clickMeToCopy"){
                               var children = e.target.parentElement.children;
                               if(children && children.length > 0){
                                   for(var i=0; i<children.length; i++){
                                       if(children[i].type === "textarea"){
                                           children[i].select();
                                           document.execCommand('copy');
                                           alert("Button copied!");
                                       }
                                   };
                               }
                            }
                         });
               </script>`;

                parentForm.find("[data-js=process-btn]").val(textAreaValueHtml);
                parentForm.find("[data-js=process-js-btn]").val(textAreaValueJs);

              if(edit){
                alert("Bulk data udpated!");
              } else{
                toEmail.val("");
                processForm[0].reset();
                dataForm[0].reset();
                parentForm.find(".inputs").slice( 2 ).remove();
//                $("[data-js=edit-bulk-configuration] [data-js=bulk-data-form] .inputs").remove();
//                $("[data-js=edit-bulk-configuration] [data-js=addMore]").before(htmlContext() );
                alert("Bulk data added!");
              }
            }
        }
      });
    };

    var createBtnCode = function(buttonCodeArea, href, heading, anchorText, buttonOnly, infoTag){
      var textarea = document.createElement("textarea");
      var copyButton = document.createElement("button");
      var anchor = document.createElement("a");
      anchor.setAttribute("target", "_blank");
      anchor.setAttribute("href", href);
      anchor.innerText = anchorText;
      anchor.setAttribute(
        "style", 
        `border: none; border-radius: 5px; background: #64798f1f; padding: 5px  10px; box-shadow: 2px 2px 3px #000;margin: 10px 0; display: inline-block; text-decoration: none; color: #000;`
      );

      if(infoTag){
        var template = document.querySelector("#template-hover").content.cloneNode(true);
        template.querySelector(".child-hover-text").innerText = infoTag;
        anchor.appendChild(template);
      }

      textarea.setAttribute("style", "height: 65px; width: 95%;max-width: 95%;height: 100px;max-height: 100px;padding: 5px; border-radius: 5px;");
      textarea.setAttribute("data-js", "textToCopy");
      textarea.innerText = anchor.outerHTML;

      copyButton.setAttribute("type", "button");
      copyButton.classList.add("custom");
      copyButton.innerText = "Copy button";
      copyButton.setAttribute("data-js", "clickMeToCopy");

      if(buttonCodeArea === ""){
        return ( ("<h3>"+heading+"</h3>") + anchor.outerHTML + textarea.outerHTML + copyButton.outerHTML)
      } else if(buttonOnly){
        buttonCodeArea.appendChild(anchor);
      } else{
        emptyElement(buttonCodeArea);
        var headingHtml = document.createElement('p');
        headingHtml.innerHTML = heading;
        buttonCodeArea.appendChild(headingHtml);
        buttonCodeArea.appendChild(anchor);
        buttonCodeArea.appendChild(textarea);
        buttonCodeArea.appendChild(copyButton);
      }
    };

    var createProcessBtn = function(e){
        if(e.target){
            var processEndPoint = e.target.value;
            var value = processEndPoint.match("/spreadsheets/d/([a-zA-Z0-9-_]+)");
            var processEndPointValue = e.target.parentElement.querySelector("[data-js=processEndPointValue]").value;
            var buttonNameValue = e.target.parentElement.querySelector("[data-js=buttonNameValue]").value;
            e.target.parentElement.querySelector("[data-js=err]").style.display = "none";
            emptyElement(e.target.parentElement.querySelector("[data-js=code-area]"));
            value && createBtnCode(e.target.parentElement.querySelector("[data-js=code-area]"), processEndPointValue.replace("sid", value[1]), "", buttonNameValue, true);
        } else{
               e.target.parentElement.querySelector("[data-js=err]").style.display = "";
               emptyElement(e.target.parentElement.querySelector("[data-js=code-area]"));
        }

        function emptyElement(ele){
            var child = ele.lastElementChild;
            while (child) {
                ele.removeChild(child);
                child = ele.lastElementChild;
            }
        }
    }

    var emptyElement = function (ele){
        var child = ele.lastElementChild;
        while (child) {
            ele.removeChild(child);
            child = ele.lastElementChild;
        }
    };

    var switchToConfig = function(){
      $(this).addClass("active");
      $("[data-js=edit-bulk]").addClass("hide");
      $("[data-js=bulk-switch-list]").removeClass("active");
      $("[data-js=view-bulk-list]").addClass("hide");
      $("[data-js=bulk-configuration]").removeClass("hide");
      resetCreatedBulkButtons();
    };

    var switchToList = function(){
      getBulkData();
      resetCreatedBulkButtons();
      $("[data-js=current-page], .pagination").show();
      $(this).addClass("active");
      $("[data-js=edit-bulk], [data-js=edit-bulk-configuration]").addClass("hide");
      $("[data-js=bulk-switch-config]").removeClass("active");
      $("[data-js=bulk-configuration]").addClass("hide");
      $("[data-js=view-bulk-list]").removeClass("hide");
    };
    var openEditFormBulk = function(e){
        $("[data-js=bulk-switch-list]").removeClass("active");
        $("[data-js=edit-bulk], [data-js=edit-bulk-configuration]").removeClass("hide");
        $("[data-js=view-bulk-list]").addClass("hide");
        $("[data-js=edit-bulk-configuration]").show();

        var parentRow = $(e.target).closest("tr");
        var channelId = parentRow.attr("data-channelId");
        var editId = parentRow.attr("data-id");
        var parentTd = parentRow.find("td");
        var buttonNameValue = parentRow.find("[data-js=buttonName]").text();
        var targetProcessingUriValue = parentRow.find("[data-js=targetProcessingUri]").text();
        var sheetNameValue = parentRow.find("[data-js=sheetName]").text();
        var toEmail = parentRow.find("[data-js=toEmail]").text();
        var stateParam = parentRow.find("[data-js=stateParam]").text();
        var infoTag = parentRow.find("[data-js=infoTag]").text();
        var bulkColumnsHtml = "";
        if(bulkColumns[channelId] && bulkColumns[channelId].length > 0){
            for(var i=0; i<bulkColumns[channelId].length; i++){
                var ele = bulkColumns[channelId][i];
                bulkColumnsHtml += htmlContext(ele);
            }
        }

        $("[data-js=edit-bulk-configuration] [name=channelId]").val(channelId);
        $("[data-js=edit-bulk-configuration] input[name=editId]").val(editId);
        $("[data-js=edit-bulk-configuration] [name=buttonName]").val(buttonNameValue);
        $("[data-js=edit-bulk-configuration] [name=targetProcessingUri]").val(targetProcessingUriValue);
        $("[data-js=edit-bulk-configuration] [name=sheetName]").val(sheetNameValue);
        $("[data-js=edit-bulk-configuration] [name=toEmail]").val(toEmail);
        $("[data-js=edit-bulk-configuration] [name=stateParam]").val(stateParam);
        $("[data-js=edit-bulk-configuration] [name=infoTag]").val(infoTag);
        $("[data-js=edit-bulk-configuration] [data-js=bulk-data-form] .inputs").remove();
        $("[data-js=edit-bulk-configuration] [data-js=addMore]").before(bulkColumnsHtml);
    };
    var switchToChannelCreate = function(){
        $(this).addClass("active");
        $("[data-js=channel-switch-list]").removeClass("active");
        $("[data-js=create-channel]").removeClass("hide");
        $("[data-js=view-channel-list]").addClass("hide");
    }
    var switchToChannelList = function(){
        $(this).addClass("active");
        $("[data-js=channel-switch-create]").removeClass("active");
        $("[data-js=create-channel]").addClass("hide");
        $("[data-js=view-channel-list]").removeClass("hide");
    };
    var showColumnDetails = function(){
        var id = $(this).closest("tr").data("id");
      $(this).closest("tbody").find("[data-js=child-row][data-id="+ id +"]").toggleClass("hide");
    };

    var getChannelDetails = function(){
        $.ajax({
            type: "GET",
            url: "/hkc/bulk/process/get/params/create/config",
            success: function(res) {
                errorResponse(res);
                if(res && res.result){
                    if(res.result.channels && res.result.channels.length > 0){
                        var channelData = res.result.channels;
                        $("[data-js=view-channel-list] table tbody").html(createTableData(channelData));
                        $("[data-js=select-channel]").html(createSelect(channelData));
                    }
                    if(res.result.dataTypes && res.result.dataTypes.length > 0){
                        dataTypes = res.result.dataTypes;
                        $("[data-js=addMore]").before(htmlContext());
                        $("[data-js=bulk-data-form]").find(".inputs [data-js=remove]").hide();
//                        validateRemoveColumnRow();
//                        $("[data-js=dataTypes]").html(createSelect(res.result.dataTypes, true)); // dataTypes var
                    }
                }
            }
          });
    }

    var errorResponse  = function(res){
        if(res && res.error){
            alert(res.message);
            return false;
        }
    }

    var getBulkData= function(){
        $.ajax({
            type: "GET",
            url: "/hkc/bulk/process/get/page/bulkConfig?pgNo="+ pgNo +"&perPg=10",
            success: function(res) {
                errorResponse(res);
                if(res && res.result && res.result.page && res.result.page.content){
                    if(res.result.page.content && res.result.page.content.length > 0){
                        $("[data-js=current-page], .pagination").show();
                        $("[data-js=search]").val("");
                        totalPages = res.result.page.totalPages;
                        $("[data-js=current-page]").text( "Page " + (pgNo + 1) + " of " + totalPages);
                        createBulkDataTable(res.result.page.content, res.result.bulkEndPoint, res.result.processEndPoint);
                    }
                }
            }
          });
    }

    var createBulkDataTable = function(content, bulkEndPoint, processEndPoint){
        var tableHtml = "";
        for(var i=0; i<content.length; i++){
            var toEmail = (content[i].toEmail && content[i].toEmail !== null ? content[i].toEmail : "");
            var infoTag = (content[i].infoTag && content[i].infoTag !== null ? content[i].infoTag : "");
            var stateParam = (content[i].stateParam && content[i].stateParam !== null ? content[i].stateParam : "");
            var processEndPoint = (processEndPoint.replace("bid", content[i].id).replace('sParam', content[i].stateParam) + content[i].targetProcessingUri);
            var buttonHtmlProcess = `<div class="flex-1 division-right" data-js="createProcess" style=" width: 80%; display: inline-block; ">
                 <input type="text" class="custom" placeholder="Enter google sheet url" data-js="processEndPoint" style="padding: 4px; border-radius: 5px; border: 2px solid #64798f1f; width: 50%;">
                 <span data-js="err" style="color: red; margin: 4px; display: none;">Please enter valid url!</span>
                 <input type="hidden" data-js="processEndPointValue" value="`+ processEndPoint +`" />
                 <input type="hidden" data-js="buttonNameValue" value="`+ content[i].buttonName +` process" />
                 <span data-js="code-area" style="display: inline-block;"></span>
              </div>`;
            var buttonJsProcess = `<script>
                   document.querySelectorAll("[data-js=processEndPoint]").forEach(function(elem) {
                       elem.addEventListener("keyup", function(e){createProcessBtn(e)});
                   });
                   var createProcessBtn = `+ createProcessBtn.toString() +`;
                   var createBtnCode = `+ createBtnCode.toString() +`
                   document.addEventListener("click", function(e){
                           if(e.target && e.target.attributes['data-js'] && e.target.attributes['data-js'].value == "clickMeToCopy"){
                               var children = e.target.parentElement.children;
                               if(children && children.length > 0){
                                   for(var i=0; i<children.length; i++){
                                       if(children[i].type === "textarea"){
                                           children[i].select();
                                           document.execCommand('copy');
                                           alert("Button copied!");
                                       }
                                   };
                               }
                            }
                         });
               </script>`;

            tableHtml += "<tr data-js='parent-row' data-channelId="+ content[i].channelId +" data-id="+ content[i].id +">";
            tableHtml += `<td>
                            <div class='font-16 font-bold' >Button name: <span class='font-normal' data-js='buttonName'>` + content[i].buttonName +`</span></div>
                            <div class="font-16 font-bold" >Sheet name: <span class='font-normal' data-js='sheetName'>`+ content[i].sheetName +`</span></div>
                            <div class="font-16 font-bold">URI: <span class='font-normal' data-js='targetProcessingUri'>`+ content[i].targetProcessingUri +`</span></div>
                            <div class='cursor' data-js='showMore'>(Show Columns)</div>
                           </td>`;
//            tableHtml += "<td data-js='targetProcessingUri'>"+ content[i].targetProcessingUri +"</td>";
//            tableHtml += "<td data-js='sheetName'>"+ content[i].sheetName +"</td>";
//            tableHtml += "<td data-js='infoTag'>"+ infoTag +"</td>";
            tableHtml += "<td data-js='stateParam'>"+ stateParam +"</td>";
            tableHtml += "<td data-js='toEmail'>"+ toEmail +"</td>";
            tableHtml += "<td><div>"
                            + createBtnCode("", bulkEndPoint.replace("bid", content[i].id), "Bulk button", (content[i].buttonName + " sheet"), "", infoTag)
                            + "</div> <div><hr>"
                            + `<div data-js='code-area'>
                                <p><b>Process button</b></p>
                                 <p>Html content for button</p>
                                 <textarea name='code' class='custom' data-js='process-btn' style='max-width: 95%' >`+ buttonHtmlProcess +`</textarea>
                                 <button type='button' class='custom' data-js='clickMeToCopy'>Copy button HTML</button>
                               </div>
                               <div data-js='code-area-js'>
                                 <p>JS content for button</p>
                                 <textarea name='code' class='custom' data-js='process-js-btn' style='max-width: 95%' >`+ buttonJsProcess +`</textarea>
                                 <button type='button' class='custom' data-js='clickMeToCopy'>Copy button JS</button>
                               </div>`
                         + "</div></td>";
            tableHtml += "<td > <button type='button' class='custom' data-js='edit-bulk-row' >Edit</button> </td>";
            tableHtml += "</tr>";

            if(content[i].columnDetails && content[i].columnDetails.length > 0){
                var cDetails = content[i].columnDetails;
                bulkColumns[content[i].channelId] = cDetails;
                cDetails.forEach(function(ele, index){
                tableHtml += '<tr class="child-row hide" data-js="child-row" data-id='+ content[i].id +'>';
                tableHtml += '<td>'+ (index+1) +'.</td> <td>Key name: '+ ele.jsonKeyName +'</td> <td>Display name: '+ ele.excelDisplayName +'</td> <td>Data type: '+ ele.dataType +'</td> ';
                tableHtml += '<td></td>';
                tableHtml += '</tr>';
                })
            }
        }

        $("[data-js=view-bulk-list] tbody").html(tableHtml);
    }

     var editTheRow = function(){
        var parentRow = $(this).closest("tr").find("td").not("[data-js=no-edit]").attr("contenteditable", true);
        $(this).closest("td").addClass("hide");
        $(this).closest("tr").find("[data-js=update-row]").closest("td").removeClass("hide");
     }

    var createTableData = function (data){
       var list = "";
       for(var i=0; i<data.length; i++){
           list += '<tr>';
            list += "<td data-js='name' value="+ data[i]["name"] +">"+ data[i]["name"] +"</td>";
            list += "<td data-js='channelEndPoint' value="+ data[i]["channelEndPoint"] +">"+ data[i]["channelEndPoint"] +"</td>";
            list += "<input type='hidden' value='"+ data[i]["id"] +"' data-js='id' />";
            list += "<td contenteditable='false' data-js='no-edit'> <button type='button' class='custom' data-js='edit-row' >Edit</button> </td>";
            list += "<td contenteditable='false' data-js='no-edit' class='width-100 hide'> <button type='button' class='custom' data-js='update-row' >Update</button> <button type='button' class='custom' data-js='cancel-row' >Cancel</button> </td>";
            list += "</tr>";
       }

       return list;
    };

    var createSelect = function (data, dataType, selectedValue){
        var list = "<option value=''>Select</option>";
        for(var i=0; i<data.length; i++){
            var selectName, selectValue;
            if(dataType){
                selectName = selectValue = data[i];
            } else{
                selectName = data[i].name;
                selectValue = data[i].id;
            }
            list += "<option value="+ selectValue +" "+ (selectValue == selectedValue ? "selected" : "") +">"+ selectName +"</option>";
        }
        return list;
    };

    var searchBulkData = function(e){
//        var code = (e.keyCode ? e.keyCode : e.which);
        var inputField = $("[data-js=search]");
        var searchText = inputField.val();
        var searchBy = $(e.target).data("by");

        if(searchText !== ''){
            $("[data-js=current-page], .pagination").hide();
            $.ajax({
                type: "GET",
                url: "/hkc/bulk/process/search/bulkConfig?" + searchBy +"="+ searchText,
                contentType: "application/json",
                success: function(res) {
                    errorResponse(res);
                    if(res && res.result && res.result.config && res.result.config.length > 0){
                            createBulkDataTable(res.result.config, res.result.bulkEndPoint, res.result.processEndPoint);
                    } else{
                        alert("No result found!");
                    }
                }
            });
        }
    };

    return {
      init: init
    };
  })();