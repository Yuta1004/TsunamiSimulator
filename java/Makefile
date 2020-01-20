# 変数
JAVA := java
JAVAC := javac
JAR := jar
JLINK := $(JAVA_HOME)/bin/jlink

SRCS := $(wildcard *.java */*.java)
JAVAFX_MODULES := javafx.controls,javafx.base,javafx.fxml,javafx.graphics,javafx.media,javafx.swing,javafx.web

OPTS := -p $(JAVAFX_PATH)/lib --add-modules $(JAVAFX_MODULES)
JLINK_OPTS := --compress=2 --add-modules $(JAVAFX_MODULES) --module-path $(JAVAFX_PATH)/jmods

# コマンド
run: Main.class
	$(JAVA) $(OPTS) Main

Main.class: $(SRCS)
	$(JAVAC) $(OPTS) Main.java

dist-darwin: clean
	$(call gen-dist,darwin,java)
	cp -r launcher/RunTsunamiSimulatorDarwin.app dist

dist-win: clean
	$(call gen-dist,win,java.exe)
	cp launcher/RunTsunamiSimulatorWindows.ps1 dist

dist-linux: clean
	$(call gen-dist,linux,java)
	cp launcher/RunTsunamiSimulatorLinux.sh dist

clean:
	rm -rf *.class */*.class classes dist *.args */*.args

# マクロ
define gen-dist
	# ディレクトリ整理
	mkdir -p classes dist

	# ビルド
	$(JAVAC) $(OPTS) -d classes Main.java
	$(JAR) cvfm dist/TsunamiSimulator-$1.jar MANIFEST.MF -C classes . fxml/

	# JRE生成
	$(JLINK) $(JLINK_OPTS):$(JMODS_PATH) --output dist/runtime-$1

	# Makefile生成
	echo "$1:\n\truntime-$1/bin/$2 -jar TsunamiSimulator-$1.jar\n\n" >> dist/Makefile

	# .class削除
	rm -rf classes
endef

.PHONY: dist-darwin, dist-win, dist-linux
