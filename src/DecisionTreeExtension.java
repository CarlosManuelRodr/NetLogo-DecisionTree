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
import java.util.ArrayList;

import weka.core.Instances;
import weka.classifiers.trees.J48;

public class DecisionTreeExtension extends org.nlogo.api.DefaultClassManager
{

    public void load(org.nlogo.api.PrimitiveManager primManager)
    {
        primManager.addPrimitive("clear-instance", new Clear());
        primManager.addPrimitive("get-instance", new Get());
        primManager.addPrimitive("make-instance", new Make());
        primManager.addPrimitive("put-instance", new Put());
        primManager.addPrimitive("remove-instance", new Remove());

        primManager.addPrimitive("make-classifier", new Remove());
        primManager.addPrimitive("clear-classifier", new Remove());
        primManager.addPrimitive("addto-classifier", new Remove());

        primManager.addPrimitive("train", new Remove());
        primManager.addPrimitive("classify", new Remove());
    }

    /*****************************
     *                           *
     *    ClassifierInstance     *
     *                           *
     ****************************/

    class J48Classifier
    {
        public Instances instances;
        public J48 classifier;
    }

    private static ArrayList<J48Classifier> j48Classifiers = new ArrayList<J48Classifier>();

    /*****************************
     *                           *
     *     TableInstance         *
     *                           *
     ****************************/

    // TableInstance is just a stripped down version of the Table extension.
    // Source: https://github.com/NetLogo/Table-Extension

    private static java.util.WeakHashMap<TableInstance, Long> tables = new java.util.WeakHashMap<TableInstance, Long>();

    private static long next = 0;

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
            tables.put(this, next);
            id = next;
            next++;
        }

        public void addAll(LogoList alist) throws ExtensionException
        {
            for (Iterator<Object> it = alist.javaIterator(); it.hasNext();)
            {
                Object pair = it.next();
                if (!(pair instanceof LogoList) || ((LogoList) pair).size() < 2)
                {
                    throw new org.nlogo.api.ExtensionException("expected a two-element list: " +
                                    org.nlogo.api.Dump.logoObject(pair));
                }
                this.put(((LogoList) pair).first(), ((LogoList) pair).butFirst().first());
            }
        }

        public TableInstance(long id)
        {
            this.id = id;
            tables.put(this, id);
            next = StrictMath.max(next, id + 1);
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

    public void clearAll()
    {
        tables.clear();
        next = 0;
    }

    public StringBuilder exportWorld()
    {
        StringBuilder buffer = new StringBuilder();
        for (TableInstance instance : tables.keySet())
        {
            buffer.append
                    (org.nlogo.api.Dump.csv().encode
                            (org.nlogo.api.Dump.extensionObject(instance, true, true, false)) + "\n");
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

    ///

    public static class Clear implements Command
    {
        public Syntax getSyntax() {
            return SyntaxJ.commandSyntax(new int[]{Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance)) {
                throw new org.nlogo.api.ExtensionException("not an instance: " + org.nlogo.api.Dump.logoObject(arg0));
            }
            ((TableInstance) arg0).clear();
        }
    }

    public static class Get implements Reporter
    {
        public Syntax getSyntax()
        {
            return SyntaxJ.reporterSyntax
                    (new int[]{Syntax.WildcardType(), Syntax.WildcardType()}, Syntax.WildcardType());
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
            if (result == null)
            {
                throw new ExtensionException("No value for " + org.nlogo.api.Dump.logoObject(key)
                                + " in instance.");
            }
            return result;
        }
    }

    public static class Make implements Reporter
    {
        public Syntax getSyntax() {
            return SyntaxJ.reporterSyntax(Syntax.WildcardType());
        }

        public Object report(Argument args[], Context context)
                throws ExtensionException, LogoException {
            return new TableInstance();
        }

    }

    public static class Put implements Command
    {
        public Syntax getSyntax()
        {
            return SyntaxJ.commandSyntax
                    (new int[]{Syntax.WildcardType(), Syntax.WildcardType(),
                            Syntax.WildcardType()});
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

    public static class Remove implements Command
    {
        public Syntax getSyntax()
        {
            return SyntaxJ.commandSyntax
                    (new int[]{Syntax.WildcardType(), Syntax.WildcardType()});
        }

        public void perform(Argument args[], Context context)
                throws ExtensionException, LogoException
        {
            Object arg0 = args[0].get();
            if (!(arg0 instanceof TableInstance)) {
                throw new org.nlogo.api.ExtensionException
                        ("not a table: " +
                                org.nlogo.api.Dump.logoObject(arg0));
            }
            ((TableInstance) arg0).remove(args[1].get());
        }
    }

    public org.nlogo.core.ExtensionObject readExtensionObject(org.nlogo.api.ExtensionManager reader,
                                                              String typeName, String value)
            throws org.nlogo.api.ExtensionException
    {
        try
        {
            String[] s = value.split(":");
            long id = Long.parseLong(s[0]);
            TableInstance instance = getOrCreateTableFromId(id);
            if (s.length > 1) {
                instance.addAll((LogoList) reader.readFromString(s[1]));
            }
            return instance;
        }
        catch (CompilerException ex) {
            throw new org.nlogo.api.ExtensionException(ex.getMessage());
        }
    }

    private TableInstance getOrCreateTableFromId(long id)
    {
        for (TableInstance instance : tables.keySet())
        {
            if (instance.id == id) {
                return instance;
            }
        }
        return new TableInstance(id);
    }

    /// helpers

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
