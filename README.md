# PGP SupportPac for IBM App Connect Enterprise (ACE)

[![License: LGPL v3](https://img.shields.io/badge/License-LGPL%20v3-blue.svg)](https://www.gnu.org/licenses/lgpl-3.0)
[![Java 17](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/projects/jdk/17/)
[![IBM ACE](https://img.shields.io/badge/IBM%20ACE-13.0.6.0+-blue.svg)](https://www.ibm.com/products/app-connect)

> **⚠️ SOURCE CODE REPOSITORY**  
> This repository contains the **source code** for the PGP SupportPac.  
> For **pre-built JAR files, installation guides, and test documentation**, visit:  
> 📦 **[PGP-SupportPac-for-IBM-ACE-V12](https://github.com/matthiasblomme/PGP-SupportPac-for-IBM-ACE-V12)**

---

## 📋 Overview

PGP SupportPac provides application-layer security for IBM App Connect Enterprise (ACE), implementing PGP cryptographic solutions for data confidentiality, integrity, and optional compression. This SupportPac extends IBM ACE with custom nodes for PGP encryption, decryption, and signature operations.

### Key Features

✅ **PGP Encrypter/Decrypter Nodes** - Easily pluggable into IBM ACE Toolkit  
✅ **Key Management Tool** - Command-line tool (`pgpkeytool`) for key generation and repository management  
✅ **Centralized Configuration** - UserDefined Configurable Service for key repositories and default parameters  
✅ **Flexible Operations** - Support for both message and file encryption/decryption  
✅ **Wide Algorithm Support** - Multiple hash, cipher, and compression algorithms  
✅ **Java 17 Modernized** - Updated for IBM ACE v13.0.6.0+ with modern Java features

---

## 🚀 Quick Start

### Prerequisites

- **IBM ACE v13.0.6.0 or later**
- **Java 17** (included with IBM ACE)
- **Bouncy Castle 1.78.1** libraries (jdk18on)

### Building from Source

1. **Clone this repository:**
   ```bash
   git clone https://github.com/matthiasblomme/MyOpenTech-PGP-SupportPac.git
   cd MyOpenTech-PGP-SupportPac
   ```

2. **Download Bouncy Castle libraries:**
   ```bash
   # Windows (PowerShell or CMD)
   cd build_scripts
   download-bouncy-castle-libs.bat
   ```

3. **Build the project:**
   ```bash
   # Windows
   build.bat
   ```

4. **Output location:**
   - Plugin JAR: `binary/ACEv13/plugins/PGPSupportPac.jar`
   - Implementation JAR: `binary/ACEv13/lib/PGPSupportPacImpl.jar`

### Installation & Usage

For installation instructions, configuration guides, and examples, see:  
📦 **[PGP-SupportPac-for-IBM-ACE-V12](https://github.com/matthiasblomme/PGP-SupportPac-for-IBM-ACE-V12)**

---

## 📚 Documentation

### In This Repository (Source Code)

- **[JAVA17_UPGRADE_NOTES.md](JAVA17_UPGRADE_NOTES.md)** - Java 17 upgrade details and changes
- **[JAVA17_MODERNIZATION_ANALYSIS.md](JAVA17_MODERNIZATION_ANALYSIS.md)** - Detailed code analysis and modernization opportunities
- **[COMPREHENSIVE_MODERNIZATION_PLAN.md](COMPREHENSIVE_MODERNIZATION_PLAN.md)** - 16-week implementation roadmap
- **[IMPLEMENTATION_ROADMAP.md](IMPLEMENTATION_ROADMAP.md)** - Visual timeline and Gantt charts

### In Binary Repository (Installation & Usage)

Visit **[PGP-SupportPac-for-IBM-ACE-V12](https://github.com/matthiasblomme/PGP-SupportPac-for-IBM-ACE-V12)** for:
- Installation guides
- Configuration examples
- Test documentation
- Sample message flows
- Pre-built JAR files

---

## 🔐 Security Features

### Why PGP for IBM ACE?

IBM ACE provides security through:
- WebSphere MQ security
- Transport layer security (SSL/TLS)
- Access controls and authentication
- WS-Security for web services

**However**, these solutions have limitations:
- WS-Security only applies to web services
- SSL encrypts entire traffic (performance overhead)
- No built-in application-layer encryption for batch/async data
- External applications often require data encryption regardless of transport security

**PGP SupportPac solves these by providing:**
- Application-layer encryption/decryption
- Digital signatures for data integrity
- Optional data compression
- Industry-standard OpenPGP (RFC 4880) compliance
- Integration directly within ACE message flows

---

## 🛠️ Technical Details

### Supported Algorithms

**Hash (Digest) Algorithms:**
- MD5, SHA1, RIPEMD160, MD2
- SHA256, SHA384, SHA512, SHA224

**Cipher Algorithms:**
- IDEA, TRIPLE_DES, CAST5, BLOWFISH, DES
- AES_128, AES_192, AES_256, TWOFISH

**Compression Algorithms:**
- UNCOMPRESSED, ZIP, ZLIB, BZIP2

### Architecture

```
PGP SupportPac
├── PGPSupportPac (Plugin)
│   ├── PGPEncrypterNodeUDN.java
│   └── PGPDecrypterNodeUDN.java
└── PGPSupportPacImpl (Implementation)
    ├── PGPEncrypter.java
    ├── PGPDecrypter.java
    ├── PGPKeyGen.java
    ├── PGPKeyUtil.java
    └── pgpkeytool (CLI)
```

### Dependencies

- **Bouncy Castle PGP** (bcpg-jdk18on-1.78.1.jar)
- **Bouncy Castle Provider** (bcprov-jdk18on-1.78.1.jar)
- **IBM ACE Runtime** (v13.0.6.0+)

---

## 🔄 Java 17 Modernization

This project has been upgraded to Java 17 with modern features:

### Implemented
- ✅ Try-with-resources for resource management
- ✅ Modern collection types (replacing Vector)
- ✅ Updated Bouncy Castle libraries (1.78.1)
- ✅ Improved error handling

### Planned (See [COMPREHENSIVE_MODERNIZATION_PLAN.md](COMPREHENSIVE_MODERNIZATION_PLAN.md))
- 🔄 Switch expressions (JEP 361)
- 🔄 Text blocks (JEP 378)
- 🔄 Pattern matching instanceof (JEP 394)
- 🔄 Records for data classes (JEP 395)
- 🔄 Sealed classes (JEP 409)
- 🔄 Stream API refactoring
- 🔄 Performance optimizations

---

## 📦 Repository Structure

```
MyOpenTech-PGP-SupportPac/
├── src/ACEv13/v2.0.1.0/          # Source code
│   ├── PGPSupportPac/            # Plugin source
│   └── PGPSupportPacImpl/        # Implementation source
├── binary/ACEv13/                # Build output
│   ├── plugins/                  # Plugin JAR
│   └── lib/                      # Implementation JARs
├── build_scripts/                # Build automation
├── JAVA17_*.md                   # Java 17 documentation
├── COMPREHENSIVE_*.md            # Planning documents
└── README.md                     # This file
```

---

## 🤝 Contributing

Contributions are welcome! This project is licensed under **LGPL-3.0**, which means:

✅ **You can:**
- Use in production (including commercial)
- Modify the code
- Distribute modified versions

📋 **You must:**
- Share modifications with the community (via pull request or public repository)
- Document your changes
- Maintain this license and attribution

### How to Contribute

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make your changes
4. Commit with clear messages (`git commit -m 'Add amazing feature'`)
5. Push to your branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

**See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines** *(coming soon)*

---

## 📄 License

This project is licensed under the **GNU Lesser General Public License v3.0 (LGPL-3.0)**.

### What This Means

✅ **Free to Use:**
- Use in production environments (including commercial)
- No licensing fees or usage restrictions

✅ **Modification Requirements:**
If you modify this software, you **must**:
1. Share your modifications with the community
2. Document your changes
3. Maintain this license and attribution

✅ **Integration Friendly:**
- Can be used with proprietary ACE applications
- Only modifications to the PGP SupportPac itself must be shared
- Your ACE flows and applications remain yours

### Why LGPL-3.0?

This license ensures:
- The community benefits from improvements
- Free production use for everyone
- Continued open-source development

**See [LICENSE](LICENSE) file for complete terms.**

---

## 🔗 Related Resources

### Repositories
- 📦 **[Binary & Documentation Repository](https://github.com/matthiasblomme/PGP-SupportPac-for-IBM-ACE-V12)** - Pre-built JARs and installation guides
- 💻 **[This Repository](https://github.com/matthiasblomme/MyOpenTech-PGP-SupportPac)** - Source code

### External Links
- [Bouncy Castle](https://www.bouncycastle.org/) - Cryptographic library
- [OpenPGP Standard (RFC 4880)](https://www.rfc-editor.org/rfc/rfc4880)
- [IBM App Connect Enterprise](https://www.ibm.com/products/app-connect)

### Community
- [IBM developerWorks Forum](https://www.ibm.com/developerworks/community/groups/community/pgpsupportpaciib)
- [MQSeries.net Forum](http://www.mqseries.net/phpBB2/viewtopic.php?t=68728)

---

## 👥 Credits

### Original Author
- **Dipak Pal** - Original PGP SupportPac creator

### Current Maintainer
- **Matthias Blomme** - Java 17 modernization and ongoing maintenance

### Contributors
See [CONTRIBUTORS.md](CONTRIBUTORS.md) for a list of contributors *(coming soon)*

---

## 📧 Contact & Support

### Questions or Issues?
- Open an issue in this repository
- Visit the community forums (links above)

### Feedback & Suggestions
- Email: **dipakpal.opentech@gmail.com** (original author)
- Create a GitHub issue for feature requests

---

## 🎯 Roadmap

See [COMPREHENSIVE_MODERNIZATION_PLAN.md](COMPREHENSIVE_MODERNIZATION_PLAN.md) for the complete 16-week modernization roadmap, including:

- **Phase 1-2 (Weeks 1-4):** Critical updates and code modernization
- **Phase 3-4 (Weeks 5-8):** Performance optimization and advanced features
- **Phase 5-6 (Weeks 9-12):** Extended PGP operations and integrations
- **Phase 7-8 (Weeks 13-16):** Testing, documentation, and release

---

## ⭐ Show Your Support

If you find this project useful, please consider:
- ⭐ Starring this repository
- 🐛 Reporting bugs or issues
- 💡 Suggesting new features
- 🤝 Contributing code improvements
- 📢 Sharing with the ACE community

---

**Made with ❤️ for the IBM App Connect Enterprise community**
