JAVA = java
JAVAC = javac
OPTS =
SRCS = $(wildcard *.java)

run: Main.class
	$(JAVA) Main

Main.class: $(SRCS)
	$(JAVAC) Main.java

clean:
	rm -rf *.class
