/*--------------------------------------------------------------*
  Copyright (C) 2006-2015 OpenSim Ltd.

  This file is distributed WITHOUT ANY WARRANTY. See the file
  'License' for details on this and other legal matters.
*--------------------------------------------------------------*/

package org.omnetpp.inifile.editor.model;

import static org.omnetpp.inifile.editor.model.ConfigRegistry.CFGID_ABSTRACT;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.CFGID_DESCRIPTION;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.CFGID_EXTENDS;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.CFGID_NETWORK;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.CFGID_REPEAT;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.DEFAULT;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.EXTENDS;
import static org.omnetpp.inifile.editor.model.ConfigRegistry.GENERAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;
import org.omnetpp.common.engine.Common;
import org.omnetpp.common.util.StringUtils;
import org.omnetpp.common.util.UIUtils;
import org.omnetpp.inifile.editor.InifileEditorPlugin;
import org.omnetpp.inifile.editor.model.IReadonlyInifileDocument.LineInfo;
import org.omnetpp.inifile.editor.model.ParamResolution.ParamResolutionType;
import org.omnetpp.ned.core.ParamUtil;
import org.omnetpp.ned.model.interfaces.ISubmoduleOrConnection;
import org.omnetpp.ned.model.pojo.SubmoduleElement;

/**
 * Various lookups in inifiles, making use of NED declarations as well.
 * This class cannot be instantiated, all functionality is provided via
 * static methods.
 *
 * @author Andras
 */
public class InifileUtils {
    // for getSectionImage():
    private static final String ICON_SECTION_PNG = "icons/full/obj16/section.png";
    private static final String ICON_SECTION_ABSTRACT_PNG = "icons/full/obj16/section_abstract.png";
    private static final String ICON_SECTION_NONEXISTENT_PNG = "icons/full/obj16/section_nonexistent.png";
    private static final String ICON_REPEAT_PNG = "icons/full/ovr16/section_repeat.png";
    private static final String ICON_ITER_PNG = "icons/full/ovr16/section_iter.png";
    private static final String ICON_WARNING_PNG = "icons/full/ovr16/warning.png";
    private static final String ICON_ERROR_PNG = "icons/full/ovr16/error.png";

    // for getKeyImage()
    public static final Image ICON_ERROR = UIUtils.ICON_ERROR;
    public static final Image ICON_INFO = UIUtils.ICON_INFO;
    public static final Image ICON_PAR_UNASSIGNED = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_unassigned.png");
    public static final Image ICON_PAR_NED = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_ned.png");
    public static final Image ICON_PAR_INIDEFAULT = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_neddefault.png");
    public static final Image ICON_PAR_INIASK = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_iniask.png");
    public static final Image ICON_PAR_INI = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_ini.png");
    public static final Image ICON_PAR_INIOVERRIDE = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_inioverride.png");
    public static final Image ICON_PAR_ININEDDEFAULT = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_inineddefault.png");
    public static final Image ICON_PAR_IMPLICITDEFAULT = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_implicitdefault.png");
    public static final Image ICON_PAR_GROUP = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_group.png");
    public static final Image ICON_PAR_UNKNOWN = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_unknown.png");

    public static final Image ICON_SIGNAL = InifileEditorPlugin.getCachedImage("icons/full/obj16/signal.png");
    public static final Image ICON_STATISTIC = InifileEditorPlugin.getCachedImage("icons/full/obj16/statistic.png");

    public static final Image ICON_PROPOSAL_MODULE = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_module.png");
    public static final Image ICON_PROPOSAL_PARAMETER = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_parameter.png");
    public static final Image ICON_PROPOSAL_GLOBALCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_globalconfig.png");
    public static final Image ICON_PROPOSAL_MODULECONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_moduleconfig.png");
    public static final Image ICON_PROPOSAL_PARAMETERCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_parameterconfig.png");
    public static final Image ICON_PROPOSAL_STATISTICCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_statisticconfig.png");
    public static final Image ICON_PROPOSAL_SCALARCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_scalarconfig.png");
    public static final Image ICON_PROPOSAL_VECTORCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_vectorconfig.png");
    public static final Image ICON_PROPOSAL_OTHERCONFIG = InifileEditorPlugin.getCachedImage("icons/full/obj16/proposal_otherconfig.png");

    public static final Image ICON_KEY_EQUALS_DEFAULT = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_equals_default.png");
    public static final Image ICON_KEY_EQUALS_ASK = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_equals_ask.png");

    public static final Image ICON_INIPARMISC = InifileEditorPlugin.getCachedImage("icons/full/obj16/par_inimisc.png");

    /**
     * Looks up a configuration value in the given section or its fallback sections.
     * Returns null if not found.
     */
    public static String lookupConfig(String section, String key, IReadonlyInifileDocument doc) {
        return lookupConfig(section, key, doc, null);
    }

    /**
     * Looks up a configuration value in the given section or its fallback sections.
     * Returns {@code defaultValue} if not found.
     */
    public static String lookupConfig(String section, String key, IReadonlyInifileDocument doc, String defaultValue) {
        String[] sectionChain = doc.getSectionChain(section);
        return lookupConfig(sectionChain, key, doc);
    }

    /**
     * Looks up a configuration value. Returns null if not found.
     */
    public static String lookupConfig(String[] sectionChain, String key, IReadonlyInifileDocument doc) {
        return lookupConfig(sectionChain, key, doc, null);
    }

    /**
     * Looks up the network name.
     */
    public static String lookupNetwork(IReadonlyInifileDocument doc, String section) {
        return InifileUtils.lookupConfig(section, CFGID_NETWORK.getName(), doc, CFGID_NETWORK.getDefaultValue());
    }

    /**
     * Looks up a configuration value. Returns {@code defaultValue} if not found.
     */
    public static String lookupConfig(String[] sectionChain, String key, IReadonlyInifileDocument doc, String defaultValue) {
        for (String section : sectionChain)
            if (doc.containsKey(section, key))
                return doc.getValue(section, key);
        return defaultValue;
    }

    /**
     * Given a parameter's fullPath, returns the key of the matching inifile
     * entry, or null if the parameter matches nothing. If hasNedDefault is set,
     * "=default" entries are also considered, otherwise they are ignored.
     *
     * Note: we need to return multiple matches because of keys like
     * "*.node[0].power=...", "*.node[1..5].power=...", and
     * "net.node[6..].power=..." etc. Scanning stops at "*.node[*].power="
     * because that eats all matching params (anything after that cannot match.)
     */
    public static List<SectionKey> lookupParameter(String paramFullPath, boolean hasNedDefault, String[] sectionChain, IReadonlyInifileDocument doc) {
        List<SectionKey> result = new ArrayList<SectionKey>();
        for (String section : sectionChain) {
            for (String key : doc.getKeys(section)) {
                ParamUtil.KeyMatcher keyMatcher = ParamUtil.getOrCreateKeyMatcher(key);
                if (keyMatcher.matcher.matches(paramFullPath)) {
                    String value = doc.getValue(section, key);
                    if (hasNedDefault || !value.equals(DEFAULT)) {
                        result.add(new SectionKey(section, key));
                        if (keyMatcher.keyEqualsGeneralizedKey)
                            return result;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Chops off potential "Config " prefix from a section name.
     */
    public static String removeSectionNamePrefix(String sectionName) {
        return sectionName.replaceFirst(".+ +", "");
    }

    public static String sectionNameToConfigName(String sectionName) {
        return GENERAL.equals(sectionName) ? "" : removeSectionNamePrefix(sectionName);
    }

    public static List<String> sectionNamesToConfigNames(List<String> sectionNames) {
        List<String> configNames = new ArrayList<String>();
        for (String sectionName : sectionNames) {
            String configName = sectionNameToConfigName(sectionName);
            if (!StringUtils.isEmpty(configName))
                configNames.add(configName);
        }
        return configNames;
    }

    /**
     * Find position of the last dot within the string key,
     * ignoring dots inside [] and {} pairs, such as those in `[0..5]`.
     * Returns -1 of not found.
     */
    public static int findLastDot(String key) {
        int lastDot = -1;
        char closer = '\0';
        for (int p = 0; p < key.length(); p++) {
            char ch = key.charAt(p);
            if (closer == '\0' && (ch == '[' || ch == '{'))
                closer = ch == '[' ? ']' : '}';
            else if (ch == closer)
                closer = '\0';
            else if (closer == '\0' && ch == '.')
                lastDot = p;
        }
        return lastDot;
    }

    /**
     * Resolves the run-time NED type of a "like" submodule, using the parameter
     * settings in the inifile. Returns null if the lookup is unsuccessful.
     */
    public static String resolveLikeExpr(String moduleFullPath, ISubmoduleOrConnection element, String activeSection,
                                            InifileAnalyzer analyzer, IReadonlyInifileDocument doc, ITimeout timeout)
        throws ParamResolutionDisabledException, ParamResolutionTimeoutException {

        // get module type expression
        String likeExpr = element.getLikeExpr();

        // note: the following lookup order is based on src/sim/netbuilder code, namely cNEDNetworkBuilder::getSubmoduleTypeName()

        //XXX this code is a near duplicate of one in ModuleTreeVisitor

        // first, try to use expression between angle braces from the NED file
        if (!element.getIsDefault() && StringUtils.isNotEmpty(likeExpr)) {
            return evaluateLikeExpr(likeExpr, moduleFullPath, activeSection, analyzer, doc, timeout);
        }

        // then, use **.typename from NED deep param assignments
        // XXX this is not implemented yet

        // then, use **.typename option in the configuration if exists
        String name = "channel";  // unless it's a submodule:
        if (element instanceof SubmoduleElement) {
            name = ((SubmoduleElement)element).getName();
            if (!StringUtils.isEmpty(((SubmoduleElement)element).getVectorSize()))
                name += "[*]";
        }
        String key = moduleFullPath + "." + name + "." + ConfigRegistry.TYPENAME;
        List<SectionKey> sectionKeys = InifileUtils.lookupParameter(key, false, doc.getSectionChain(activeSection), doc);

        // prefer the one with all asterisk indices, "[*]"
        SectionKey chosenKey = null;
        for (SectionKey sectionKey : sectionKeys)
            if (ParamUtil.isTotalParamAssignment(sectionKey.key))
                chosenKey = sectionKey;
        if (chosenKey == null && !sectionKeys.isEmpty())
            chosenKey = sectionKeys.get(0);
        if (chosenKey != null) {
            // we only understand if there's a string constant there
            String value = doc.getValue(chosenKey.section, chosenKey.key);
            try {
                return Common.parseQuotedString(value);
            }
            catch (RuntimeException e) {
                return null; // it is something we don't understand
            }
        }

        // then, use **.typename=default() expressions from NED deep param assignments
        // XXX this is not implemented yet

        // last, use default(expression) between angle braces from the NED file
        if (StringUtils.isEmpty(likeExpr))
            return null; // cannot happen (<default()> is not an accepted NED syntax)
        return evaluateLikeExpr(likeExpr, moduleFullPath, activeSection, analyzer, doc, timeout);
    }

    protected static String evaluateLikeExpr(String likeExpr, String moduleFullPath, String activeSection,
            InifileAnalyzer analyzer, IReadonlyInifileDocument doc, ITimeout timeout)
    throws ParamResolutionDisabledException, ParamResolutionTimeoutException {
        // understands string literals and references to parent module parameters;
        // return null for anything else (i.e. when we are not sophisticated enough to figure it out)
        if (likeExpr.charAt(0) == '"') {
            try {
                // looks like a string literal
                return Common.parseQuotedString(likeExpr);
            }
            catch (RuntimeException e) {
                return null;  // nope
            }
        }
        else if (likeExpr.matches("[A-Za-z_][A-Za-z0-9_]*")) {
            // identifier: it should be parameter of the parent module; we should look up and
            // return its value (note: we cannot use InifileUtils.resolveLikeParam() here yet)
            ParamResolution res = analyzer.getParamResolutionForModuleParam(moduleFullPath, likeExpr, activeSection, timeout);
            String value = getParamValue(res, doc);
            if (value == null)
                return null; // likely unassigned
            try {
                return Common.parseQuotedString(value);
            }
            catch (RuntimeException e) {
                return null; // value is not a string literal
            }
        }
        else {
            return null;
        }
    }

    /**
     * Whether the section chain contains the given section. Useful for detecting
     * cycles in the "extends" hierarchy.
     */
    public static boolean sectionChainContains(IReadonlyInifileDocument doc, String chainStartSection, String section) {
        String[] sectionChain = doc.getSectionChain(chainStartSection);
        return ArrayUtils.indexOf(sectionChain, section) >= 0;
    }

    /**
     * Returns the names of the sections the given section extends.
     * Returns empty list for the [General] section (it doesn't extend anything).
     * Non-existing bases are omitted from the result; also, it only returns [General]
     * if such section really exists.
     */
    public static List<String> resolveBaseSections(IReadonlyInifileDocument doc, String section) {
        ArrayList<String> baseSections = new ArrayList<String>();
        if (section.equals(GENERAL))
            return baseSections;

        List<String> extendsList = parseExtendsList(doc.getValue(section, EXTENDS));
        if (extendsList.isEmpty()) {
            if (doc.containsSection(GENERAL))
                baseSections.add(GENERAL);
        }
        else {
            for (String configName : extendsList) {
                if (doc.containsSection(configName))
                    baseSections.add(configName);
            }
        }

        return baseSections;
    }

    /**
     * Returns the list of config names in the 'extends' value.
     *
     * The value of the 'extends' is a comma or whitespace separated list of config names.
     * White space and empty names are omitted.
     * Example:
     *   " foo,, bar  baz " -> ["foo", "bar", "baz"]
     */
    public static List<String> parseExtendsList(String extendsList) {
        List<String> configNames = new ArrayList<String>();
        if (extendsList != null)
            for (String configName : extendsList.split("[, \t]"))
                if (!StringUtils.isEmpty(configName))
                    configNames.add(configName);
        return configNames;
    }

    public static String formatExtendsList(List<String> configNames) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String configName : configNames) {
            if (!first)
                sb.append(',');
            sb.append(configName);
            first = false;
        }
        return sb.toString();
    }


    /**
     * Same as resolveBaseSection(), but it returns [General] even if it doesn't exist
     */
    public static List<String> resolveBaseSectionsPretendingGeneralExists(IReadonlyInifileDocument doc, String section) {
        List<String> baseSection = resolveBaseSections(doc, section);
        return (baseSection.isEmpty() && !section.equals(GENERAL) && !doc.containsKey(GENERAL, EXTENDS)) ?
                    Collections.singletonList(GENERAL) : baseSection;
    }

    /**
     * Parse a boolean config value. Anything not recognized also counts as false.
     */
    public static boolean parseAsBool(String value) {
        if (value.equals("yes") || value.equals("true") || value.equals("on") || value.equals("1"))
            return true;
        else if (value.equals("no") || value.equals("false") || value.equals("off") || value.equals("0"))
            return false;
        else
            return false; // unrecognized
    }

    /**
     * Insert a section at the right place in the file.
     * [General] at top, and other sections ordered alphabetically.
     */
    public static void addSection(IInifileDocument doc, String newSection) {
        if (doc.containsSection(newSection))
            return;
        String[] sections = doc.getSectionNames();
        if (newSection.equals(GENERAL)) {
            doc.addSection(newSection, sections.length==0 ? null : sections[0]);
            return;
        }
        for (String section : sections) {
            if (section.compareToIgnoreCase(newSection) > 0 && !section.equals(GENERAL)) {
                doc.addSection(newSection, section);
                return;
            }
        }
        doc.addSection(newSection, null);
    }

    /**
     * Insert a key at the right place in the file. Config keys at top
     * (extends= first, description= next, network= after, and the
     * rest follows in alphabetical order), then parameters.
     * If even the section is not present, it is added first.
     * The entry MUST NOT exist yet.
     */
    public static void addEntry(IInifileDocument doc, String section, String newKey, String value, String rawComment) {
        if (!doc.containsSection(section))
            addSection(doc, section);
        Assert.isTrue(!doc.containsKey(section, newKey));
        for (String key : doc.getKeys(section)) {
            if (precedesKey(newKey, key)) {
                doc.addEntry(section, newKey, value, rawComment, key);
                return;
            }
        }
        doc.addEntry(section, newKey, value, rawComment, null);
    }

    /**
     * Returns true if key1 should precede key2 when new keys are added
     * in the inifile (preferred order)
     */
    private static boolean precedesKey(String key1, String key2) {
        int rank1 = getKeyRank(key1);
        int rank2 = getKeyRank(key2);
        return rank1 == rank2 ? key1.compareToIgnoreCase(key2) < 0 : rank1 < rank2;
    }

    private static int getKeyRank(String key) {
        if (key.equals(CFGID_EXTENDS.getName())) return 1;
        if (key.equals(CFGID_DESCRIPTION.getName())) return 2;
        if (key.equals(CFGID_NETWORK.getName())) return 3;
        KeyType type = KeyType.getKeyType(key);
        if (type == KeyType.CONFIG) return 4;
        //FIXME "=default" should come here: if (key.endsWith(dot_APPLY_DEFAULT)) return 7; // (!!!)
        if (type == KeyType.PER_OBJECT_CONFIG) return 5;
        if (type == KeyType.PARAM) return 6;
        return 100; // cannot get here
    }

    public static void addOrSetOrRemoveEntry(IInifileDocument doc, String section, String key, String rawValue) {
        if (rawValue == null) {
            // remove
            if (doc.containsKey(section, key))
                doc.removeKey(section, key);
        }
        else {
            // set or add
            if (!doc.containsKey(section, key))
                addEntry(doc, section, key, rawValue, "");
            else if (!rawValue.equals(doc.getValue(section, key)))
                doc.setRawValue(section, key, rawValue);
        }
    }

    /**
     * Renames the given section. Also changes the extends= keys in other sections
     * that refer to it.
     */
    public static void renameSection(IInifileDocument doc, String oldSectionName, String newSectionName) {
        doc.renameSection(oldSectionName, newSectionName);

        // change referring extends= keys in other sections
        String oldName = removeSectionNamePrefix(oldSectionName);
        String newName = removeSectionNamePrefix(newSectionName);
        Pattern pattern = Pattern.compile("(^|[, \t])\\Q" + oldName + "\\E($|[, \t])");
        for (String section : doc.getSectionNames()) {
            String oldValue = doc.getValue(section, EXTENDS);
            if (oldValue != null && pattern.matcher(oldValue).find()) {
                StringBuilder newValue = new StringBuilder();
                for (String token : StringUtils.splitPreservingSeparators(oldValue, Pattern.compile("[, \t]")))
                    newValue.append(token.equals(oldName) ? newName : token);
                doc.setRawValue(section, EXTENDS, newValue.toString());
            }
        }
    }

    /**
     * Returns problem markers for an inifile entry or section heading
     */
    public static IMarker[] getProblemMarkersFor(String section, String key, IReadonlyInifileDocument doc) {
        LineInfo line = key==null ? doc.getSectionLineDetails(section) : doc.getEntryLineDetails(section, key);
        return line==null ? new IMarker[0] : getProblemMarkersFor(line.getFile(), line.getLineNumber(), line.getLineNumber()+1);
    }

    /**
     * Returns problem markers for a whole inifile section
     */
    public static IMarker[] getProblemMarkersForWholeSection(String section, IReadonlyInifileDocument doc) {
        LineInfo line = doc.getSectionLineDetails(section);
        return line==null ? new IMarker[0] : getProblemMarkersFor(line.getFile(), line.getLineNumber(), line.getLineNumber() + line.getNumLines());
    }

    /**
     * Returns the problem markers for the given line range in the given file.
     */
    public static IMarker[] getProblemMarkersFor(IFile file, int startLine, int endLine) {
        try {
            IMarker[] markers = file.findMarkers(IMarker.PROBLEM, true, 0);
            ArrayList<IMarker> result = new ArrayList<IMarker>();
            for (IMarker marker : markers) {
                int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
                if (lineNumber >= startLine && lineNumber < endLine)
                    result.add(marker);
            }
            return result.toArray(new IMarker[]{});
        }
        catch (CoreException e) {
            InifileEditorPlugin.logError(e);
            return new IMarker[0];
        }
    }

    /**
     * Returns the maximum severity of the given markers, or -1.
     * Complements getProblemMarkersFor().
     */
    public static int getMaximumSeverity(IMarker[] markers) {
        int severity = -1;
        for (IMarker marker : markers)
            if (severity < marker.getAttribute(IMarker.SEVERITY, -1))
                severity = marker.getAttribute(IMarker.SEVERITY, -1);
        return severity;
    }

    /**
     * Returns an image for the given section, complete with error/warning markers etc.
     */
    public static Image getSectionImage(String sectionName, InifileAnalyzer analyzer) {
        IReadonlyInifileDocument doc = analyzer.getDocument();
        boolean exists = doc.containsSection(sectionName);
        boolean containsIteration = exists ? analyzer.containsIteration(sectionName) : false;
        boolean containsRepeat = exists ? lookupConfig(sectionName, CFGID_REPEAT.getName(), doc) != null : false;
        boolean isAbstract = exists ? "true".equals(doc.getValue(sectionName, CFGID_ABSTRACT.getName())) : false;
        IMarker[] markers = getProblemMarkersForWholeSection(sectionName, doc);
        int maxProblemSeverity = getMaximumSeverity(markers);
        boolean hasError =  maxProblemSeverity == IMarker.SEVERITY_ERROR;
        boolean hasWarning = maxProblemSeverity == IMarker.SEVERITY_WARNING;

        return InifileEditorPlugin.getCachedDecoratedImage(
                !exists ? ICON_SECTION_NONEXISTENT_PNG : isAbstract ? ICON_SECTION_ABSTRACT_PNG : ICON_SECTION_PNG,
                new String[] {
                    containsIteration ? ICON_ITER_PNG : null, // TOP_LEFT
                    containsRepeat ? ICON_REPEAT_PNG : null,  // TOP_RIGHT
                    hasError ? ICON_ERROR_PNG : hasWarning ? ICON_WARNING_PNG : null, // BOTTOM_LEFT
                    null  // BOTTOM_RIGHT
                });
    }

    /**
     * Returns an image for a given inifile key, suitable for displaying in a table or tree.
     */
    public static Image getKeyImage(String section, String key, InifileAnalyzer analyzer, ITimeout timeout) {
        // return an icon based on ParamResolutions
        try {
            ParamResolution[] paramResolutions = analyzer.getParamResolutionsForKey(section, key, timeout);
            if (paramResolutions == null || paramResolutions.length == 0)
                return ICON_PAR_INI;
            if (paramResolutions.length == 1)
                return suggestImage(paramResolutions[0].type);

            // there are more than one ParamResolutions -- collect their types
            Set<ParamResolutionType> types = new HashSet<ParamResolutionType>();
            for (ParamResolution p : paramResolutions)
                types.add(p.type);
            if (types.size() == 1)
                return suggestImage(paramResolutions[0].type);
            return ICON_INIPARMISC;
        } catch (ParamResolutionDisabledException e) {
            return ICON_PAR_UNKNOWN;
        } catch (ParamResolutionTimeoutException e) {
            return ICON_PAR_UNKNOWN;
        }
    }

    /**
     * Helper function: suggests an icon for a table or tree entry.
     */
    public static Image suggestImage(ParamResolutionType type) {
        switch (type) {
            case UNASSIGNED: return ICON_PAR_UNASSIGNED;
            case NED: return ICON_PAR_NED;
            case INI: return ICON_PAR_INI;
            case INI_DEFAULT: return ICON_PAR_INIDEFAULT;
            case INI_ASK: return ICON_PAR_INIASK;
            case INI_OVERRIDE:  return ICON_PAR_INIOVERRIDE;
            case INI_NEDDEFAULT: return ICON_PAR_ININEDDEFAULT;
            case IMPLICITDEFAULT: return ICON_PAR_IMPLICITDEFAULT;
        }
        return null;
    }

    public static String getParamRemark(ParamResolution res, IReadonlyInifileDocument doc) {
        String remark;
        String nedDefaultIfPresent = res.paramAssignment != null ? " (NED default: " + res.paramAssignment.getValue() + ")" : "";
        switch (res.type) {
            case UNASSIGNED: remark = "unassigned" + nedDefaultIfPresent; break;
            case NED: remark = "NED"; break;
            case INI: remark = "ini"; break;
            case INI_ASK: remark = "ask" + nedDefaultIfPresent; break;
            case INI_DEFAULT: remark = "NED default applied"; break;
            case INI_OVERRIDE: remark = "ini (overrides NED default: " + res.paramAssignment.getValue() + ")"; break;
            case INI_NEDDEFAULT: remark = "ini (sets same value as NED default)"; break;
            case IMPLICITDEFAULT: remark = "NED default applied implicitly"; break;
            default: throw new IllegalStateException("invalid param resolution type: "+res.type);
        }
        if (res.key!=null)
            remark += "; see [" + res.section + "] / " + res.key + "=" + doc.getValue(res.section, res.key);
        else if (res.paramAssignment != null && res.paramAssignment.getIsPattern())
            remark += "; see (" + res.paramAssignment.getEnclosingTypeElement().getName() + ") / " + res.paramAssignment.getNedSource().trim();
        return remark;
    }

    public static String getParamValue(ParamResolution res, IReadonlyInifileDocument doc) {
        return getParamValue(res, doc, true);
    }

    public static String getParamValue(ParamResolution res, IReadonlyInifileDocument doc, boolean allowNull) {
        if (res == null)
            return null;
        switch (res.type) {
            case UNASSIGNED:
                if (allowNull)
                    return null;
                else
                    return "(unassigned)";
            case INI_ASK:
                if (allowNull)
                    return null;
                else
                    return "(ask)";
            case NED: case INI_DEFAULT: case IMPLICITDEFAULT:
                return res.paramAssignment.getValue();
            case INI: case INI_OVERRIDE: case INI_NEDDEFAULT:
                return doc.getValue(res.section, res.key);
            default: throw new IllegalArgumentException("invalid param resolution type: "+res.type);
        }
    }
}
