#!/bin/bash
#
# iconify.sh - Generate app icons for all platforms from a single source image
#
# This script takes a high-resolution source PNG and generates all the icon
# sizes needed for Android, iOS, and desktop (libGDX/LWJGL3) applications.
#
# REQUIREMENTS:
#   Install ImageMagick on your system:
#
#   Ubuntu/Debian:
#     sudo apt-get install imagemagick
#
#   Fedora/RHEL:
#     sudo dnf install ImageMagick
#
#   Arch Linux:
#     sudo pacman -S imagemagick
#
#   macOS (Homebrew):
#     brew install imagemagick
#
#   Windows (WSL):
#     sudo apt-get install imagemagick
#
# USAGE:
#   ./iconify.sh [source_image]
#
#   If no source image is provided, defaults to lwjgl3/icons/logo.png
#
# RECOMMENDED SOURCE:
#   Use a square PNG image of at least 1024x1024 pixels for best quality.
#   The image should have transparency if you want transparent icons.
#
# Author: NexiVIBE
# License: MIT
#

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default source image location
DEFAULT_SOURCE="lwjgl3/icons/logo.png"

# Parse arguments
SOURCE_IMAGE="${1:-$DEFAULT_SOURCE}"

# Get script directory (project root)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║              ICONIFY - App Icon Generator                  ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Check if ImageMagick is installed
if ! command -v convert &> /dev/null; then
    echo -e "${RED}ERROR: ImageMagick is not installed.${NC}"
    echo ""
    echo "Please install it using one of the following commands:"
    echo "  Ubuntu/Debian: sudo apt-get install imagemagick"
    echo "  Fedora/RHEL:   sudo dnf install ImageMagick"
    echo "  Arch Linux:    sudo pacman -S imagemagick"
    echo "  macOS:         brew install imagemagick"
    exit 1
fi

# Check if source image exists
if [ ! -f "$SOURCE_IMAGE" ]; then
    echo -e "${RED}ERROR: Source image not found: $SOURCE_IMAGE${NC}"
    echo ""
    echo "Usage: $0 [source_image.png]"
    echo "Default source: $DEFAULT_SOURCE"
    exit 1
fi

echo -e "${GREEN}Source image:${NC} $SOURCE_IMAGE"

# Get source image dimensions
SOURCE_SIZE=$(identify -format "%wx%h" "$SOURCE_IMAGE" 2>/dev/null || echo "unknown")
echo -e "${GREEN}Source size:${NC}  $SOURCE_SIZE"

# Warn if source is too small
SOURCE_WIDTH=$(identify -format "%w" "$SOURCE_IMAGE" 2>/dev/null || echo "0")
if [ "$SOURCE_WIDTH" -lt 1024 ]; then
    echo -e "${YELLOW}WARNING: Source image is smaller than 1024px. Icons may appear pixelated.${NC}"
fi

echo ""

# Helper function to resize an image
resize_icon() {
    local size=$1
    local output=$2
    local dir=$(dirname "$output")

    # Create directory if it doesn't exist
    mkdir -p "$dir"

    # Resize with high quality settings
    convert "$SOURCE_IMAGE" \
        -resize "${size}x${size}" \
        -gravity center \
        -background none \
        -extent "${size}x${size}" \
        "$output"

    echo -e "  ${GREEN}✓${NC} $output (${size}x${size})"
}

# ============================================================================
# ANDROID ICONS
# ============================================================================
echo -e "${BLUE}▶ Generating Android icons...${NC}"

ANDROID_RES="android/res"

# Remove any existing adaptive icon XML files (they override PNG icons on Android 8+)
# These often contain placeholder "CHANGE ME" vectors from project templates
if [ -d "$ANDROID_RES/drawable-anydpi-v26" ]; then
    echo -e "  ${YELLOW}Removing adaptive icon XMLs (will use PNG icons instead)${NC}"
    rm -rf "$ANDROID_RES/drawable-anydpi-v26"
fi
if [ -d "$ANDROID_RES/mipmap-anydpi-v26" ]; then
    rm -rf "$ANDROID_RES/mipmap-anydpi-v26"
fi

# Standard launcher icons (ic_launcher.png)
# mdpi: 48x48, hdpi: 72x72, xhdpi: 96x96, xxhdpi: 144x144, xxxhdpi: 192x192
resize_icon 48 "$ANDROID_RES/drawable-mdpi/ic_launcher.png"
resize_icon 72 "$ANDROID_RES/drawable-hdpi/ic_launcher.png"
resize_icon 96 "$ANDROID_RES/drawable-xhdpi/ic_launcher.png"
resize_icon 144 "$ANDROID_RES/drawable-xxhdpi/ic_launcher.png"
resize_icon 192 "$ANDROID_RES/drawable-xxxhdpi/ic_launcher.png"

# Mipmap icons (preferred location for launcher icons on modern Android)
mkdir -p "$ANDROID_RES/mipmap-mdpi"
mkdir -p "$ANDROID_RES/mipmap-hdpi"
mkdir -p "$ANDROID_RES/mipmap-xhdpi"
mkdir -p "$ANDROID_RES/mipmap-xxhdpi"
mkdir -p "$ANDROID_RES/mipmap-xxxhdpi"

resize_icon 48 "$ANDROID_RES/mipmap-mdpi/ic_launcher.png"
resize_icon 72 "$ANDROID_RES/mipmap-hdpi/ic_launcher.png"
resize_icon 96 "$ANDROID_RES/mipmap-xhdpi/ic_launcher.png"
resize_icon 144 "$ANDROID_RES/mipmap-xxhdpi/ic_launcher.png"
resize_icon 192 "$ANDROID_RES/mipmap-xxxhdpi/ic_launcher.png"

# Play Store / Web icon (512x512)
resize_icon 512 "android/ic_launcher-web.png"

# Clean Android build cache to ensure new icons are used
if [ -d "android/build" ]; then
    echo -e "  ${YELLOW}Cleaning Android build cache...${NC}"
    rm -rf "android/build"
fi

echo ""

# ============================================================================
# iOS ICONS
# ============================================================================
echo -e "${BLUE}▶ Generating iOS icons...${NC}"

IOS_ICONS="ios/data/Media.xcassets/AppIcon.appiconset"

# iPhone icons
resize_icon 40 "$IOS_ICONS/iphone-notification-icon-20@2x.png"      # 20@2x = 40
resize_icon 60 "$IOS_ICONS/iphone-notification-icon-20@3x.png"      # 20@3x = 60
resize_icon 58 "$IOS_ICONS/iphone-spotlight-settings-icon-29@2x.png" # 29@2x = 58
resize_icon 87 "$IOS_ICONS/iphone-spotlight-settings-icon-29@3x.png" # 29@3x = 87
resize_icon 80 "$IOS_ICONS/iphone-spotlight-icon-40@2x.png"         # 40@2x = 80
resize_icon 120 "$IOS_ICONS/iphone-spotlight-icon-40@3x.png"        # 40@3x = 120
resize_icon 120 "$IOS_ICONS/iphone-app-icon-60@2x.png"              # 60@2x = 120
resize_icon 180 "$IOS_ICONS/iphone-app-icon-60@3x.png"              # 60@3x = 180

# iPad icons
resize_icon 20 "$IOS_ICONS/ipad-notifications-icon-20@1x.png"       # 20@1x = 20
resize_icon 40 "$IOS_ICONS/ipad-notifications-icon-20@2x.png"       # 20@2x = 40
resize_icon 29 "$IOS_ICONS/ipad-settings-icon-29@1x.png"            # 29@1x = 29
resize_icon 58 "$IOS_ICONS/ipad-settings-icon-29@2x.png"            # 29@2x = 58
resize_icon 40 "$IOS_ICONS/ipad-spotlight-icon-40@1x.png"           # 40@1x = 40
resize_icon 80 "$IOS_ICONS/ipad-spotlight-icon-40@2x.png"           # 40@2x = 80
resize_icon 76 "$IOS_ICONS/ipad-app-icon-76@1x.png"                 # 76@1x = 76
resize_icon 152 "$IOS_ICONS/ipad-app-icon-76@2x.png"                # 76@2x = 152
resize_icon 167 "$IOS_ICONS/ipad-pro-app-icon-83.5@2x.png"          # 83.5@2x = 167

# App Store icon (1024x1024)
resize_icon 1024 "$IOS_ICONS/app-store-icon-1024@1x.png"

# iOS Logo.imageset (used within the app)
IOS_LOGO="ios/data/Media.xcassets/Logo.imageset"
resize_icon 128 "$IOS_LOGO/libgdx@1x.png"
resize_icon 256 "$IOS_LOGO/libgdx@2x.png"
resize_icon 512 "$IOS_LOGO/libgdx@3x.png"

echo ""

# ============================================================================
# DESKTOP ICONS (libGDX/LWJGL3)
# ============================================================================
echo -e "${BLUE}▶ Generating desktop icons...${NC}"

DESKTOP_ICONS="lwjgl3/icons"

resize_icon 16 "$DESKTOP_ICONS/libgdx16.png"
resize_icon 32 "$DESKTOP_ICONS/libgdx32.png"
resize_icon 64 "$DESKTOP_ICONS/libgdx64.png"
resize_icon 128 "$DESKTOP_ICONS/libgdx128.png"

# Also copy the source as logo.png if it's not already there
if [ "$SOURCE_IMAGE" != "$DESKTOP_ICONS/logo.png" ]; then
    cp "$SOURCE_IMAGE" "$DESKTOP_ICONS/logo.png"
    echo -e "  ${GREEN}✓${NC} $DESKTOP_ICONS/logo.png (copied from source)"
fi

echo ""

# ============================================================================
# WINDOWS .ICO FILE
# ============================================================================
echo -e "${BLUE}▶ Generating Windows .ico file...${NC}"

# Create temporary directory for ICO components
ICO_TEMP=$(mktemp -d)

# Generate all sizes needed for ICO
# Standard sizes: 16, 24, 32, 48, 64, 128, 256
for size in 16 24 32 48 64 128 256; do
    convert "$SOURCE_IMAGE" \
        -resize "${size}x${size}" \
        -gravity center \
        -background none \
        -extent "${size}x${size}" \
        "$ICO_TEMP/icon_${size}.png"
done

# Combine into ICO file
# ICO format supports multiple sizes - Windows picks the appropriate one
convert "$ICO_TEMP/icon_16.png" \
        "$ICO_TEMP/icon_24.png" \
        "$ICO_TEMP/icon_32.png" \
        "$ICO_TEMP/icon_48.png" \
        "$ICO_TEMP/icon_64.png" \
        "$ICO_TEMP/icon_128.png" \
        "$ICO_TEMP/icon_256.png" \
        "$DESKTOP_ICONS/logo.ico"

echo -e "  ${GREEN}✓${NC} $DESKTOP_ICONS/logo.ico (multi-resolution: 16-256px)"

# Cleanup temp directory
rm -rf "$ICO_TEMP"

echo ""

# ============================================================================
# macOS .ICNS FILE (if iconutil is available)
# ============================================================================
if command -v iconutil &> /dev/null || command -v png2icns &> /dev/null; then
    echo -e "${BLUE}▶ Generating macOS .icns file...${NC}"

    if command -v iconutil &> /dev/null; then
        # macOS native method using iconutil
        ICONSET_DIR="$DESKTOP_ICONS/logo.iconset"
        mkdir -p "$ICONSET_DIR"

        # iconutil requires specific naming convention
        convert "$SOURCE_IMAGE" -resize 16x16     "$ICONSET_DIR/icon_16x16.png"
        convert "$SOURCE_IMAGE" -resize 32x32     "$ICONSET_DIR/icon_16x16@2x.png"
        convert "$SOURCE_IMAGE" -resize 32x32     "$ICONSET_DIR/icon_32x32.png"
        convert "$SOURCE_IMAGE" -resize 64x64     "$ICONSET_DIR/icon_32x32@2x.png"
        convert "$SOURCE_IMAGE" -resize 128x128   "$ICONSET_DIR/icon_128x128.png"
        convert "$SOURCE_IMAGE" -resize 256x256   "$ICONSET_DIR/icon_128x128@2x.png"
        convert "$SOURCE_IMAGE" -resize 256x256   "$ICONSET_DIR/icon_256x256.png"
        convert "$SOURCE_IMAGE" -resize 512x512   "$ICONSET_DIR/icon_256x256@2x.png"
        convert "$SOURCE_IMAGE" -resize 512x512   "$ICONSET_DIR/icon_512x512.png"
        convert "$SOURCE_IMAGE" -resize 1024x1024 "$ICONSET_DIR/icon_512x512@2x.png"

        iconutil -c icns "$ICONSET_DIR" -o "$DESKTOP_ICONS/logo.icns"
        rm -rf "$ICONSET_DIR"
        echo -e "  ${GREEN}✓${NC} $DESKTOP_ICONS/logo.icns (via iconutil)"
    elif command -v png2icns &> /dev/null; then
        # Linux alternative using png2icns (from libicns)
        # Install: sudo apt-get install icnsutils
        ICNS_TEMP=$(mktemp -d)
        convert "$SOURCE_IMAGE" -resize 1024x1024 "$ICNS_TEMP/icon_1024.png"
        convert "$SOURCE_IMAGE" -resize 512x512   "$ICNS_TEMP/icon_512.png"
        convert "$SOURCE_IMAGE" -resize 256x256   "$ICNS_TEMP/icon_256.png"
        convert "$SOURCE_IMAGE" -resize 128x128   "$ICNS_TEMP/icon_128.png"
        convert "$SOURCE_IMAGE" -resize 32x32     "$ICNS_TEMP/icon_32.png"
        convert "$SOURCE_IMAGE" -resize 16x16     "$ICNS_TEMP/icon_16.png"

        png2icns "$DESKTOP_ICONS/logo.icns" \
            "$ICNS_TEMP/icon_1024.png" \
            "$ICNS_TEMP/icon_512.png" \
            "$ICNS_TEMP/icon_256.png" \
            "$ICNS_TEMP/icon_128.png" \
            "$ICNS_TEMP/icon_32.png" \
            "$ICNS_TEMP/icon_16.png"

        rm -rf "$ICNS_TEMP"
        echo -e "  ${GREEN}✓${NC} $DESKTOP_ICONS/logo.icns (via png2icns)"
    fi
    echo ""
else
    echo -e "${YELLOW}▶ Skipping macOS .icns file (iconutil/png2icns not available)${NC}"
    echo -e "  Install icnsutils for Linux: ${BLUE}sudo apt-get install icnsutils${NC}"
    echo ""
fi

# ============================================================================
# NOTE: assets/logo.png is NOT modified by this script
# The assets folder contains in-game assets (splash screen, etc.) which are
# separate from app icons. Modify assets/logo.png manually if needed.
# ============================================================================

# ============================================================================
# SUMMARY
# ============================================================================
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                    ICON GENERATION COMPLETE                ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Generated icons for:"
echo -e "  ${BLUE}Android${NC}  - drawable-*/ic_launcher.png, mipmap-*/ic_launcher.png"
echo -e "  ${BLUE}iOS${NC}      - AppIcon.appiconset/* (all required sizes)"
echo -e "  ${BLUE}Desktop${NC}  - libgdx16/32/64/128.png, logo.ico"
if [ -f "$DESKTOP_ICONS/logo.icns" ]; then
echo -e "  ${BLUE}macOS${NC}    - logo.icns"
fi
echo ""
echo -e "${YELLOW}TIP:${NC} For best results, use a square PNG source image of at least 1024x1024 pixels."
echo ""
