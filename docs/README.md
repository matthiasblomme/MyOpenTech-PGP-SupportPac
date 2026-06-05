# PGP SupportPac - Documentation

This directory contains documentation for the PGP SupportPac project.

## Current Documentation

### Active Documents
- **[ACE_13.0.7.0_VALIDATION_REPORT.md](ACE_13.0.7.0_VALIDATION_REPORT.md)** - Current validation testing for ACE v13.0.7.0
- **[PASSPHRASE_CURRENT_SETUP.md](PASSPHRASE_CURRENT_SETUP.md)** - Passphrase configuration documentation

### Archived Documents
Historical documentation has been archived with the `archive-` prefix:
- `archive-phase1-java17-upgrade/` - Phase 1 Java 17 upgrade documentation (completed Feb 2026)
- `archive-COMPREHENSIVE_MODERNIZATION_PLAN.md` - Original modernization planning
- `archive-IMPLEMENTATION_ROADMAP.md` - Original implementation roadmap

## Current Status

### Project State
- **Current Version**: 2.0.1.0
- **Java Version**: 17.0.18 (IBM Semeru Runtime)
- **Bouncy Castle**: 1.81 (jdk18on)
- **Last Validated ACE Version**: 13.0.6.0
- **Current Validation Target**: 13.0.7.0 (in progress)

### Recent Updates
- **June 5, 2026**: Started ACE v13.0.7.0 validation
  - Updated build configurations
  - Successfully built JARs with ACE 13.0.7.0 environment
  - Created validation report template

## Documentation Guidelines

### For Current Work
- Keep active documentation in the root `docs/` folder
- Use clear, descriptive filenames
- Include dates and version numbers in reports

### For Historical Documentation
- Prefix archived files with `archive-`
- Maintain original content for reference
- Update this README when archiving documents

## Quick Links

### For Developers
1. Review [ACE_13.0.7.0_VALIDATION_REPORT.md](ACE_13.0.7.0_VALIDATION_REPORT.md) for current testing status
2. Check [PASSPHRASE_CURRENT_SETUP.md](PASSPHRASE_CURRENT_SETUP.md) for configuration details
3. See archived docs for historical context

### For Operations
1. Use build scripts in project root:
   - `build_scripts/build.bat` - Standard build (recommended)
   - `build-maven-java17.bat` - Maven build (requires ACE Toolkit)
2. Review validation reports before deployment

## Version History

| Date | Version | ACE Version | Status | Notes |
|------|---------|-------------|--------|-------|
| 2026-06-05 | 2.0.1.0 | 13.0.7.0 | 🔄 Testing | Validation in progress |
| 2026-02-12 | 2.0.1.0 | 13.0.6.0 | ✅ Validated | Java 17 upgrade complete |

## Contact

For questions or issues:
- Create an issue in the project repository
- Review existing documentation
- Check archived docs for historical context

---

**Last Updated:** June 5, 2026  
**Current Focus:** ACE v13.0.7.0 Validation