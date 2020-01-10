JAVA = java
JAVAC = javac
OPTS =

make:
	make Main.java
	make run

Main.class:
	$(JAVAC) Main.java

run: Main.class
	$(JAVA) Main

clean:
	rm -rf *.class
