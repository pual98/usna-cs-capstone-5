JAVAC=javac
sources := $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f *.class
	rm -f .config

%.class : %.java
	$(JAVAC) $<
