import org.nlogo.api.*;
import org.nlogo.api.Dump;
import org.nlogo.core.CompilerException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.core.ExtensionObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.Instance;
import weka.classifiers.trees.J48;
import weka.classifiers.meta.FilteredClassifier;
import weka.filters.unsupervised.attribute.Discretize;

/**
 * This is an extension to make J48 decision trees in NetLogo
 * @author github.com/CarlosManuelRodr
 *
 */

public class DecisionTreeExtension extends org.nlogo.api.DefaultClassManager
{

    public void load(org.nlogo.api.PrimitiveManager primManager)
    {
        primManager.addPrimitive("make-instance", new InstanceMake());        // make-instance
        primManager.addPrimitive("put-instance", new InstancePut());          // put-instance <instance> <key> <value>

        primManager.addPrimitive("make-classifier", new ClassifierMake());    // make-classifier [attribute_names] [attribute_types] <class_index>
        primManager.addPrimitive("clear-classifier", new ClassifierClear());  // clear-classifier <classifier>
        primManager.addPrimitive("addto-classifier", new ClassifierAddTo());  // addto-classifier <classifier> <instance>
        primManager.addPrimitive("train-classifier", new ClassifierTrain());  // train-classifier <classifier>
        primManager.addPrimitive("classify", new Classify());                 // classify <classifier> <instance>
    }

    /***********************************
     *                                 *
     * ClassifierInstance manipulation *
     *                                 *
     **********************************/

    /**
     * NetLogo object that contains the classifier and the train data.
     */
    public static class J48Classifier implements ExtensionObject
    {
        public Instances train_data;
        public J48 j48_classifier;
        public FilteredClassifier filtered_classifier;

        private ArrayList<Attribute> attributes;
        private int m_class_index;

        public J48Classifier(LogoList attribute_names, LogoList attribute_types, int class_index) throws ExtensionException
        {
            // Build attribute list
            attributes = new ArrayList<Attribute>(attribute_names.length());
            m_class_index = class_index;
            for (int i = 0; i<attribute_names.length(); i++)
            {
                Object type = attribute_types.get(i);
                if (type instanceof LogoList)
                {
                    LogoList possible_class_values = (LogoList) type;
                    if (possible_class_values.length() == 0) {
                        attributes.add(new Attribute(attribute_names.get(i).toString()));
                    }
                    else
                    {

                        List class_values = new ArrayList(possible_class_values.length());
                        for (Iterator<Object> it = possible_class_values.javaIterator(); it.hasNext(); )
                            class_values.add(it.next());

                        attributes.add(new Attribute(attribute_names.get(i).toString(), class_values));
                    }
                }
                else
                    throw new ExtensionException("invalid entry at index " + i + ", in attribute_types list " + Dump.logoObject(type));
            }
            this.setup();
        }

        public void setup()
        {
            train_data = new Instances("train_data", attributes, 0);
            train_data.setClassIndex(m_class_index);
            Discretize discretization_filter = new Discretize();
            j48_classifier = new J48();
            j48_classifier.setUnpruned(true);
            filtered_classifier = new FilteredClassifier();
            filtered_classifier.setFilter(discretization_filter);
            filtered_classifier.setClassifier(j48_classifier);
        }

        public String dump(boolean readable, boolean exportable, boolean reference)
        {
            int id = 4;
            if (exportable && reference) {
                return ("" + id);
            }
            else
            {
                String dumpTxt = "Number of train instances: " + train_data.size();
                dumpTxt += " Decision Tree: " + j48_classifier.toString();
                return (exportable ? (id + ": ") : "") + Dump.logoObject(dumpTxt, true, exportable);
            }
        }

        public String getExtensionName() {
            return "decision-tree";
        }

        public String getNLTypeName() {
            return "classifier";
        }

        public boolean recursivelyEqual(Object o)
        {
            return true;
        }
    }

    /**
     * Command to reinitialize the classifier object. Syntax: clear-classifier <classifier>
     */
    public static class ClassifierClear implements Command
    {
        public Syntax getSyntax(){
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context) throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (arg0 instanceof J48Classifier)
            {
                J48Classifier j48 = (J48Classifier) arg0;
                j48.setup();
            }
            else
                throw new ExtensionException("Not a classifier" + Dump.logoObject(arg0));

        }
    }

    /**
     * Command to add an instance to the classifier object. Syntax: addto-classifier <classifier> <instance>
     */
    public static class ClassifierAddTo implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType(), Syntax.WildcardType()});
        }

        public void perform(Argument[] args, Context context) throws ExtensionException, LogoException
        {
            if (args.length != 2)
                throw new ExtensionException("Incorrect number of arguments");

            Object arg0 = args[0].get();
            Object arg1 = args[1].get();
            if (arg0 instanceof J48Classifier)
            {
                J48Classifier j48 = (J48Classifier) arg0;
                if (arg1 instanceof TableInstance)
                {
                    TableInstance tableInstance = (TableInstance) arg1;
                    j48.train_data.add(tableInstance.getWekaInstance(j48.attributes));
                }
                else
                    throw new ExtensionException("Not a instance " + Dump.logoObject(arg1));
            }
            else
                throw new ExtensionException("Not a classifier " + Dump.logoObject(arg0));
        }
    }

    /**
     * Command to create the classifier object.
     * Syntax: make-classifier [attribute_names] [attribute_types] <class_index>
     * The list [attribute_types] must contain an empty list [] if the corresponding attribute is numeric
     * or a list containing the possible string values if the corresponding attribute is nominal.
     * <class_index> is the index of the attribute to be predicted in the classifier.
     * Example: decision-tree:make-classifier ["sepal-length" "sepal-width" "petal-length" "petal-width" "species"] [[] [] [] [] ["setosa" "versicolor" "virginica"]] 4
     */
    public static class ClassifierMake implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(
                    new int[]{Syntax.WildcardType(), Syntax.WildcardType(), Syntax.WildcardType()},
                    Syntax.WildcardType()
            );
        }

        public Object report(Argument[] args, Context context) throws ExtensionException, LogoException
        {
            if (args.length == 3)
            {
                Object arg0 = args[0].get();
                Object arg1 = args[1].get();
                Object arg2 = args[2].get();

                if (arg0 instanceof LogoList)
                {
                    if (arg1 instanceof LogoList)
                    {
                        if (arg2 instanceof Double)
                            return new J48Classifier((LogoList) arg0, (LogoList) arg1, ((Double) arg2).intValue() );
                        else
                            throw new ExtensionException("Expecting a integer in argument 2: " + Dump.logoObject(arg2));
                    }
                    else
                        throw new ExtensionException("Expecting a list in argument 1: " + Dump.logoObject(arg1));
                }
                else
                    throw new ExtensionException("Expecting a list in argument 0: " + Dump.logoObject(arg0));
            }
            else
                throw new ExtensionException("Incorrect number of arguments");
        }

    }

    /**
     * Command to train the classifier object. Syntax: train-classifier <classifier>
     */
    public static class ClassifierTrain implements Command
    {
        public Syntax getSyntax(){
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context) throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (arg0 instanceof J48Classifier)
            {
                J48Classifier j48 = (J48Classifier) arg0;
                Instances train_data = j48.train_data;
                try {
                    j48.filtered_classifier.buildClassifier(train_data);
                } catch (Exception e) {
                    throw new ExtensionException("Weka error: " + e.toString());
                }
            }
            else
                throw new ExtensionException("Not a classifier" + Dump.logoObject(arg0));

        }
    }

    /**
     * Command to train the classifier object. Syntax: classify <classifier> <instance>
     */
    public static class Classify implements Reporter
    {
        public Syntax getSyntax(){
            return SyntaxJ.reporterSyntax(new int[]{Syntax.WildcardType(), Syntax.WildcardType()}, Syntax.StringType());
        }

        public Object report(Argument args[], Context context) throws ExtensionException, LogoException
        {
            if (args.length != 2)
                throw new ExtensionException("Incorrect number of arguments");

            Object arg0 = args[0].get();
            Object arg1 = args[1].get();
            if (arg0 instanceof J48Classifier)
            {
                J48Classifier j48 = (J48Classifier) arg0;
                if (arg1 instanceof TableInstance)
                {
                    TableInstance query = (TableInstance) arg1;
                    try
                    {
                        Instance wekaInstance = query.getWekaInstance(j48.attributes);
                        wekaInstance.setDataset(j48.train_data);
                        double pred = j48.filtered_classifier.classifyInstance(wekaInstance);
                        return j48.train_data.classAttribute().value((int) pred);

                    }
                    catch (Exception e) {
                        throw new ExtensionException("Weka error: " + e.toString());
                    }

                }
                else
                    throw new ExtensionException("Not a instance " + Dump.logoObject(arg1));
            }
            else
                throw new ExtensionException("Not a classifier " + Dump.logoObject(arg0));
        }
    }

    /*******************************
     *                             *
     * TableInstance manipulation  *
     *                             *
     ******************************/

    private static java.util.WeakHashMap<TableInstance, Long> tables = new java.util.WeakHashMap<TableInstance, Long>();
    private static long nextTable = 0;

    /**
     * Object that represents a data instance exposed to NetLogo just as "instance". This class is
     * just a stripped down version of the Table extension. Here is used as holder for Instances that will be passed to Weka
     * in its own Instance data type.
     * Original source: https://github.com/NetLogo/Table-Extension
     */

    public static class TableInstance extends java.util.LinkedHashMap<Object, Object> implements ExtensionObject
    {
        private final long id;

        public TableInstance()
        {
            tables.put(this, nextTable);
            id = nextTable;
            nextTable++;
        }

        public boolean equals(Object obj) {
            return this == obj;
        }

        public LogoList toList()
        {
            LogoListBuilder alist = new LogoListBuilder();
            for (Iterator<java.util.Map.Entry<Object, Object>> entries = entrySet().iterator(); entries.hasNext();)
            {
                java.util.Map.Entry<Object, Object> entry = entries.next();
                LogoListBuilder pair = new LogoListBuilder();
                pair.add(entry.getKey());
                pair.add(entry.getValue());
                alist.add(pair.toLogoList());
            }
            return alist.toLogoList();
        }

        public String dump(boolean readable, boolean exportable, boolean reference)
        {
            if (exportable && reference) {
                return ("" + id);
            }
            else {
                return (exportable ? (id + ": ") : "") + org.nlogo.api.Dump.logoObject(this.toList(), true, exportable);
            }
        }

        public String getExtensionName() {
            return "decision-tree";
        }

        public String getNLTypeName() {
            return "instance";
        }

        public boolean recursivelyEqual(Object o)
        {
            if (!(o instanceof TableInstance)) {
                return false;
            }
            TableInstance otherInstance = (TableInstance) o;
            if (size() != otherInstance.size()) {
                return false;
            }
            for (Iterator<Object> iter = keySet().iterator(); iter.hasNext();)
            {
                Object key = iter.next();
                if (!otherInstance.containsKey(key) || !org.nlogo.api.Equality.equals(get(key), otherInstance.get(key))) {
                    return false;
                }
            }
            return true;
        }

        public Instance getWekaInstance(ArrayList<Attribute> attributes)
        {
            Instance inst = new DenseInstance(attributes.size());
            attributes.forEach((attr) -> {
                if (this.containsKey(attr.name()))
                {
                    Object value = this.get(attr.name());
                    if (value instanceof Double)
                        inst.setValue(attr, (Double) value);
                    else
                        inst.setValue(attr, (String) value);
                }
            });
            return inst;
        }
    }

    /**
     * Reporter that creates an empty instance object. Syntax: make-instance
     */
    public static class InstanceMake implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(Syntax.WildcardType());
        }

        public Object report(Argument args[], Context context) throws LogoException {
            return new TableInstance();
        }

    }

    /**
     * Command to put a key-value pair in the instance. Syntax: put-instance <instance> <key> <value>
     */
    public static class InstancePut implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType(), Syntax.WildcardType(), Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context) throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance))
            {
                throw new org.nlogo.api.ExtensionException("not an instance: " + org.nlogo.api.Dump.logoObject(arg0));
            }
            Object key = args[1].get();
            ensureKeyValidity(key);
            ((TableInstance) arg0).put(key, args[2].get());
        }
    }

    // Abstracts definitions

    public void clearAll()
    {
        tables.clear();
        nextTable = 0;
    }

    public StringBuilder exportWorld()
    {
        StringBuilder buffer = new StringBuilder();
        for (TableInstance instance : tables.keySet())
        {
            buffer.append(
                    org.nlogo.api.Dump.csv().encode(
                        org.nlogo.api.Dump.extensionObject(instance, true, true, false)
                        )
                    + "\n");
        }
        return buffer;
    }

    public void importWorld(java.util.List<String[]> lines, org.nlogo.api.ExtensionManager reader,
                            org.nlogo.api.ImportErrorHandler handler) throws ExtensionException
    {
        for (String[] line : lines)
        {
            try {
                reader.readFromString(line[0]);
            }
            catch (CompilerException e) {
                handler.showError("Error importing arrays", e.getMessage(), "This array will be ignored");
            }
        }
    }

    /// Helpers

    private static boolean isValidKey(Object key)
    {
        return key instanceof Double || key instanceof String || key instanceof Boolean ||
                        (key instanceof LogoList && containsOnlyValidKeys((LogoList) key));
    }

    private static void ensureKeyValidity(Object key) throws ExtensionException
    {
        if (!isValidKey(key))
        {
            throw new org.nlogo.api.ExtensionException(
                    org.nlogo.api.Dump.logoObject(key) + " is not a valid table key "
                    + "(a table key may only be a number, a string, true or false, or a list "
                    + "whose items are valid keys)");

        }
    }

    private static boolean containsOnlyValidKeys(LogoList list)
    {
        for (Iterator<Object> it = list.javaIterator(); it.hasNext();)
        {
            Object o = it.next();
            if (!isValidKey(o)) {
                return false;
            }
        }
        return true;
    }
}
