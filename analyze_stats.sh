#!/bin/bash

echo "===== ScheduleMC Comprehensive Statistical Analysis ====="
echo ""
echo "Generated on: $(date)"
echo ""

BASE_DIR="/home/user/ScheduleMC"
SRC_DIR="$BASE_DIR/src/main/java/de/rolandsw/schedulemc"
TEST_DIR="$BASE_DIR/src/test"
RESOURCES_DIR="$BASE_DIR/src/main/resources"

echo "========================================"
echo "1. CODE METRICS BY PACKAGE"
echo "========================================"
echo ""

# Function to count lines in a directory
count_package_stats() {
    local dir=$1
    local name=$2

    if [ -d "$dir" ]; then
        local files=$(find "$dir" -name "*.java" | wc -l)
        local lines=$(find "$dir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        local classes=$(grep -rh "^public class\|^class\|^public final class\|^final class" "$dir" --include="*.java" 2>/dev/null | wc -l)
        local interfaces=$(grep -rh "^public interface\|^interface" "$dir" --include="*.java" 2>/dev/null | wc -l)
        local enums=$(grep -rh "^public enum\|^enum" "$dir" --include="*.java" 2>/dev/null | wc -l)
        local methods=$(grep -rh "^\s*public.*(" "$dir" --include="*.java" 2>/dev/null | wc -l)

        printf "%-25s | Files: %4d | Lines: %6d | Classes: %3d | Interfaces: %2d | Enums: %2d | Methods: %4d\n" \
               "$name" "$files" "$lines" "$classes" "$interfaces" "$enums" "$methods"
    fi
}

# Production systems
echo "Production Systems:"
count_package_stats "$SRC_DIR/tobacco" "tobacco"
count_package_stats "$SRC_DIR/cannabis" "cannabis"
count_package_stats "$SRC_DIR/coca" "coca"
count_package_stats "$SRC_DIR/poppy" "poppy"
count_package_stats "$SRC_DIR/mushroom" "mushroom"
count_package_stats "$SRC_DIR/meth" "meth"
count_package_stats "$SRC_DIR/mdma" "mdma"
count_package_stats "$SRC_DIR/lsd" "lsd"
count_package_stats "$SRC_DIR/production" "production (core)"

echo ""
echo "Core Systems:"
count_package_stats "$SRC_DIR/vehicle" "vehicle"
count_package_stats "$SRC_DIR/npc" "npc"
count_package_stats "$SRC_DIR/economy" "economy"
count_package_stats "$SRC_DIR/warehouse" "warehouse"
count_package_stats "$SRC_DIR/lightmap" "lightmap"

echo ""
echo "Support Systems:"
count_package_stats "$SRC_DIR/achievement" "achievement"
count_package_stats "$SRC_DIR/tutorial" "tutorial"
count_package_stats "$SRC_DIR/territory" "territory"
count_package_stats "$SRC_DIR/region" "region"
count_package_stats "$SRC_DIR/market" "market"
count_package_stats "$SRC_DIR/messaging" "messaging"

echo ""
echo "Infrastructure:"
count_package_stats "$SRC_DIR/api" "api"
count_package_stats "$SRC_DIR/client" "client"
count_package_stats "$SRC_DIR/config" "config"
count_package_stats "$SRC_DIR/commands" "commands"
count_package_stats "$SRC_DIR/data" "data"
count_package_stats "$SRC_DIR/events" "events"
count_package_stats "$SRC_DIR/gui" "gui"
count_package_stats "$SRC_DIR/items" "items"
count_package_stats "$SRC_DIR/managers" "managers"
count_package_stats "$SRC_DIR/util" "util"
count_package_stats "$SRC_DIR/utility" "utility"

echo ""
echo "========================================"
echo "2. FILE SIZE ANALYSIS"
echo "========================================"
echo ""

echo "Largest Java Files (by line count):"
find "$SRC_DIR" -name "*.java" -exec wc -l {} + | sort -rn | head -20 | awk '{printf "%-6d lines: %s\n", $1, $2}'

echo ""
echo "Average file sizes by package:"
for dir in tobacco cannabis coca poppy mushroom meth mdma lsd production vehicle npc economy warehouse lightmap; do
    if [ -d "$SRC_DIR/$dir" ]; then
        avg=$(find "$SRC_DIR/$dir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk -v files=$(find "$SRC_DIR/$dir" -name "*.java" | wc -l) '{if(files>0) printf "%.1f", $1/files; else print "0"}')
        printf "%-25s: %s lines/file\n" "$dir" "$avg"
    fi
done

echo ""
echo "========================================"
echo "3. ARCHITECTURE COMPONENTS"
echo "========================================"
echo ""

echo "BlockEntity Classes:"
grep -rh "extends BlockEntity\|implements BlockEntity" "$SRC_DIR" --include="*.java" | wc -l
echo ""
echo "Sample BlockEntities:"
grep -rl "extends BlockEntity\|BlockEntity.class" "$SRC_DIR" --include="*.java" | head -10 | sed 's|.*/||' | sed 's|\.java||'

echo ""
echo "Block Classes:"
grep -rh "extends Block\|extends AbstractPlantBlock\|extends AbstractProcessingBlock" "$SRC_DIR" --include="*.java" | wc -l

echo ""
echo "Item Classes:"
grep -rh "extends Item\|implements ItemLike" "$SRC_DIR" --include="*.java" | wc -l

echo ""
echo "Network Packets:"
find "$SRC_DIR" -name "*Packet.java" -o -name "*S2C.java" -o -name "*C2S.java" | wc -l
echo "Packet files:"
find "$SRC_DIR" -name "*Packet.java" -o -name "*S2C.java" -o -name "*C2S.java" | sed 's|.*/||'

echo ""
echo "GUI Screens:"
find "$SRC_DIR" -name "*Screen.java" -o -name "*Gui.java" | wc -l

echo ""
echo "Manager Classes:"
find "$SRC_DIR" -name "*Manager.java" | wc -l
echo "Manager files:"
find "$SRC_DIR" -name "*Manager.java" | sed 's|.*/||'

echo ""
echo "Entity Classes:"
find "$SRC_DIR" -name "*Entity.java" ! -name "*BlockEntity.java" | wc -l

echo ""
echo "========================================"
echo "4. PRODUCTION SYSTEMS DETAILED"
echo "========================================"
echo ""

for system in tobacco cannabis coca poppy mushroom meth mdma lsd; do
    if [ -d "$SRC_DIR/$system" ]; then
        echo "=== $system ==="
        files=$(find "$SRC_DIR/$system" -name "*.java" | wc -l)
        lines=$(find "$SRC_DIR/$system" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        blocks=$(find "$SRC_DIR/$system" -name "*Block.java" | wc -l)
        items=$(find "$SRC_DIR/$system" -name "*Item.java" | wc -l)
        blockentities=$(find "$SRC_DIR/$system" -name "*BlockEntity.java" | wc -l)

        echo "  Files: $files"
        echo "  Lines: $lines"
        echo "  Blocks: $blocks"
        echo "  Items: $items"
        echo "  BlockEntities: $blockentities"
        echo "  Subdirectories:"
        find "$SRC_DIR/$system" -type d | tail -n +2 | sed 's|.*/|    - |'
        echo ""
    fi
done

echo ""
echo "========================================"
echo "5. TEST COVERAGE"
echo "========================================"
echo ""

if [ -d "$TEST_DIR" ]; then
    echo "Test Files:"
    find "$TEST_DIR" -name "*.java" | wc -l

    echo ""
    echo "Test Methods:"
    grep -rh "@Test" "$TEST_DIR" --include="*.java" 2>/dev/null | wc -l

    echo ""
    echo "Test Classes:"
    find "$TEST_DIR" -name "*Test.java" | wc -l

    echo ""
    echo "Test Packages:"
    find "$TEST_DIR" -type d | tail -n +2 | sed 's|.*/|  - |'
fi

echo ""
echo "========================================"
echo "6. RESOURCE STATISTICS"
echo "========================================"
echo ""

if [ -d "$RESOURCES_DIR" ]; then
    echo "Language Files:"
    find "$RESOURCES_DIR" -name "*.json" -path "*/lang/*" | wc -l
    find "$RESOURCES_DIR" -name "*.json" -path "*/lang/*" | sed 's|.*/|  - |'

    echo ""
    echo "Texture Files:"
    find "$RESOURCES_DIR" -name "*.png" | wc -l

    echo ""
    echo "Model Files (JSON):"
    find "$RESOURCES_DIR" -name "*.json" -path "*/models/*" | wc -l

    echo ""
    echo "Blockstate Files:"
    find "$RESOURCES_DIR" -name "*.json" -path "*/blockstates/*" | wc -l

    echo ""
    echo "Recipe Files:"
    find "$RESOURCES_DIR" -name "*.json" -path "*/recipes/*" | wc -l

    echo ""
    echo "Total Resource Files:"
    find "$RESOURCES_DIR" -type f | wc -l

    echo ""
    echo "Resource Breakdown by Type:"
    find "$RESOURCES_DIR" -type f | sed 's|.*\.||' | sort | uniq -c | sort -rn | head -10
fi

echo ""
echo "========================================"
echo "7. DEPENDENCY ANALYSIS"
echo "========================================"
echo ""

echo "CoreLib Imports:"
grep -rh "import.*corelib" "$SRC_DIR" --include="*.java" | wc -l

echo ""
echo "Files using CoreLib:"
grep -rl "import.*corelib" "$SRC_DIR" --include="*.java" | wc -l

echo ""
echo "Most common imports:"
grep -rh "^import" "$SRC_DIR" --include="*.java" | sed 's/import //' | sed 's/;.*//' | cut -d. -f1-3 | sort | uniq -c | sort -rn | head -15

echo ""
echo "========================================"
echo "8. COMPLEXITY INDICATORS"
echo "========================================"
echo ""

echo "Files with most methods:"
for file in $(find "$SRC_DIR" -name "*.java"); do
    methods=$(grep -c "^\s*public.*(" "$file" 2>/dev/null || echo 0)
    echo "$methods $file"
done | sort -rn | head -10 | awk '{printf "%3d methods: %s\n", $1, $2}' | sed 's|.*/||'

echo ""
echo "Files with most imports:"
for file in $(find "$SRC_DIR" -name "*.java"); do
    imports=$(grep -c "^import" "$file" 2>/dev/null || echo 0)
    echo "$imports $file"
done | sort -rn | head -10 | awk '{printf "%3d imports: %s\n", $1, $2}' | sed 's|.*/||'

echo ""
echo "========================================"
echo "SUMMARY"
echo "========================================"
echo ""
echo "Total Statistics:"
echo "  Java Files: $(find "$SRC_DIR" -name "*.java" | wc -l)"
echo "  Total Lines: $(find "$SRC_DIR" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')"
echo "  Classes: $(grep -rh "^public class\|^class\|^final class" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)"
echo "  Interfaces: $(grep -rh "^public interface\|^interface" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)"
echo "  Enums: $(grep -rh "^public enum\|^enum" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)"
echo "  Public Methods: $(grep -rh "^\s*public.*(" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)"
echo ""
