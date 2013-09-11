::
:: Batch Processing of Training/Testing Samples
:: NOTE: May take quite some time to execute.
:: Ported from testing.sh tcsh script.
::
:: Copyright (C) 2003 - 2006 The MARF Research and Development Group
::
:: Serguei Mokhov
:: Shuxin Fan
::
:: $Header: /cvsroot/marf/apps/SpeakerIdentApp/testing.bat,v 1.9 2006/01/15 20:51:53 mokhov Exp $
::

@echo off

::
:: Set environment variables, if needed
::

set CLASSPATH=.;marf.jar

::
:: Set flags to use in the batch execution
::

set java=java -ea -Xmx512m
::set debug=-debug
set debug=
set graph=
::set graph=-graph
::set spectrogram=-spectrogram
set spectrogram=

if "%debug%" == "-debug" echo on


::
:: Options
::

if "%1" == "--reset" goto RESETSTATS
if "%1" == "--retrain" goto RETRAIN
goto TESTING


:RETRAIN
	echo Training...

	::
	:: Prepare temporary training .bat files
	::

	:: Preprocessing
	echo for %%%%p in (-norm -boost -low -high -band -highpassboost -raw -endp) do call tmp-feat-train.bat %%%%p > tmp-prep-train.bat

	:: Feature Extraction
	echo for %%%%f in (-fft -lpc -randfe -minmax -aggr) do call tmp-class-train.bat %%1 %%%%f > tmp-feat-train.bat

	:: Classification

	:: Here we specify which classification modules to use for
	:: training. Since Neural Net wasn't working the default
	:: distance training was performed; now we need to distinguish them
	:: here. NOTE: for distance classifiers it's not important
	:: which exactly it is, because the one of generic Distance is used.
	:: Exception for this rule is Mahalanobis Distance, which needs
	:: to learn its Covariance Matrix.
	echo for %%%%c in (-cheb -mah -randcl -nn) do call tmp-train.bat %%1 %%2 %%%%c > tmp-class-train.bat

	:: Training
	echo echo Config: %%1 %%2 %%3 %spectrogram% %graph% %debug% > tmp-train.bat
	::echo echo. | date >> tmp-train.bat
	
	:: XXX: We cannot cope gracefully right now with these combinations --- too many
	:: links in the fully-connected NNet, so run out of memory quite often; hence,
	:: skip it for now.

	echo if "%%3" == "-nn" goto FEATTEST >> tmp-train.bat
	echo goto TRAIN >> tmp-train.bat

	echo :FEATTEST >> tmp-train.bat
	echo if "%%2" == "-fft" goto SKIP >> tmp-train.bat
	echo if "%%2" == "-randfe" goto SKIP >> tmp-train.bat
	echo if "%%2" == "-aggr" goto SKIP >> tmp-train.bat

	echo :TRAIN >> tmp-train.bat
	echo %java% SpeakerIdentApp --train training-samples %%1 %%2 %%3 %spectrogram% %graph% %debug% >> tmp-train.bat
	echo goto END >> tmp-train.bat

	echo :SKIP >> tmp-train.bat
	echo echo skipping... >> tmp-train.bat

	echo :END >> tmp-train.bat

	::
	:: Run training starting from preprocessing
	::

	:: Always reset stats before retraining the whole thing
	%java% SpeakerIdentApp --reset

	:: Preprocessing
	call tmp-prep-train.bat


:TESTING

	echo Testing...

	::
	:: Prepare temporary testing .bat files
	::

	:: Preprocessing
	echo for %%%%p in (-norm -boost -low -high -band -highpassboost -raw -endp) do call tmp-feat-ident.bat %%%%p > tmp-prep-ident.bat

	:: Feature Extraction
	echo for %%%%f in (-fft -lpc -randfe -minmax -aggr) do call tmp-class-ident.bat %%1 %%%%f > tmp-feat-ident.bat

	:: Classification
	echo for %%%%c in (-eucl -cheb -mink -mah -diff -randcl -nn) do call tmp-ident.bat %%1 %%2 %%%%c > tmp-class-ident.bat

	:: Identification
	echo echo Config: %%1 %%2 %%3 %spectrogram% %graph% %debug% > tmp-ident.bat
	::echo echo. | date >> tmp-ident.bat

	echo if "%%3" == "-nn" goto FEATTEST >> tmp-ident.bat
	echo goto IDENT >> tmp-ident.bat

	echo :FEATTEST >> tmp-ident.bat
	echo if "%%2" == "-fft" goto SKIP >> tmp-ident.bat
	echo if "%%2" == "-randfe" goto SKIP >> tmp-ident.bat
	echo if "%%2" == "-aggr" goto SKIP >> tmp-ident.bat

	echo :IDENT >> tmp-ident.bat
	echo echo =-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= >> tmp-ident.bat
	echo echo Config: %%1 %%2 %%3 %spectrogram% %graph% %debug% >> tmp-ident.bat
	echo echo ============================================= >> tmp-ident.bat
	echo %java% SpeakerIdentApp --batch-ident testing-samples %%1 %%2 %%3 %spectrogram% %graph% %debug% >> tmp-ident.bat
	echo echo --------------------------------------------- >> tmp-ident.bat
	echo goto END >> tmp-ident.bat

	echo :SKIP >> tmp-ident.bat
	echo echo skipping... >> tmp-ident.bat

	echo :END >> tmp-ident.bat

	:: Perform the above for each testing sample
	call tmp-prep-ident.bat


:STATS

echo Stats:

%java% SpeakerIdentApp --stats > stats.txt
%java% SpeakerIdentApp --best-score > best-score.tex
echo. | date > stats-date.tex
echo. | time >> stats-date.tex

more stats.txt best-score.tex stats-date.tex

echo Testing Done
goto END


:RESETSTATS

echo Resetting Stats...
%java% SpeakerIdentApp --reset


:END

:: Remove the temporary generated files.
:: Don't complain if they are not there. 
if not "%debug%" == "-debug" del tmp-*.bat > NUL

set java=
set debug=
set graph=
set spectrogram=

:: EOF
