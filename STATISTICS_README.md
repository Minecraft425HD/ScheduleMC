# ScheduleMC Statistical Analysis - Quick Guide

This directory contains comprehensive statistical analysis of the ScheduleMC mod codebase.

## Generated Files

### 📊 Main Reports

1. **STATISTICS_REPORT.md** (26 KB)
   - Comprehensive statistical analysis
   - Detailed breakdowns by package and feature
   - Architecture documentation
   - Test coverage analysis
   - Resource statistics
   - Complexity metrics
   - **Best for:** Deep dive analysis, documentation reference

2. **STATISTICS_VISUAL.txt** (23 KB)
   - Visual ASCII charts and graphs
   - Quick-reference statistics
   - Bar charts for code distribution
   - Easy-to-scan tables
   - **Best for:** Terminal viewing, quick overview, presentations

3. **statistics_data.csv** (7.6 KB)
   - Structured data export
   - Import into Excel, Google Sheets, or data analysis tools
   - Multiple data tables
   - **Best for:** Data analysis, creating custom charts

### 🔧 Analysis Scripts

4. **analyze_stats.sh** (9.4 KB)
   - Main statistical analysis script
   - Can be re-run to update statistics
   - Usage: `./analyze_stats.sh`

5. **detailed_analysis.sh** (9.9 KB)
   - Detailed breakdown script
   - Additional metrics and comparisons
   - Usage: `./detailed_analysis.sh`

## Quick Statistics

- **Total Java Files:** 773
- **Total Lines of Code:** 110,762
- **Resource Files:** 934
- **Test Coverage:** 37.90%
- **Supported Languages:** 45
- **Network Packets:** 38
- **GUI Screens:** 37

## Key Findings

### Code Distribution
1. **Production Systems** (29.7%) - 8 different drug production chains
2. **NPC System** (13.2%) - Advanced AI and crime mechanics
3. **Lightmap System** (12.7%) - Map rendering and world visualization
4. **Vehicle System** (12.2%) - Vehicle physics and management
5. **Economy System** (6.3%) - Complete financial framework

### Largest Systems
1. **Vehicle** - 139 files, 1,002 methods
2. **Lightmap** - 98 files, 746 methods
3. **NPC** - 78 files, 567 methods
4. **Tobacco** - 77 files (most comprehensive production system)

### Architecture Highlights
- 82 BlockEntities for processing and storage
- 135 Block classes
- 72 Item classes
- 37 Manager classes coordinating systems
- 14 API interfaces for extensibility

### Internationalization
- **45 languages** supported (outstanding)
- 271+ translation keys
- Full localization for EU, Asia, Americas, Middle East

## Codebase Health Score: 8/10

| Category              | Score |
|-----------------------|-------|
| Modularity            | 9/10  |
| Extensibility         | 9/10  |
| Internationalization  | 10/10 |
| Maintainability       | 8/10  |
| Documentation         | 7/10  |
| Complexity            | 7/10  |
| Test Coverage         | 6/10  |

## Files to Monitor

These files exceed 1,000 lines and may benefit from refactoring:

1. MinimapRenderer.java (1,708 lines)
2. PlotCommand.java (1,653 lines)
3. WarehouseScreen.java (1,358 lines)
4. BlockColorCache.java (1,258 lines)
5. NPCCommand.java (1,227 lines)

## How to Use These Reports

### For Project Managers
- Read **STATISTICS_VISUAL.txt** for quick overview
- Reference specific sections in **STATISTICS_REPORT.md**
- Use health scores to track project quality

### For Developers
- Check **STATISTICS_REPORT.md** sections 3 and 8 for complexity hotspots
- Review package-specific metrics for your area
- Use scripts to track changes over time

### For Data Analysis
- Import **statistics_data.csv** into your preferred tool
- Create custom visualizations
- Compare metrics across versions

### For Documentation
- Reference architecture statistics from Section 2
- Use feature distribution data from Section 4
- Include internationalization stats from Section 6

## Re-Running Analysis

To update statistics after code changes:

```bash
cd /home/user/ScheduleMC
./analyze_stats.sh > new_analysis.txt
./detailed_analysis.sh >> new_analysis.txt
```

## Next Steps

### Recommended Actions
1. **Increase test coverage** from 37.9% to 60%+
2. **Refactor large files** exceeding 1,000 lines
3. **Add JavaDoc** to public API interfaces
4. **Performance testing** for lightmap and vehicle systems
5. **Memory profiling** for NPC system

### Strengths to Maintain
- Excellent modular architecture
- Outstanding internationalization
- Strong API design
- Comprehensive feature set
- Clean package organization

---

**Generated:** December 26, 2025
**Analysis Version:** 1.0
**Mod Branch:** claude/mod-analysis-legal-performance-EaPv3
