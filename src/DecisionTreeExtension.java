import org.nlogo.api.Argument;
import org.nlogo.api.Command;
import org.nlogo.api.Context;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.LogoException;
import org.nlogo.api.LogoListBuilder;
import org.nlogo.api.Reporter;
import org.nlogo.core.CompilerException;
import org.nlogo.core.LogoList;
import org.nlogo.core.Syntax;
import org.nlogo.core.SyntaxJ;
import org.nlogo.core.ExtensionObject;

import java.util.Iterator;

import weka.core.Instances;
import weka.classifiers.trees.J48;

public class DecisionTreeExtension extends org.nlogo.api.DefaultClassManager
{

    public void load(org.nlogo.api.PrimitiveManager primManager)
    {
        primManager.addPrimitive("clear-instance", new InstanceClear());
        primManager.addPrimitive("get-instance", new InstanceGet());
        primManager.addPrimitive("make-instance", new InstanceMake());
        primManager.addPrimitive("put-instance", new InstancePut());
        primManager.addPrimitive("remove-instance", new InstanceRemove());

        primManager.addPrimitive("make-classifier", new ClassifierMake());
        primManager.addPrimitive("clear-classifier", new ClassifierClear());
        primManager.addPrimitive("addto-classifier", new ClassifierAddTo());

        primManager.addPrimitive("train", new InstanceRemove());
        primManager.addPrimitive("classify", new InstanceRemove());
    }

    /***********************************
     *                                 *
     * ClassifierInstance manipulation *
     *                                 *
     **********************************/


    public static class J48Classifier implements ExtensionObject
    {
        //private final long id;
        private Instances instances;
        private J48 classifier;

        public J48Classifier()
        {

        }

        public String dump(boolean readable, boolean exportable, boolean reference)
        {
            int id = 4;
            if (exportable && reference) {
                return ("" + id);
            }
            else {
                return (exportable ? (id + ": ") : "") + org.nlogo.api.Dump.logoObject("dump text", true, exportable);
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

    public static class ClassifierClear implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException, LogoException
        {

        }
    }

    public static class ClassifierAddTo implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax
                    (new int[]{Syntax.WildcardType(), Syntax.WildcardType(),
                            Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException, LogoException
        {

        }
    }

    public static class ClassifierMake implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(Syntax.WildcardType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, LogoException {
            return new J48Classifier();
        }

    }

    /*******************************
     *                             *
     * TableInstance manipulation  *
     *                             *
     ******************************/

    // TableInstance is just a stripped down version of the Table extension. Here is used as
    // holder for Instances that will be passed to Weka.
    // Source: https://github.com/NetLogo/Table-Extension

    private static java.util.WeakHashMap<TableInstance, Long> tables = new java.util.WeakHashMap<TableInstance, Long>();

    private static long nextTable = 0;

    // It's important that we extend LinkedHashMap here, rather than
    // plain HashMap, because we want model results to be reproducible
    // cross-platform.

    public static class TableInstance
            extends java.util.LinkedHashMap<Object, Object>
            // new NetLogo data types defined by extensions must implement
            // this interface
            implements ExtensionObject
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
    }

    public static class InstanceClear implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context) throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance)) {
                throw new org.nlogo.api.ExtensionException("not an instance: " + org.nlogo.api.Dump.logoObject(arg0));
            }
            ((TableInstance) arg0).clear();
        }
    }

    public static class InstanceGet implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(new int[]{Syntax.WildcardType(), Syntax.WildcardType()}, Syntax.WildcardType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance)) {
                throw new org.nlogo.api.ExtensionException("not a table: " + org.nlogo.api.Dump.logoObject(arg0));
            }
            Object key = args[1].get();
            Object result = ((TableInstance) arg0).get(key);
            if (result == null) {
                throw new ExtensionException("No value for " + org.nlogo.api.Dump.logoObject(key) + " in instance.");
            }
            return result;
        }
    }

    public static class InstanceMake implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(Syntax.WildcardType());
        }

        public Object report(Argument args[], Context context) throws ExtensionException, LogoException {
            return new TableInstance();
        }

    }

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

    public static class InstanceRemove implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType(), Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance)) {
                throw new org.nlogo.api.ExtensionException("not a table: " + org.nlogo.api.Dump.logoObject(arg0));
            }
            ((TableInstance) arg0).remove(args[1].get());
        }
    }

    // Abstract definitions

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
                    + "\n"
            );
        }
        return buffer;
    }

    public void importWorld(java.util.List<String[]> lines, org.nlogo.api.ExtensionManager reader,
                            org.nlogo.api.ImportErrorHandler handler)
            throws ExtensionException
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
        return
                key instanceof Double ||
                        key instanceof String ||
                        key instanceof Boolean ||
                        (key instanceof LogoList &&
                                containsOnlyValidKeys((LogoList) key));
    }

    private static void ensureKeyValidity(Object key) throws ExtensionException
    {
        if (!isValidKey(key))
        {
            throw new org.nlogo.api.ExtensionException
                    (org.nlogo.api.Dump.logoObject(key) + " is not a valid table key "
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
