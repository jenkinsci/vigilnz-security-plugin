Behaviour.specify(".reloadBtn", "vigilnz-reload", 0, function (btn) {
    console.log("Vigilnz JS loaded");
    btn.addEventListener("click", function () {
        window.location.reload();
    });
});

