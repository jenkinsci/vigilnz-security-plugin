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

//    var imageField = document.querySelector(".imageNameClass");
//    if (imageField) {
//        imageField.addEventListener('change',function(){
//            var val = this.value || "";
//            var providerField = document.querySelector(".providerNameClass");
//            if (!providerField) return;
//
//            let providerValue = "dockerhub"; // default
//
//            if (val.includes("ecr")) providerValue = "aws-ecr";
//            else if (val.includes("gcr.io")) providerValue = "google";
//            else if (val.includes("azurecr.io")) providerValue = "azure";
//            else if (val.includes("github")) providerValue = "github";
//            else if (val.includes("gitlab")) providerValue = "gitlab";
//            else if (val.includes("quay")) providerValue = "quay";
//            else if (val.includes("docker.io") || val.includes("docker")) providerValue = "dockerhub";
//
//            // set the dropdown selection
//            providerField.value = providerValue;
//        })
//    }

    var providerSelect = document.querySelector(".providerNameClass");

    function toggleProviderFields() {
        var val = providerSelect.value;
        // hide all
        document.querySelectorAll("#dockerhubFields, #awsEcrFields, #githubFields, #googleFields, #azureFields, #quayFields")
            .forEach(div => div.style.display = "none");
        console.log("val-------",val)
        if (val === "dockerhub") document.getElementById("dockerhubFields").style.display = "block";
        if (val === "aws-ecr") document.getElementById("awsEcrFields").style.display = "block";
        if (val === "github" || val === "gitlab") document.getElementById("githubFields").style.display = "block";
        if (val === "google") document.getElementById("googleFields").style.display = "block";
        if (val === "azure") document.getElementById("azureFields").style.display = "block";
        if (val === "quay") document.getElementById("quayFields").style.display = "block";

    }

    // initial + listener
    toggleProviderFields();

    providerSelect.addEventListener("change", toggleProviderFields);

//    // Example: DockerHub auth toggle
//    var dockerAuthSelect = document.querySelector(".dockerAuthMethodClass");
//    if (dockerAuthSelect) {
//        dockerAuthSelect.addEventListener("change", function() {
//            document.getElementById("dockerUserPass").style.display = (this.value === "username-password") ? "block" : "none";
//            document.getElementById("authTypeId").value = this.value || "";
//            document.getElementById("accessTokenFieldId").value = "";
//            document.getElementById("registryTypeId").value = "";
//        });
//    }
//
//    // Example: ECS auth toggle
//    var dockerAuthSelect = document.querySelector(".awsEcrFieldsClass");
//    if (dockerAuthSelect) {
//        dockerAuthSelect.addEventListener("change", function() {
//            document.getElementById("awsPrivateFields").style.display = (this.value === "ecr-private") ? "block" : "none";
//            document.getElementById("authTypeId").value = "token";
//            document.getElementById("accessTokenFieldId").value = document.getElementById("awsPrivateFields").querySelector(".accessTokenTextbox").value || "";
//            document.getElementById("registryTypeId").value = this.value || "";
//        });
//    }
//
//    // Example: GitHub or GitLab auth toggle
//    var dockerAuthSelect = document.querySelector(".gitHubFieldsClass");
//    if (dockerAuthSelect) {
//        dockerAuthSelect.addEventListener("change", function() {
//            document.getElementById("gitHubPrivateFields").style.display = (this.value === "token") ? "block" : "none";
//            document.getElementById("authTypeId").value = "token";
//            document.getElementById("accessTokenFieldId").value = document.getElementById("gitHubPrivateFields").querySelector(".accessTokenTextbox").value || "";
//            document.getElementById("registryTypeId").value = "";
//        });
//    }
//
//    // Example: Google container auth toggle
//    var dockerAuthSelect = document.querySelector(".googleFieldsClass");
//    if (dockerAuthSelect) {
//        dockerAuthSelect.addEventListener("change", function() {
//            document.getElementById("registryTypeId").value = this.value || "";
//            document.getElementById("googleFields").style.display = "block";
//            if(this.value === "gcr"){
//                document.getElementById("googleGCRDropdown").style.display = "block";
//                var authenticateSelect = document.querySelector(".googleAuthenticateFieldsClass");
//                authenticateSelect.addEventListener("change", function() {
//                    document.getElementById("googleAccessTokenFieldId").style.display = "none";
//                    document.getElementById("authTypeId").value = this.value || "";
//                    if(this.value=="token"){
//                        document.getElementById("googleAccessTokenFieldId").style.display = (this.value === "token") ? "block" : "none";
//                        document.getElementById("accessTokenFieldId").value = document.getElementById("googleFields").querySelector(".accessTokenTextbox").value || "";
//                    }else{
//                        document.getElementById("accessTokenFieldId").value = "none";
//                    }
//                });
//            } else {
//                document.getElementById("googleGCRDropdown").style.display = "none";
//                document.getElementById("googleAccessTokenFieldId").style.display = "block";
//                document.getElementById("authTypeId").value = "token";
//                document.getElementById("accessTokenFieldId").value = document.getElementById("googleFields").querySelector(".accessTokenTextbox").value || "";
//            }
//
//        });
//    }

    function hideAll() {
        document.querySelectorAll(
            "#dockerhubFields,#awsEcrFields,#githubFields,#googleFields,#azureFields,#quayFields"
        ).forEach(el => el.style.display = "none");
    }

    function renderUI() {
        const provider = document.querySelector(".providerNameClass")?.value;
        const auth = document.querySelector(".dockerAuthMethodClass")?.value;
        const registry = document.querySelector(".awsEcrFieldsClass")?.value;
        const gitAuthValue = document.querySelector(".gitHubFieldsClass")?.value;
        const googleRegistry = document.querySelector(".googleFieldsClass")?.value;
        const googleAuthValue= document.querySelector(".googleAuthenticateFieldsClass")?.value;
        const quayAuthValue= document.querySelector(".quayAuthenticateFieldsClass")?.value;
        const azureRegistry= document.querySelector(".azureFieldsClass")?.value;
        const azureAuthValue= document.querySelector(".azureAuthenticateFieldsClass")?.value;

        hideAll();
        console.log("googleRegistry-----000---", googleRegistry)

        switch (provider) {

            case "dockerhub":
                show("dockerhubFields");
                document.querySelector("#registryTypeClass").value = "";
                document.querySelector("#accessTokenFieldClass").value = "";
                if (auth === "username-password") {
                    show("dockerAuthFields");
                    document.querySelector("#authTypeClass").value = "username-password";
                } else {
                    hide("dockerAuthFields")
                    document.querySelector("#authTypeClass").value = "none";
                }
                break;

            case "aws-ecr":
                show("awsEcrFields");
                document.querySelector("#registryTypeClass").value = registry || "";
                if (registry === "ecr-private") {
                    show("ecrAuthFields");
                    document.querySelector("#accessTokenFieldClass").value = getToken("ecrAuthFields")
                    document.querySelector("#authTypeClass").value = "token";
                }
                else {
                    hide("ecrAuthFields")
                    document.querySelector("#accessTokenFieldClass").value = "";
                    document.querySelector("#authTypeClass").value = "none";
                }
                break;

            case "github":
            case "gitlab":
                show("githubFields");
                document.querySelector("#registryTypeClass").value = "";
                if (gitAuthValue === "token") {
                    show("gitHubAuthFields");
                    document.querySelector("#accessTokenFieldClass").value = getToken("gitHubAuthFields")
                    document.querySelector("#authTypeClass").value = "token";
                }
                else {
                    hide("gitHubAuthFields")
                    document.querySelector("#accessTokenFieldClass").value = "";
                    document.querySelector("#authTypeClass").value = "none";
                }
                break;

            case "google":
                show("googleFields");
                document.querySelector("#registryTypeClass").value = googleRegistry || "";
                if (googleRegistry === "gcr") {
                    show("googleGCRDropdown");
                    hide("googleRegistryUrlFieldId");
                    if (googleAuthValue === "token") {
                        show("googleAccessTokenFieldId");
                        document.querySelector("#authTypeClass").value = "token";
                        document.querySelector("#accessTokenFieldClass").value = getToken("googleFields")
                    }
                    else {
                        hide("googleAccessTokenFieldId")
                        document.querySelector("#accessTokenFieldClass").value = ""
                        document.querySelector("#authTypeClass").value = "none";
                    }
                } else {
                    hide("googleGCRDropdown")
                    show("googleAccessTokenFieldId");
                    show("googleRegistryUrlFieldId");
                    document.querySelector("#accessTokenFieldClass").value = getToken("googleFields")
                    document.querySelector("#authTypeClass").value = "token";
                }
                break;

            case "azure":
                show("azureFields");
                document.querySelector("#registryTypeClass").value = azureRegistry || "";
                if (azureRegistry === "acr-private") {
                    show("azureGCRDropdown");
                    show("azureRegistryUrlFieldId")
                    if (azureAuthValue === "token") {
                        show("azureAccessTokenFieldId");
                        hide("azureAuthFields")
                        document.querySelector("#authTypeClass").value = "token";
                        document.querySelector("#accessTokenFieldClass").value = getToken("azureFields")
                    } else if (azureAuthValue === "username-password") {
                        show("azureAuthFields")
                        hide("azureAccessTokenFieldId")
                        document.querySelector("#accessTokenFieldClass").value = ""
                        document.querySelector("#authTypeClass").value = "username-password";
                    }
                } else {
                    hide("azureGCRDropdown")
                    hide("azureRegistryUrlFieldId")
                    document.querySelector("#accessTokenFieldClass").value = ""
                    document.querySelector("#authTypeClass").value = "none";
                }
                break;

            case "quay":
                show("quayFields");
                document.querySelector("#registryTypeClass").value = "";
                if (quayAuthValue === "token"){
                    document.querySelector("#accessTokenFieldClass").value = getToken("quayFields")
                    document.querySelector("#authTypeClass").value = "token";
                    show("quayAccessTokenFieldId");
                    hide("quayAuthFields")
                }else if (quayAuthValue === "username-password"){
                    document.querySelector("#accessTokenFieldClass").value = ""
                    document.querySelector("#authTypeClass").value = "username-password";
                    hide("quayAccessTokenFieldId")
                    show("quayAuthFields");
                }else{
                    hide("quayAuthFields")
                    hide("quayAccessTokenFieldId")
                    document.querySelector("#authTypeClass").value = "none";
                    document.querySelector("#accessTokenFieldClass").value = ""
                }
                break;
        }
    }

    function show(id) {
        const el = document.getElementById(id);
        if (el) el.style.display = "block";
    }

    function hide(id) {
        const el = document.getElementById(id);
        if (el) el.style.display = "none";
    }

    function getToken(id) {
        const el = document.getElementById(id);
        if (el) {
            return el.querySelector(".accessTokenTextbox").value || "";
        }
        return "";
    }

    function init() {
        document.querySelectorAll(
            ".providerNameClass,.dockerAuthMethodClass,.awsEcrFieldsClass,.gitHubFieldsClass,.googleFieldsClass,.googleAuthenticateFieldsClass,.quayAuthenticateFieldsClass,.azureFieldsClass,.azureAuthenticateFieldsClass"
        ).forEach(el => {
            el.onchange = renderUI;
        });

        renderUI();
    }

    init()

//    new MutationObserver(init)
//        .observe(document.body, { childList: true, subtree: true });

});

