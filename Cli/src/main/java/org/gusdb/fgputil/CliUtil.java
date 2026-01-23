package org.gusdb.fgputil;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;

public class CliUtil {

  private final static String NL = System.getProperty("line.separator");

  public static void addOption(Options options, String argName, String desc,
    boolean isRequired) {
    addOption(options, argName, desc, isRequired, true);
  }

  public static void addOption(Options options, String argName, String desc,
    boolean isRequired, boolean hasArg) {

    Option option = new Option(argName, hasArg, desc);
    option.setRequired(isRequired);
    option.setArgName(argName);
    options.addOption(option);
  }

  public static CommandLine parseOptions(String cmdlineSyntax,
    String cmdDescrip, String usageNotes, Options options, String[] args) {

    CommandLineParser parser = new DefaultParser();
    CommandLine cmdLine = null;
    try {
      // parse the command line arguments
      cmdLine = parser.parse(options, args);
    }
    catch (ParseException exp) {
      // oops, something went wrong
      System.err.println();
      System.err.println("Parsing failed.  Reason: " + exp.getMessage());
      System.err.println();
      usage(cmdlineSyntax, cmdDescrip, usageNotes, options);
    }

    return cmdLine;
  }

  public static void usage(String cmdlineSyntax, String cmdDescrip,
    String usageNotes, Options options) {

    String header = NL + cmdDescrip + NL + NL + "Options:";

    printHelp(cmdlineSyntax, header, options, NL);
    System.err.println(usageNotes);
    System.exit(1);
  }

  public static void printHelp(String cmdlineSyntax, String header, Options options, String footer) {
    try {
      HelpFormatter formatter = HelpFormatter.builder().get();
      formatter.printHelp(cmdlineSyntax, header, options, footer, false);
    }
    catch (IOException e) {
      throw new RuntimeException("Unable to print help", e);
    }
  }
}
