function DisplayProgressMessage(button, msg) {
    var input = document.getElementById("inputFile");
    if (input.files.length == 0 || input.value == null){
        input.focus();
        return false;
    } else if (!(input.value.split('.')[1] == 'zip')){
        alert("El fichero debe tener formato '.zip'");
        input.focus();
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

function newProjectButton(){
    var input = document.getElementById("inputfile-new-project");
    if (input.files.length == 0 || input.value == null){
        input.focus();
        return false;
    }else if (!(input.value.split('.')[1] == 'zip')){
        alert("El fichero debe tener formato '.zip'");
        input.focus();
        return false;
    }
}

function confirmDelete(){
    return confirm('¿Seguro que quieres eliminar la práctica?');
}

