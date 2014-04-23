var speedmode = 10000.0;
var scrollcount = 0;
var elem;

function checkOverflow(elem_id,height){
    var elem = document.getElementById(elem_id);
    //var real_width = elem.offsetWidth;
    var real_height = elem.offsetHeight;
    if(real_height > height){
        return true;
    } else {
        return false;
    }
}

function setCommonPageTime(offset, height){
    setTimeout(function(){
    	document.location = document.getElementById("next_url").innerHTML;
    }, getCommonPageTime(offset, height).toFixed(0));
}

function getCommonPageTime(offset, height)
{
    return speedmode*height/(offset);
}

function scrollDown(offset, height){
    var time = 0.0;
    scrollcount++;
    if((scrollcount+1)*offset < height)
    	elem.scrollTop += offset;
    else
    	elem.scrollTop = elem.scrollHeight;
    if(offset*(scrollcount+1) < height){
        time = speedmode;
    } else {
        time = speedmode*(height-offset*scrollcount)/offset;
    }
    setTimeout("scrollDown("+offset+","+height+")", time);
}
