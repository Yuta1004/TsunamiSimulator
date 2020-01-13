JAVA := java
JAVAC := javac
JAR := jar
JLINK := $(JAVA_HOME)/bin/jlink

OPTS := -p $(JAVAFX_PATH)/lib --add-modules javafx.controls
JLINK_OPTS := --compress=2 --add-modules javafx.controls --module-path $(JAVAFX_PATH)/jmods

SRCS := $(wildcard *.java)
MANIFEST := MANIFEST.MF

run: Main.class
	$(JAVA) $(OPTS) Main

Main.class: $(SRCS)
	$(JAVAC) $(OPTS) Main.java

dist-darwin:
	$(call gen-dist,darwin,java)

dist-win:
	$(call gen-dist,win,java.exe)

dist-linux:
	$(call gen-dist,linux,java)

clean:
	rm -rf *.class */*.class classes dist *.args

define gen-dist
	# ディレクトリ整理
	mkdir -p classes dist

	# ビルド
	$(JAVAC) $(OPTS) -d classes Main.java
	$(JAR) cvfm dist/TsunamiSimulator.jar $(MANIFEST) -C classes .

	# JRE生成
	$(JLINK) $(JLINK_OPTS):$(JMODS_PATH) --output dist/runtime-$1

	# Makefile生成
	echo "run:\n\truntime-$1/bin/$2 -jar TsunamiSimulator.jar" > dist/Makefile

	# .class削除
	rm -rf classes
endef

.PHONY: dist-darwin, dist-win, dist-linux
