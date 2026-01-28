document.addEventListener('DOMContentLoaded', function() {
    console.log("The script is calling")

    const element = document.querySelector(".passwordClass");
    if(element){
        element.addEventListener("blur", function(){
            // Manually trigger the onchange event
            const event = new Event("change", { bubbles: true });
            element.dispatchEvent(event);
        });
    }

});