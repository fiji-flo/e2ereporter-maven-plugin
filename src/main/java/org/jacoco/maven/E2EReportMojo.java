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
public class E2EReportMojo extends ReportITMojo {

    /**
     * @parameter
     */
    List<String> classFiles;
    /**
     * @parameter
     */
    List<String> sourceFiles;

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

