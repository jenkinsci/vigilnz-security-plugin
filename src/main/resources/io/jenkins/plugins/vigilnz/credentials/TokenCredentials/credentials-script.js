Behaviour.specify(".passwordClass", "vigilnz-token-change", 0, function (element) {
    console.log("Vigilnz credentials JS loaded");

    element.addEventListener("blur", function () {
        const event = new Event("change", { bubbles: true });
        element.dispatchEvent(event);
    });
});
