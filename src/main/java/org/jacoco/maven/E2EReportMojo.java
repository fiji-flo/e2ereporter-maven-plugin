package org.jacoco.maven;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.jacoco.core.analysis.*;
import org.jacoco.report.*;

/**
 * @phase verify
 * @goal report-integration
 * @requiresProject true
 * @threadSafe
 * @since 0.7.6
 */
public class E2EReportMojo extends AbstractReportMojo {

    /**
     * @parameter
     */
    List<String> classFiles;
    /**
     * @parameter
     */
    List<String> sourceFiles;

    /**
     * @parameter default-value="${project.reporting.outputDirectory}/e2ereport"
     */
    private File outputDirectory;

    /**
     * File with execution data.
     *
     * @parameter default-value="${project.build.directory}/e2e.exec"
     */
    private File dataFile;

    private static String output = "e2ereport";

    @Override
    protected String getOutputDirectory() {
        return outputDirectory.getAbsolutePath();
    }

    @Override
    public void setReportOutputDirectory(final File reportOutputDirectory) {
        if (reportOutputDirectory != null
                && !reportOutputDirectory.getAbsolutePath().endsWith(
                output)) {
            outputDirectory = new File(reportOutputDirectory, output);
        } else {
            outputDirectory = reportOutputDirectory;
        }
    }

    @Override
    File getDataFile() {
        return dataFile;
    }

    @Override
    File getOutputDirectoryFile() {
        return outputDirectory;
    }

    @Override
    public String getOutputName() {
        return output + "/index";
    }

    void createReport(final IReportGroupVisitor visitor) throws IOException {
        final FileFilter fileFilter = new FileFilter(getIncludes(),
                getExcludes());
        final E2EBundleCreator creator = new E2EBundleCreator(getProject(),
                fileFilter, classFiles, getLog());
        final IBundleCoverage bundle = creator.createBundle(executionDataStore);
        final SourceFileCollection locator = new SourceFileCollection(
                getCompileSourceRoots(), sourceEncoding);
        checkForMissingDebugInformation(bundle);
        visitor.visitBundle(bundle, locator);
    }

    List<File> getCompileSourceRoots() {
        final List<File> result = new ArrayList<File>();
        for (final Object path : getProject().getCompileSourceRoots()) {
            getLog().warn("Adding file: " + path);
            result.add(resolvePath((String) path));
        }
        for (final String sourceDir : sourceFiles) {
            getLog().warn("Adding file: " + sourceDir);
            result.add(resolvePath(sourceDir));
        }
        return result;
    }

    public String getName(final Locale locale) {
            return "E2E Reporter";
    }

    private static class SourceFileCollection implements ISourceFileLocator {

        private final List<File> sourceRoots;
        private final String encoding;

        public SourceFileCollection(final List<File> sourceRoots,
                                    final String encoding) {
            this.sourceRoots = sourceRoots;
            this.encoding = encoding;
        }

        public Reader getSourceFile(final String packageName,
                                    final String fileName) throws IOException {
            final String r;
            if (packageName.length() > 0) {
                r = packageName + '/' + fileName;
            } else {
                r = fileName;
            }
            for (final File sourceRoot : sourceRoots) {
                final File file = new File(sourceRoot, r);
                if (file.exists() && file.isFile()) {
                    return new InputStreamReader(new FileInputStream(file),
                            encoding);
                }
            }
            return null;
        }

        public int getTabWidth() {
            return 4;
        }
    }
}

