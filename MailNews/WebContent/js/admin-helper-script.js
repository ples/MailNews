var request;
var URL;
function bindFormOnAJAX(form_id, onComplete)
{
	$(form_id).submit(function(event){
		if (request) {
			request.abort();
		}
		var $form = $(this);
		var $inputs = $form.find("input, select, button, textarea");
		var serializedData = $form.serialize();


		$inputs.prop("disabled", true);

		request = $.ajax({
			url: URL,
			type: "post",
			data: serializedData
		});

		request.done(function (response, textStatus, jqXHR){

			onComplete(response);
		});

		request.fail(function (jqXHR, textStatus, errorThrown){
			console.error(
					"The following error occured: "+
					textStatus, errorThrown
			);
		});

		request.always(function () {
			$inputs.prop("disabled", false);
		});

		event.preventDefault();
	});

}

function onFilterComplete(aResponse)
{
	if(aResponse == "saved")
		noty({text: 'SAVED!'});
}

function onSpeedComplete(aResponse)
{
	if(aResponse == "saved")
		noty({text: 'SAVED!'});
}

function onManagerComplete(aResponse)
{
	var spamSubjectsNode = $("#spam-subjects");
	var spamContentNode = $("#spam-content");
	if(aResponse.indexOf("saved") != -1)
	{
		noty({text: 'SAVED!'});
		var idsStr = $("#selected-mails-input").attr("value");
		if (aResponse.indexOf(":") != -1)
		{
			idsStr += "," + aResponse.split(":")[1];
		}

		var ids = idsStr.split(",");
		for ( var i = 0; i < ids.length; i++)
		{
			var subject = $("#"+ ids[i]);
			var content = $("#"+ ids[i] + "-content");
			subject.detach();
			content.detach();
			if (aResponse.indexOf(":") != -1)
			{
				spamSubjectsNode.append(subject);
				spamContentNode.append(content);
			} else
			{
				subject.remove();
				content.remove();
			}

		}
	}
	else
		if(aResponse.indexOf("refresh-result:")!=-1)
		{
			refreshMails(aResponse);
		}
}



function onSpamManagerComplete(aResponse)
{
	var mailSubjectsNode = $("#subjects");
	var mailContentNode = $("#mails-content");
	if(aResponse.indexOf("saved") != -1)
	{
		noty({text: 'SAVED!'});
		var idsStr = $("#selected-spam-input").attr("value");
		var ids = idsStr.split(",");
		for ( var i = 0; i < ids.length; i++)
		{
			var subject = $("#"+ ids[i]);
			var content = $("#"+ ids[i] + "-content");
			subject.detach();
			content.detach();
			if(aResponse.indexOf("deleted") != -1)
			{
				subject.remove();
				content.remove();
			} else
			{
				mailSubjectsNode.append(subject);
				mailContentNode.append(content);
			}

		}
	}
	else
		if(aResponse.indexOf("refresh-result:")!=-1)
		{
			refreshSpam(aResponse);
		}
}

function refreshSpam(aResponse)
{
	var parts = aResponse.split("#*separator*#");
	if(parts.length == 3)
	{
		var spamSubjectsNode = $("#spam-subjects");
		var spamContentNode = $("#spam-content");
		spamSubjectsNode.html(parts[1]);
		spamContentNode.html(parts[2]);
	}
}

function refreshMails(aResponse)
{
	var parts = aResponse.split("#*separator*#");
	if(parts.length == 3)
	{
		var mailSubjectsNode = $("#subjects");
		var mailContentNode = $("#mails-content");
		mailSubjectsNode.html(parts[1]);
		mailContentNode.html(parts[2]);
		
	}
}

function refreshAll()
{
	var myRequest = $.ajax({
		url: URL,
		type: "post",
		data: "admin-command=refresh-mails"
	});

	myRequest.done(function (response, textStatus, jqXHR){
		refreshMails(response);
	});

	myRequest.fail(function (jqXHR, textStatus, errorThrown){
		console.error(
				"The following error occured: "+
				textStatus, errorThrown
		);
	});

	myRequest.always(function () {
		
	});
	
	var mySpamRequest = $.ajax({
		url: URL,
		type: "post",
		data: "admin-command=refresh-spam"
	});

	mySpamRequest.done(function (response, textStatus, jqXHR){
		refreshSpam(response);
	});

	mySpamRequest.fail(function (jqXHR, textStatus, errorThrown){
		console.error(
				"The following error occured: "+
				textStatus, errorThrown
		);
	});

	mySpamRequest.always(function () {
		
	});
}

function onEditClick()
{
	var content = $("#dictionary").text();
	$("#dictionary").css("display", "none");
	$("#edit-dictionary-area").css("display", "inline");
	$("#edit-dictionary-area").val(content);
	$("#save-dictionary-button").css("display","inline");
	$("#cancel-edit").css("display","inline");
	$("#edit-dictionary-button").css("display","none");
}

function onCancelClick()
{
	$("#dictionary").css("display", "block");
	$("#edit-dictionary-area").css("display", "none");
	//$("#edit-dictionary-area").text("");
	$("#save-dictionary-button").css("display","none");
	$("#cancel-edit").css("display","none");
	$("#edit-dictionary-button").css("display","inline");
}

function onSaveClick()
{
	$("#new-dictionary-id").val($("#edit-dictionary-area").val());
	$("#dictionary").text($("#edit-dictionary-area").val());
}

function onClassifierComplete(aResponse)
{
	if(aResponse == "saved")
	{
		noty({text: 'SAVED!'});
		$("#dictionary").css("display", "block");
		$("#edit-dictionary-area").css("display", "none");
		//$("#edit-dictionary-area").text("");
		$("#save-dictionary-button").css("display","none");
		$("#cancel-edit").css("display","none");
		$("#edit-dictionary-button").css("display","inline");
	}
	else
	{
		noty({text: 'ERROR!'});
	}
}

function onAddClick()
{
	$("#dictionary").text(
			$("#dictionary").text() +" " + $("#words-to-add").val());
	$("#new-dictionary-id").val(
			$("#new-dictionary-id").val() +" " + $("#words-to-add").val());
}