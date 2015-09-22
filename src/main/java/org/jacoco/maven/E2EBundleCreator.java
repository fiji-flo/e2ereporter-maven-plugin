package org.jacoco.maven;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.maven.FileFilter;

public final class E2EBundleCreator {

    private final MavenProject project;
    private final FileFilter fileFilter;
    private List<String> classFiles;
    private final Log log;

    /**
     * @param project
     *            the MavenProject
     * @param fileFilter
     *            the FileFilter
     * @param log
     *            for log output
     */
    public E2EBundleCreator(final MavenProject project,
                         final FileFilter fileFilter, final List<String> classFiles, final Log log) {
        this.project = project;
        this.fileFilter = fileFilter;
        this.classFiles = classFiles;
        this.log = log;
    }

    /**
     * @param executionDataStore
     *            the execution data.
     * @return the coverage data.
     * @throws IOException
     */
    public IBundleCoverage createBundle(
            final ExecutionDataStore executionDataStore) throws IOException {
        final CoverageBuilder builder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionDataStore, builder);
        final File classesDir = new File(this.project.getBuild()
                .getOutputDirectory());

        @SuppressWarnings("unchecked")
        final List<File> filesToAnalyze = FileUtils.getFiles(classesDir,
                fileFilter.getIncludes(), fileFilter.getExcludes());

        for (String classFile: classFiles) {
            filesToAnalyze.addAll(FileUtils.getFiles(new File(classFile),
                    fileFilter.getIncludes(), fileFilter.getExcludes()));
        }
        for (final File file : filesToAnalyze) {
            analyzer.analyzeAll(file);
        }

        final IBundleCoverage bundle = builder
                .getBundle(this.project.getName());
        log.info(format("Analyzed bundle '%s' with %s classes",
                bundle.getName(),
                bundle.getClassCounter().getTotalCount()));

        return bundle;
    }
}

