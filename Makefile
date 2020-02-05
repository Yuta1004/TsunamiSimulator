# 変数
JAVA := java
JAVAC := javac
JAR := jar
JLINK := $(JAVA_HOME)/bin/jlink

SRCS := $(wildcard *.java */*.java)
JAVAFX_MODULES := javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web

OPTS := -p $(JAVAFX_PATH)/lib --add-modules $(JAVAFX_MODULES)
JAVA_OPTS := $(OPTS) -classpath bin
JAVAC_OPTS := $(OPTS) -sourcepath src -d bin
JLINK_OPTS := --compress=2 --add-modules $(JAVAFX_MODULES) --module-path $(JAVAFX_PATH)/jmods

# コマンド
run: Main.class
	cp -r src/fxml bin
	cp -r src/data bin
	$(JAVA) $(JAVA_OPTS) Main

Main.class: $(SRCS)
	$(JAVAC) $(JAVAC_OPTS) src/Main.java

dist-darwin: clean
	$(call gen-dist,darwin,java)
	cp -r src/launcher/RunTsunamiSimulatorDarwin.app dist

dist-win: clean
	$(call gen-dist,win,java.exe)
	cp src/launcher/RunTsunamiSimulatorWindows.ps1 dist

dist-linux: clean
	$(call gen-dist,linux,java)
	cp src/launcher/RunTsunamiSimulatorLinux.sh dist

clean:
	rm -rf bin dist **/*.args

clean-hard:
	make clean
	rm -rf .*.*.un* .*.un* **/.*.*.un* **/.*.un* **/**/.*.*.un* **/**/.*.un*

# マクロ
define gen-dist
	# ディレクトリ整理
	mkdir -p bin dist

	# ビルド
	$(JAVAC) $(JAVAC_OPTS) src/Main.java
	cp -r src/fxml .
	cp -r src/data .
	$(JAR) cvfm dist/TsunamiSimulator-$1.jar MANIFEST.MF -C bin . fxml data
	rm -rf fxml data

	# JRE生成
	$(JLINK) $(JLINK_OPTS):$(JMODS_PATH) --output dist/runtime-$1

	# Makefile生成
	echo "$1:\n\tchmod +x runtime-$1/bin/*\n\truntime-$1/bin/$2 -jar TsunamiSimulator-$1.jar\n\n" > dist/Makefile

	# README生成
	echo "# TsunamiSimulator ($1)\n\n## HowToUse\nrun \`make\` or launcher(.app, .sh, .ps1)" > dist/README.md

	# .class削除
	rm -rf bin
endef

.PHONY: dist-darwin, dist-win, dist-linux
