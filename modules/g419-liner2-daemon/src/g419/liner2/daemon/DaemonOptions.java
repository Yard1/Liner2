package g419.liner2.daemon;

import g419.liner2.api.LinerOptions;
import g419.corpus.Logger;
import g419.liner2.api.tools.ParameterException;
import org.apache.commons.cli.*;
import org.ini4j.Ini;
import org.ini4j.Profile;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by michal on 6/25/14.
 */
public class DaemonOptions extends LinerOptions {

    static protected final LinerOptions daemonOptions = new DaemonOptions();

    Options options = new Options();

    public static final String OPTION_MODELS = "models";
    public static final String OPTION_DB_HOST = "db_host";
    public static final String OPTION_DB_NAME = "db_name";
    public static final String OPTION_DB_PASSWORD = "db_pass";
    public static final String OPTION_DB_PORT = "db_port";
    public static final String OPTION_DB_USER = "db_user";
    public static final String OPTION_DB_URI = "db_uri";
    public static final String OPTION_IP = "ip";
    public static final String OPTION_MAX_THREADS = "max_threads";
    public static final String OPTION_PORT = "p";
    public static final String OPTION_VERBOSE = "v";
    public static final String OPTION_VERBOSE_LONG = "verbose";
    public static final String OPTION_DB_TYPE = "db_type";
    public static final String OPTION_DB_PATH = "db_path";

    public DaemonOptions(){
        options.addOption(OptionBuilder.withArgName("type").hasArg()
                .withDescription("database type (file | sql )")
                .isRequired()
                .create(OPTION_DB_TYPE));
        options.addOption(OptionBuilder.withArgName("db_path").hasArg()
                .withDescription("path to database directory (with following folders created within: requests, processing, errors, results")
                .create(OPTION_DB_PATH));
        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("sql database host name")
                .create(OPTION_DB_HOST));
        options.addOption(OptionBuilder.withArgName("name").hasArg()
                .withDescription("sql database name")
                .create(OPTION_DB_NAME));
        options.addOption(OptionBuilder.withArgName("password").hasArg()
                .withDescription("sql database password")
                .create(OPTION_DB_PASSWORD));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("sql database port number")
                .create(OPTION_DB_PORT));
        options.addOption(OptionBuilder.withArgName("address").hasArg()
                .withDescription("sql database URI address")
                .create(OPTION_DB_URI));
        options.addOption(OptionBuilder.withArgName("username").hasArg()
                .withDescription("sql database user name ")
                .create(OPTION_DB_USER));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("maximum number of processing threads")
                .create(OPTION_MAX_THREADS));
        options.addOption(OptionBuilder.withArgName("address").hasArg()
                .withDescription("IP address for daemon")
                .create(OPTION_IP));
        options.addOption(OptionBuilder.withArgName("number").hasArg()
                .withDescription("port to listen on")
                .create(OPTION_PORT));
        options.addOption(OptionBuilder.withArgName("models").hasArg()
                .withDescription("multiple models config for daemon")
                .create(OPTION_MODELS));
        options.addOption(OptionBuilder
                .withLongOpt(OPTION_VERBOSE_LONG).withDescription("verbose actions")
                .create(OPTION_VERBOSE));
    }

    public static LinerOptions getGlobal(){
        return daemonOptions;
    }

    public void parse(String[] args) throws Exception{
        CommandLine line = new GnuParser().parse(this.options, args);
            HashSet<String> argNames = new HashSet<String>();
            for(Option opt: line.getOptions()){
                String argName = opt.getOpt();
                if(argNames.contains(argName)){
                    throw new ParameterException("Repeated argument:" + argName);
                }
                else{
                    argNames.add(argName);
                }
            }
        Iterator<?> i_options = line.iterator();
        while (i_options.hasNext()) {
            Option o = (Option) i_options.next();
            if(o.getOpt().equals(OPTION_VERBOSE)){
                Logger.verbose = true;
            }
            else if(o.getOpt().equals(OPTION_MODELS)){
                parseModelsIni(o.getValue());
            }
            else{
                this.properties.setProperty(o.getOpt(), o.getValue());
            }
        }
    }


    private void parseModelsIni(String iniFile) throws Exception {
        String iniPath = new File(iniFile).getAbsoluteFile().getParentFile().getAbsolutePath();
        models = new HashMap<String, LinerOptions>();
        Ini ini = new Ini(new File(iniFile));
        this.defaultModel = ini.get("main", "default");
        Profile.Section modelsDef = ini.get("models");
        for(String model: modelsDef.keySet()){
            LinerOptions modelConfig = new LinerOptions();
            modelConfig.parseModelIni(modelsDef.get(model).replace("{INI_DIR}", iniPath));
            models.put(model, modelConfig);
        }
    }


    protected void printModes(){
        System.out.println("Liner2 service daemon - listen and process requests from a given database");
        System.out.println("Daemon works in 2 modes: with sql or filebased badabase.");
        System.out.println("Required parameteres: -db_type, -models");
        System.out.println("Required parameteres for file mode: -db_path");
        System.out.println("Required parameteres for sql mode: -ip, -p, -db_*");
        System.out.println();
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(98);
        formatter.printHelp("./liner2-daemon [options]", this.options);
    }

}
