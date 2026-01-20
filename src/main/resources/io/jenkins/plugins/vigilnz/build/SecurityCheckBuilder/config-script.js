document.addEventListener('DOMContentLoaded', function() {
    console.log("Script Called")

    const dastScanCheckBox = document.getElementById("dast-scan-id");
    const dastSection = document.getElementById("dast-scan-section");

    if(dastScanCheckBox && dastSection){
        // Initial state on page load
        dastSection.style.display = dastScanCheckBox.checked ? "block" : "none";

        // Update on change
        dastScanCheckBox.addEventListener('change',function(){
            dastSection.style.display = this.checked ? "block" : "none";
        })
    }

    // Container Scan
    const containerScanCheckBox = document.getElementById("container-scan-id");
    const containerSection = document.getElementById("container-scan-section");

    if(containerScanCheckBox && containerSection){
        // Initial state on page load
        containerSection.style.display = containerScanCheckBox.checked ? "block" : "none";

        // Update on change
        containerScanCheckBox.addEventListener('change',function(){
           containerSection.style.display = this.checked ? "block" : "none";
        })
   }
});