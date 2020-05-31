function DisplayProgressMessage(button, msg) {
    if (document.getElementById("inputFile").files.length == 0 || document.getElementById("inputFile").value == null){
        document.getElementById("inputFile").focus();
        return false;
    } else {
        var e = button;
        setTimeout(function(){e.disabled=true;},0);
        $("#overlay").show();
        $("#home-link").bind('click', false);
        $("#go-to-login").bind('click', false);
        $(button).text(msg);
        return true;
    }
}

