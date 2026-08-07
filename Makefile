APP     := app
WRAPPER := $(APP)/gradlew
GRADLE  := sh $(WRAPPER)
SDK     := $(or $(ANDROID_HOME),$(ANDROID_SDK_ROOT),$(HOME)/Android/Sdk)
JAVA_HOME := $(shell for d in "$$JAVA_HOME" "$$HOME"/.jdks/*/; do j="$$d/bin/java"; [ -x "$$j" ] || continue; if $$j -version 2>&1 | grep -qE 'version "1[1-7]'; then echo "$$d"; break; fi; done)
VERSION := $(shell grep -m1 versionName $(APP)/app/build.gradle | sed 's/.*"\([^"]*\)".*/\1/')
.PHONY: all default debug release assemble install uninstall clean dist sdk properties apk components vortek gladio android_alsa
default: debug
all: assemble
sdk:
	@test -d "$(SDK)" || { echo "!! Android SDK not found at $(SDK)"; echo "!! set ANDROID_HOME or ANDROID_SDK_ROOT"; exit 1; }
	@echo ">> SDK: $(SDK)"

properties: sdk
	@test -f "$(APP)/local.properties" && echo ">> local.properties: present" || \
		{ printf 'sdk.dir=%s\n' "$(SDK)" > $(APP)/local.properties; echo ">> local.properties: written -> $(SDK)"; }

debug: properties
	@$(GRADLE) -p $(APP) assembleDebug

release: properties
	@$(GRADLE) -p $(APP) assembleRelease

assemble: debug
install: properties
	@$(GRADLE) -p $(APP) installDebug

uninstall:
	@$(GRADLE) -p $(APP) uninstallDebug

clean:
	@$(GRADLE) -p $(APP) clean

JOBS := $(shell nproc)
BUILD_DIR := build

vortek gladio android_alsa: CFLAGS += -O2 -Wl,-rpath=/data/data/com.winlator/files/rootfs/lib
vortek gladio android_alsa:
	@cmake -S $@ -B $@/$(BUILD_DIR)
	@cmake --build $@/$(BUILD_DIR) -j$(JOBS)

components: vortek gladio android_alsa

apk = $(firstword $(wildcard $(APP)/app/build/outputs/apk/debug/*.apk))
dist: debug
	@mkdir -p dist
	@cp "$(apk)" "dist/DiamondRuntime-$(VERSION).apk"
	@echo ">> packed -> dist/DiamondRuntime-$(VERSION).apk"
