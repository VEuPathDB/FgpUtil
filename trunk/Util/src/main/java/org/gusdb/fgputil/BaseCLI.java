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

  private String _command;
  private String _description;

  private Options _options;
  private Map<String, Object> _defaults;
  private CommandLine _commandLine;

  protected BaseCLI(String command, String description) {
    _command = (command == null) ? getClass().getSimpleName() : command;
    _description = description;
    _options = new Options();
    _defaults = new LinkedHashMap<String, Object>();

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
      System.exit(1);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  private void parseCommandLine(String[] args) throws ParseException {
    CommandLineParser parser = new BasicParser();
    _commandLine = parser.parse(_options, args);
  }

  public void printUsage() {
    String newline = System.getProperty("line.separator");

    StringBuffer syntax = new StringBuffer(_command);

    // group options by groups
    Set<OptionGroup> requiredGroups = new HashSet<OptionGroup>();
    Set<OptionGroup> optionalGroups = new HashSet<OptionGroup>();
    Set<Option> requiredOptions = new HashSet<Option>();
    Set<Option> optionalOptions = new HashSet<Option>();
    for (Object obj : _options.getOptions()) {
      Option option = (Option) obj;
      OptionGroup group = _options.getOptionGroup(option);
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

    String header = newline + _description + newline + newline + "Options:";

    String footer = " ";

    // PrintWriter stderr = new PrintWriter(System.err);
    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(syntax.toString(), header, _options, footer);
  }

  protected void addNonValueOption(String name, Boolean defaultValue, String description) {
    Option option = new Option(name, name, false, description);
    _options.addOption(option);
    _defaults.put(name, defaultValue);
  }

  protected void addSingleValueOption(String name, boolean required, String defaultValue, String description) {
    Option option = new Option(name, name, true, description);
    option.setRequired(required);
    option.setArgs(1);
    _options.addOption(option);

    _defaults.put(name, defaultValue);
  }

  protected void addMultiValueOption(String name, boolean required, int numArgs, String[] defaultValues,
      String description) {
    if (numArgs != Option.UNLIMITED_VALUES && numArgs < 2)
      throw new IndexOutOfBoundsException("The number of arguments must "
          + "be greater than 1, or Option.UNLIMITED_VALUES");
    Option option = new Option(name, name, true, description);
    option.setRequired(required);
    option.setArgs(numArgs);
    _options.addOption(option);

    _defaults.put(name, defaultValues);
  }

  protected void addGroup(boolean groupRequired, String... optionNames) {
    OptionGroup group = new OptionGroup();
    group.setRequired(groupRequired);
    for (String optionName : optionNames) {
      Option option = _options.getOption("-" + optionName);
      if (option == null)
        throw new NullPointerException("Option '" + optionName + "' does not exist.");
      group.addOption(option);
    }
    _options.addOptionGroup(group);
  }

  /**
   * @param name name of option to check
   * @return If the option doesn't exist, a default value of the option will be returned. If the option
   *         exists, and it doesn't require value, it will return true; if the option requires a single value,
   *         the string of that value will be returned; if the option allows multiple value, a String[] will
   *         be returned.
   */
  protected Object getOptionValue(String name) {
    if (!_commandLine.hasOption(name))
      return _defaults.get(name);

    Option option = _options.getOption("-" + name);
    if (!option.hasArg())
      return true;
    else if (option.getArgs() == 1)
      return _commandLine.getOptionValue(name);
    else
      return _commandLine.getOptionValues(name);
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
