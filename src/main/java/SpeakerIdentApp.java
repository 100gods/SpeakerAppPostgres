import java.io.File;
import java.util.Date;

import marf.MARF;
import marf.Storage.ModuleParams;
import marf.Storage.TrainingSet;
import marf.util.Debug;
import marf.util.MARFException;
import marf.util.OptionProcessor;


/**
 * <p>Indentfies a speaker independently of text, based on the MARF framework,
 * <a href="http://marf.sf.net">http://marf.sf.net</a>.
 * </p>
 *
 * <p>$Id: SpeakerIdentApp.java,v 1.56 2006/01/23 00:03:34 mokhov Exp $</p>
 *
 * @author Serguei Mokhov
 * @author Stephen Sinclair
 * @author Ian Clement
 * @author Dimitrios Nicolacopoulos
 * @author The MARF Research and Development Group
 *
 * @version 0.3.0, $Revision: 1.56 $
 * @since 0.0.1
 */
public class SpeakerIdentApp
{
	/*
	 * ----------------
	 * Apps. Versioning
	 * ----------------
	 */

	/**
	 * Current major version of the application.
	 */
	public static final int MAJOR_VERSION = 0;

	/**
	 * Current minor version of the application.
	 */
	public static final int MINOR_VERSION = 3;

	/**
	 * Current revision of the application.
	 */
	public static final int REVISION      = 0;

	
	/*
	 * ----------------------------------
	 * Major and Misc Options Enumeration
	 * ----------------------------------
	 */

	/**
	 * Numeric equivalent of the option <code>--train</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_TRAIN = 0;

	/**
	 * Numeric equivalent of the option <code>--ident</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_IDENT = 1;

	/**
	 * Numeric equivalent of the option <code>--stats</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_STATS = 2;

	/**
	 * Numeric equivalent of the option <code>--reset</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_RESET = 3;

	/**
	 * Numeric equivalent of the option <code>--version</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_VERSION = 4;

	/**
	 * Numeric equivalent of the option <code>--help</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_HELP_LONG = 5;

	/**
	 * Numeric equivalent of the option <code>-h</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_HELP_SHORT = 6;

	/**
	 * Numeric equivalent of the option <code>-debug</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_DEBUG = 7;

	/**
	 * Numeric equivalent of the option <code>-graph</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_GRAPH = 8;

	/**
	 * Numeric equivalent of the option <code>-spectrogram</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_SPECTROGRAM = 9;

	/**
	 * Numeric equivalent of the option <code>&lt;speaker ID&gt;</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_EXPECTED_SPEAKER_ID = 10;

	/**
	 * Numeric equivalent of the option <code>--batch-ident</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_BATCH_IDENT = 11;

	/**
	 * Numeric equivalent of the option <code>--single-train</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_SINGLE_TRAIN = 12;

	/**
	 * Numeric equivalent of the option <code>&lt;sample-file-or-directory-name&gt;</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_DIR_OR_FILE = 13;

	/**
	 * Numeric equivalent of the option <code>--best-score</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_BEST_SCORE = 14;
	
	/*
	 * ---------------------
	 * State Data Structures
	 * ---------------------
	 */

	/**
	 * Instance of the database of speakers.
	 * @since 0.3.0.5
	 */
	protected static SpeakersIdentDb soDB = new SpeakersIdentDb("speakers.txt");

	/**
	 * Intance of the option processing utility.
	 * @since 0.3.0.5
	 */
	protected static OptionProcessor soGetOpt = new OptionProcessor();

	/*
	 * -----------------
	 * Static State Init
	 * -----------------
	 */

	static
	{
		// Main options
		soGetOpt.addValidOption(OPT_TRAIN, "--train");
		soGetOpt.addValidOption(OPT_SINGLE_TRAIN, "--single-train");
		soGetOpt.addValidOption(OPT_IDENT, "--ident");
		soGetOpt.addValidOption(OPT_BATCH_IDENT, "--batch-ident");
		soGetOpt.addValidOption(OPT_STATS, "--stats");
		soGetOpt.addValidOption(OPT_BEST_SCORE, "--best-score");
		soGetOpt.addValidOption(OPT_RESET, "--reset");
		soGetOpt.addValidOption(OPT_VERSION, "--version");
		soGetOpt.addValidOption(OPT_HELP_LONG, "--help");
		soGetOpt.addValidOption(OPT_HELP_SHORT, "-h");

		// Preprocessing
		soGetOpt.addValidOption(MARF.DUMMY, "-norm");
		soGetOpt.addValidOption(MARF.HIGH_FREQUENCY_BOOST_FFT_FILTER, "-boost");
		soGetOpt.addValidOption(MARF.HIGH_PASS_FFT_FILTER, "-high");
		soGetOpt.addValidOption(MARF.LOW_PASS_FFT_FILTER, "-low");
		soGetOpt.addValidOption(MARF.BANDPASS_FFT_FILTER, "-band");
		soGetOpt.addValidOption(MARF.HIGH_PASS_BOOST_FILTER, "-highpassboost");
		soGetOpt.addValidOption(MARF.RAW, "-raw");
		soGetOpt.addValidOption(MARF.ENDPOINT, "-endp");
		
		// Feature extraction
		soGetOpt.addValidOption(MARF.FFT, "-fft");
		soGetOpt.addValidOption(MARF.LPC, "-lpc");
		soGetOpt.addValidOption(MARF.RANDOM_FEATURE_EXTRACTION, "-randfe");
		soGetOpt.addValidOption(MARF.MIN_MAX_AMPLITUDES, "-minmax");
		soGetOpt.addValidOption(MARF.FEATURE_EXTRACTION_AGGREGATOR, "-aggr");

		// Classification
		soGetOpt.addValidOption(MARF.NEURAL_NETWORK, "-nn");
		soGetOpt.addValidOption(MARF.EUCLIDEAN_DISTANCE, "-eucl");
		soGetOpt.addValidOption(MARF.CHEBYSHEV_DISTANCE, "-cheb");
		soGetOpt.addValidOption(MARF.MINKOWSKI_DISTANCE, "-mink");
		soGetOpt.addValidOption(MARF.MAHALANOBIS_DISTANCE, "-mah");
		soGetOpt.addValidOption(MARF.RANDOM_CLASSIFICATION, "-randcl");
		soGetOpt.addValidOption(MARF.DIFF_DISTANCE, "-diff");

		// Misc
		soGetOpt.addValidOption(OPT_SPECTROGRAM, "-spectrogram");
		soGetOpt.addValidOption(OPT_DEBUG, "-debug");
		soGetOpt.addValidOption(OPT_GRAPH, "-graph");
	}
	
	
	/**
	 * Main body.
	 * @param argv command-line arguments
	 */
	public static final void main(String[] argv)
	{
		try
		{
			// Since some new API is always introduced...
			validateVersions();

			/*
			 * Load the speakers database
			 */
			// TODO connect to pg
			soDB.connect();
			soDB.query();

			setDefaultConfig();
			
			// Parse extra arguments
			int iValidOptions = soGetOpt.parse(argv);

			if(iValidOptions == 0)
			{
				throw new Exception("No valid options found: " + soGetOpt);
			}

			setCustomConfig();

			/*
			 * If supplied in the command line, the system when classifying,
			 * will output this ID next to the guessed one.
			 */
			int iExpectedID = -1;
//TODO pass params as array of strings mock command line params
			switch(soGetOpt.getInvalidOptions().size())
			{
				// Unknown
				case 0:
				{
					iExpectedID = -1;
					break;
				}
				
				/*
				 * Extract and make active and valid option out of
				 * a filename and and an expected speaker ID. An
				 * assumption is that the expected speaker ID is
				 * always second on the command line somewhere after
				 * a file or directory name. Presence of the expected
				 * speaker ID always implies presence of the file
				 * or directory argument. 
				 */
				case 2:
				{
					try
					{
						iExpectedID = Integer.parseInt(soGetOpt.getInvalidOptions().elementAt(2).toString());
						soGetOpt.addActiveOption(OPT_EXPECTED_SPEAKER_ID, soGetOpt.getInvalidOptions().elementAt(2).toString());
					}

					/*
					 * May happend when trying to get expected ID,
					 * but the argument doesn't parse as an int.
					 */
					catch(NumberFormatException e)
					{
						iExpectedID = -1;

						System.err.println
						(
							"SpeakerIdentApp: WARNING: could not parse expected speaker ID ("
							+ e.getMessage() + "), ignoring..."
						);
					}

					// No break required as the file or directory
					// must always be present. Also, the clearance
					// of the invalid options need to be postponed.
				}
				
				/*
				 * In the case of a single invalid option
				 * always assume it is either a filename
				 * or a directory name for the --ident
				 * or --train options.
				 */
				case 1:
				{
					soGetOpt.addActiveOption(OPT_DIR_OR_FILE, soGetOpt.getInvalidOptions().firstElement().toString());
					soGetOpt.getInvalidOptions().clear();
					break;
				}

				default:
				{
					throw new Exception("Unrecognized options found: " + soGetOpt.getInvalidOptions());
				}
			}

			// Set misc configuration
			MARF.setDumpSpectrogram(soGetOpt.isActiveOption(OPT_SPECTROGRAM));
			MARF.setDumpWaveGraph(soGetOpt.isActiveOption(OPT_GRAPH));
			Debug.enableDebug(soGetOpt.isActiveOption(OPT_DEBUG));

			Debug.debug("Option set: " + soGetOpt);

			int iMainOption = soGetOpt.getOption(argv[0]);

			switch(iMainOption)
			{
				/*
				 * --------------
				 * Identification
				 * --------------
				 */
				
				// Single case
				case OPT_IDENT:
				{
					ident(getConfigString(argv), soGetOpt.getOption(OPT_DIR_OR_FILE), iExpectedID);
					break;
				}

				// A directory with files for identification
				case OPT_BATCH_IDENT:
				{
					// Store config and error/successes for that config
					String strConfig = getConfigString(argv);

					// Dir contents
					File[] aoWaveFiles = new File(soGetOpt.getOption(OPT_DIR_OR_FILE)).listFiles();

					for(int i = 0; i < aoWaveFiles.length; i++)
					{
						String strFileName = aoWaveFiles[i].getPath();

						if(aoWaveFiles[i].isFile() && strFileName.toLowerCase().endsWith(".wav"))
						{
							ident(strConfig, strFileName, iExpectedID);
						}
					}

					break;
				}

				/*
				 * --------
				 * Training
				 * --------
				 */

				// Add a single smaple to the training set
				case OPT_SINGLE_TRAIN:
				{
					train(soGetOpt.getOption(OPT_DIR_OR_FILE));
					System.out.println("Done training with file \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\".");
					break;
				}
				
				// Train on a directory of files
				case OPT_TRAIN:
				{
					try
					{
						// Dir contents
						File[] aoFiles = new File(soGetOpt.getOption(OPT_DIR_OR_FILE)).listFiles();
	
						Debug.debug("Files array: " + aoFiles);
						
						if(Debug.isDebugOn())
						{
							System.getProperties().list(System.err);
						}
						
						String strFileName = "";

						// XXX: this loop has to be in MARF
						for(int i = 0; i < aoFiles.length; i++)
						{
							strFileName = aoFiles[i].getPath();
	
							if(aoFiles[i].isFile() && strFileName.toLowerCase().endsWith(".wav"))
							{
								train(strFileName);
							}
						}
					}
					catch(NullPointerException e)
					{
						System.err.println("Folder \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\" not found.");
						e.printStackTrace(System.err);
						System.exit(-1);
					}

					System.out.println("Done training on folder \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\".");

					break;
				}

				/*
				 * -----
				 * Stats
				 * -----
				 */
				case OPT_STATS:
				{
					soDB.restore();
					soDB.printStats();
					break;
				}

				/*
				 * Best Result with Stats
				 */
				case OPT_BEST_SCORE:
				{
					soDB.restore();
					soDB.printStats(true);
					break;
				}

				/*
				 * Reset Stats
				 */
				case OPT_RESET:
				{
					soDB.resetStats();
					System.out.println("SpeakerIdentApp: Statistics has been reset.");
					break;
				}

				/*
				 * Versioning
				 */
				case OPT_VERSION:
				{
					System.out.println("Text-Independent Speaker Identification Application, v." + getVersion());
					System.out.println("Using MARF, v." + MARF.getVersion());
					validateVersions();
					break;
				}

				/*
				 * Help
				 */
				case OPT_HELP_SHORT:
				case OPT_HELP_LONG:
				{
					usage();
					break;
				}

				/*
				 * Invalid major option
				 */
				default:
				{
					throw new Exception("Unrecognized option: " + argv[0]);
				}
			} // major option switch
		} // try

		/*
		 * No arguments have been specified
		 */
		catch(ArrayIndexOutOfBoundsException e)
		{
			usage();
		}

		/*
		 * MARF-specific errors
		 */
		catch(MARFException e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}

		/*
		 * Invalid option and/or option argument
		 */
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			usage();
		}

		/*
		 * Regardless whatever happens, close the db connection.
		 */
		finally
		{
			try
			{
				Debug.debug("Closing DB connection...");
				soDB.close();
			}
			catch(Exception e)
			{
				Debug.debug("Closing DB connection failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(-1);
			}
		}
	}

	/**
	 * Indetifies a speaker using MARF given configuration,
	 * wave filename, and possibly expected speaker ID.
	 *
	 * @param pstrConfig configuration string for stats
	 * @param pstrFilename name of the wave file with voice sample to identify
	 * @param piExpectedID expected speaker ID; if -1 then no stats is kept
	 *
	 * @throws MARFException in case of any error while processing is in MARF
	 * @since 0.3.0.5
	 */
	public static final void ident(String pstrConfig, String pstrFilename, int piExpectedID)
	throws MARFException
	{
		/*
		 * If no expected speaker present on the command line,
		 * attempt to fetch it from the database by filename.
		 */
		if(piExpectedID < 0)
		{
			piExpectedID = soDB.getIDByFilename(pstrFilename, false);
		}

		MARF.setSampleFile(pstrFilename);
		MARF.recognize();

		// First guess
		int iIdentifiedID = MARF.queryResultID();

		// Second best
		int iSecondClosestID = MARF.getResultSet().getSecondClosestID();

		System.out.println("                 File: " + pstrFilename);
		System.out.println("               Config: " + pstrConfig);
		System.out.println("         Speaker's ID: " + iIdentifiedID);
		System.out.println("   Speaker identified: " + soDB.getName(iIdentifiedID));

		/*
		 * Only collect stats if we have expected speaker
		 */
		if(piExpectedID > 0)
		{
			System.out.println("Expected Speaker's ID: " + piExpectedID);
			System.out.println("     Expected Speaker: " + soDB.getName(piExpectedID));

			soDB.restore();
			{
				// 1st match
				soDB.addStats(pstrConfig, (iIdentifiedID == piExpectedID));

				// 2nd best: must be true if either 1st true or second true (or both :))
				boolean bSecondBest =
					iIdentifiedID == piExpectedID
					||
					iSecondClosestID == piExpectedID;

				soDB.addStats(pstrConfig, bSecondBest, true);
			}
			soDB.dump();
		}

		System.out.println("       Second Best ID: " + iSecondClosestID);
		System.out.println("     Second Best Name: " + soDB.getName(iSecondClosestID));
		System.out.println("            Date/time: " + new Date());
		System.out.println("----------------------------8<------------------------------");
	}

	/**
	 * Updates training set with a new sample from a given file.
	 *
	 * @param pstrFilename name of the wave file with voice sample train the system on
	 *
	 * @throws MARFException in case of any error while processing is in MARF
	 * @since 0.3.0.5
	 */
	public static final void train(String pstrFilename)
	throws MARFException
	{
		MARF.setSampleFile(pstrFilename);
		
		int iID = soDB.getIDByFilename(pstrFilename, true);

		if(iID == -1)
		{
			System.out.println("No speaker found for \"" + pstrFilename + "\" for training.");
		}
		else
		{
			MARF.setCurrentSubject(iID);
			MARF.train();
		}
	}

	/**
	 * Displays application's usage information and exits.
	 */
	private static final void usage()
	{
		System.out.println
		(
			"Usage:\n" +
			"  java SpeakerIdentApp --train <samples-dir> [options]        -- train mode\n" +
			"                       --single-train <sample> [options]      -- add a single sample to the training set\n" +
			"                       --ident <sample> [options]             -- identification mode\n" +
			"                       --batch-ident <samples-dir> [options]  -- batch identification mode\n" +
			"                       --stats                                -- display stats\n" +
			"                       --best-score                           -- display best classification result\n" +
			"                       --reset                                -- reset stats\n" +
			"                       --version                              -- display version info\n" +
			"                       --help | -h                            -- display this help and exit\n\n" +

			"Options (one or more of the following):\n\n" +

			"Preprocessing:\n\n" +
			"  -raw          - no preprocessing\n" +
			"  -norm         - use just normalization, no filtering\n" +
			"  -low          - use low-pass filter\n" +
			"  -high         - use high-pass filter\n" +
			"  -boost        - use high-frequency-boost preprocessor\n" +
			"  -band         - use band-pass filter\n" +
			"  -endp         - use endpointing\n" +
			"\n" +

			"Feature Extraction:\n\n" +
			"  -lpc          - use LPC\n" +
			"  -fft          - use FFT\n" +
			"  -minmax       - use Min/Max Amplitudes\n" +
			"  -randfe       - use random feature extraction\n" +
			"  -aggr         - use aggregated FFT+LPC feature extraction\n" +
			"\n" +

			"Classification:\n\n" +
			"  -nn           - use Neural Network\n" +
			"  -cheb         - use Chebyshev Distance\n" +
			"  -eucl         - use Euclidean Distance\n" +
			"  -mink         - use Minkowski Distance\n" +
			"  -diff         - use Diff-Distance\n" +
			"  -randcl       - use random classification\n" +
			"\n" +

			"Misc:\n\n" +
			"  -debug        - include verbose debug output\n" +
			"  -spectrogram  - dump spectrogram image after feature extraction\n" +
			"  -graph        - dump wave graph before preprocessing and after feature extraction\n" +
			"  <integer>     - expected speaker ID\n" +
			"\n"
		);

		System.exit(0);
	}

	/**
	 * Retrieves String representation of the application's version.
	 * @return version String
	 */
	public static final String getVersion()
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION;
	}

	/**
	 * Retrieves integer representation of the application's version.
	 * @return integer version
	 */
	public static final int getIntVersion()
	{
		return MAJOR_VERSION * 100 + MINOR_VERSION * 10 + REVISION;
	}

	/**
	 * Makes sure the applications isn't run against older MARF version.
	 * Exits with 1 if the MARF version is too old.
	 */
	public static final void validateVersions()
	{
		if(MARF.getDoubleVersion() < (0 * 100 + 3 * 10 + 0 + .5))
		{
			System.err.println
			(
				"Your MARF version (" + MARF.getVersion() +
				") is too old. This application requires 0.3.0.5 or above."
			);

			System.exit(1);
		}
	}

	/**
	 * Composes the current configuration of in a string form.
	 *
	 * @param pstrArgv set of configuration options passed through the command line;
	 * can be null or empty. If latter is the case, MARF itself is queried for its
	 * numerical set up inside.
	 *
	 * @return the current configuration setup
	 */
	public static final String getConfigString(String[] pstrArgv)
	{
		// Store config and error/successes for that config
		String strConfig = "";

		if(pstrArgv != null && pstrArgv.length > 2)
		{
			// Get config from the command line
			for(int i = 2; i < pstrArgv.length; i++)
			{
				strConfig += pstrArgv[i] + " ";
			}
		}
		else
		{
			// Query MARF for it's current config
			strConfig = MARF.getConfig();
		}
		
		return strConfig;
	}
	
	/**
	 * Sets default MARF configuration parameters as normalization
	 * for preprocessing, FFT for feature extraction, Eucludean
	 * distance for training and classification with no spectrogram
	 * dumps and no debug information, assuming WAVE file format.
	 *
	 * @throws MARFException
	 * @since 0.3.0.5
	 */
	public static final void setDefaultConfig()
	throws MARFException
	{
		/*
		 * Default MARF setup
		 */
		MARF.setPreprocessingMethod(MARF.DUMMY);
		MARF.setFeatureExtractionMethod(MARF.FFT);
		MARF.setClassificationMethod(MARF.EUCLIDEAN_DISTANCE);
		MARF.setDumpSpectrogram(false);
		MARF.setSampleFormat(MARF.WAV);

		Debug.enableDebug(false);
	}
	
	/**
	 * Customizes MARF's configuration based on the options.
	 * @throws MARFException if some options are out of range
	 * @since 0.3.0.5 
	 */
	public static final void setCustomConfig()
	throws MARFException
	{
		ModuleParams oParams = new ModuleParams();
		
		for
		(
			int iPreprocessingMethod = MARF.MIN_PREPROCESSING_METHOD;
			iPreprocessingMethod <= MARF.MAX_PREPROCESSING_METHOD;
			iPreprocessingMethod++
		)
		{
			if(soGetOpt.isActiveOption(iPreprocessingMethod))
			{
				MARF.setPreprocessingMethod(iPreprocessingMethod);
				
				switch(iPreprocessingMethod)
				{
					case MARF.DUMMY:
					case MARF.HIGH_FREQUENCY_BOOST_FFT_FILTER:
					case MARF.HIGH_PASS_FFT_FILTER:
					case MARF.LOW_PASS_FFT_FILTER:
					case MARF.BANDPASS_FFT_FILTER:
					case MARF.HIGH_PASS_BOOST_FILTER:
					case MARF.RAW:
					case MARF.ENDPOINT:
						// For now do nothing; customize when these methods
						// become parametrizable.
						break;
					
					default:
						assert false;
				} // switch

				break;
			}
		}

		for
		(
			int iFeatureExtractionMethod = MARF.MIN_FEATUREEXTRACTION_METHOD;
			iFeatureExtractionMethod <= MARF.MAX_FEATUREEXTRACTION_METHOD;
			iFeatureExtractionMethod++
		)
		{
			if(soGetOpt.isActiveOption(iFeatureExtractionMethod))
			{
				MARF.setFeatureExtractionMethod(iFeatureExtractionMethod);
				
				switch(iFeatureExtractionMethod)
				{
					case MARF.FFT:
					case MARF.LPC:
					case MARF.RANDOM_FEATURE_EXTRACTION:
					case MARF.MIN_MAX_AMPLITUDES:
						// For now do nothing; customize when these methods
						// become parametrizable.
						break;
					
					case MARF.FEATURE_EXTRACTION_AGGREGATOR:
					{
						// For now aggregate FFT followed by LPC until
						// it becomes customizable
						oParams.addFeatureExtractionParam(new Integer(MARF.FFT));
						oParams.addFeatureExtractionParam(null);
						oParams.addFeatureExtractionParam(new Integer(MARF.LPC));
						oParams.addFeatureExtractionParam(null);
						break;
					}

					default:
						assert false;
				} // switch

				break;
			}
		}

		for
		(
			int iClassificationMethod = MARF.MIN_CLASSIFICATION_METHOD;
			iClassificationMethod <= MARF.MAX_CLASSIFICATION_METHOD;
			iClassificationMethod++
		)
		{
			if(soGetOpt.isActiveOption(iClassificationMethod))
			{
				MARF.setClassificationMethod(iClassificationMethod);
				
				switch(iClassificationMethod)
				{
					case MARF.NEURAL_NETWORK:
					{
						// Dump/Restore Format of the TrainingSet
						oParams.addClassificationParam(new Integer(TrainingSet.DUMP_GZIP_BINARY));
	
						// Training Constant
						oParams.addClassificationParam(new Double(0.5));
	
						// Epoch number
						oParams.addClassificationParam(new Integer(20));
	
						// Min. error
						oParams.addClassificationParam(new Double(0.1));
						
						break;
					}

					case MARF.MINKOWSKI_DISTANCE:
					{
						// Dump/Restore Format
						oParams.addClassificationParam(new Integer(TrainingSet.DUMP_GZIP_BINARY));
	
						// Minkowski Factor
						oParams.addClassificationParam(new Double(6.0));

						break;
					}

					case MARF.EUCLIDEAN_DISTANCE:
					case MARF.CHEBYSHEV_DISTANCE:
					case MARF.MAHALANOBIS_DISTANCE:
					case MARF.RANDOM_CLASSIFICATION:
					case MARF.DIFF_DISTANCE:
						// For now do nothing; customize when these methods
						// become parametrizable.
						break;
					
					default:
						assert false;
				} // switch
				
				// Method is found, break out of the look up loop
				break;
			}
		}

		// Assign meaningful params only
		if(oParams.size() > 0)
		{
			MARF.setModuleParams(oParams);
		}
	}
}

// EOF
