JAVAC=javac
sources := $(wildcard *.java)
sources := $(filter-out KMode.java, $(sources))
sources := $(filter-out KMeans.java, $(sources))
sources := $(filter-out SharingEntity.java, $(sources))
sources := $(filter-out DKMeans.java, $(sources))
sources := $(filter-out alert.java, $(sources))
sources := $(filter-out Message.java, $(sources))
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f *.class
	rm -f .config

%.class : %.java
	$(JAVAC) $<
