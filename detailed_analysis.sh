#!/bin/bash

BASE_DIR="/home/user/ScheduleMC"
SRC_DIR="$BASE_DIR/src/main/java/de/rolandsw/schedulemc"

echo "=========================================="
echo "ADDITIONAL DETAILED STATISTICS"
echo "=========================================="
echo ""

echo "1. PRODUCTION SYSTEMS COMPARISON TABLE"
echo "========================================"
echo ""
printf "%-12s | %-6s | %-7s | %-7s | %-6s | %-6s | %-8s | %-8s\n" "System" "Files" "Lines" "Avg/File" "Blocks" "Items" "BEntities" "Screens"
printf "%-12s-+-%-6s-+-%-7s-+-%-7s-+-%-6s-+-%-6s-+-%-8s-+-%-8s\n" "------------" "------" "-------" "-------" "------" "------" "--------" "--------"

for system in tobacco cannabis coca poppy mushroom meth mdma lsd; do
    if [ -d "$SRC_DIR/$system" ]; then
        files=$(find "$SRC_DIR/$system" -name "*.java" | wc -l)
        lines=$(find "$SRC_DIR/$system" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        avg=$(awk "BEGIN {printf \"%.1f\", $lines/$files}")
        blocks=$(find "$SRC_DIR/$system" -name "*Block.java" ! -name "*BlockEntity.java" | wc -l)
        items=$(find "$SRC_DIR/$system" -name "*Item.java" | wc -l)
        bentities=$(find "$SRC_DIR/$system" -name "*BlockEntity.java" | wc -l)
        screens=$(find "$SRC_DIR/$system" -name "*Screen.java" | wc -l)

        printf "%-12s | %6d | %7d | %7s | %6d | %6d | %8d | %8d\n" "$system" "$files" "$lines" "$avg" "$blocks" "$items" "$bentities" "$screens"
    fi
done

echo ""
echo "2. CORE SYSTEMS BREAKDOWN"
echo "========================================"
echo ""

echo "=== Vehicle System Components ==="
if [ -d "$SRC_DIR/vehicle" ]; then
    echo "Subdirectories:"
    find "$SRC_DIR/vehicle" -maxdepth 1 -type d | tail -n +2 | sed 's|.*/|  - |'
    echo ""
    for subdir in $(find "$SRC_DIR/vehicle" -maxdepth 1 -type d | tail -n +2); do
        name=$(basename "$subdir")
        files=$(find "$subdir" -name "*.java" | wc -l)
        lines=$(find "$subdir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        [ -z "$lines" ] && lines=0
        printf "  %-20s: %3d files, %5d lines\n" "$name" "$files" "$lines"
    done
fi

echo ""
echo "=== NPC System Components ==="
if [ -d "$SRC_DIR/npc" ]; then
    echo "Subdirectories:"
    find "$SRC_DIR/npc" -maxdepth 1 -type d | tail -n +2 | sed 's|.*/|  - |'
    echo ""
    for subdir in $(find "$SRC_DIR/npc" -maxdepth 1 -type d | tail -n +2); do
        name=$(basename "$subdir")
        files=$(find "$subdir" -name "*.java" | wc -l)
        lines=$(find "$subdir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        [ -z "$lines" ] && lines=0
        printf "  %-20s: %3d files, %5d lines\n" "$name" "$files" "$lines"
    done
fi

echo ""
echo "=== Economy System Components ==="
if [ -d "$SRC_DIR/economy" ]; then
    echo "Subdirectories:"
    find "$SRC_DIR/economy" -maxdepth 1 -type d | tail -n +2 | sed 's|.*/|  - |'
    echo ""
    for subdir in $(find "$SRC_DIR/economy" -maxdepth 1 -type d | tail -n +2); do
        name=$(basename "$subdir")
        files=$(find "$subdir" -name "*.java" | wc -l)
        lines=$(find "$subdir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        [ -z "$lines" ] && lines=0
        printf "  %-20s: %3d files, %5d lines\n" "$name" "$files" "$lines"
    done
fi

echo ""
echo "=== Lightmap System Components ==="
if [ -d "$SRC_DIR/lightmap" ]; then
    echo "Subdirectories:"
    find "$SRC_DIR/lightmap" -maxdepth 1 -type d | tail -n +2 | sed 's|.*/|  - |'
    echo ""
    for subdir in $(find "$SRC_DIR/lightmap" -maxdepth 1 -type d | tail -n +2); do
        name=$(basename "$subdir")
        files=$(find "$subdir" -name "*.java" | wc -l)
        lines=$(find "$subdir" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
        [ -z "$lines" ] && lines=0
        printf "  %-20s: %3d files, %5d lines\n" "$name" "$files" "$lines"
    done
fi

echo ""
echo "3. PACKAGE SIZE DISTRIBUTION"
echo "========================================"
echo ""

# Count packages by size
small=0
medium=0
large=0
xlarge=0

for dir in "$SRC_DIR"/*; do
    if [ -d "$dir" ]; then
        files=$(find "$dir" -name "*.java" | wc -l)
        if [ $files -lt 10 ]; then
            ((small++))
        elif [ $files -lt 30 ]; then
            ((medium++))
        elif [ $files -lt 80 ]; then
            ((large++))
        else
            ((xlarge++))
        fi
    fi
done

echo "Package Distribution by File Count:"
echo "  Small (< 10 files):    $small packages"
echo "  Medium (10-29 files):  $medium packages"
echo "  Large (30-79 files):   $large packages"
echo "  X-Large (80+ files):   $xlarge packages"

echo ""
echo "4. CODE ORGANIZATION METRICS"
echo "========================================"
echo ""

total_files=$(find "$SRC_DIR" -name "*.java" | wc -l)
total_lines=$(find "$SRC_DIR" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
total_classes=$(grep -rh "^public class\|^class\|^final class" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)
total_methods=$(grep -rh "^\s*public.*(" "$SRC_DIR" --include="*.java" 2>/dev/null | wc -l)

echo "Averages:"
printf "  Average file size:           %.1f lines\n" $(awk "BEGIN {print $total_lines/$total_files}")
printf "  Average methods per file:    %.1f methods\n" $(awk "BEGIN {print $total_methods/$total_files}")
printf "  Average methods per class:   %.1f methods\n" $(awk "BEGIN {print $total_methods/$total_classes}")

echo ""
echo "Code Distribution:"
production_lines=$(find "$SRC_DIR"/{tobacco,cannabis,coca,poppy,mushroom,meth,mdma,lsd,production} -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
vehicle_lines=$(find "$SRC_DIR/vehicle" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
npc_lines=$(find "$SRC_DIR/npc" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
economy_lines=$(find "$SRC_DIR/economy" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
lightmap_lines=$(find "$SRC_DIR/lightmap" -name "*.java" -exec wc -l {} + 2>/dev/null | tail -1 | awk '{print $1}')
other_lines=$((total_lines - production_lines - vehicle_lines - npc_lines - economy_lines - lightmap_lines))

printf "  Production Systems:  %6d lines (%.1f%%)\n" "$production_lines" $(awk "BEGIN {print $production_lines*100/$total_lines}")
printf "  Vehicle System:      %6d lines (%.1f%%)\n" "$vehicle_lines" $(awk "BEGIN {print $vehicle_lines*100/$total_lines}")
printf "  Lightmap System:     %6d lines (%.1f%%)\n" "$lightmap_lines" $(awk "BEGIN {print $lightmap_lines*100/$total_lines}")
printf "  NPC System:          %6d lines (%.1f%%)\n" "$npc_lines" $(awk "BEGIN {print $npc_lines*100/$total_lines}")
printf "  Economy System:      %6d lines (%.1f%%)\n" "$economy_lines" $(awk "BEGIN {print $economy_lines*100/$total_lines}")
printf "  Other Systems:       %6d lines (%.1f%%)\n" "$other_lines" $(awk "BEGIN {print $other_lines*100/$total_lines}")

echo ""
echo "5. NETWORK PACKET ANALYSIS"
echo "========================================"
echo ""

echo "Packets by Type:"
s2c=$(find "$SRC_DIR" -name "*S2C.java" | wc -l)
c2s=$(find "$SRC_DIR" -name "*C2S.java" | wc -l)
other=$(find "$SRC_DIR" -name "*Packet.java" ! -name "*S2C.java" ! -name "*C2S.java" | wc -l)

echo "  Server to Client (S2C):  $s2c"
echo "  Client to Server (C2S):  $c2s"
echo "  Other/Bidirectional:     $other"

echo ""
echo "Packets by Package:"
for dir in "$SRC_DIR"/*; do
    if [ -d "$dir" ]; then
        name=$(basename "$dir")
        count=$(find "$dir" -name "*Packet.java" -o -name "*S2C.java" -o -name "*C2S.java" 2>/dev/null | wc -l)
        if [ $count -gt 0 ]; then
            printf "  %-20s: %2d packets\n" "$name" "$count"
        fi
    fi
done

echo ""
echo "6. GUI/SCREEN ANALYSIS"
echo "========================================"
echo ""

echo "Screens by Package:"
for dir in "$SRC_DIR"/*; do
    if [ -d "$dir" ]; then
        name=$(basename "$dir")
        count=$(find "$dir" -name "*Screen.java" -o -name "*Gui.java" 2>/dev/null | wc -l)
        if [ $count -gt 0 ]; then
            printf "  %-20s: %2d screens\n" "$name" "$count"
        fi
    fi
done

echo ""
echo "7. TEST COVERAGE DETAILS"
echo "========================================"
echo ""

TEST_DIR="$BASE_DIR/src/test"
if [ -d "$TEST_DIR" ]; then
    echo "Test Distribution:"
    for dir in $(find "$TEST_DIR/java/de/rolandsw/schedulemc" -maxdepth 1 -type d 2>/dev/null | tail -n +2); do
        name=$(basename "$dir")
        files=$(find "$dir" -name "*.java" | wc -l)
        tests=$(grep -rh "@Test" "$dir" --include="*.java" 2>/dev/null | wc -l)
        if [ $files -gt 0 ]; then
            printf "  %-20s: %2d files, %3d tests\n" "$name" "$files" "$tests"
        fi
    done

    echo ""
    echo "Test Method Distribution:"
    total_test_methods=293
    total_java_files=773
    coverage_ratio=$(awk "BEGIN {printf \"%.2f\", $total_test_methods*100/$total_java_files}")
    echo "  Test/Production Ratio: $coverage_ratio% (293 test methods for 773 Java files)"
fi

echo ""
echo "8. CONFIGURATION FILES"
echo "========================================"
echo ""

CONFIG_DIR="$SRC_DIR/config"
if [ -d "$CONFIG_DIR" ]; then
    echo "Configuration Classes:"
    find "$CONFIG_DIR" -name "*.java" -exec basename {} \; | sort

    echo ""
    echo "Config Lines:"
    for file in $(find "$CONFIG_DIR" -name "*.java"); do
        lines=$(wc -l < "$file")
        name=$(basename "$file")
        printf "  %-40s: %4d lines\n" "$name" "$lines"
    done
fi

echo ""
echo "9. API INTERFACE ANALYSIS"
echo "========================================"
echo ""

API_DIR="$SRC_DIR/api"
if [ -d "$API_DIR" ]; then
    echo "API Interfaces:"
    find "$API_DIR" -name "*.java" -exec basename {} \;

    echo ""
    echo "API Methods Count:"
    for file in $(find "$API_DIR" -name "*.java"); do
        methods=$(grep -c "^\s*.*(" "$file" 2>/dev/null || echo 0)
        name=$(basename "$file")
        printf "  %-40s: %3d methods\n" "$name" "$methods"
    done
fi

echo ""
