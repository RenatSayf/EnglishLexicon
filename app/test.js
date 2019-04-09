(function(){
    if(window.getSelection().toString() !== ""){
        return window.getSelection().toString();
    }
    else{
        return null;
    }
})