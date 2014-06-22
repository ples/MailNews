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
	refreshAll();

}



function onSpamManagerComplete(aResponse)
{
	refreshAll();
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
		$("#mails-init").css("display","none");
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
		$("#spam-init").css("display","none");
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
	$("#save-dictionary-button").css("display","none");
	$("#cancel-edit").css("display","none");
	$("#edit-dictionary-button").css("display","inline");
}

function onSaveClick()
{
	if($('input[name=dictionary-class]:checked', '#classifier-form').val() == "spam")
	{
		$("#new-dictionary-spam").val($("#edit-dictionary-area").val());
	}
	else
	{
		$("#new-dictionary-ham").val($("#edit-dictionary-area").val());
	}
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
		refreshAll();
	}
	else
	{
		noty({text: 'ERROR!'});
	}
}

function onIdentifierComplete(aResponse)
{
	if(aResponse == "not-free")
	{
		$("#not-free").html("This identifier name are not free! Please enter differrent name.");
	}
	if(aResponse == "ok")
	{
		window.location = "mail?name=" + $("#connection_id").val();
	}
	
}

function onSpamRadioClick()
{
	$("#dictionary").text($("#new-dictionary-spam").val());
	$("#edit-dictionary-area").val($("#new-dictionary-spam").val());
}

function onHamRadioClick()
{
	$("#dictionary").text($("#new-dictionary-ham").val());
	$("#edit-dictionary-area").val($("#new-dictionary-ham").val());
}