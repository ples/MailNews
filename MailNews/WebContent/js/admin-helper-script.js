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
	var n = noty({text: 'SAVED!'});
}

function onSpeedComplete(aResponse)
{
	if(aResponse == "saved")
		var n = noty({text: 'SAVED!'});
}

function onManagerComplete(aResponse)
{
	if(aResponse == "saved")
	var n = noty({text: 'SAVED!'});
}
