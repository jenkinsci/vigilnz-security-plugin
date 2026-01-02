# Vigilnz Security Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/vigilnz-security.svg)](https://plugins.jenkins.io/vigilnz-security)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/vigilnz-security.svg?color=blue)](https://plugins.jenkins.io/vigilnz-security)

Vigilnz Security Plugin integrates comprehensive security scanning capabilities into Jenkins CI/CD pipelines.

## Features

-  **Multiple Scan Types**: Support for SCA, SBOM, SAST and more
-  **Secure Credential Management**: Store and manage Vigilnz API credentials securely
-  **Freestyle & Pipeline Support**: Works with both traditional and modern Jenkins jobs
-  **Detailed Results**: View scan results directly in the Jenkins build sidebar
- ️ **Flexible Configuration**: Select which scan types to run per build
-  **Credential Management**: Automatic refresh and caching

## Requirements

- Jenkins 2.528.3 or later
- Java 17 or later
- Vigilnz API access (API key required)

## Usage
To use the plugin you will need to take the following steps in order:

1. [Install the Vigilnz Security Plugin](#installation)
2. [Generate API Key From Vigilnz Security](#api-Key-generation)
3. [Configure a Vigilnz API Key Credential](#configure-vigilnz-credentials)
4. [Add Vigilnz Security to your Project](#add-vigilnz-security-to-your-project)
5. [View Your Vigilnz Scan Report](#viewing-results)

## Installation

### Option A: From Jenkins Update Center

1. Go to **Manage Jenkins** → **Manage Plugins**
2. Search for "Vigilnz Security"
3. Click **Install without restart** or **Download now and install after restart**

![Plugin Manager search result](src/main/resources/images/search_result.png)

### Option B: Manual Upload

1. Download the latest `.hpi` file from [GitHub Releases](https://github.com/jenkinsci/vigilnz-security-plugin/releases)
2. Go to **Manage Jenkins** → **Manage Plugins** → **Advanced**
3. Upload the `.hpi` file under **Upload Plugin**
4. Restart Jenkins

![Upload Plugin screen](src/main/resources/images/manual_upload.png)


## API Key Generation

### To generate your Vigilnz API Key:

1. Login to the [Vigilnz](https://vigilnz.com/) application.
2. Navigate to Settings → API Keys.
3. Click Generate New Key or View API Key (If exits).
4. Copy the API Key and store it securely.

![API Key generation screen](src/main/resources/images/vigilnz_api.png)


## Configure Vigilnz Credentials

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **Add Credentials**
3. Select **Vigilnz Security Token** from the kind dropdown
4. Enter:
   - **Token**: Your Vigilnz API key
   - **ID**: Unique identifier (optional, auto-generated if not provided)
   - **Description**: Description for this credential
5. Click **OK**

![Add Vigilnz credential](src/main/resources/images/vigilnz_credential.png)

## Add Vigilnz Security to your Project

### 1. Using Vigilnz in Freestyle Jobs

1. Create a new Freestyle project or edit an existing one
2. In **Build Steps**, click **Add build step** → **Invoke Vigilnz Security Task**
3. Configure:
   - **Credentials**: Select your Vigilnz credential
   - **Target File**: (Optional) File or path to scan
   - **Scan Types**: Select at least one scan type (SCA, SAST, SBOM)
4. Save and run the build

![Freestyle job configuration](src/main/resources/images/freestyle.png)


### 2. Using Vigilnz in Pipeline Jobs

```groovy
pipeline {
    agent any

    stages {
        stage('Security Scan') {
            steps {
                vigilnzScan(
                    credentialsId: 'my-vigilnz-creds',
                    scanTypes: 'sca,sast,sbom'
                )
            }
        }
    }
}

```

![Pipeline job configuration](src/main/resources/images/pipeline.png)

## Parameters Reference

| Parameter     | Required | Description                           |
|---------------|----------|---------------------------------------|
| credentialsId | True     | ID of Vigilnz credential              |
| scanTypes     | True     | Comma-separated list: `sca,sast,sbom` |

[//]: # (| targetFile    | False    | File/path to scan &#40;optional&#41;          |)


## Configuration

### Scan Types

- **SCA**: Software Composition Analysis
- **SBOM**: Software Bill of Materials
- **SAST**: Static Application Security Testing
- **IAC**: Infrastructure as Code — checks configuration files (Terraform, Kubernetes, etc.) for misconfigurations.
- **SECRET SCAN**: Secret Detection — finds hardcoded credentials, API keys, and sensitive information in source code.

## Viewing Results

### After a build completes:

1. **Sidebar Summary**: View a quick summary in the build page sidebar
2. **Full Details**: Click "Vigilnz Scan Results" in the sidebar to see complete scan results
3. **Console Output**: Check the build console for detailed scan logs

![Vigilnz Scan Result screen](src/main/resources/images/vigilnz_result.png)

## Pipeline Examples

### Basic Usage

```groovy
vigilnzScan(
    credentialsId: 'my-vigilnz-token',
    scanTypes: 'sca'
)
```

### Multiple Scan Types

```groovy
vigilnzScan(
    credentialsId: 'my-vigilnz-token',
    scanTypes: 'sca,sast,sbom,iac,secret'
)
```

### With Credentials Binding

```groovy
pipeline {
    agent any

    stages {
        stage('Security Scan') {
            steps {
                withCredentials([string(credentialsId: 'vigilnz-token', variable: 'VIGILNZ_TOKEN')]) {
                    vigilnzScan(
                        credentialsId: 'vigilnz-token',
                        scanTypes: 'sca,sast'
                    )
                }
            }
        }
    }
}
```

## Troubleshooting

### Authentication Failed

- Verify your API key is correct
- Check that the authentication URL is accessible
- Ensure the API Key has not expired

### Scan Types Not Selected

- At least one scan type must be selected
- Check the checkbox selections in the build configuration

### No Results in Sidebar

- Ensure the build completed successfully
- Check the build console for any errors
- Verify the API response was successful

## Support

- **Issues**: Report issues on [GitHub Issues](https://github.com/jenkinsci/vigilnz-security-plugin/issues)
- **Documentation**: [Plugin](https://github.com/jenkinsci/vigilnz-security-plugin)

[//]: # (- **Email**: support@vigilnz.com)

[//]: # (## Contributing)

[//]: # (Contributions are welcome! Please see [CONTRIBUTING.md]&#40;CONTRIBUTING.md&#41; for guidelines.)

## Changelog

### Version 1.0

- Initial release
- Support for SCA, SAST, SBOM, IAC, Secret scan types
- Freestyle and Pipeline job support
- Secure credential management
- Build sidebar results display

## License

Licensed under MIT License. See [LICENSE](LICENSE.md) for details.
