# Vigilnz Security Plugin

[![Jenkins Plugin](https://img.shields.io/jenkins/plugin/v/vigilnz-security.svg)](https://plugins.jenkins.io/vigilnz-security)
[![Jenkins Plugin Installs](https://img.shields.io/jenkins/plugin/i/vigilnz-security.svg?color=blue)](https://plugins.jenkins.io/vigilnz-security)

Vigilnz Security Plugin integrates comprehensive security scanning capabilities into Jenkins CI/CD pipelines. Run CVE, SAST, SBOM, and other security scans as part of your build process.

## Features

- 🔒 **Multiple Scan Types**: Support for CVE, SAST, SBOM, and more
- 🔐 **Secure Credential Management**: Store and manage Vigilnz API tokens securely
- 🚀 **Freestyle & Pipeline Support**: Works with both traditional and modern Jenkins jobs
- 📊 **Detailed Results**: View scan results directly in the Jenkins build sidebar
- ⚙️ **Flexible Configuration**: Select which scan types to run per build
- 🔄 **Token Management**: Automatic token refresh and caching

## Requirements

- Jenkins 2.516.3 or later
- Java 17 or later
- Vigilnz API access (API key required)

## Installation

### From Jenkins Update Center

1. Go to **Manage Jenkins** → **Manage Plugins**
2. Search for "Vigilnz Security"
3. Click **Install without restart** or **Download now and install after restart**

### Manual Installation

1. Download the latest `.hpi` file from [GitHub Releases](https://github.com/your-org/vigilnz-security-plugin/releases)
2. Go to **Manage Jenkins** → **Manage Plugins** → **Advanced**
3. Upload the `.hpi` file under **Upload Plugin**
4. Restart Jenkins

## Getting Started

### 1. Configure Vigilnz Credentials

1. Go to **Manage Jenkins** → **Manage Credentials**
2. Click **Add Credentials**
3. Select **Vigilnz Security Token** from the kind dropdown
4. Enter:
   - **Token**: Your Vigilnz API key
   - **ID**: Unique identifier (optional, auto-generated if not provided)
   - **Description**: Description for this credential
5. Click **OK**

### 2. Use in Freestyle Job

1. Create a new Freestyle project or edit an existing one
2. In **Build Steps**, click **Add build step** → **Invoke Vigilnz Security Task**
3. Configure:
   - **Token**: Select your Vigilnz credential
   - **Target File**: (Optional) File or path to scan
   - **Scan Types**: Select at least one scan type (CVE, SAST, SBOM)
4. Save and run the build

### 3. Use in Pipeline

```groovy
pipeline {
    agent any

    stages {
        stage('Security Scan') {
            steps {
                vigilnzScan(
                    token: 'my-vigilnz-token',
                    scanTypes: ['cve', 'sast', 'sbom']
                )
            }
        }
    }
}
```

## Configuration

### Scan Types

- **CVE**: Common Vulnerabilities and Exposures scan
- **SAST**: Static Application Security Testing
- **SBOM**: Software Bill of Materials

## Viewing Results

After a build completes:

1. **Sidebar Summary**: View a quick summary in the build page sidebar
2. **Full Details**: Click "View Details →" in the sidebar to see complete scan results
3. **Console Output**: Check the build console for detailed scan logs

## Pipeline Examples

### Basic Usage

```groovy
vigilnzScan(
    token: 'my-vigilnz-token',
    scanTypes: ['cve']
)
```

### Multiple Scan Types

```groovy
vigilnzScan(
    token: 'my-vigilnz-token',
    scanTypes: ['cve', 'sast', 'sbom']
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
                        token: 'vigilnz-token',
                        scanTypes: ['cve', 'sast']
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
- Ensure the token has not expired

### Scan Types Not Selected

- At least one scan type must be selected
- Check the checkbox selections in the build configuration

### No Results in Sidebar

- Ensure the build completed successfully
- Check the build console for any errors
- Verify the API response was successful

## Support

- **Issues**: Report issues on [GitHub Issues](https://github.com/your-org/vigilnz-security-plugin/issues)
- **Documentation**: [Plugin Wiki](https://github.com/your-org/vigilnz-security-plugin/wiki)
- **Email**: support@vigilnz.com

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](CONTRIBUTING.md) for guidelines.

## Changelog

### Version 1.0

- Initial release
- Support for CVE, SAST, SBOM scan types
- Freestyle and Pipeline job support
- Secure credential management
- Build sidebar results display

## License

Licensed under MIT License. See [LICENSE](LICENSE.md) for details.
