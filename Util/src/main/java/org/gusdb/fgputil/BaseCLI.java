/**
 * 
 */
package org.gusdb.fgputil;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * @author xingao
 * 
 */
public abstract class BaseCLI {

  protected static final String ARG_PROJECT_ID = "model";

  private String command;
  private String description;
  protected String footer;

  private Options options;
  private Map<String, Object> defaults;
  private CommandLine commandLine;

  protected BaseCLI(String command, String description) {
    this.command = (command == null) ? getClass().getSimpleName() : command;
    this.description = description;
    this.footer = "";
    options = new Options();
    defaults = new LinkedHashMap<String, Object>();

    declareOptions();
  }

  protected abstract void execute() throws Exception;

  protected abstract void declareOptions();

  public void invoke(String[] args) throws Exception {
    try {
      parseCommandLine(args);
      execute();
    }
    catch (ParseException ex) {
      printUsage();
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  private void parseCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new BasicParser();
    commandLine = parser.parse(options, args);
  }

  public void printUsage() {
    String newline = System.getProperty("line.separator");

    StringBuffer syntax = new StringBuffer(command);

    // group options by groups
    Set<OptionGroup> requiredGroups = new HashSet<OptionGroup>();
    Set<OptionGroup> optionalGroups = new HashSet<OptionGroup>();
    Set<Option> requiredOptions = new HashSet<Option>();
    Set<Option> optionalOptions = new HashSet<Option>();
    for (Object obj : options.getOptions()) {
      Option option = (Option) obj;
      OptionGroup group = options.getOptionGroup(option);
      if (group == null) {
        if (option.isRequired())
          requiredOptions.add(option);
        else
          optionalOptions.add(option);
      }
      else {
        if (group.isRequired() && !requiredGroups.contains(group)) {
          requiredGroups.add(group);
        }
        else if (!group.isRequired() && !optionalGroups.contains(group)) {
          optionalGroups.add(group);
        }
      }
    }

    printOptionGroups(syntax, requiredGroups);
    for (Option option : requiredOptions) {
      syntax.append(" ");
      if (!option.isRequired())
        syntax.append("[");
      printOption(syntax, option);
      if (!option.isRequired())
        syntax.append("]");
    }
    printOptionGroups(syntax, optionalGroups);
    for (Option option : optionalOptions) {
      syntax.append(" ");
      if (!option.isRequired())
        syntax.append("[");
      printOption(syntax, option);
      if (!option.isRequired())
        syntax.append("]");
    }

    String header = newline + description + newline + newline + "Options:";

    String footer = " ";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(syntax.toString(), header, options, footer);
  }

  protected void addNonValueOption(String name, Boolean defaultValue, String description) {
    Option option = new Option(name, name, false, description);
    options.addOption(option);
    defaults.put(name, defaultValue);
  }

  protected void addSingleValueOption(String name, boolean required, String defaultValue, String description) {
    Option option = new Option(name, name, true, description);
    option.setRequired(required);
    option.setArgs(1);
    options.addOption(option);

    defaults.put(name, defaultValue);
  }

  protected void addMultiValueOption(String name, boolean required, int numArgs, String[] defaultValues,
      String description) {
    if (numArgs != Option.UNLIMITED_VALUES && numArgs < 2)
      throw new IndexOutOfBoundsException("The number of arguments must "
          + "be greater than 1, or Option.UNLIMITED_VALUES");
    Option option = new Option(name, name, true, description);
    option.setRequired(required);
    option.setArgs(numArgs);
    options.addOption(option);

    defaults.put(name, defaultValues);
  }

  protected void addGroup(boolean groupRequired, String... optionNames) {
    OptionGroup group = new OptionGroup();
    group.setRequired(groupRequired);
    for (String optionName : optionNames) {
      Option option = options.getOption("-" + optionName);
      if (option == null)
        throw new NullPointerException("Option '" + optionName + "' does not exist.");
      group.addOption(option);
    }
    options.addOptionGroup(group);
  }

  /**
   * @param name
   * @return If the option doesn't exist, a default value of the option will be returned. If the option
   *         exists, and it doesn't require value, it will return true; if the option requires a single value,
   *         the string of that value will be returned; if the option allows multiple value, a String[] will
   *         be returned.
   */
  protected Object getOptionValue(String name) {
    if (!commandLine.hasOption(name))
      return defaults.get(name);

    Option option = options.getOption("-" + name);
    if (!option.hasArg())
      return true;
    else if (option.getArgs() == 1)
      return commandLine.getOptionValue(name);
    else
      return commandLine.getOptionValues(name);
  }

  private void printOptionGroups(StringBuffer syntax, Set<OptionGroup> groups) {
    syntax.append(" ");
    for (OptionGroup group : groups) {
      syntax.append(group.isRequired() ? "(" : "[");
      boolean first = true;
      for (Object obj : group.getOptions()) {
        if (first)
          first = false;
        else
          syntax.append(" | ");
        Option option = (Option) obj;
        printOption(syntax, option);
      }
      syntax.append(group.isRequired() ? ")" : "]");
    }
  }

  private void printOption(StringBuffer syntax, Option option) {
    syntax.append("-" + option.getOpt());
    if (option.hasArg()) {
      syntax.append(" <" + option.getOpt() + "_value");
      if (option.getArgs() > 1)
        syntax.append("s");
      syntax.append(">");
    }
  }
}
