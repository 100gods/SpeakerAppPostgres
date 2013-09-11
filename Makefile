# SpeakerIdentApp Build Makefile
# Use with GNU make.
#
# Serguei Mokhov
#
# COPYRIGHT (c) 2002 - 2006 The MARF Research Development Group
#
# $Header: /cvsroot/marf/apps/SpeakerIdentApp/Makefile,v 1.28 2006/02/18 16:11:39 mokhov Exp $
#

# Common vars
VERSION=0.3.0-devel-`date +%Y%m%d`
TOOLSDIR=../../marf/src/tools

# Dirs of MARF modules
MARFDIR = ../../marf/distro
MARFJAR = marf.jar

# Lists of all *.java and *.class files
JAVAFILES = *.java
CLASSES = *.class
EXE = SpeakerIdentApp

#
# Java Settings
#

JAVAC = javac
CLASSPATH = ".:$(MARFJAR)"
JFLAGS = -g -source 1.4 -verbose -deprecation -classpath $(CLASSPATH) -extdirs ".:$(MARFJAR)" -sourcepath .
JVM = java

JAVADOCFLAGS = -source 1.4 -verbose -use -version -author -classpath ".:$(MARFJAR)"

#
# Build
#

all: $(EXE).class
	@echo "SpeakerIdentApp build ok"

$(EXE).class: $(JAVAFILES)
	$(JAVAC) $(JFLAGS) $(EXE).java

#
# Run
#

run:
	@echo "'make run' is ambiguous. Use 'make train-run' or 'make ident-run' instead."

train-run: all
	time -p ./testing.sh --retrain > output.txt 2>&1 &
	echo "The Training and Testing Process has begun."

ident-run: all
	time -p ./testing.sh > output.txt 2>&1 &
	echo "The Testing Process has begun."

#
# Javadoc
#

javadoc-html: $(JAVAFILES)
	@echo "Compiling javadoc..."
	mkdir -p html
	javadoc $(JAVADOCFLAGS) -private -d html $(JAVAFILES)

#
# Distro
#

DISTROSRCFILES = \
	$(JAVAFILES) \
	testing.sh testing.bat retrain.lnk \
	Makefile $(EXE).jpx .project .classpath manifest.mf \
	speakers.txt \
	COPYRIGHT README coding.html ChangeLog

DISTROBINFILES = \
	testing.sh testing.bat retrain.lnk \
	speakers.txt \
	README COPYRIGHT ChangeLog \
	$(EXE).jar marf.jar

SAMPLEDIRS = testing-samples training-samples

DISTROFILES = $(EXE)-src-$(VERSION).tar.gz \
              $(EXE)-samples-$(VERSION).tar.gz \
              $(EXE)-bundle-$(VERSION).tar.gz \
              $(EXE)-bin-$(VERSION).tar.gz \
              $(EXE)-training-sets-$(VERSION).tar.gz

distro: $(DISTROFILES)
	@rm -rf distro && mkdir distro
	@mv *.tar.* distro/
	@echo "Done bundling the application:"
	@rm -rf tarball.exclude html
	@ls -lhG distro/{*.gz,*.bz2}
	@echo "Creating MD5 checksums..."
	$(TOOLSDIR)/md5sumall.sh distro
	@echo "$(EXE) distro tarballs are ready."

$(EXE)-src-$(VERSION).tar.gz: $(DISTROSRCFILES) $(SAMPLEDIRS) javadoc-html tarball.exclude
	@rm -f $(EXE)-src-*.tar.gz
	@echo "Sources tarball..."
	@tar --exclude-from=tarball.exclude \
		-cvhf $(EXE)-src-$(VERSION).tar \
		$(DISTROSRCFILES) $(SAMPLEDIRS) html
	@gzip --best $(EXE)-src-$(VERSION).tar
	@zcat "$(EXE)-src-$(VERSION).tar.gz" | bzip2 -9 > "$(EXE)-src-$(VERSION).tar.bz2"

$(EXE)-samples-$(VERSION).tar.gz: $(SAMPLEDIRS) COPYRIGHT
	@rm -f $(EXE)-samples-*.tar.gz
	@echo "Samples-only tarball..."
	@tar --exclude CVS \
		-cvhf $(EXE)-samples-$(VERSION).tar \
		$(SAMPLEDIRS) COPYRIGHT
	@gzip --best $(EXE)-samples-$(VERSION).tar
	@zcat "$(EXE)-samples-$(VERSION).tar.gz" | bzip2 -9 > "$(EXE)-samples-$(VERSION).tar.bz2"

$(EXE)-bin-$(VERSION).tar.gz: all $(DISTROBINFILES) $(SAMPLEDIRS) tarball.exclude
	@rm -f $(EXE)-bin-*.tar.gz
	@echo "Binary no-samples tarball..."
	@tar --exclude-from=tarball.exclude \
		-cvhf $(EXE)-bin-$(VERSION).tar \
		$(DISTROBINFILES) $(SAMPLEDIRS)
	@gzip --best $(EXE)-bin-$(VERSION).tar
	@zcat "$(EXE)-bin-$(VERSION).tar.gz" | bzip2 -9 > "$(EXE)-bin-$(VERSION).tar.bz2"

$(EXE)-bundle-$(VERSION).tar.gz: all $(DISTROSRCFILES) $(SAMPLEDIRS) javadoc-html $(EXE).jar marf.jar
	@rm -f $(EXE)-bundle-*.tar.gz
	@echo "Bundle tarball..."
	@tar --exclude CVS \
		-cvhf $(EXE)-bundle-$(VERSION).tar \
		$(DISTROSRCFILES) marf.jar $(EXE).jar $(SAMPLEDIRS) html
	@gzip --best $(EXE)-bundle-$(VERSION).tar
	@zcat "$(EXE)-bundle-$(VERSION).tar.gz" | bzip2 -9 > "$(EXE)-bundle-$(VERSION).tar.bz2"

$(EXE)-training-sets-$(VERSION).tar.gz: COPYRIGHT
	@rm -f $(EXE)-training-sets-*.tar.gz
	@echo "Training Sets..."
	@tar -cvhf $(EXE)-training-sets-$(VERSION).tar *.xml *.gzbin COPYRIGHT
	@gzip --best $(EXE)-training-sets-$(VERSION).tar
	@zcat "$(EXE)-training-sets-$(VERSION).tar.gz" | bzip2 -9 > "$(EXE)-training-sets-$(VERSION).tar.bz2"

$(EXE).jar: all
	jar cvmf manifest.mf $(EXE).jar *.class

tarball.exclude:
	@(echo "*.wav"; echo "CVS") > tarball.exclude

COPYRIGHT: ../../marf/COPYRIGHT
	ln -sf ../../marf/COPYRIGHT COPYRIGHT

coding.html: ../../www/coding.html
	ln -sf ../../www/coding.html coding.html

usage: all
	( \
		echo "\begin{verbatim}" ; \
		$(JVM) -classpath $(CLASSPATH) $(EXE) --help ; \
		echo "\end{verbatim}" ; \
	) > speaker-ident-app-usage.tex

change-log:
	$(TOOLSDIR)/cvs2cl.pl


#
# Clean Up
#

clean:
	rm -f $(CLASSES) *~

maintainer-clean: clean
	rm -f *.stats *log training.set output.txt *.diff *.bin *.xml *.gzbin \
		stats.txt stats-date.tex best-score.tex coding.html COPYRIGHT $(EXE).jar
	rm -f training-samples/*.txt training-samples/*.ppm testing-samples/*.txt testing-samples/*.ppm

# EOF
